# Camt Report Generator – Database Model (MS SQL Server)

---

## Table of Contents

1. [Overview & Design Principles](#overview--design-principles)
2. [Entity Relationship Summary](#entity-relationship-summary)
3. [Table Definitions](#table-definitions)
   - [Customer](#1-customer)
   - [CustomerAccount](#2-customeraccount)
   - [ProductPart](#3-productpart)
   - [Agreement](#4-agreement)
   - [AgreementVersion](#5-agreementversion)
   - [AgreementContact](#6-agreementcontact)
   - [AgreementRecipient](#7-agreementrecipient)
   - [AgreementRecipientProductPart](#8-agreementrecipientproductpart)
   - [AgreementRecipientProductPartAccount](#9-agreementrecipientproductpartaccount)
   - [AgreementPricing](#10-agreementpricing)
   - [Cart](#11-cart)
4. [Workflow Walkthrough](#workflow-walkthrough)
   - [Creating an Agreement](#a-creating-an-agreement)
   - [Editing an Agreement](#b-editing-an-agreement)
   - [Cancelling an Agreement](#c-cancelling-an-agreement)
5. [Status Reference](#status-reference)
6. [DDL Scripts](#ddl-scripts)

---

## Overview & Design Principles

The model is built around a **versioned agreement** pattern. A single logical agreement (`Agreement`) can have many **versions** (`AgreementVersion`). At any point in time, only one version per agreement is `ACTIVE`; all others are historical or pending.

Key design decisions:

- **Immutable versions**: Once a version transitions out of `DRAFT`, its data is never mutated. Edits always produce a new version row. This gives a full audit trail with zero additional audit tables required.
- **Normalised product parts**: The six Camt report types are seeded reference data, not hard-coded enums, making it easy to add new types in future.
- **External system separation**: Pricing data from the external system is stored in its own table. Only the pricing order reference is persisted — no pricing amounts or logic live in this database.
- **Cart as a lightweight queue**: The cart is a simple junction between a customer and one or more draft agreement versions awaiting approval.

---

## Entity Relationship Summary

```
Customer
  └── CustomerAccount           (1:N — the bank accounts the customer holds)
  └── Agreement                 (1:N — logical agreements owned by the customer)
        └── AgreementVersion    (1:N — one per create/edit cycle)
              └── AgreementContact                        (1:N)
              └── AgreementRecipient                      (1:N)
                    └── AgreementRecipientProductPart     (1:N)
                          └── AgreementRecipientProductPartAccount  (1:N)
              └── AgreementPricing                        (1:1)

ProductPart                     (reference / seed data — 6 rows)

Cart
  └── Links Customer ←→ AgreementVersion  (pending approval items)
```

---

## Table Definitions

---

### 1. `Customer`

Represents a corporate banking customer onboarded to the portal.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `CustomerId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `ExternalCustomerRef` | `NVARCHAR(100)` | NOT NULL, UNIQUE | Reference from the core banking / CRM system |
| `CustomerName` | `NVARCHAR(255)` | NOT NULL | Legal name of the corporate customer |
| `IsActive` | `BIT` | NOT NULL, DEFAULT 1 | Soft-delete flag |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Record creation timestamp (UTC) |
| `UpdatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Last update timestamp (UTC) |

**Usage:** Populated during customer onboarding. `ExternalCustomerRef` is the key used to look up the customer from the portal's authentication layer. All agreements, accounts, and cart items hang off this table.

---

### 2. `CustomerAccount`

The pre-existing bank accounts linked to a customer. This table is **read-only from the agreement workflow's perspective** — it is populated and maintained by an upstream system (core banking).

| Column | Type | Constraints | Description |
|---|---|---|---|
| `CustomerAccountId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `CustomerId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `Customer` | Owning customer |
| `AccountNumber` | `NVARCHAR(50)` | NOT NULL | The bank account number (e.g. IBAN or internal account ID) |
| `AccountName` | `NVARCHAR(255)` | NULL | Friendly display name for the account |
| `CurrencyCode` | `CHAR(3)` | NOT NULL | ISO 4217 currency code (e.g. `GBP`, `EUR`, `USD`) |
| `IsActive` | `BIT` | NOT NULL, DEFAULT 1 | Whether the account is currently available for selection |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Record creation timestamp (UTC) |

**Usage:** Screen 3 presents a dropdown/multiselect of `CustomerAccount` rows filtered by `CustomerId` and `IsActive = 1`. Foreign key references from `AgreementRecipientProductPartAccount` point here.

---

### 3. `ProductPart`

Reference / seed data table. Contains the six Camt report types available for selection.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `ProductPartId` | `TINYINT` | PK, IDENTITY | Surrogate primary key |
| `Code` | `VARCHAR(30)` | NOT NULL, UNIQUE | Machine-readable code (e.g. `CAMT052_BAL_ONLY`) |
| `DisplayName` | `NVARCHAR(100)` | NOT NULL | Human-readable label shown in the UI |
| `CamtMessageType` | `VARCHAR(10)` | NOT NULL | The parent message type: `camt.052`, `camt.053`, or `camt.054` |
| `SortOrder` | `TINYINT` | NOT NULL | Controls display ordering in the UI |
| `IsActive` | `BIT` | NOT NULL, DEFAULT 1 | Allows a product part to be retired without deletion |

**Seed data (6 rows):**

| `ProductPartId` | `Code` | `DisplayName` | `CamtMessageType` |
|---|---|---|---|
| 1 | `CAMT052_BAL_ONLY` | Camt.052 Balances Only | camt.052 |
| 2 | `CAMT052_BAL_TXN` | Camt.052 Balances & Transactions | camt.052 |
| 3 | `CAMT053_STANDARD` | Camt.053 Standard | camt.053 |
| 4 | `CAMT053_EXTENDED` | Camt.053 Extended | camt.053 |
| 5 | `CAMT054_DEBIT` | Camt.054 Debit Notifications | camt.054 |
| 6 | `CAMT054_CREDIT` | Camt.054 Credit Notifications | camt.054 |

**Usage:** Screen 1 reads from this table. Never mutated by application workflow — any additions or retirements are a DBA/release operation.

---

### 4. `Agreement`

The **logical agreement entity**. This row is created once and persists for the lifetime of the agreement, regardless of how many times it is edited. It acts as the stable anchor that all versions hang from.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `AgreementId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `CustomerId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `Customer` | Owning customer |
| `AgreementNumber` | `NVARCHAR(50)` | NOT NULL, UNIQUE | Human-readable reference (e.g. `AGR-2025-000042`) |
| `CurrentStatus` | `VARCHAR(20)` | NOT NULL | Denormalised current status: `DRAFT`, `ACTIVE`, `SUSPENDED`, `TERMINATED` |
| `ActiveVersionId` | `UNIQUEIDENTIFIER` | NULL, FK → `AgreementVersion` | Points to the currently `ACTIVE` version (NULL when no active version exists yet) |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | When the agreement was first created |
| `UpdatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Last status change timestamp |

> **Note on `ActiveVersionId`:** This is a convenience denormalisation to allow fast lookup of the active version without scanning `AgreementVersion`. It creates a circular FK relationship (`Agreement` → `AgreementVersion` → `Agreement`). In MS SQL Server, manage this by setting one of the FKs `WITH NOCHECK` during initial insert, or by using deferred constraint checking. Alternatively, `ActiveVersionId` can be omitted and the active version found via a query on `AgreementVersion` — the trade-off is a slightly more expensive lookup.

**Usage:**
- Created (with `CurrentStatus = 'DRAFT'`) on the very first submission in Screen 5.
- `CurrentStatus` and `ActiveVersionId` are updated atomically each time a version is approved or the agreement is terminated.

---

### 5. `AgreementVersion`

One row per **create or edit cycle**. Captures the complete state of the agreement at a point in time. All child tables (`AgreementContact`, `AgreementRecipient`, etc.) reference this table, not `Agreement` directly.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `AgreementVersionId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `AgreementId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `Agreement` | Parent logical agreement |
| `VersionNumber` | `INT` | NOT NULL | Monotonically increasing version counter per agreement (starts at 1) |
| `AgreementName` | `NVARCHAR(255)` | NOT NULL | Agreement name entered on Screen 2 |
| `Status` | `VARCHAR(20)` | NOT NULL | `DRAFT`, `ACTIVE`, `SUPERSEDED`, `TERMINATED` |
| `DebitAccountId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `CustomerAccount` | Account selected on Screen 4 for monthly fee debiting |
| `PricingType` | `VARCHAR(20)` | NOT NULL | `STANDARD` or `INDIVIDUAL` |
| `SubmittedAt` | `DATETIME2` | NULL | When the customer confirmed on Screen 5 (created the draft) |
| `ApprovedAt` | `DATETIME2` | NULL | When the customer approved from the cart (status → ACTIVE) |
| `SupersededAt` | `DATETIME2` | NULL | When this version was replaced by a newer active version |
| `TerminatedAt` | `DATETIME2` | NULL | When this version was terminated |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |
| `CreatedBy` | `NVARCHAR(255)` | NOT NULL | Portal user identity (from auth layer) |

**Unique constraint:** `(AgreementId, VersionNumber)` — ensures version numbers are unique per agreement.

**Usage:**
- A new row is inserted every time the customer goes through the wizard and confirms on Screen 5.
- The wizard saves intermediate progress to session/cache; only the final confirmation on Screen 5 writes to this table.
- Once `Status` is set to `ACTIVE`, this row is never updated again (except to mark it `SUPERSEDED` or `TERMINATED`).

---

### 6. `AgreementContact`

Stores one or more contacts provided on Screen 2.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `AgreementContactId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `AgreementVersionId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `AgreementVersion` | Parent version |
| `ContactName` | `NVARCHAR(255)` | NOT NULL | Full name of the contact |
| `ContactEmail` | `NVARCHAR(320)` | NOT NULL | Email address of the contact |
| `SortOrder` | `INT` | NOT NULL, DEFAULT 0 | Preserves the order in which contacts were entered |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |

**Usage:** Inserted in bulk when `AgreementVersion` is created. Minimum one row required per version (enforced at application layer).

---

### 7. `AgreementRecipient`

Each message recipient defined on Screen 3.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `AgreementRecipientId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `AgreementVersionId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `AgreementVersion` | Parent version |
| `RecipientName` | `NVARCHAR(255)` | NOT NULL | Label for this recipient (e.g. "Message-Recipient-1") |
| `SortOrder` | `INT` | NOT NULL, DEFAULT 0 | Preserves display ordering |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |

**Usage:** One row per recipient block on Screen 3. The customer can add multiple recipients, each independently configured.

---

### 8. `AgreementRecipientProductPart`

The mapping of a product part to a recipient — the first level of the Screen 3 hierarchy.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `AgreementRecipientProductPartId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `AgreementRecipientId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `AgreementRecipient` | Parent recipient |
| `ProductPartId` | `TINYINT` | NOT NULL, FK → `ProductPart` | The selected product part |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |

**Unique constraint:** `(AgreementRecipientId, ProductPartId)` — a recipient cannot have the same product part assigned twice.

**Usage:** Only product parts selected on Screen 1 are eligible. Application layer enforces this; the database does not, to avoid coupling the two tables directly.

---

### 9. `AgreementRecipientProductPartAccount`

The leaf-level table: which bank accounts are assigned to a specific product part for a specific recipient.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `AgreementRecipientProductPartAccountId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `AgreementRecipientProductPartId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `AgreementRecipientProductPart` | Parent product-part-recipient mapping |
| `CustomerAccountId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `CustomerAccount` | The assigned bank account |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |

**Unique constraint:** `(AgreementRecipientProductPartId, CustomerAccountId)` — an account cannot be assigned to the same product-part/recipient combination twice.

**Usage:** The most granular data in the model. When the Report Generation engine (Phase b) runs, it will query this table joined back up through the hierarchy to know which accounts need which reports sent to which recipients.

---

### 10. `AgreementPricing`

Stores the reference returned by the external pricing system after the customer confirms on Screen 5.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `AgreementPricingId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `AgreementVersionId` | `UNIQUEIDENTIFIER` | NOT NULL, UNIQUE, FK → `AgreementVersion` | Parent version (one-to-one) |
| `PricingOrderReference` | `NVARCHAR(255)` | NOT NULL | The reference key returned by the external pricing system |
| `CreatedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | When the pricing order was created (i.e. when Screen 5 was confirmed) |

**Usage:** Created immediately after the external pricing system API call succeeds on Screen 5 confirmation. If the external call fails, the `AgreementVersion` creation should be rolled back (treat the two operations as a logical unit). Only the reference is stored — no pricing amounts, breakdowns, or business rules.

---

### 11. `Cart`

A lightweight table tracking which draft agreement versions are awaiting customer approval.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `CartId` | `UNIQUEIDENTIFIER` | PK, DEFAULT `NEWSEQUENTIALID()` | Surrogate primary key |
| `CustomerId` | `UNIQUEIDENTIFIER` | NOT NULL, FK → `Customer` | The customer who owns this cart item |
| `AgreementVersionId` | `UNIQUEIDENTIFIER` | NOT NULL, UNIQUE, FK → `AgreementVersion` | The draft version in the cart |
| `AddedAt` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | When the item was added to the cart |
| `ExpiresAt` | `DATETIME2` | NOT NULL | `AddedAt + 30 days` — cart item expiry |
| `IsExpired` | `AS (CASE WHEN SYSUTCDATETIME() > ExpiresAt THEN 1 ELSE 0 END)` | Computed column | Convenience flag for expired items |

**Unique constraint:** `AgreementVersionId` — a draft version can only appear in the cart once.

**Usage:**
- A row is inserted here immediately after the `AgreementVersion` (and `AgreementPricing`) rows are successfully created on Screen 5 confirmation.
- The cart page queries `Cart` filtered by `CustomerId` and `IsExpired = 0`.
- When the customer approves from the cart: the `AgreementVersion.Status` is set to `ACTIVE`, the previous active version (if any) is set to `SUPERSEDED`, `Agreement.ActiveVersionId` and `Agreement.CurrentStatus` are updated, and the `Cart` row is deleted.
- A background job should periodically clean up or flag expired cart rows.

---

## Workflow Walkthrough

### A. Creating an Agreement

This covers the full journey from Screen 1 through cart approval.

**Screens 1–4 (in-progress):**
No database writes occur. All selections are held in the server-side session or a temporary browser cache. This avoids polluting the database with incomplete, abandoned wizard sessions.

**Screen 5 – Customer Confirms (creates DRAFT):**

The following writes occur inside a **single database transaction**:

1. **`Agreement`** — insert one row with `CurrentStatus = 'DRAFT'`, `ActiveVersionId = NULL`.
2. **`AgreementVersion`** — insert one row with `VersionNumber = 1`, `Status = 'DRAFT'`, `SubmittedAt = SYSUTCDATETIME()`.
3. **`AgreementContact`** — insert N rows (one per contact from Screen 2).
4. **`AgreementRecipient`** — insert N rows (one per recipient from Screen 3).
5. **`AgreementRecipientProductPart`** — insert rows for each recipient × product part mapping.
6. **`AgreementRecipientProductPartAccount`** — insert rows for each mapping × account combination.
7. **External Pricing API call** — made *within* the transaction scope where possible, or immediately after commit with compensating logic on failure.
8. **`AgreementPricing`** — insert one row with the returned `PricingOrderReference`.
9. **`Cart`** — insert one row linking the customer to the new `AgreementVersion`, with `ExpiresAt = DATEADD(day, 30, SYSUTCDATETIME())`.

If any step fails, the entire transaction is rolled back.

**Cart Approval (DRAFT → ACTIVE):**

Inside a single transaction:

1. Update `AgreementVersion.Status = 'ACTIVE'`, set `ApprovedAt = SYSUTCDATETIME()`.
2. Update `Agreement.CurrentStatus = 'ACTIVE'`, set `ActiveVersionId` to this version's ID.
3. Delete the `Cart` row.

---

### B. Editing an Agreement

The customer opens an existing `ACTIVE` agreement, is shown the wizard pre-populated with the active version's data, makes changes, and confirms on Screen 5.

**Screens 1–4 (pre-populated, in-progress):**
The application reads from the current `ACTIVE` `AgreementVersion` and its child tables to populate the wizard. Changes are held in session — no writes occur.

**Screen 5 – Customer Confirms (creates new DRAFT):**

Inside a single transaction:

1. **`AgreementVersion`** — insert a new row with `VersionNumber = (previous max + 1)`, `Status = 'DRAFT'`, `SubmittedAt = SYSUTCDATETIME()`. The *existing* active version is **not touched**.
2. **`AgreementContact`, `AgreementRecipient`, `AgreementRecipientProductPart`, `AgreementRecipientProductPartAccount`** — full set of child rows inserted for the **new version only**.
3. **`AgreementPricing`** — new row with fresh `PricingOrderReference` from the external system.
4. **`Cart`** — insert new row for the new draft version.
5. **`Agreement.CurrentStatus`** remains `ACTIVE` — the original version keeps serving reports.

At this point, two versions exist simultaneously: `VersionNumber = 1` with `Status = 'ACTIVE'`, and `VersionNumber = 2` with `Status = 'DRAFT'` in the cart.

**Cart Approval (new DRAFT → ACTIVE, old ACTIVE → SUPERSEDED):**

Inside a single transaction:

1. Fetch the `Agreement.ActiveVersionId` (the currently active version).
2. Update old version: `AgreementVersion.Status = 'SUPERSEDED'`, `SupersededAt = SYSUTCDATETIME()`.
3. Update new version: `AgreementVersion.Status = 'ACTIVE'`, `ApprovedAt = SYSUTCDATETIME()`.
4. Update `Agreement.ActiveVersionId` to the new version, `Agreement.CurrentStatus = 'ACTIVE'`, `Agreement.UpdatedAt = SYSUTCDATETIME()`.
5. Delete the `Cart` row.

---

### C. Cancelling an Agreement

Cancellation (termination) can only be applied to the `ACTIVE` version and transitions the agreement to a terminal state.

Inside a single transaction:

1. Update `AgreementVersion.Status = 'TERMINATED'`, `TerminatedAt = SYSUTCDATETIME()` for the active version.
2. Update `Agreement.CurrentStatus = 'TERMINATED'`, `Agreement.ActiveVersionId = NULL`, `Agreement.UpdatedAt = SYSUTCDATETIME()`.
3. If there is a `DRAFT` version sitting in the cart for this agreement, also update that version's `Status = 'TERMINATED'` and delete its `Cart` row — a terminated agreement cannot have pending drafts.

No child rows are deleted — the full historical data is preserved.

---

## Status Reference

### `Agreement.CurrentStatus`

| Status | Meaning |
|---|---|
| `DRAFT` | Agreement exists but no version has ever been approved |
| `ACTIVE` | At least one version is currently active |
| `SUSPENDED` | Agreement is temporarily paused (lifecycle operation, future use) |
| `TERMINATED` | Agreement has been permanently cancelled |

### `AgreementVersion.Status`

| Status | Meaning |
|---|---|
| `DRAFT` | Created on Screen 5 confirmation; awaiting cart approval |
| `ACTIVE` | Currently the live, serving version of this agreement |
| `SUPERSEDED` | Was previously active but has been replaced by a newer version |
| `TERMINATED` | Explicitly cancelled |

---

## DDL Scripts

```sql
-- ============================================================
-- REFERENCE DATA
-- ============================================================

CREATE TABLE ProductPart (
    ProductPartId   TINYINT         NOT NULL IDENTITY(1,1),
    Code            VARCHAR(30)     NOT NULL,
    DisplayName     NVARCHAR(100)   NOT NULL,
    CamtMessageType VARCHAR(10)     NOT NULL,
    SortOrder       TINYINT         NOT NULL,
    IsActive        BIT             NOT NULL CONSTRAINT DF_ProductPart_IsActive DEFAULT 1,
    CONSTRAINT PK_ProductPart PRIMARY KEY (ProductPartId),
    CONSTRAINT UQ_ProductPart_Code UNIQUE (Code)
);

INSERT INTO ProductPart (Code, DisplayName, CamtMessageType, SortOrder) VALUES
    ('CAMT052_BAL_ONLY',  'Camt.052 Balances Only',              'camt.052', 1),
    ('CAMT052_BAL_TXN',   'Camt.052 Balances & Transactions',    'camt.052', 2),
    ('CAMT053_STANDARD',  'Camt.053 Standard',                   'camt.053', 3),
    ('CAMT053_EXTENDED',  'Camt.053 Extended',                   'camt.053', 4),
    ('CAMT054_DEBIT',     'Camt.054 Debit Notifications',        'camt.054', 5),
    ('CAMT054_CREDIT',    'Camt.054 Credit Notifications',       'camt.054', 6);


-- ============================================================
-- CUSTOMER & ACCOUNTS
-- ============================================================

CREATE TABLE Customer (
    CustomerId              UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_Customer_Id DEFAULT NEWSEQUENTIALID(),
    ExternalCustomerRef     NVARCHAR(100)       NOT NULL,
    CustomerName            NVARCHAR(255)       NOT NULL,
    IsActive                BIT                 NOT NULL CONSTRAINT DF_Customer_IsActive DEFAULT 1,
    CreatedAt               DATETIME2           NOT NULL CONSTRAINT DF_Customer_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt               DATETIME2           NOT NULL CONSTRAINT DF_Customer_UpdatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Customer PRIMARY KEY (CustomerId),
    CONSTRAINT UQ_Customer_ExternalRef UNIQUE (ExternalCustomerRef)
);

CREATE TABLE CustomerAccount (
    CustomerAccountId   UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_CustomerAccount_Id DEFAULT NEWSEQUENTIALID(),
    CustomerId          UNIQUEIDENTIFIER    NOT NULL,
    AccountNumber       NVARCHAR(50)        NOT NULL,
    AccountName         NVARCHAR(255)       NULL,
    CurrencyCode        CHAR(3)             NOT NULL,
    IsActive            BIT                 NOT NULL CONSTRAINT DF_CustomerAccount_IsActive DEFAULT 1,
    CreatedAt           DATETIME2           NOT NULL CONSTRAINT DF_CustomerAccount_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_CustomerAccount PRIMARY KEY (CustomerAccountId),
    CONSTRAINT FK_CustomerAccount_Customer FOREIGN KEY (CustomerId) REFERENCES Customer (CustomerId)
);

CREATE INDEX IX_CustomerAccount_CustomerId ON CustomerAccount (CustomerId);


-- ============================================================
-- AGREEMENT (LOGICAL) & VERSIONS
-- ============================================================

CREATE TABLE Agreement (
    AgreementId         UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_Agreement_Id DEFAULT NEWSEQUENTIALID(),
    CustomerId          UNIQUEIDENTIFIER    NOT NULL,
    AgreementNumber     NVARCHAR(50)        NOT NULL,
    CurrentStatus       VARCHAR(20)         NOT NULL,
    ActiveVersionId     UNIQUEIDENTIFIER    NULL,   -- FK added after AgreementVersion is created
    CreatedAt           DATETIME2           NOT NULL CONSTRAINT DF_Agreement_CreatedAt DEFAULT SYSUTCDATETIME(),
    UpdatedAt           DATETIME2           NOT NULL CONSTRAINT DF_Agreement_UpdatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_Agreement PRIMARY KEY (AgreementId),
    CONSTRAINT UQ_Agreement_Number UNIQUE (AgreementNumber),
    CONSTRAINT FK_Agreement_Customer FOREIGN KEY (CustomerId) REFERENCES Customer (CustomerId),
    CONSTRAINT CK_Agreement_Status CHECK (CurrentStatus IN ('DRAFT','ACTIVE','SUSPENDED','TERMINATED'))
);

CREATE INDEX IX_Agreement_CustomerId ON Agreement (CustomerId);

CREATE TABLE AgreementVersion (
    AgreementVersionId  UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_AgreementVersion_Id DEFAULT NEWSEQUENTIALID(),
    AgreementId         UNIQUEIDENTIFIER    NOT NULL,
    VersionNumber       INT                 NOT NULL,
    AgreementName       NVARCHAR(255)       NOT NULL,
    Status              VARCHAR(20)         NOT NULL,
    DebitAccountId      UNIQUEIDENTIFIER    NOT NULL,
    PricingType         VARCHAR(20)         NOT NULL,
    SubmittedAt         DATETIME2           NULL,
    ApprovedAt          DATETIME2           NULL,
    SupersededAt        DATETIME2           NULL,
    TerminatedAt        DATETIME2           NULL,
    CreatedAt           DATETIME2           NOT NULL CONSTRAINT DF_AgreementVersion_CreatedAt DEFAULT SYSUTCDATETIME(),
    CreatedBy           NVARCHAR(255)       NOT NULL,
    CONSTRAINT PK_AgreementVersion PRIMARY KEY (AgreementVersionId),
    CONSTRAINT UQ_AgreementVersion_Number UNIQUE (AgreementId, VersionNumber),
    CONSTRAINT FK_AgreementVersion_Agreement FOREIGN KEY (AgreementId) REFERENCES Agreement (AgreementId),
    CONSTRAINT FK_AgreementVersion_DebitAccount FOREIGN KEY (DebitAccountId) REFERENCES CustomerAccount (CustomerAccountId),
    CONSTRAINT CK_AgreementVersion_Status CHECK (Status IN ('DRAFT','ACTIVE','SUPERSEDED','TERMINATED')),
    CONSTRAINT CK_AgreementVersion_PricingType CHECK (PricingType IN ('STANDARD','INDIVIDUAL'))
);

CREATE INDEX IX_AgreementVersion_AgreementId ON AgreementVersion (AgreementId);

-- Add the deferred FK from Agreement back to AgreementVersion
ALTER TABLE Agreement
    ADD CONSTRAINT FK_Agreement_ActiveVersion
    FOREIGN KEY (ActiveVersionId) REFERENCES AgreementVersion (AgreementVersionId);


-- ============================================================
-- CONTACTS
-- ============================================================

CREATE TABLE AgreementContact (
    AgreementContactId      UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_AgreementContact_Id DEFAULT NEWSEQUENTIALID(),
    AgreementVersionId      UNIQUEIDENTIFIER    NOT NULL,
    ContactName             NVARCHAR(255)       NOT NULL,
    ContactEmail            NVARCHAR(320)       NOT NULL,
    SortOrder               INT                 NOT NULL CONSTRAINT DF_AgreementContact_SortOrder DEFAULT 0,
    CreatedAt               DATETIME2           NOT NULL CONSTRAINT DF_AgreementContact_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_AgreementContact PRIMARY KEY (AgreementContactId),
    CONSTRAINT FK_AgreementContact_Version FOREIGN KEY (AgreementVersionId) REFERENCES AgreementVersion (AgreementVersionId)
);

CREATE INDEX IX_AgreementContact_VersionId ON AgreementContact (AgreementVersionId);


-- ============================================================
-- RECIPIENTS & ACCOUNT MAPPINGS
-- ============================================================

CREATE TABLE AgreementRecipient (
    AgreementRecipientId    UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_AgreementRecipient_Id DEFAULT NEWSEQUENTIALID(),
    AgreementVersionId      UNIQUEIDENTIFIER    NOT NULL,
    RecipientName           NVARCHAR(255)       NOT NULL,
    SortOrder               INT                 NOT NULL CONSTRAINT DF_AgreementRecipient_SortOrder DEFAULT 0,
    CreatedAt               DATETIME2           NOT NULL CONSTRAINT DF_AgreementRecipient_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_AgreementRecipient PRIMARY KEY (AgreementRecipientId),
    CONSTRAINT FK_AgreementRecipient_Version FOREIGN KEY (AgreementVersionId) REFERENCES AgreementVersion (AgreementVersionId)
);

CREATE INDEX IX_AgreementRecipient_VersionId ON AgreementRecipient (AgreementVersionId);

CREATE TABLE AgreementRecipientProductPart (
    AgreementRecipientProductPartId UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_ARPP_Id DEFAULT NEWSEQUENTIALID(),
    AgreementRecipientId            UNIQUEIDENTIFIER    NOT NULL,
    ProductPartId                   TINYINT             NOT NULL,
    CreatedAt                       DATETIME2           NOT NULL CONSTRAINT DF_ARPP_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_AgreementRecipientProductPart PRIMARY KEY (AgreementRecipientProductPartId),
    CONSTRAINT UQ_AgreementRecipientProductPart UNIQUE (AgreementRecipientId, ProductPartId),
    CONSTRAINT FK_ARPP_Recipient FOREIGN KEY (AgreementRecipientId) REFERENCES AgreementRecipient (AgreementRecipientId),
    CONSTRAINT FK_ARPP_ProductPart FOREIGN KEY (ProductPartId) REFERENCES ProductPart (ProductPartId)
);

CREATE INDEX IX_ARPP_RecipientId ON AgreementRecipientProductPart (AgreementRecipientId);

CREATE TABLE AgreementRecipientProductPartAccount (
    AgreementRecipientProductPartAccountId  UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_ARPPA_Id DEFAULT NEWSEQUENTIALID(),
    AgreementRecipientProductPartId         UNIQUEIDENTIFIER    NOT NULL,
    CustomerAccountId                       UNIQUEIDENTIFIER    NOT NULL,
    CreatedAt                               DATETIME2           NOT NULL CONSTRAINT DF_ARPPA_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_AgreementRecipientProductPartAccount PRIMARY KEY (AgreementRecipientProductPartAccountId),
    CONSTRAINT UQ_AgreementRecipientProductPartAccount UNIQUE (AgreementRecipientProductPartId, CustomerAccountId),
    CONSTRAINT FK_ARPPA_ProductPart FOREIGN KEY (AgreementRecipientProductPartId) REFERENCES AgreementRecipientProductPart (AgreementRecipientProductPartId),
    CONSTRAINT FK_ARPPA_CustomerAccount FOREIGN KEY (CustomerAccountId) REFERENCES CustomerAccount (CustomerAccountId)
);

CREATE INDEX IX_ARPPA_ProductPartId ON AgreementRecipientProductPartAccount (AgreementRecipientProductPartId);


-- ============================================================
-- PRICING
-- ============================================================

CREATE TABLE AgreementPricing (
    AgreementPricingId      UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_AgreementPricing_Id DEFAULT NEWSEQUENTIALID(),
    AgreementVersionId      UNIQUEIDENTIFIER    NOT NULL,
    PricingOrderReference   NVARCHAR(255)       NOT NULL,
    CreatedAt               DATETIME2           NOT NULL CONSTRAINT DF_AgreementPricing_CreatedAt DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_AgreementPricing PRIMARY KEY (AgreementPricingId),
    CONSTRAINT UQ_AgreementPricing_VersionId UNIQUE (AgreementVersionId),
    CONSTRAINT FK_AgreementPricing_Version FOREIGN KEY (AgreementVersionId) REFERENCES AgreementVersion (AgreementVersionId)
);


-- ============================================================
-- CART
-- ============================================================

CREATE TABLE Cart (
    CartId              UNIQUEIDENTIFIER    NOT NULL CONSTRAINT DF_Cart_Id DEFAULT NEWSEQUENTIALID(),
    CustomerId          UNIQUEIDENTIFIER    NOT NULL,
    AgreementVersionId  UNIQUEIDENTIFIER    NOT NULL,
    AddedAt             DATETIME2           NOT NULL CONSTRAINT DF_Cart_AddedAt DEFAULT SYSUTCDATETIME(),
    ExpiresAt           DATETIME2           NOT NULL,
    IsExpired           AS (CASE WHEN SYSUTCDATETIME() > ExpiresAt THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END),
    CONSTRAINT PK_Cart PRIMARY KEY (CartId),
    CONSTRAINT UQ_Cart_VersionId UNIQUE (AgreementVersionId),
    CONSTRAINT FK_Cart_Customer FOREIGN KEY (CustomerId) REFERENCES Customer (CustomerId),
    CONSTRAINT FK_Cart_AgreementVersion FOREIGN KEY (AgreementVersionId) REFERENCES AgreementVersion (AgreementVersionId)
);

CREATE INDEX IX_Cart_CustomerId ON Cart (CustomerId);
```
