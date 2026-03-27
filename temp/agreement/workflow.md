# Corporate Reporting Agreement — Operation Workflow Reference

**Version:** 1.1  
**Status:** DRAFT  

---

## Table of Contents

1. [Status Reference](#1-status-reference)
2. [Create — New Agreement Onboarding](#2-create--new-agreement-onboarding)
3. [Edit — Modifying an Active Agreement](#3-edit--modifying-an-active-agreement)
4. [Cancel — Initiating Agreement Cancellation](#4-cancel--initiating-agreement-cancellation)
5. [Cart Expiry — No Approval Within 30 Days](#5-cart-expiry--no-approval-within-30-days)

---

## 1. Status Reference

### 1.1 `agreement_version.status`

| Status | Description |
|---|---|
| `DRAFT` | Created on new agreement or edit. Awaiting customer approval in cart. |
| `ACTIVE` | Approved and live. Exactly one `ACTIVE` version per agreement at any time. |
| `SUPERSEDED` | Was `ACTIVE`; replaced when a newer version was approved. |
| `DRAFT_CANCEL` | Cancellation initiated. Awaiting customer approval in cart. |
| `CANCELLED` | Agreement terminated. Set on `DRAFT_CANCEL` approval. |
| `EXPIRED` | Cart item expired before approval. Set by nightly job. |
| `INVALIDATED` | A sibling `DRAFT` or `DRAFT_CANCEL` was approved, orphaning this version. |

### 1.2 `message_recipient.status`

| Status | Description |
|---|---|
| `DRAFT` | Recipient is on a `DRAFT` or `DRAFT_CANCEL` version. Transitions on parent version approval. |
| `ACTIVE` | Set when parent version is approved as `ACTIVE`. Live and receiving reports. |
| `CANCELLED` | Set immediately at edit time (replaced or removed), or on approval when parent version is `CANCELLED`. |

---

## 2. Create — New Agreement Onboarding

Triggered when a customer completes all wizards and confirms to add to cart, for the first time. No prior agreement exists.

### 2.1 Initial Submission

```
1. INSERT agreement
       agreement_name frozen here — never updated across versions

2. INSERT agreement_version
       version = 1, status = DRAFT

3. INSERT agreement_contact
       One or more contacts (name + email). At least one required.

4. INSERT message_recipient
       All recipients, status = DRAFT

5. INSERT product_part_assignment
       One row per recipient-productpart pair. DRAFT recipients only.

6. INSERT payment_type_assignment          [CAMT054_CREDIT only]
       One row per payment type selected for each CAMT054_CREDIT assignment.
       At least one payment type required when product_part = CAMT054_CREDIT.
       References product_part_assignment.id.

7. For each payment_type_assignment, insert accounts and/or aliases whichever applicable:

       INSERT assignment_account           [accounts path]
           One or more accounts per payment_type_assignment.
           References product_part_assignment.id.
           Same account_id may appear across multiple recipients for the same
           product part — no exclusivity constraint.

       INSERT alias_assignment             [aliases path]
           One or more aliases per payment_type_assignment.
           References payment_type_assignment.id.

   For all other product parts (non-CAMT054_CREDIT):

       INSERT assignment_account as before — one row per account per assignment.
       Same account_id may appear across multiple recipients for the same
       product part — no exclusivity constraint.

8. INSERT cart_item
       expires_at = now + 30 days
```

### 2.2 On Approval (customer approves from cart)

```
9.  UPDATE agreement_version
        status = ACTIVE, activated_at = now

10. UPDATE message_recipient
        All DRAFT recipients on this version → ACTIVE, activated_at = now

11. UPDATE cart_item
        approved_at = now
```

---

## 3. Edit — Modifying an Active Agreement

Triggered when a customer opens an `ACTIVE` agreement and clicks Edit. The system creates a new `DRAFT` version (N+1). The `ACTIVE` version continues running uninterrupted until the draft is approved.

> `branched_from_version_id` is stored at insert time and validated at approval time. Sibling `DRAFT` and `DRAFT_CANCEL` versions are invalidated on approval.

### 3.1 Edit Submission (customer completes all 5 wizards and confirms to add to cart)

```
1. INSERT agreement_version
       status = DRAFT, version = N+1
       branched_from_version_id = current ACTIVE version id

2. INSERT agreement_contact
       Copied forward from current version, or modified if customer changed contacts.

3. INSERT message_recipient — behaviour per case:

       Unchanged recipient      → status = DRAFT

       Newly added recipient    → status = DRAFT

       Replaced recipient (old) → status = CANCELLED
                                  cancelled_at = now
                                  replaced_by_recipient_id = new recipient id
                                  Note: 1-to-1 replace only. A split (one → two) is modelled as a remove + two adds.

       Replaced recipient (new) → status = DRAFT

       Removed recipient        → status = CANCELLED
                                  cancelled_at = now
                                  replaced_by_recipient_id = NULL

4. INSERT product_part_assignment
       For DRAFT recipients only.
       CANCELLED recipients carry no assignments — their assignments are readable from the prior version.

5. INSERT payment_type_assignment          [CAMT054_CREDIT only]
       One row per payment type for each CAMT054_CREDIT assignment on DRAFT recipients.
       Copied forward from current version, or modified if customer changed payment types.

6. For each payment_type_assignment, insert accounts and/or aliases whichever applicable:

       INSERT assignment_account           [accounts path]
           One or more accounts per payment_type_assignment. DRAFT recipients only.
           Copied forward or modified.

       INSERT alias_assignment             [aliases path]
           One or more aliases per payment_type_assignment. DRAFT recipients only.
           Copied forward or modified.

   For all other product parts (non-CAMT054_CREDIT):

       INSERT assignment_account as before — for DRAFT recipients only.

7. INSERT cart_item
       expires_at = now + 30 days
       Note: A DRAFT and a DRAFT_CANCEL may coexist in the cart
       for the same agreement simultaneously.
```

### 3.2 On Approval (customer approves from cart)

```
PRE-CHECK:
       branched_from_version_id must equal the current ACTIVE version id.
       If not → reject with 409 CONFLICT: "Draft is stale. The agreement has changed since you started editing. Please start a new edit."

8.  UPDATE agreement_version (N)
        status = SUPERSEDED, superseded_at = now

9.  UPDATE agreement_version (N+1)
        status = ACTIVE, activated_at = now

10. UPDATE message_recipient
        All DRAFT recipients on version N+1 → ACTIVE, activated_at = now

11. UPDATE cart_item
        approved_at = now

12. UPDATE agreement_version
        status = INVALIDATED, invalidated_at = now
        WHERE agreement_id = same
          AND status IN ('DRAFT', 'DRAFT_CANCEL')
          AND id != N+1
    UPDATE cart_item
        expired_at = now
        for all cart items belonging to invalidated versions
```

---

## 4. Cancel — Initiating Agreement Cancellation

Triggered when a customer opens an `ACTIVE` agreement and clicks Cancel. The system creates a new `DRAFT_CANCEL` version (N+1). The `ACTIVE` version continues running uninterrupted until the cancellation is approved.

### 4.1 Cancellation Initiation

```
1. INSERT agreement_version
       status = DRAFT_CANCEL, version = N+1
       branched_from_version_id = current ACTIVE version id

2. INSERT agreement_contact
       Copied forward from current version.

3. INSERT message_recipient
       All recipients copied forward, status = DRAFT

4. INSERT product_part_assignment
       Copied forward from current version.

5. INSERT payment_type_assignment          [CAMT054_CREDIT only]
       Copied forward from current version for all CAMT054_CREDIT assignments.

6. INSERT assignment_account / alias_assignment
       Copied forward from current version for all payment types and product parts.

7. INSERT cart_item
       expires_at = now + 30 days
       Note: A DRAFT_CANCEL and a DRAFT may coexist in the cart
       for the same agreement simultaneously.
```

### 4.2 On Approval (customer approves from cart)

```
PRE-CHECK:
       branched_from_version_id must equal the current ACTIVE version id.
       If not → reject with 409 CONFLICT:
       "This cancellation request is stale. The agreement has changed since you initiated the cancellation. Please start again."

8.  UPDATE agreement_version (N)
        status = SUPERSEDED, superseded_at = now

9.  UPDATE agreement_version (N+1)
        status = CANCELLED, cancelled_at = now

10. UPDATE message_recipient
        All DRAFT recipients on version N+1 → CANCELLED
        cancelled_at = now

11. UPDATE cart_item
        approved_at = now

12. UPDATE agreement_version
        status = INVALIDATED, invalidated_at = now
        WHERE agreement_id = same
          AND status IN ('DRAFT', 'DRAFT_CANCEL')
          AND id != N+1
    UPDATE cart_item
        expired_at = now
        for all cart items belonging to invalidated versions
```

---

## 5. Cart Expiry — No Approval Within 30 Days

Triggered by a nightly scheduled job. Finds all cart items that have passed their `expires_at` timestamp without being approved.

### 5.1 Nightly Expiry Job

```
1. SELECT cart_item
       WHERE expires_at < now
         AND expired_at IS NULL
         AND approved_at IS NULL

2. UPDATE cart_item
       expired_at = now

3. UPDATE agreement_version
       status = EXPIRED, expired_at = now
       WHERE id = cart_item.agreement_version_id
         AND status IN ('DRAFT', 'DRAFT_CANCEL')
```

### 5.2 After Expiry

```
— The previously ACTIVE version (N) stays ACTIVE — completely unaffected.
— The customer may start a fresh edit or cancel after expiry.
— A fresh edit creates a new DRAFT branched from the still-ACTIVE version.
```

---
