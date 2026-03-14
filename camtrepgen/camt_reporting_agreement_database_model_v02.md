# Camt Report Generator – Consolidated Database Model v2
## MS SQL Server

---

## Table of Contents

1. [Design Principles](#1-design-principles)
2. [Entity Relationship Summary](#2-entity-relationship-summary)
3. [Table Definitions](#3-table-definitions)
   - [agreement_status_lu](#31-agreement_status_lu)
   - [recipient_status_lu](#32-recipient_status_lu)
   - [product_part_lu](#33-product_part_lu)
   - [customer](#34-customer)
   - [customer_account](#35-customer_account)
   - [contact](#36-contact)
   - [agreement](#37-agreement)
   - [agreement_version](#38-agreement_version)
   - [agreement_version_contact](#39-agreement_version_contact)
   - [agreement_recipient](#310-agreement_recipient)
   - [agreement_recipient_product_part](#311-agreement_recipient_product_part)
   - [agreement_recipient_product_part_account](#312-agreement_recipient_product_part_account)
   - [agreement_pricing](#313-agreement_pricing)
   - [cart_item](#314-cart_item)
   - [agreement_history](#315-agreement_history)
4. [Workflow Walkthrough](#4-workflow-walkthrough)
   - [Creating an Agreement](#41-creating-an-agreement)
   - [Editing an Agreement](#42-editing-an-agreement)
   - [Recipient Status Computation at Approval](#43-recipient-status-computation-at-approval)
   - [Cancelling an Agreement](#44-cancelling-an-agreement)
5. [Recipient Status Logic Reference](#5-recipient-status-logic-reference)
6. [DDL Scripts](#6-ddl-scripts)

---

## 1. Design Principles

| Principle | Decision |
|---|---|
| Stable agreement identity | `agreement.agreement_id` never changes. Edits produce a new `agreement_version` row — the agreement anchor row is immutable. |
| Immutable versions | Once an `agreement_version` moves out of `DRAFT`, its rows are never updated (except status transition columns). |
| Recipient-level status | Computed at approval time by diffing the incoming version against the previous active version. Written to `agreement_recipient.recipient_status_code`. |
| Pricing isolation | Only the external pricing order reference is stored. No amounts, no pricing logic. |
| Contact reusability | Contacts are customer-owned (`contact` table) and linked to versions via `agreement_version_contact`. The same contact can appear across many versions without duplication. |
| Audit trail | Every status transition is written to `agreement_history`. A JSON `snapshot_data` column captures point-in-time detail where needed. |
| Status extensibility | All status values live in lookup tables (`agreement_status_lu`, `recipient_status_lu`), not CHECK constraints. |
| PK strategy | `BIGINT IDENTITY` throughout — simpler, more performant indexes than GUIDs. GUIDs are only used where cross-system portability is a hard requirement. |

---

## 2. Entity Relationship Summary

```
customer
  └── customer_account            (1:N)
  └── contact                     (1:N — customer-owned, reusable)
  └── agreement                   (1:N — stable anchor, never mutated)
        └── agreement_version     (1:N — one row per create/edit cycle)
              └── agreement_version_contact          (N:M → contact)
              └── agreement_recipient                (1:N)
                    └── agreement_recipient_product_part      (1:N → product_part_lu)
                          └── agreement_recipient_product_part_account  (1:N → customer_account)
              └── agreement_pricing                  (1:1)

agreement_status_lu               (reference — agreement/version statuses)
recipient_status_lu               (reference — recipient statuses)
product_part_lu                   (reference — 6 Camt report types)
cart_item                         (links customer ↔ draft agreement_version)
agreement_history                 (audit log — one row per status event)
```

---

## 3. Table Definitions

---

### 3.1 `agreement_status_lu`

Lookup table for all valid statuses applicable to both `agreement` and `agreement_version`.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `status_code` | `NVARCHAR(20)` | PK | Machine-readable status key |
| `description` | `NVARCHAR(100)` | NOT NULL | Human-readable description |
| `is_active` | `BIT` | NOT NULL, DEFAULT 1 | Whether this status is still in use |

**Seed data:**

| `status_code` | `description` |
|---|---|
| `DRAFT` | Created on Screen 5 confirmation; awaiting cart approval |
| `ACTIVE` | Currently the live, serving version |
| `SUPERSEDED` | Was active but replaced by a newer version |
| `TERMINATED` | Permanently cancelled |

---

### 3.2 `recipient_status_lu`

Lookup table for statuses applicable to `agreement_recipient`. Kept separate from `agreement_status_lu` because recipient statuses have a different semantic meaning and lifecycle.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `status_code` | `NVARCHAR(20)` | PK | Machine-readable status key |
| `description` | `NVARCHAR(100)` | NOT NULL | Human-readable description |
| `is_active` | `BIT` | NOT NULL, DEFAULT 1 | Whether this status is still in use |

**Seed data:**

| `status_code` | `description` |
|---|---|
| `ACTIVE` | Recipient is active in this version |
| `CANCELLED` | Recipient existed in the previous version but was removed in this version |
| `NEW` | Recipient did not exist in any previous version (first-ever appearance) |

> **Note on `NEW` vs `ACTIVE`:** Both represent a recipient that is currently live. `NEW` distinguishes a recipient that was just introduced in this version — useful for display ("newly added") and auditing. If you prefer to keep it simple, `NEW` can be merged into `ACTIVE`; the distinction is cosmetic.

---

### 3.3 `product_part_lu`

Reference table for the six Camt report types. Seeded once; never mutated by application workflow.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `product_part_code` | `NVARCHAR(30)` | PK | Machine-readable code |
| `product_part_name` | `NVARCHAR(100)` | NOT NULL | Display name shown in the UI |
| `camt_message_type` | `VARCHAR(10)` | NOT NULL | Parent message type: `camt.052`, `camt.053`, `camt.054` |
| `sort_order` | `TINYINT` | NOT NULL | Controls UI display ordering |
| `is_active` | `BIT` | NOT NULL, DEFAULT 1 | Allows retirement without deletion |

**Seed data:**

| `product_part_code` | `product_part_name` | `camt_message_type` |
|---|---|---|
| `C052_BAL_ONLY` | Camt.052 Balances Only | camt.052 |
| `C052_BAL_TXN` | Camt.052 Balances & Transactions | camt.052 |
| `C053_STANDARD` | Camt.053 Standard | camt.053 |
| `C053_EXTENDED` | Camt.053 Extended | camt.053 |
| `C054_DEBIT` | Camt.054 Debit Notifications | camt.054 |
| `C054_CREDIT` | Camt.054 Credit Notifications | camt.054 |

---

### 3.4 `customer`

Represents a corporate banking customer. Populated and maintained by an upstream onboarding system.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `customer_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `external_customer_ref` | `NVARCHAR(100)` | NOT NULL, UNIQUE | Reference from core banking / CRM |
| `customer_name` | `NVARCHAR(255)` | NOT NULL | Legal name of the corporate customer |
| `is_active` | `BIT` | NOT NULL, DEFAULT 1 | Soft-delete flag |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Record creation timestamp (UTC) |
| `updated_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Last update timestamp (UTC) |

---

### 3.5 `customer_account`

Pre-existing bank accounts linked to a customer. **Read-only** from the agreement workflow — populated by core banking upstream.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `customer_account_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `customer_id` | `BIGINT` | NOT NULL, FK → `customer` | Owning customer |
| `account_number` | `NVARCHAR(50)` | NOT NULL | Bank account number (IBAN or internal ID) |
| `account_name` | `NVARCHAR(255)` | NULL | Friendly display name |
| `currency_code` | `CHAR(3)` | NOT NULL | ISO 4217 currency code |
| `is_active` | `BIT` | NOT NULL, DEFAULT 1 | Whether available for selection on Screen 3 |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Record creation timestamp (UTC) |

**Usage:** Screen 3 presents accounts filtered by `customer_id` and `is_active = 1`. All account assignments in `agreement_recipient_product_part_account` reference this table via FK — guaranteeing that only real, customer-owned accounts can be assigned.

---

### 3.6 `contact`

Customer-owned contacts, reusable across multiple agreement versions. Populated on Screen 2 and persisted independently of any specific agreement version.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `contact_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `customer_id` | `BIGINT` | NOT NULL, FK → `customer` | Owning customer |
| `contact_name` | `NVARCHAR(200)` | NOT NULL | Full name |
| `email` | `NVARCHAR(320)` | NOT NULL | Email address |
| `is_active` | `BIT` | NOT NULL, DEFAULT 1 | Soft-delete flag |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Record creation timestamp (UTC) |
| `updated_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Last update timestamp (UTC) |

**Unique constraint:** `(customer_id, email)` — a customer cannot have two contacts with the same email.

**Usage:** When the customer enters a contact on Screen 2, the application checks whether a `contact` row already exists for that `customer_id + email`. If yes, it reuses the existing `contact_id`. If no, it inserts a new row. The link to a specific version is recorded in `agreement_version_contact`.

---

### 3.7 `agreement`

The **stable logical anchor** for an agreement. Created once when the customer first confirms on Screen 5. Never updated except for `current_status`, `active_version_id`, and `updated_at` — and only via controlled status transitions.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `agreement_id` | `BIGINT IDENTITY` | PK | Surrogate primary key — **never changes for the lifetime of the agreement** |
| `customer_id` | `BIGINT` | NOT NULL, FK → `customer` | Owning customer |
| `agreement_number` | `NVARCHAR(50)` | NOT NULL, UNIQUE | Human-readable reference (e.g. `AGR-2025-000042`) |
| `current_status` | `NVARCHAR(20)` | NOT NULL, FK → `agreement_status_lu` | Denormalised current status of the agreement |
| `active_version_id` | `BIGINT` | NULL, FK → `agreement_version` | Points to the currently `ACTIVE` version. NULL until first approval. |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | When the agreement was first created |
| `updated_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Last status change timestamp |

> **On `active_version_id`:** This creates a circular FK (`agreement` → `agreement_version` → `agreement`). Handle in SQL Server by adding the FK from `agreement` to `agreement_version` after both tables exist (shown in DDL). Use `WITH NOCHECK` for the initial insert, then update `active_version_id` in the same transaction once the version row exists.

---

### 3.8 `agreement_version`

One row per **create or edit cycle**. Captures the complete structural snapshot of the agreement at that point in time. All child tables reference `agreement_version_id`, not `agreement_id` directly — this is what keeps the same `agreement_id` stable while allowing independent versioned content.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `agreement_version_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `agreement_id` | `BIGINT` | NOT NULL, FK → `agreement` | Parent logical agreement — **same value across all versions** |
| `version_number` | `INT` | NOT NULL | Monotonically increasing per agreement (starts at 1) |
| `agreement_name` | `NVARCHAR(255)` | NOT NULL | Agreement name from Screen 2 |
| `status_code` | `NVARCHAR(20)` | NOT NULL, FK → `agreement_status_lu` | Version-level status |
| `debit_account_id` | `BIGINT` | NOT NULL, FK → `customer_account` | Account for monthly fee debiting (Screen 4) |
| `pricing_type` | `NVARCHAR(20)` | NOT NULL | `STANDARD` or `INDIVIDUAL` |
| `submitted_at` | `DATETIME2` | NULL | When customer confirmed on Screen 5 |
| `approved_at` | `DATETIME2` | NULL | When customer approved from the cart |
| `superseded_at` | `DATETIME2` | NULL | When this version was replaced |
| `terminated_at` | `DATETIME2` | NULL | When this version was terminated |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |
| `created_by` | `NVARCHAR(255)` | NOT NULL | Portal user identity |

**Unique constraint:** `(agreement_id, version_number)`.

---

### 3.9 `agreement_version_contact`

Junction table linking contacts (customer-owned) to a specific agreement version. Replaces the per-version embedded contact approach.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `agreement_version_contact_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `agreement_version_id` | `BIGINT` | NOT NULL, FK → `agreement_version` | The version this contact is linked to |
| `contact_id` | `BIGINT` | NOT NULL, FK → `contact` | The contact being linked |
| `sort_order` | `INT` | NOT NULL, DEFAULT 0 | Preserves the order contacts were entered |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |

**Unique constraint:** `(agreement_version_id, contact_id)` — the same contact cannot be linked twice to the same version.

---

### 3.10 `agreement_recipient`

Each message recipient defined on Screen 3, scoped to a specific agreement version.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `agreement_recipient_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `agreement_version_id` | `BIGINT` | NOT NULL, FK → `agreement_version` | Parent version |
| `recipient_name` | `NVARCHAR(255)` | NOT NULL | Label for this recipient |
| `recipient_status_code` | `NVARCHAR(20)` | NOT NULL, FK → `recipient_status_lu` | Computed at approval time by diffing against previous active version |
| `sort_order` | `INT` | NOT NULL, DEFAULT 0 | Preserves display ordering |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |
| `status_computed_at` | `DATETIME2` | NULL | Timestamp when `recipient_status_code` was last written by the approval process |

> **On initial creation (Version 1):** There is no previous version to diff against. All recipients are written with `recipient_status_code = 'NEW'` at approval time, since every recipient is brand new.

> **On edits (Version 2+):** The approval process compares recipients in the incoming version against the previous active version and writes `ACTIVE`, `NEW`, or `CANCELLED` accordingly. See [Section 4.3](#43-recipient-status-computation-at-approval) for the full logic.

---

### 3.11 `agreement_recipient_product_part`

Maps a product part to a specific recipient within a specific version.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `agreement_recipient_product_part_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `agreement_recipient_id` | `BIGINT` | NOT NULL, FK → `agreement_recipient` | Parent recipient |
| `product_part_code` | `NVARCHAR(30)` | NOT NULL, FK → `product_part_lu` | The assigned product part |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |

**Unique constraint:** `(agreement_recipient_id, product_part_code)`.

---

### 3.12 `agreement_recipient_product_part_account`

The leaf-level table. Maps specific bank accounts to a product part / recipient combination within a version.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `agreement_recipient_product_part_account_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `agreement_recipient_product_part_id` | `BIGINT` | NOT NULL, FK → `agreement_recipient_product_part` | Parent product part mapping |
| `customer_account_id` | `BIGINT` | NOT NULL, FK → `customer_account` | The assigned bank account |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | Row creation timestamp |

**Unique constraint:** `(agreement_recipient_product_part_id, customer_account_id)`.

**Usage:** The report generation engine (Phase b) queries this table, joined back up through the hierarchy, to determine which accounts need which reports sent to which recipients under the currently active version.

---

### 3.13 `agreement_pricing`

Stores only the reference returned by the external pricing system. One row per version.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `agreement_pricing_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `agreement_version_id` | `BIGINT` | NOT NULL, UNIQUE, FK → `agreement_version` | Parent version (one-to-one) |
| `pricing_order_reference` | `NVARCHAR(255)` | NOT NULL | Reference returned by the external pricing system |
| `created_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | When the pricing order was created |

---

### 3.14 `cart_item`

Tracks draft agreement versions awaiting customer approval. One row per pending draft.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `cart_item_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `customer_id` | `BIGINT` | NOT NULL, FK → `customer` | The customer who owns this cart item |
| `agreement_version_id` | `BIGINT` | NOT NULL, UNIQUE, FK → `agreement_version` | The draft version in the cart |
| `added_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | When added to the cart |
| `expires_at` | `DATETIME2` | NOT NULL | `added_at + 30 days` |
| `is_expired` | `AS (CASE WHEN SYSUTCDATETIME() > expires_at THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END)` | Computed | Convenience expiry flag |

---

### 3.15 `agreement_history`

Audit log. One row is appended for every meaningful status transition or lifecycle event across the agreement and its versions. Never updated; append-only.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `history_id` | `BIGINT IDENTITY` | PK | Surrogate primary key |
| `agreement_id` | `BIGINT` | NOT NULL, FK → `agreement` | The agreement this event belongs to |
| `agreement_version_id` | `BIGINT` | NULL, FK → `agreement_version` | The specific version involved (NULL for agreement-level events) |
| `action_type` | `NVARCHAR(50)` | NOT NULL | e.g. `VERSION_CREATED`, `VERSION_APPROVED`, `VERSION_SUPERSEDED`, `AGREEMENT_TERMINATED` |
| `old_status` | `NVARCHAR(20)` | NULL | Status before the transition |
| `new_status` | `NVARCHAR(20)` | NOT NULL | Status after the transition |
| `changed_by` | `NVARCHAR(255)` | NOT NULL | Portal user identity |
| `changed_at` | `DATETIME2` | NOT NULL, DEFAULT `SYSUTCDATETIME()` | When the event occurred (UTC) |
| `change_reason` | `NVARCHAR(500)` | NULL | Optional free-text reason |
| `snapshot_data` | `NVARCHAR(MAX)` | NULL | JSON snapshot of key state at time of event |

---

## 4. Workflow Walkthrough

### 4.1 Creating an Agreement

**Screens 1–4:** No database writes. All selections held in server-side session.

**Screen 5 – Customer Confirms (DRAFT creation):**

All steps inside a single transaction:

1. **`agreement`** — insert one row: `current_status = 'DRAFT'`, `active_version_id = NULL`.
2. **`agreement_version`** — insert one row: `agreement_id` from step 1, `version_number = 1`, `status_code = 'DRAFT'`, `submitted_at = SYSUTCDATETIME()`.
3. **`contact`** — for each contact on Screen 2: upsert (insert if `customer_id + email` not found; otherwise reuse existing `contact_id`).
4. **`agreement_version_contact`** — insert one row per contact linking to the new version.
5. **`agreement_recipient`** — insert one row per recipient. Set `recipient_status_code = 'NEW'` at this point (will be confirmed at approval; setting it now keeps the column NOT NULL).
6. **`agreement_recipient_product_part`** — insert rows for each recipient × product part mapping.
7. **`agreement_recipient_product_part_account`** — insert rows for each mapping × account.
8. **External Pricing API** — call the pricing system, receive `pricing_order_reference`.
9. **`agreement_pricing`** — insert one row with the returned reference.
10. **`cart_item`** — insert one row: `expires_at = DATEADD(day, 30, SYSUTCDATETIME())`.
11. **`agreement_history`** — insert one row: `action_type = 'VERSION_CREATED'`, `new_status = 'DRAFT'`.

If any step fails, roll back the entire transaction.

**Cart Approval (DRAFT → ACTIVE, Version 1):**

Single transaction:

1. Compute recipient statuses — all `NEW` (no previous version exists).
2. Update `agreement_recipient.recipient_status_code = 'NEW'` and `status_computed_at = SYSUTCDATETIME()` for all recipients in this version.
3. Update `agreement_version`: `status_code = 'ACTIVE'`, `approved_at = SYSUTCDATETIME()`.
4. Update `agreement`: `current_status = 'ACTIVE'`, `active_version_id = <this version>`, `updated_at = SYSUTCDATETIME()`.
5. Delete `cart_item` row.
6. **`agreement_history`** — insert: `action_type = 'VERSION_APPROVED'`, `old_status = 'DRAFT'`, `new_status = 'ACTIVE'`.

---

### 4.2 Editing an Agreement

**Screens 1–4 (pre-populated):** Application reads from the current `ACTIVE` `agreement_version` and its children to pre-fill the wizard. No writes.

**Screen 5 – Customer Confirms (new DRAFT, same `agreement_id`):**

Single transaction:

1. **`agreement_version`** — insert new row: **same `agreement_id`**, `version_number = (MAX + 1)`, `status_code = 'DRAFT'`, `submitted_at = SYSUTCDATETIME()`. The existing active version is **not touched**.
2. **`contact`** — upsert any new contacts.
3. **`agreement_version_contact`** — insert contact links for the **new version only**.
4. **`agreement_recipient`** — insert recipient rows for the **new version**. Set `recipient_status_code = 'NEW'` as a placeholder (will be computed at approval).
5. **`agreement_recipient_product_part`** and **`agreement_recipient_product_part_account`** — insert for the new version.
6. **External Pricing API** — call, receive new reference.
7. **`agreement_pricing`** — insert new row for the new version.
8. **`cart_item`** — insert new row for the new draft version.
9. **`agreement_history`** — insert: `action_type = 'VERSION_CREATED'`, `new_status = 'DRAFT'`.

At this point: `version 1` is still `ACTIVE` and unmodified. `version 2` is `DRAFT` in the cart. The `agreement_id` is identical on both.

**Cart Approval (new DRAFT → ACTIVE, old ACTIVE → SUPERSEDED):**

Single transaction:

1. **Compute recipient statuses** — see Section 4.3.
2. Write computed statuses to `agreement_recipient.recipient_status_code` and `status_computed_at`.
3. Update old active version: `status_code = 'SUPERSEDED'`, `superseded_at = SYSUTCDATETIME()`.
4. Update new version: `status_code = 'ACTIVE'`, `approved_at = SYSUTCDATETIME()`.
5. Update `agreement`: `active_version_id = <new version id>`, `updated_at = SYSUTCDATETIME()`. `current_status` stays `ACTIVE`.
6. Delete `cart_item` row.
7. **`agreement_history`** — insert two rows:
   - `action_type = 'VERSION_SUPERSEDED'`, `old_status = 'ACTIVE'`, `new_status = 'SUPERSEDED'` for the old version.
   - `action_type = 'VERSION_APPROVED'`, `old_status = 'DRAFT'`, `new_status = 'ACTIVE'` for the new version.

---

### 4.3 Recipient Status Computation at Approval

This logic runs at approval time and writes to `agreement_recipient.recipient_status_code`. It compares the **incoming DRAFT version** against the **previous ACTIVE version** at the level of `(recipient_name, product_part_code, account_set)`.

**The comparison key** is the tuple: `(recipient_name, product_part_code, sorted_account_list)`.

This means a recipient is considered "the same" only if all three elements match. Changing the recipient name — while keeping the product part and accounts identical — produces one `CANCELLED` and one `NEW`, which is exactly the behaviour described in Requirement 2.

**Decision table:**

| Exists in Previous Active Version? | Exists in Incoming Draft Version? | Status Written |
|---|---|---|
| No | Yes | `NEW` |
| Yes | Yes (same name + parts + accounts) | `ACTIVE` |
| Yes | No | `CANCELLED` |

**Important:** `CANCELLED` recipients are included in the new version's `agreement_recipient` rows. They are carried forward as explicit records so the UI can display the "what changed" summary. They are **not** sent to the report generation engine — that engine filters on `recipient_status_code IN ('ACTIVE', 'NEW')`.

**Example from Requirement 2:**

Previous active version (v1):
- `Recipient-1` → `C054_DEBIT` → `[ACC001, ACC002]`

Incoming draft version (v2):
- `Recipient-2` → `C054_DEBIT` → `[ACC001, ACC002]`

Computation at v2 approval:
- `Recipient-1` / `C054_DEBIT` / `[ACC001, ACC002]` → exists in v1, not in v2 → **CANCELLED**
- `Recipient-2` / `C054_DEBIT` / `[ACC001, ACC002]` → exists in v2, not in v1 → **NEW**

Both rows exist in `agreement_recipient` for v2. The overall `agreement.current_status` remains `ACTIVE`.

---

### 4.4 Cancelling an Agreement

Single transaction:

1. Update active `agreement_version`: `status_code = 'TERMINATED'`, `terminated_at = SYSUTCDATETIME()`.
2. Update `agreement`: `current_status = 'TERMINATED'`, `active_version_id = NULL`, `updated_at = SYSUTCDATETIME()`.
3. If a `DRAFT` version exists in the cart for this agreement: set its `status_code = 'TERMINATED'` and delete its `cart_item` row.
4. **`agreement_history`** — insert: `action_type = 'AGREEMENT_TERMINATED'`, `old_status = 'ACTIVE'`, `new_status = 'TERMINATED'`.

No child rows are deleted. Full historical data is preserved.

---

## 5. Recipient Status Logic Reference

| Scenario | Recipient in v_new | Recipient Status |
|---|---|---|
| Brand new agreement (v1) | All recipients | `NEW` |
| Edit: recipient unchanged (name + parts + accounts identical) | Carried forward | `ACTIVE` |
| Edit: recipient name changed, parts + accounts same | Old name | `CANCELLED` |
| Edit: recipient name changed, parts + accounts same | New name | `NEW` |
| Edit: new recipient added | New recipient | `NEW` |
| Edit: existing recipient removed | Not present in v_new but carried forward | `CANCELLED` |
| Edit: recipient's product part changed | Old combination | `CANCELLED` |
| Edit: recipient's product part changed | New combination | `NEW` |
| Edit: account added/removed on existing recipient + part | Old combination | `CANCELLED` |
| Edit: account added/removed on existing recipient + part | New combination | `NEW` |

---

## 6. DDL Scripts

```sql
-- ============================================================
-- LOOKUP TABLES
-- ============================================================

CREATE TABLE agreement_status_lu (
    status_code     NVARCHAR(20)    NOT NULL,
    description     NVARCHAR(100)   NOT NULL,
    is_active       BIT             NOT NULL CONSTRAINT DF_agreement_status_lu_is_active DEFAULT 1,
    CONSTRAINT PK_agreement_status_lu PRIMARY KEY (status_code)
);

INSERT INTO agreement_status_lu (status_code, description) VALUES
    ('DRAFT',       'Created on Screen 5 confirmation; awaiting cart approval'),
    ('ACTIVE',      'Currently the live, serving version'),
    ('SUPERSEDED',  'Was active but replaced by a newer version'),
    ('TERMINATED',  'Permanently cancelled');


CREATE TABLE recipient_status_lu (
    status_code     NVARCHAR(20)    NOT NULL,
    description     NVARCHAR(100)   NOT NULL,
    is_active       BIT             NOT NULL CONSTRAINT DF_recipient_status_lu_is_active DEFAULT 1,
    CONSTRAINT PK_recipient_status_lu PRIMARY KEY (status_code)
);

INSERT INTO recipient_status_lu (status_code, description) VALUES
    ('NEW',         'Recipient did not exist in any previous version'),
    ('ACTIVE',      'Recipient carried forward unchanged from previous version'),
    ('CANCELLED',   'Recipient existed in previous version but removed in this version');


CREATE TABLE product_part_lu (
    product_part_code   NVARCHAR(30)    NOT NULL,
    product_part_name   NVARCHAR(100)   NOT NULL,
    camt_message_type   VARCHAR(10)     NOT NULL,
    sort_order          TINYINT         NOT NULL,
    is_active           BIT             NOT NULL CONSTRAINT DF_product_part_lu_is_active DEFAULT 1,
    CONSTRAINT PK_product_part_lu PRIMARY KEY (product_part_code)
);

INSERT INTO product_part_lu (product_part_code, product_part_name, camt_message_type, sort_order) VALUES
    ('C052_BAL_ONLY',   'Camt.052 Balances Only',               'camt.052', 1),
    ('C052_BAL_TXN',    'Camt.052 Balances & Transactions',     'camt.052', 2),
    ('C053_STANDARD',   'Camt.053 Standard',                    'camt.053', 3),
    ('C053_EXTENDED',   'Camt.053 Extended',                    'camt.053', 4),
    ('C054_DEBIT',      'Camt.054 Debit Notifications',         'camt.054', 5),
    ('C054_CREDIT',     'Camt.054 Credit Notifications',        'camt.054', 6);


-- ============================================================
-- CUSTOMER & ACCOUNTS
-- ============================================================

CREATE TABLE customer (
    customer_id             BIGINT          NOT NULL IDENTITY(1,1),
    external_customer_ref   NVARCHAR(100)   NOT NULL,
    customer_name           NVARCHAR(255)   NOT NULL,
    is_active               BIT             NOT NULL CONSTRAINT DF_customer_is_active DEFAULT 1,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_customer_created_at DEFAULT SYSUTCDATETIME(),
    updated_at              DATETIME2       NOT NULL CONSTRAINT DF_customer_updated_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_customer PRIMARY KEY (customer_id),
    CONSTRAINT UQ_customer_external_ref UNIQUE (external_customer_ref)
);


CREATE TABLE customer_account (
    customer_account_id BIGINT          NOT NULL IDENTITY(1,1),
    customer_id         BIGINT          NOT NULL,
    account_number      NVARCHAR(50)    NOT NULL,
    account_name        NVARCHAR(255)   NULL,
    currency_code       CHAR(3)         NOT NULL,
    is_active           BIT             NOT NULL CONSTRAINT DF_customer_account_is_active DEFAULT 1,
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_customer_account_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_customer_account PRIMARY KEY (customer_account_id),
    CONSTRAINT FK_customer_account_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
);

CREATE INDEX IX_customer_account_customer_id ON customer_account (customer_id);


-- ============================================================
-- CONTACT (customer-owned, reusable across versions)
-- ============================================================

CREATE TABLE contact (
    contact_id      BIGINT          NOT NULL IDENTITY(1,1),
    customer_id     BIGINT          NOT NULL,
    contact_name    NVARCHAR(200)   NOT NULL,
    email           NVARCHAR(320)   NOT NULL,
    is_active       BIT             NOT NULL CONSTRAINT DF_contact_is_active DEFAULT 1,
    created_at      DATETIME2       NOT NULL CONSTRAINT DF_contact_created_at DEFAULT SYSUTCDATETIME(),
    updated_at      DATETIME2       NOT NULL CONSTRAINT DF_contact_updated_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_contact PRIMARY KEY (contact_id),
    CONSTRAINT FK_contact_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT UQ_contact_customer_email UNIQUE (customer_id, email)
);

CREATE INDEX IX_contact_customer_id ON contact (customer_id);


-- ============================================================
-- AGREEMENT (stable anchor — agreement_id never changes)
-- ============================================================

CREATE TABLE agreement (
    agreement_id        BIGINT          NOT NULL IDENTITY(1,1),
    customer_id         BIGINT          NOT NULL,
    agreement_number    NVARCHAR(50)    NOT NULL,
    current_status      NVARCHAR(20)    NOT NULL,
    active_version_id   BIGINT          NULL,   -- FK added below after agreement_version exists
    created_at          DATETIME2       NOT NULL CONSTRAINT DF_agreement_created_at DEFAULT SYSUTCDATETIME(),
    updated_at          DATETIME2       NOT NULL CONSTRAINT DF_agreement_updated_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_agreement PRIMARY KEY (agreement_id),
    CONSTRAINT UQ_agreement_number UNIQUE (agreement_number),
    CONSTRAINT FK_agreement_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT FK_agreement_status FOREIGN KEY (current_status) REFERENCES agreement_status_lu (status_code)
);

CREATE INDEX IX_agreement_customer_id ON agreement (customer_id);


-- ============================================================
-- AGREEMENT VERSION
-- ============================================================

CREATE TABLE agreement_version (
    agreement_version_id    BIGINT          NOT NULL IDENTITY(1,1),
    agreement_id            BIGINT          NOT NULL,
    version_number          INT             NOT NULL,
    agreement_name          NVARCHAR(255)   NOT NULL,
    status_code             NVARCHAR(20)    NOT NULL,
    debit_account_id        BIGINT          NOT NULL,
    pricing_type            NVARCHAR(20)    NOT NULL,
    submitted_at            DATETIME2       NULL,
    approved_at             DATETIME2       NULL,
    superseded_at           DATETIME2       NULL,
    terminated_at           DATETIME2       NULL,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_agreement_version_created_at DEFAULT SYSUTCDATETIME(),
    created_by              NVARCHAR(255)   NOT NULL,
    CONSTRAINT PK_agreement_version PRIMARY KEY (agreement_version_id),
    CONSTRAINT UQ_agreement_version_number UNIQUE (agreement_id, version_number),
    CONSTRAINT FK_agreement_version_agreement FOREIGN KEY (agreement_id) REFERENCES agreement (agreement_id),
    CONSTRAINT FK_agreement_version_status FOREIGN KEY (status_code) REFERENCES agreement_status_lu (status_code),
    CONSTRAINT FK_agreement_version_debit_account FOREIGN KEY (debit_account_id) REFERENCES customer_account (customer_account_id),
    CONSTRAINT CK_agreement_version_pricing_type CHECK (pricing_type IN ('STANDARD', 'INDIVIDUAL'))
);

CREATE INDEX IX_agreement_version_agreement_id ON agreement_version (agreement_id);

-- Circular FK: agreement → agreement_version (added after both tables exist)
ALTER TABLE agreement
    ADD CONSTRAINT FK_agreement_active_version
    FOREIGN KEY (active_version_id) REFERENCES agreement_version (agreement_version_id);


-- ============================================================
-- AGREEMENT VERSION CONTACT
-- ============================================================

CREATE TABLE agreement_version_contact (
    agreement_version_contact_id    BIGINT      NOT NULL IDENTITY(1,1),
    agreement_version_id            BIGINT      NOT NULL,
    contact_id                      BIGINT      NOT NULL,
    sort_order                      INT         NOT NULL CONSTRAINT DF_agreement_version_contact_sort DEFAULT 0,
    created_at                      DATETIME2   NOT NULL CONSTRAINT DF_agreement_version_contact_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_agreement_version_contact PRIMARY KEY (agreement_version_contact_id),
    CONSTRAINT UQ_agreement_version_contact UNIQUE (agreement_version_id, contact_id),
    CONSTRAINT FK_avc_version FOREIGN KEY (agreement_version_id) REFERENCES agreement_version (agreement_version_id),
    CONSTRAINT FK_avc_contact FOREIGN KEY (contact_id) REFERENCES contact (contact_id)
);

CREATE INDEX IX_avc_version_id ON agreement_version_contact (agreement_version_id);


-- ============================================================
-- AGREEMENT RECIPIENT
-- ============================================================

CREATE TABLE agreement_recipient (
    agreement_recipient_id  BIGINT          NOT NULL IDENTITY(1,1),
    agreement_version_id    BIGINT          NOT NULL,
    recipient_name          NVARCHAR(255)   NOT NULL,
    recipient_status_code   NVARCHAR(20)    NOT NULL,
    sort_order              INT             NOT NULL CONSTRAINT DF_agreement_recipient_sort DEFAULT 0,
    status_computed_at      DATETIME2       NULL,
    created_at              DATETIME2       NOT NULL CONSTRAINT DF_agreement_recipient_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_agreement_recipient PRIMARY KEY (agreement_recipient_id),
    CONSTRAINT FK_agreement_recipient_version FOREIGN KEY (agreement_version_id) REFERENCES agreement_version (agreement_version_id),
    CONSTRAINT FK_agreement_recipient_status FOREIGN KEY (recipient_status_code) REFERENCES recipient_status_lu (status_code)
);

CREATE INDEX IX_agreement_recipient_version_id ON agreement_recipient (agreement_version_id);


-- ============================================================
-- AGREEMENT RECIPIENT PRODUCT PART
-- ============================================================

CREATE TABLE agreement_recipient_product_part (
    agreement_recipient_product_part_id BIGINT          NOT NULL IDENTITY(1,1),
    agreement_recipient_id              BIGINT          NOT NULL,
    product_part_code                   NVARCHAR(30)    NOT NULL,
    created_at                          DATETIME2       NOT NULL CONSTRAINT DF_arpp_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_agreement_recipient_product_part PRIMARY KEY (agreement_recipient_product_part_id),
    CONSTRAINT UQ_agreement_recipient_product_part UNIQUE (agreement_recipient_id, product_part_code),
    CONSTRAINT FK_arpp_recipient FOREIGN KEY (agreement_recipient_id) REFERENCES agreement_recipient (agreement_recipient_id),
    CONSTRAINT FK_arpp_product_part FOREIGN KEY (product_part_code) REFERENCES product_part_lu (product_part_code)
);

CREATE INDEX IX_arpp_recipient_id ON agreement_recipient_product_part (agreement_recipient_id);


-- ============================================================
-- AGREEMENT RECIPIENT PRODUCT PART ACCOUNT
-- ============================================================

CREATE TABLE agreement_recipient_product_part_account (
    agreement_recipient_product_part_account_id BIGINT      NOT NULL IDENTITY(1,1),
    agreement_recipient_product_part_id         BIGINT      NOT NULL,
    customer_account_id                         BIGINT      NOT NULL,
    created_at                                  DATETIME2   NOT NULL CONSTRAINT DF_arppa_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_agreement_recipient_product_part_account PRIMARY KEY (agreement_recipient_product_part_account_id),
    CONSTRAINT UQ_arppa UNIQUE (agreement_recipient_product_part_id, customer_account_id),
    CONSTRAINT FK_arppa_product_part FOREIGN KEY (agreement_recipient_product_part_id) REFERENCES agreement_recipient_product_part (agreement_recipient_product_part_id),
    CONSTRAINT FK_arppa_account FOREIGN KEY (customer_account_id) REFERENCES customer_account (customer_account_id)
);

CREATE INDEX IX_arppa_product_part_id ON agreement_recipient_product_part_account (agreement_recipient_product_part_id);


-- ============================================================
-- AGREEMENT PRICING
-- ============================================================

CREATE TABLE agreement_pricing (
    agreement_pricing_id        BIGINT          NOT NULL IDENTITY(1,1),
    agreement_version_id        BIGINT          NOT NULL,
    pricing_order_reference     NVARCHAR(255)   NOT NULL,
    created_at                  DATETIME2       NOT NULL CONSTRAINT DF_agreement_pricing_created_at DEFAULT SYSUTCDATETIME(),
    CONSTRAINT PK_agreement_pricing PRIMARY KEY (agreement_pricing_id),
    CONSTRAINT UQ_agreement_pricing_version UNIQUE (agreement_version_id),
    CONSTRAINT FK_agreement_pricing_version FOREIGN KEY (agreement_version_id) REFERENCES agreement_version (agreement_version_id)
);


-- ============================================================
-- CART ITEM
-- ============================================================

CREATE TABLE cart_item (
    cart_item_id            BIGINT      NOT NULL IDENTITY(1,1),
    customer_id             BIGINT      NOT NULL,
    agreement_version_id    BIGINT      NOT NULL,
    added_at                DATETIME2   NOT NULL CONSTRAINT DF_cart_item_added_at DEFAULT SYSUTCDATETIME(),
    expires_at              DATETIME2   NOT NULL,
    is_expired              AS (CASE WHEN SYSUTCDATETIME() > expires_at THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END),
    CONSTRAINT PK_cart_item PRIMARY KEY (cart_item_id),
    CONSTRAINT UQ_cart_item_version UNIQUE (agreement_version_id),
    CONSTRAINT FK_cart_item_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id),
    CONSTRAINT FK_cart_item_version FOREIGN KEY (agreement_version_id) REFERENCES agreement_version (agreement_version_id)
);

CREATE INDEX IX_cart_item_customer_id ON cart_item (customer_id);
CREATE INDEX IX_cart_item_expires_at ON cart_item (expires_at);


-- ============================================================
-- AGREEMENT HISTORY (append-only audit log)
-- ============================================================

CREATE TABLE agreement_history (
    history_id              BIGINT          NOT NULL IDENTITY(1,1),
    agreement_id            BIGINT          NOT NULL,
    agreement_version_id    BIGINT          NULL,
    action_type             NVARCHAR(50)    NOT NULL,
    old_status              NVARCHAR(20)    NULL,
    new_status              NVARCHAR(20)    NOT NULL,
    changed_by              NVARCHAR(255)   NOT NULL,
    changed_at              DATETIME2       NOT NULL CONSTRAINT DF_agreement_history_changed_at DEFAULT SYSUTCDATETIME(),
    change_reason           NVARCHAR(500)   NULL,
    snapshot_data           NVARCHAR(MAX)   NULL,
    CONSTRAINT PK_agreement_history PRIMARY KEY (history_id),
    CONSTRAINT FK_agreement_history_agreement FOREIGN KEY (agreement_id) REFERENCES agreement (agreement_id),
    CONSTRAINT FK_agreement_history_version FOREIGN KEY (agreement_version_id) REFERENCES agreement_version (agreement_version_id)
);

CREATE INDEX IX_agreement_history_agreement_id ON agreement_history (agreement_id);
CREATE INDEX IX_agreement_history_changed_at ON agreement_history (changed_at);
```
