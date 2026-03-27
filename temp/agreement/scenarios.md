# Corporate Reporting Agreement — Workflow Scenarios

**Version:** 1.0  
**Status:** DRAFT

---

## Table of Contents

1. [Scenario 1: Simple Happy Path — New Customer Onboarding](#scenario-1-simple-happy-path--new-customer-onboarding)
2. [Scenario 2: Adding a New Recipient](#scenario-2-adding-a-new-recipient)
3. [Scenario 3: Replacing a Recipient (1-to-1)](#scenario-3-replacing-a-recipient-1-to-1)
4. [Scenario 4: Splitting a Recipient (Remove + Two Adds)](#scenario-4-splitting-a-recipient-remove--two-adds)
5. [Scenario 5: Removing a Recipient](#scenario-5-removing-a-recipient)
6. [Scenario 6: Cancelling the Entire Agreement](#scenario-6-cancelling-the-entire-agreement)
7. [Scenario 7: Concurrent Edit and Cancel in Cart](#scenario-7-concurrent-edit-and-cancel-in-cart)
8. [Scenario 8: Cart Expiry — No Approval Within 30 Days](#scenario-8-cart-expiry--no-approval-within-30-days)
9. [Scenario 9: Multiple Accounts Across Recipients (No Exclusivity)](#scenario-9-multiple-accounts-across-recipients-no-exclusivity)
10. [Scenario 10: Historical State Query (Audit)](#scenario-10-historical-state-query-audit)

---

## Scenario 1: Simple Happy Path — New Customer Onboarding

**Context:** ABC Corp signs up for cash management services.

### Day 1 — Create

```
POST /agreements
→ INSERT agreement
      agreement_name = "ABC Corp Agreement"
      corporate_id   = "CORP_ABC001"

→ INSERT agreement_version
      version_number = 1
      status         = DRAFT

→ INSERT agreement_contact (x2)
      "John Smith" <john@abccorp.com>
      "Finance Team" <finance@abccorp.com>

→ INSERT message_recipient (x2)
      "REC_DAILY"   status = DRAFT
      "REC_MONTHLY" status = DRAFT

→ INSERT product_part_assignment
      REC_DAILY   → CAMT052_BALANCES_TX
      REC_MONTHLY → CAMT053_STANDARD

→ INSERT account_assignment
      CAMT052_BALANCES_TX → account_bban = "ACC123"
      CAMT052_BALANCES_TX → account_bban = "ACC456"
      CAMT053_STANDARD    → account_bban = "ACC123"   (same account, different product)

→ INSERT cart_item
      expires_at = Day 31
```

### Day 2 — Approve

```
POST /cart/{cartItemId}/approve
→ UPDATE agreement_version v1   status = ACTIVE,     activated_at = Day 2
→ UPDATE message_recipient      status = ACTIVE,     activated_at = Day 2  (both recipients)
→ UPDATE cart_item              approved_at = Day 2
```

### Expected State

| Entity | Value |
|---|---|
| `agreement_version` | v1 = ACTIVE |
| `message_recipient` | REC_DAILY = ACTIVE, REC_MONTHLY = ACTIVE |
| `account_assignment` | ACC123 assigned to both product parts, ACC456 to CAMT052_BALANCES_TX |

---

## Scenario 2: Adding a New Recipient

**Context:** ABC Corp acquires a new subsidiary with a new account.

### Starting State (from Scenario 1)

- Version 1 ACTIVE with 2 recipients (REC_DAILY, REC_MONTHLY)

### Day 60 — Edit

```
POST /agreements/{agreementId}/edit
→ INSERT agreement_version
      version_number           = 2
      status                   = DRAFT
      branched_from_version_id = v1.id

→ INSERT agreement_contact (x2)   — copied forward unchanged

→ INSERT message_recipient
      "REC_DAILY"      status = DRAFT   (unchanged, copied forward)
      "REC_MONTHLY"    status = DRAFT   (unchanged, copied forward)
      "REC_SUBSIDIARY" status = DRAFT   (new)

→ INSERT product_part_assignment
      REC_DAILY      → CAMT052_BALANCES_TX
      REC_MONTHLY    → CAMT053_STANDARD
      REC_SUBSIDIARY → CAMT054_CREDIT   (inferred from payment_type_1)

→ INSERT payment_type_assignment
      REC_SUBSIDIARY / CAMT054_CREDIT → payment_type_1

→ INSERT account_assignment
      CAMT052_BALANCES_TX → ACC123, ACC456   (copied forward)
      CAMT053_STANDARD    → ACC123            (copied forward)
      payment_type_1      → ACC789            (new)

→ INSERT cart_item
      expires_at = Day 90
```

### Day 61 — Approve

```
POST /cart/{cartItemId}/approve
PRE-CHECK: branched_from_version_id = v1.id = current ACTIVE version ✓

→ UPDATE agreement_version v1   status = SUPERSEDED,  superseded_at = Day 61
→ UPDATE agreement_version v2   status = ACTIVE,      activated_at  = Day 61
→ UPDATE message_recipient      status = ACTIVE,      activated_at  = Day 61  (all 3 on v2)
→ UPDATE cart_item              approved_at = Day 61
```

### Expected State

| Entity | Value |
|---|---|
| `agreement_version` | v1 = SUPERSEDED, v2 = ACTIVE |
| `message_recipient` on v2 | REC_DAILY = ACTIVE, REC_MONTHLY = ACTIVE, REC_SUBSIDIARY = ACTIVE |

### Historical Query: What was active on Day 30?

```sql
SELECT mr.recipient_id, ppa.product_part
FROM   message_recipient mr
JOIN   product_part_assignment ppa ON ppa.message_recipient_id = mr.id
JOIN   agreement_version av        ON av.id = mr.agreement_version_id
WHERE  av.agreement_id  = 'ABC_AGREEMENT_ID'
  AND  av.activated_at  <= 'Day 30'
  AND  (av.superseded_at IS NULL OR av.superseded_at > 'Day 30')
  AND  av.status IN ('ACTIVE', 'SUPERSEDED')
  AND  mr.status = 'ACTIVE'
```

→ Returns REC_DAILY and REC_MONTHLY only (Version 1).

---

## Scenario 3: Replacing a Recipient (1-to-1)

**Context:** REC_DAILY's contact details have changed — a new signer is taking over.

### Starting State (from Scenario 2)

- Version 2 ACTIVE with 3 recipients

### Day 100 — Edit

```
POST /agreements/{agreementId}/edit
→ INSERT agreement_version
      version_number           = 3
      status                   = DRAFT
      branched_from_version_id = v2.id

→ INSERT message_recipient
      "REC_DAILY" (old)  status = CANCELLED
                         cancelled_at = Day 100
                         replaced_by_recipient_id = REC_DAILY_V2.id

      "REC_DAILY_V2" (new) status = DRAFT

      "REC_MONTHLY"        status = DRAFT   (unchanged, copied forward)
      "REC_SUBSIDIARY"     status = DRAFT   (unchanged, copied forward)

→ INSERT product_part_assignment   (DRAFT recipients only — not REC_DAILY old)
      REC_DAILY_V2 → CAMT052_BALANCES_TX
      REC_MONTHLY  → CAMT053_STANDARD
      REC_SUBSIDIARY → CAMT054_CREDIT

→ INSERT payment_type_assignment
      REC_SUBSIDIARY / CAMT054_CREDIT → payment_type_1

→ INSERT account_assignment
      REC_DAILY_V2 / CAMT052_BALANCES_TX → ACC123, ACC456   (copied forward)
      REC_MONTHLY  / CAMT053_STANDARD    → ACC123            (copied forward)
      REC_SUBSIDIARY / payment_type_1    → ACC789            (copied forward)

→ INSERT cart_item
      expires_at = Day 130
```

### Day 101 — Approve

```
POST /cart/{cartItemId}/approve
PRE-CHECK: branched_from_version_id = v2.id = current ACTIVE version ✓

→ UPDATE agreement_version v2   status = SUPERSEDED,  superseded_at = Day 101
→ UPDATE agreement_version v3   status = ACTIVE,      activated_at  = Day 101
→ UPDATE message_recipient      status = ACTIVE,      activated_at  = Day 101
        (REC_DAILY_V2, REC_MONTHLY, REC_SUBSIDIARY — all DRAFT on v3)
→ UPDATE cart_item              approved_at = Day 101
```

### Tracing Replace Lineage

```sql
-- Find the old recipient that was replaced
SELECT id, recipient_id, status, replaced_by_recipient_id
FROM   message_recipient
WHERE  agreement_version_id = 'v3.id'
  AND  status = 'CANCELLED'
-- → Returns REC_DAILY (old), replaced_by_recipient_id = REC_DAILY_V2.id

-- Find the new recipient that replaced it
SELECT id, recipient_id, status
FROM   message_recipient
WHERE  id = (
  SELECT replaced_by_recipient_id
  FROM   message_recipient
  WHERE  recipient_id = 'REC_DAILY'
    AND  agreement_version_id = 'v3.id'
    AND  status = 'CANCELLED'
)
-- → Returns REC_DAILY_V2
```

### Expected State

| Entity | Value |
|---|---|
| `agreement_version` | v2 = SUPERSEDED, v3 = ACTIVE |
| `message_recipient` on v3 | REC_DAILY (old) = CANCELLED, REC_DAILY_V2 = ACTIVE, REC_MONTHLY = ACTIVE, REC_SUBSIDIARY = ACTIVE |
| CANCELLED recipient | Has `replaced_by_recipient_id` pointing to REC_DAILY_V2 |
| Product assignments | REC_DAILY (old) carries none — readable from v2 |

---

## Scenario 4: Splitting a Recipient (Remove + Two Adds)

**Context:** Daily reporting needs to be split — ACC123 and ACC456 must now go to separate recipients.  
A split is not a 1-to-1 replace. It is modelled as one REMOVE and two ADDs.

### Starting State (from Scenario 3)

- Version 3 ACTIVE with 4 recipients (REC_DAILY_V2 has ACC123 + ACC456)

### Day 120 — Edit

```
POST /agreements/{agreementId}/edit
→ INSERT agreement_version
      version_number           = 4
      status                   = DRAFT
      branched_from_version_id = v3.id

→ INSERT message_recipient
      "REC_DAILY_V2" (removed) status = CANCELLED
                               cancelled_at = Day 120
                               replaced_by_recipient_id = NULL   ← removal, not replace

      "REC_DAILY_A" (new) status = DRAFT
      "REC_DAILY_B" (new) status = DRAFT

      "REC_MONTHLY"       status = DRAFT   (unchanged, copied forward)
      "REC_SUBSIDIARY"    status = DRAFT   (unchanged, copied forward)

→ INSERT product_part_assignment   (DRAFT recipients only)
      REC_DAILY_A  → CAMT052_BALANCES_TX
      REC_DAILY_B  → CAMT052_BALANCES_TX
      REC_MONTHLY  → CAMT053_STANDARD
      REC_SUBSIDIARY → CAMT054_CREDIT

→ INSERT payment_type_assignment
      REC_SUBSIDIARY / CAMT054_CREDIT → payment_type_1

→ INSERT account_assignment
      REC_DAILY_A / CAMT052_BALANCES_TX → ACC123
      REC_DAILY_B / CAMT052_BALANCES_TX → ACC456
      REC_MONTHLY / CAMT053_STANDARD    → ACC123   (copied forward)
      REC_SUBSIDIARY / payment_type_1   → ACC789   (copied forward)

→ INSERT cart_item
      expires_at = Day 150
```

### Day 121 — Approve

```
POST /cart/{cartItemId}/approve
PRE-CHECK: branched_from_version_id = v3.id = current ACTIVE version ✓

→ UPDATE agreement_version v3   status = SUPERSEDED,  superseded_at = Day 121
→ UPDATE agreement_version v4   status = ACTIVE,      activated_at  = Day 121
→ UPDATE message_recipient      status = ACTIVE,      activated_at  = Day 121
        (REC_DAILY_A, REC_DAILY_B, REC_MONTHLY, REC_SUBSIDIARY — all DRAFT on v4)
→ UPDATE cart_item              approved_at = Day 121
```

### Expected State

| Entity | Value |
|---|---|
| `agreement_version` | v3 = SUPERSEDED, v4 = ACTIVE |
| `message_recipient` on v4 | REC_DAILY_V2 = CANCELLED (no replacement), REC_DAILY_A = ACTIVE, REC_DAILY_B = ACTIVE, REC_MONTHLY = ACTIVE, REC_SUBSIDIARY = ACTIVE |
| CANCELLED recipient | `replaced_by_recipient_id = NULL` — confirms removal, not replace |

---

## Scenario 5: Removing a Recipient

**Context:** Subsidiary divested — REC_SUBSIDIARY no longer needed.

### Starting State (from Scenario 4)

- Version 4 ACTIVE with 4 recipients (REC_DAILY_A, REC_DAILY_B, REC_MONTHLY, REC_SUBSIDIARY)

### Day 180 — Edit

```
POST /agreements/{agreementId}/edit
→ INSERT agreement_version
      version_number           = 5
      status                   = DRAFT
      branched_from_version_id = v4.id

→ INSERT message_recipient
      "REC_DAILY_A"    status = DRAFT   (unchanged, copied forward)
      "REC_DAILY_B"    status = DRAFT   (unchanged, copied forward)
      "REC_MONTHLY"    status = DRAFT   (unchanged, copied forward)
      "REC_SUBSIDIARY" status = CANCELLED
                       cancelled_at = Day 180
                       replaced_by_recipient_id = NULL

→ INSERT product_part_assignment   (DRAFT recipients only — not REC_SUBSIDIARY)
      REC_DAILY_A → CAMT052_BALANCES_TX
      REC_DAILY_B → CAMT052_BALANCES_TX
      REC_MONTHLY → CAMT053_STANDARD

→ INSERT account_assignment        (copied forward for DRAFT recipients)
      REC_DAILY_A / CAMT052_BALANCES_TX → ACC123
      REC_DAILY_B / CAMT052_BALANCES_TX → ACC456
      REC_MONTHLY / CAMT053_STANDARD    → ACC123

→ INSERT cart_item
      expires_at = Day 210
```

### Day 181 — Approve

```
POST /cart/{cartItemId}/approve
PRE-CHECK: branched_from_version_id = v4.id = current ACTIVE version ✓

→ UPDATE agreement_version v4   status = SUPERSEDED,  superseded_at = Day 181
→ UPDATE agreement_version v5   status = ACTIVE,      activated_at  = Day 181
→ UPDATE message_recipient      status = ACTIVE,      activated_at  = Day 181
        (REC_DAILY_A, REC_DAILY_B, REC_MONTHLY — all DRAFT on v5)
→ UPDATE cart_item              approved_at = Day 181
```

### Expected State

| Entity | Value |
|---|---|
| `agreement_version` | v4 = SUPERSEDED, v5 = ACTIVE |
| `message_recipient` on v5 | REC_SUBSIDIARY = CANCELLED (`replaced_by_recipient_id = NULL`), REC_DAILY_A = ACTIVE, REC_DAILY_B = ACTIVE, REC_MONTHLY = ACTIVE |

---

## Scenario 6: Cancelling the Entire Agreement

**Context:** ABC Corp switches to a competitor.

### Starting State (from Scenario 5)

- Version 5 ACTIVE with 3 recipients (REC_DAILY_A, REC_DAILY_B, REC_MONTHLY)

### Day 240 — Cancel Initiation

```
POST /agreements/{agreementId}/cancel
→ INSERT agreement_version
      version_number           = 6
      status                   = DRAFT_CANCEL
      branched_from_version_id = v5.id

→ INSERT agreement_contact         — copied forward from v5
→ INSERT message_recipient         — all copied forward, status = DRAFT
      "REC_DAILY_A"   status = DRAFT
      "REC_DAILY_B"   status = DRAFT
      "REC_MONTHLY"   status = DRAFT

→ INSERT product_part_assignment   — copied forward from v5
→ INSERT account_assignment        — copied forward from v5

→ INSERT cart_item
      expires_at = Day 270
```

### Day 241 — Approve Cancellation

```
POST /cart/{cartItemId}/approve
PRE-CHECK: branched_from_version_id = v5.id = current ACTIVE version ✓

→ UPDATE agreement_version v5   status = SUPERSEDED,  superseded_at = Day 241
→ UPDATE agreement_version v6   status = CANCELLED,   cancelled_at  = Day 241
→ UPDATE message_recipient      status = CANCELLED,   cancelled_at  = Day 241
        (all DRAFT recipients on v6 — REC_DAILY_A, REC_DAILY_B, REC_MONTHLY)
→ UPDATE cart_item              approved_at = Day 241
```

### Expected State

| Entity | Value |
|---|---|
| `agreement_version` | v5 = SUPERSEDED, v6 = CANCELLED |
| `message_recipient` on v6 | REC_DAILY_A = CANCELLED, REC_DAILY_B = CANCELLED, REC_MONTHLY = CANCELLED |

> **Note:** Recipients on a `DRAFT_CANCEL` version become `CANCELLED` on approval — not `ACTIVE`.  
> This is distinct from an edit approval where DRAFT recipients become `ACTIVE`.

---

## Scenario 7: Concurrent Edit and Cancel in Cart

**Context:** An edit and a cancellation are both in the cart simultaneously.  
A DRAFT and a DRAFT_CANCEL may coexist in the cart for the same agreement at the same time.

### Starting State

- Version 1 ACTIVE (for simplicity, a fresh agreement)

### Day 300 Morning — Edit Submitted

```
POST /agreements/{agreementId}/edit
→ INSERT agreement_version
      version_number           = 2
      status                   = DRAFT
      branched_from_version_id = v1.id
→ INSERT cart_item A   expires_at = Day 330
```

### Day 300 Afternoon — Cancel Submitted

```
POST /agreements/{agreementId}/cancel
→ INSERT agreement_version
      version_number           = 3
      status                   = DRAFT_CANCEL
      branched_from_version_id = v1.id
→ INSERT cart_item B   expires_at = Day 330
```

### Cart State at End of Day 300

| Cart Item | Version | Status |
|---|---|---|
| Cart A | v2 | DRAFT |
| Cart B | v3 | DRAFT_CANCEL |

Both are valid. The customer chooses which to approve first.

---

### Day 301 — Customer Approves the Cancellation (Cart B)

```
POST /cart/{cartItemB}/approve
PRE-CHECK: branched_from_version_id = v1.id = current ACTIVE version ✓

→ UPDATE agreement_version v1   status = SUPERSEDED,  superseded_at = Day 301
→ UPDATE agreement_version v3   status = CANCELLED,   cancelled_at  = Day 301
→ UPDATE message_recipient      status = CANCELLED,   cancelled_at  = Day 301  (all on v3)
→ UPDATE cart_item B            approved_at = Day 301

Sibling invalidation:
→ UPDATE agreement_version v2   status = INVALIDATED, invalidated_at = Day 301
→ UPDATE cart_item A            expired_at = Day 301
```

### Final State

| Version | Status | Notes |
|---|---|---|
| v1 | SUPERSEDED | Was the active version |
| v2 | INVALIDATED | Edit draft orphaned by cancellation approval |
| v3 | CANCELLED | Agreement is now terminated |

> **Key rule:** On any approval, all sibling `DRAFT` or `DRAFT_CANCEL` versions for the same  
> agreement are immediately `INVALIDATED` and their cart items expired.  
> This means Cart A is no longer approvable even if the customer tries.

---

### Alternate: Day 301 — Customer Approves the Edit Instead (Cart A)

```
POST /cart/{cartItemA}/approve
PRE-CHECK: branched_from_version_id = v1.id = current ACTIVE version ✓

→ UPDATE agreement_version v1   status = SUPERSEDED,  superseded_at = Day 301
→ UPDATE agreement_version v2   status = ACTIVE,      activated_at  = Day 301
→ UPDATE message_recipient      status = ACTIVE,      activated_at  = Day 301  (all DRAFT on v2)
→ UPDATE cart_item A            approved_at = Day 301

Sibling invalidation:
→ UPDATE agreement_version v3   status = INVALIDATED, invalidated_at = Day 301
→ UPDATE cart_item B            expired_at = Day 301
```

> Agreement remains live under v2. The cancellation draft is orphaned and invalidated.

---

## Scenario 8: Cart Expiry — No Approval Within 30 Days

**Context:** A user submits an edit but never returns to approve it.

### Day 1 — Edit Submitted

```
POST /agreements/{agreementId}/edit
→ INSERT agreement_version
      version_number           = 2
      status                   = DRAFT
      branched_from_version_id = v1.id
→ INSERT cart_item   expires_at = Day 31
```

### Day 31 — Nightly Expiry Job Runs

```sql
-- Step 1: Find expired, unapproved cart items
SELECT * FROM cart_item
WHERE  expires_at  < now
  AND  expired_at  IS NULL
  AND  approved_at IS NULL

-- Step 2: Expire the cart item
UPDATE cart_item
SET    expired_at = now
WHERE  id = cart_item.id

-- Step 3: Expire the version
UPDATE agreement_version
SET    status     = EXPIRED,
       expired_at = now
WHERE  id     = cart_item.agreement_version_id
  AND  status IN ('DRAFT', 'DRAFT_CANCEL')
```

### Day 32 — User Attempts to Approve

```
POST /cart/{cartItemId}/approve
→ 409 CONFLICT: Cart item is already expired.
```

### Final State

| Entity | Value |
|---|---|
| `agreement_version` v1 | ACTIVE — completely unaffected |
| `agreement_version` v2 | EXPIRED |
| `cart_item` | `expired_at` set by nightly job |

> The previously ACTIVE version stays ACTIVE. The customer may start a fresh  
> edit at any time, which will branch from the still-ACTIVE v1.

---

## Scenario 9: Multiple Accounts Across Recipients (No Exclusivity)

**Context:** The same account BBAN can appear across multiple recipients and product parts within the same version. There is no exclusivity constraint.

### Version State

```
Version 1 ACTIVE:
  Recipient A: CAMT052_BALANCES_TX
    → account_bban = "ACC123"
    → account_bban = "ACC456"
  Recipient B: CAMT053_STANDARD
    → account_bban = "ACC123"   ← same BBAN, different recipient + product part
    → account_bban = "ACC789"
```

### Query: All Active Recipients Receiving Data for ACC123

```sql
SELECT DISTINCT
       mr.id            AS recipient_row_id,
       mr.recipient_id,
       ppa.product_part
FROM   message_recipient mr
JOIN   product_part_assignment ppa ON ppa.message_recipient_id = mr.id
JOIN   account_assignment aa       ON aa.product_part_assignment_id = ppa.id
JOIN   agreement_version av        ON av.id = mr.agreement_version_id
WHERE  av.status        = 'ACTIVE'
  AND  mr.status        = 'ACTIVE'
  AND  aa.account_bban  = 'ACC123'
```

→ Returns two rows: Recipient A / CAMT052_BALANCES_TX and Recipient B / CAMT053_STANDARD.

> **Design note:** ACC123 appearing twice is intentional. Each row in `account_assignment`  
> belongs to a specific `product_part_assignment`, so the same BBAN can independently  
> feed different product types to different recipients.

---

## Scenario 10: Historical State Query (Audit)

**Context:** Compliance requires knowing exactly which recipients were active on any given past date.

### Timeline

| Day | Event |
|---|---|
| Day 1 | Version 1 ACTIVE — Recipient X only |
| Day 100 | Version 2 ACTIVE — Recipients X, Y |
| Day 200 | Version 3 ACTIVE — Recipients X, Y, Z |
| Day 300 | Version 4 ACTIVE — Recipients X, Z (Y removed) |

### Query: Who Was Active on Day 150?

```sql
SELECT mr.recipient_id,
       ppa.product_part,
       av.version_number
FROM   message_recipient mr
JOIN   product_part_assignment ppa ON ppa.message_recipient_id = mr.id
JOIN   agreement_version av        ON av.id = mr.agreement_version_id
WHERE  av.agreement_id   = 'ABC_AGREEMENT_ID'
  AND  av.activated_at   <= 'Day 150'
  AND  (av.superseded_at  IS NULL OR av.superseded_at > 'Day 150')
  AND  av.status         IN ('ACTIVE', 'SUPERSEDED')
  AND  mr.status         = 'ACTIVE'
```

→ Returns Recipients X and Y under Version 2. Version 3 had not yet been activated.

### Query: Full Audit Trail for an Agreement

```sql
SELECT av.version_number,
       av.status,
       av.activated_at,
       av.superseded_at,
       av.cancelled_at,
       mr.recipient_id,
       mr.status AS recipient_status
FROM   agreement_version av
JOIN   message_recipient mr ON mr.agreement_version_id = av.id
WHERE  av.agreement_id = 'ABC_AGREEMENT_ID'
ORDER  BY av.version_number ASC, mr.recipient_id ASC
```

→ Returns the complete version and recipient history across all versions in chronological order.
