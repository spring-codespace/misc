# Is it possible to combine agreement and agreement_version table?

Short answer — technically yes, but I would strongly advise against it. Here's why.

---

## What combining them would look like

You'd end up with a single `agreement` table that has all version-specific fields on it, and you'd either:

**Option A — One row per version (duplicate name/corporate_id on every row)**
```
id | agreement_id | agreement_name | corporate_id | version | status | pricing_type | ...
```
- `agreement_name` and `corporate_id` repeated on every version row
- "Frozen after creation" becomes a convention you enforce in code, not a structural guarantee
- Nothing in the schema prevents someone updating `agreement_name` on version 3

**Option B — Single row, overwrite on each version**
```
id | agreement_name | corporate_id | version | status | pricing_type | ...
```
- You lose all version history — no way to query "what did version 2 look like"
- Breaks the entire audit trail and historical query capability
- Immediately rules out Scenario 9 from your test document (point-in-time queries)

---

## Why the split exists and must stay

The two tables encode two fundamentally different things:

| `agreement` | `agreement_version` |
|---|---|
| What never changes | What changes over time |
| Identity anchor | Versioned state |
| One row forever | One row per lifecycle event |
| `agreement_name`, `corporate_id` | `status`, `pricing_type`, `debit_account_id`, contacts, recipients |

The separation is doing real structural work. `agreement.id` is the stable foreign key that every child table (`message_recipient`, `cart_item` via `agreement_version`) ultimately traces back to. If you collapse the tables, you lose that stable anchor and every query that asks "give me all versions of this agreement" becomes ambiguous.

---

## The one thing worth reconsidering

If your concern is **join overhead** — having to always join `agreement` to `agreement_version` to get the name — that is a valid operational concern but the solution is not to merge the tables. Instead:

- Denormalise `agreement_name` and `corporate_id` onto `agreement_version` as read-only copied fields
- Keep the two tables as-is structurally
- Your reads get the name without an extra join, but the source of truth stays on `agreement`

But only do this if you find the join is genuinely painful in practice. For most queries it won't be.

---

## Recommendation

Keep the tables separate. The split is not accidental — it directly supports versioning, audit history, point-in-time queries, and the frozen `agreement_name` guarantee. Merging them would require compensating logic elsewhere that the schema currently enforces for free.

# Why do we need the branched_from_version_id column in agreement_version table?

Good question. Let me explain exactly what work this column does.

---

## What it solves — the race condition

Cast your mind back to **Gap 2** from your test scenarios. The scenario was:

```
Morning:   Customer creates DRAFT (edit) — version 6
Afternoon: Someone else creates DRAFT_CANCEL — version 7
Next day:  Customer approves the DRAFT_CANCEL first → agreement CANCELLED
           Now version 6 (DRAFT edit) is still sitting in the cart
           Customer tries to approve version 6
           → Should this be allowed?
```

Without `branched_from_version_id`, the approval endpoint has no way to answer the question **"was this draft created against the version that is still currently active?"**

---

## How it works

When a DRAFT is created during an edit, the system records which ACTIVE version it was branched from:

```
Version 1: ACTIVE
  ↓
Customer starts edit → Version 2: DRAFT
  branched_from_version_id = Version 1's id
```

At approval time, the pre-check is:

```
Is version 2's branched_from_version_id 
still the current ACTIVE version?

YES → safe to approve, nothing changed underneath
NO  → reject with 409 — the agreement changed since editing started
```

---

## A concrete example from your scenarios

```
Version 4: ACTIVE  (3 recipients)

Day 300 morning:
  User A starts edit → Version 5 DRAFT
  branched_from_version_id = Version 4

Day 300 afternoon:
  User B initiates cancel → Version 6 DRAFT_CANCEL
  branched_from_version_id = Version 4

Day 301:
  User B approves cancel first
  Version 4 → SUPERSEDED
  Version 6 → CANCELLED

  User A now tries to approve Version 5 (DRAFT edit)
  PRE-CHECK:
    Version 5's branched_from_version_id = Version 4
    Current ACTIVE version = NULL (agreement is cancelled)
    → MISMATCH → reject with 409
```

Without this column, the approval endpoint would have to guess — or worse, allow the approval through and produce an inconsistent state.

---

## Could you derive this without the column?

Theoretically yes — you could infer it by looking at `version_number - 1` and checking its status. But this breaks in several edge cases:

- What if the previous version was itself a DRAFT that expired? The version numbers would not be contiguous in terms of ACTIVE history
- What if a DRAFT_CANCEL was created at version N+1 and an edit at version N+2 — the N+2 edit's "parent" is still the ACTIVE version N, not the DRAFT_CANCEL at N+1
- Version number arithmetic is fragile. An explicit FK is unambiguous.

---

## Summary

`branched_from_version_id` exists for one specific purpose — **to make the stale draft pre-check at approval time reliable and explicit**. Without it you either accept the race condition risk, or you write fragile version number arithmetic to compensate.

That said — this is a **Gap 2 column**, which is still pending your business confirmation. If the business decides they do not need the concurrent DRAFT + DRAFT_CANCEL protection at all, this column goes away along with the pre-check logic. That is exactly why it is marked in the schema with the Gap 2 revert checklist.

# Having 2 DRAFT or DRAFT_CANCEL is not possible, do I still need this column?

Great clarification. If only one DRAFT and one DRAFT_CANCEL can exist at a time per agreement, let me re-examine whether the column is still needed.

---

## What changes with this constraint

If the system enforces:
```
Only one DRAFT allowed per agreement at any time
Only one DRAFT_CANCEL allowed per agreement at any time
```

Then the race condition scenario from Gap 2 **cannot happen** the way it was described, because:

- Before creating a new DRAFT, the system checks no DRAFT exists → blocks if one does
- Before creating a DRAFT_CANCEL, the system checks no DRAFT_CANCEL exists → blocks if one does
- A DRAFT and a DRAFT_CANCEL can still coexist (you confirmed this earlier)

---

## Does the column still protect anything?

Let me re-examine the one remaining concurrent scenario:

```
Version 4: ACTIVE

Day 300 morning:
  User A creates DRAFT edit → Version 5
  (only one DRAFT allowed — this is fine)

Day 300 afternoon:
  User B creates DRAFT_CANCEL → Version 6
  (only one DRAFT_CANCEL allowed — this is fine)
  Both coexist in cart — allowed

Day 301:
  User B approves DRAFT_CANCEL first
  Version 4 → SUPERSEDED
  Version 6 → CANCELLED
  Version 5 (DRAFT edit) → INVALIDATED

  User A tries to approve Version 5
  → Already INVALIDATED — blocked before even reaching the pre-check
```

The INVALIDATED status itself already blocks the approval. `branched_from_version_id` adds nothing here because the system already knows Version 5 is invalid.

---

## The only remaining case — cart expiry timing

The one edge case worth checking:

```
Version 4: ACTIVE

Day 1:   User A creates DRAFT edit → Version 5
Day 31:  Version 5 cart item expires → Version 5 → EXPIRED
Day 32:  User A somehow tries to approve Version 5

Without branched_from_version_id:
  Version 5 status = EXPIRED → blocked immediately
  → branched_from_version_id not needed
```

Again, the `EXPIRED` status blocks it before the pre-check is ever reached.

---

## Verdict — No, you do not need this column

With the constraint that only one DRAFT and one DRAFT_CANCEL can exist per agreement at any time, every scenario that `branched_from_version_id` was designed to protect against is already handled by:

| Protection | Handled by |
|---|---|
| Approving a stale DRAFT after DRAFT_CANCEL approved | `INVALIDATED` status |
| Approving an expired DRAFT | `EXPIRED` status |
| Two concurrent DRAFTs | Business rule — blocked at creation time |
| Two concurrent DRAFT_CANCELs | Business rule — blocked at creation time |

`branched_from_version_id` is therefore redundant. You can remove it along with all Gap 2 schema and workflow entries entirely — not just defer them, but close them out as resolved by the "one DRAFT at a time" constraint.

---

## What this means for the schema and workflow

**Remove from `agreement_version`:**
- `branched_from_version_id`
- `invalidated_at`
- `expired_at`

**Remove from `agreement_version_status` enum:**
- `INVALIDATED`

**Keep `EXPIRED`** — the nightly expiry job still needs it to mark versions whose cart item lapsed. But the Gap 2 note can be dropped since it is now a confirmed clean design decision, not a pending one.

**Remove from workflows:**
- The PRE-CHECK block in Edit approval
- The PRE-CHECK block in Cancel approval
- Step 12 (invalidate siblings) in both Edit and Cancel approval

**Add one new business rule** to the workflow — at the point of creating a DRAFT or DRAFT_CANCEL:

```
PRE-CHECK on Edit initiation:
  Agreement must have exactly one ACTIVE version.
  Agreement must have no existing DRAFT in the cart.
  If DRAFT exists → reject: "You already have a pending edit
  in your cart. Please approve or discard it before starting
  a new edit."

PRE-CHECK on Cancel initiation:
  Agreement must have exactly one ACTIVE version.
  Agreement must have no existing DRAFT_CANCEL in the cart.
  If DRAFT_CANCEL exists → reject: "You already have a pending
  cancellation in your cart. Please approve or discard it
  before initiating a new cancellation."
```

This is cleaner than Gap 2 was — you prevent the problem at creation time rather than detecting it at approval time.

Would you like me to produce updated clean versions of both the DBML and the workflow markdown reflecting all of this?

# why do I need replaced_by_recipient_id in message_recipient table?

Same kind of question as before — let me examine exactly what work this column does and whether you actually need it.

---

## What it was designed for

It was introduced to support the **explicit replace** scenario — when a customer swaps `msg-rec-1` for `msg-rec-2` on the same product part and accounts. The column creates a pointer from the old cancelled recipient to the new one:

```
msg-rec-1  status=CANCELLED  replaced_by_recipient_id → msg-rec-2
msg-rec-2  status=ACTIVE
```

The idea was to distinguish between:
- **Replaced** — cancelled because a new recipient took over
- **Removed** — cancelled with no successor

---

## Do you actually need that distinction?

Ask yourself these questions:

**1. Does any screen in the UI show "this recipient was replaced by that recipient"?**
If the agreement view just shows `msg-rec-1` as CANCELLED and `msg-rec-2` as ACTIVE side by side, the user can infer the replacement visually without needing an explicit pointer.

**2. Does any business process depend on the pointer?**
- Report generation only looks at ACTIVE recipients — does not care about the lineage
- Audit history shows all recipients per version with their statuses — the replace story is visible from the data without the pointer
- No downstream system was identified that consumes this relationship

**3. Is the replace action itself distinguishable from remove + add without the column?**
In your current UI model, replace is an **explicit action** the customer takes. But at the data level, after the operation completes, the result is identical to a remove + add:
```
Remove:   msg-rec-1 → CANCELLED, replaced_by_recipient_id = NULL
Replace:  msg-rec-1 → CANCELLED, replaced_by_recipient_id = msg-rec-2
Add:      msg-rec-2 → DRAFT
```
Without the pointer, remove and replace look identical in the database. The only difference is the pointer.

---

## The real question

**Do you need to know, after the fact, whether a cancellation was due to a replace or a remove?**

If yes — keep the column. It is the only way to answer that question from the data alone.

If no — drop it. The CANCELLED status is sufficient. The UI already knows the intent at interaction time, and nothing downstream needs the lineage.

---

## My assessment

Looking at everything designed so far — the screens, the workflows, the report generation engine, the API contract — **nothing consumes this pointer**. It was added as a nice-to-have audit trail detail, not because any feature requires it.

The replace story is already told by the version history:

```
Version 2 (SUPERSEDED):  msg-rec-1 → ACTIVE
Version 3 (ACTIVE):      msg-rec-1 → CANCELLED
                         msg-rec-2 → ACTIVE
```

Anyone reading the version history can see that msg-rec-1 was active in V2 and cancelled in V3, and msg-rec-2 appeared in V3. The pointer adds precision but not information that is otherwise unavailable.

---

## Verdict

**You do not need `replaced_by_recipient_id`** unless you have a specific requirement to:
- Show a visual "replaced by" link in the UI
- Run a query that traces replacement chains
- Distinguish replace from remove in a report or audit export

If none of those are requirements, drop the column. `status = CANCELLED` is sufficient, and the version history provides the full audit trail without it.

---

## Knock-on effect

Dropping `replaced_by_recipient_id` also simplifies the edit workflow. The recipient cases reduce from five to four:

```
Before:                          After:
Unchanged    → DRAFT             Unchanged    → DRAFT
Newly added  → DRAFT             Newly added  → DRAFT
Replaced old → CANCELLED         Removed      → CANCELLED
Replaced new → DRAFT             (replace is just remove + add)
Removed      → CANCELLED
```

And the UI's "explicit replace" action becomes purely a frontend concern — it presents a nicer interaction to the user but produces the same two database operations as remove + add. No special handling needed in the backend or stored procedures.

Would you like me to update the DBML and workflow document to reflect this simplification?

# Coming back to the question about "can we merge agreement and agreement_version table". Since I can only have one active and one cancel and one draft at a time, and if I introduce an addition column in agreement_version table say flag marking it to active always between edits. Will it work?

Interesting idea. Let me think through it carefully.

---

## What you are proposing

Add a flag column — say `is_current` — to `agreement_version`:

```dbml
is_current  boolean  [not null, default: false,
            note: "True on the version that represents the current state.
                   Exactly one row per agreement has is_current = true at any time."]
```

Then merge `agreement` into `agreement_version`, so you have one table:

```
agreement_version
  id
  agreement_id        ← still needed as the stable grouping key
  agreement_name      ← frozen, repeated on every version row
  corporate_id        ← frozen, repeated on every version row
  version_number
  status
  is_current          ← your new flag
  pricing_type
  ...
```

The idea is that instead of joining `agreement` to get the current state, you just query:

```sql
SELECT * FROM agreement_version
WHERE agreement_id = ?
AND is_current = true
```

---

## Does it work technically?

Yes, it works. But let me show you what `is_current` would look like across the full lifecycle to check if it actually simplifies anything:

```
Version 1: DRAFT         is_current = true   ← only version, so it's current
Version 1: ACTIVE        is_current = true   ← approved, still current

--- customer starts edit ---
Version 1: ACTIVE        is_current = true   ← still the live version
Version 2: DRAFT         is_current = false  ← pending, not current yet

--- customer approves edit ---
Version 1: SUPERSEDED    is_current = false  ← no longer current
Version 2: ACTIVE        is_current = true   ← now current

--- customer initiates cancel ---
Version 2: ACTIVE        is_current = true   ← still live
Version 3: DRAFT_CANCEL  is_current = false  ← pending

--- customer approves cancel ---
Version 2: SUPERSEDED    is_current = false
Version 3: CANCELLED     is_current = true   ← last known state, current

--- cart expiry ---
Version 2: ACTIVE        is_current = true   ← unaffected
Version 3: EXPIRED       is_current = false  ← expired, never became current
```

---

## The problems this creates

**Problem 1 — `is_current` duplicates what `status` already tells you**

Look at the lifecycle above. `is_current = true` always points to the same version that you could already find by querying:

```sql
-- Without is_current flag:
SELECT * FROM agreement_version
WHERE agreement_id = ?
AND status = 'ACTIVE'

-- is identical in result to:
SELECT * FROM agreement_version
WHERE agreement_id = ?
AND is_current = true
```

The flag adds a second source of truth for the same fact. You now have to keep `status` and `is_current` in sync on every state transition. If they ever diverge due to a bug, you have two columns giving contradictory answers about which version is current.

**Problem 2 — `agreement_name` and `corporate_id` repeat on every row**

Merging the tables means these frozen fields appear on every version row:

```
V1: agreement_id=X, agreement_name="ABC Corp", corporate_id="CUST_001", status=SUPERSEDED
V2: agreement_id=X, agreement_name="ABC Corp", corporate_id="CUST_001", status=SUPERSEDED
V3: agreement_id=X, agreement_name="ABC Corp", corporate_id="CUST_001", status=ACTIVE
```

This is denormalisation. The schema no longer structurally enforces that `agreement_name` is frozen — you enforce it in application code or stored procedures instead. A bug could update the name on one version and not others, giving you inconsistent history.

**Problem 3 — `agreement_id` still needs to exist**

You still need a stable grouping key to answer "give me all versions of this agreement." That key is `agreement_id`. But now it is a self-referencing concept — a column on `agreement_version` that groups rows of `agreement_version`. You have not eliminated the two-entity concept, you have just put both entities in one table, which is less clear not more.

---

## What you actually gain

If you merged the tables, the only concrete benefit is:

- One fewer JOIN when fetching an agreement with its current version details

That is a minor query convenience, not a structural improvement.

---

## My verdict

The `is_current` flag idea is clever but it does not justify merging the tables because:

1. It duplicates information already in `status`
2. It creates a two-column sync problem on every state transition
3. `agreement_name` and `corporate_id` become unenforceable as frozen fields
4. `agreement_id` still exists as a concept — you have not actually eliminated the two-entity model, just obscured it

The separation of `agreement` and `agreement_version` is not about query convenience — it is about **what is immutable vs what changes over time**. That distinction is real in your domain and the schema should reflect it.

---

## If the join genuinely bothers you

The cleanest solution is a database view, not a table merge:

```sql
CREATE VIEW current_agreement AS
SELECT
    a.id              AS agreement_id,
    a.agreement_name,
    a.corporate_id,
    av.*
FROM agreement a
JOIN agreement_version av
    ON av.agreement_id = a.id
    AND av.status = 'ACTIVE'
```

You query the view as if it were one table, but the underlying model stays clean and normalised. No sync problem, no repeated frozen fields, no structural ambiguity.

Keep the two tables. Use a view if you want the convenience of querying them as one.

# I'm thinking of renaming message_recipient table to agreement_scope table, then add product_part column from product_part_assignment table to agreement_scope table, and remove product_part_assignment table.

Interesting idea. Let me think through the full implications carefully.

---

## What you are proposing

Current structure:
```
message_recipient (1)
  └── product_part_assignment (many — one per product part)
        └── account_assignment (many — one per account)
```

Proposed structure:
```
agreement_scope (1 — one per recipient+product part combination)
  └── account_assignment (many — one per account)
```

So `agreement_scope` becomes a flattened combination of `message_recipient` and `product_part_assignment` — one row per recipient-productpart pair rather than one row per recipient.

---

## Let me check this against your data

From Scenario 1 in your test document:

```
Current model:
  message_recipient: REC_DAILY
    product_part_assignment: CAMT052_BALANCES_TX → [ACC123, ACC456]
  message_recipient: REC_MONTHLY
    product_part_assignment: CAMT053_STANDARD → [ACC123]

Proposed model:
  agreement_scope: REC_DAILY  + CAMT052_BALANCES_TX → [ACC123, ACC456]
  agreement_scope: REC_MONTHLY + CAMT053_STANDARD   → [ACC123]
```

From Scenario 3 — one recipient with multiple product parts:

```
Current model:
  message_recipient: REC_DAILY
    product_part_assignment: CAMT052_BALANCES_TX  → [ACC123, ACC456]
    product_part_assignment: CAMT053_STANDARD     → [ACC002]
    product_part_assignment: CAMT053_EXTENDED     → [ACC004, ACC005]

Proposed model:
  agreement_scope: REC_DAILY + CAMT052_BALANCES_TX → [ACC123, ACC456]
  agreement_scope: REC_DAILY + CAMT053_STANDARD    → [ACC002]
  agreement_scope: REC_DAILY + CAMT053_EXTENDED    → [ACC004, ACC005]
```

This works structurally — the data fits.

---

## What you gain

**Simpler hierarchy** — three levels instead of four:
```
agreement_version
  └── agreement_scope        (recipient + product part in one row)
        └── account_assignment
```

**Fewer joins** — queries that need recipient + product part + accounts drop one join level.

**Cleaner report generation** — the report job query becomes more straightforward. Currently it needs:
```
message_recipient → product_part_assignment → account_assignment
```
With the proposed model:
```
agreement_scope → account_assignment
```

---

## The problems I see

**Problem 1 — Recipient-level status becomes ambiguous**

This is the most significant issue. Currently `message_recipient.status` tracks the state of a recipient as a whole. One recipient, one status.

In the proposed model, if `REC_DAILY` has three product parts, you have three `agreement_scope` rows for `REC_DAILY`. What happens to status?

```
agreement_scope: REC_DAILY + CAMT052_BALANCES_TX  status = ?
agreement_scope: REC_DAILY + CAMT053_STANDARD     status = ?
agreement_scope: REC_DAILY + CAMT053_EXTENDED     status = ?
```

If a recipient is cancelled, do all three rows get cancelled? Yes — but now you have to update N rows instead of one, and you have to ensure they all stay in sync. If one row is ACTIVE and another is CANCELLED for the same recipient, what does that mean?

**Problem 2 — Replace operation becomes more complex**

Currently replacing `REC_DAILY` with `REC_DAILY_V2` means:
```
1 row: REC_DAILY   → CANCELLED
1 row: REC_DAILY_V2 → DRAFT
```

With the proposed model, if REC_DAILY had three product parts:
```
3 rows: REC_DAILY + CAMT052  → CANCELLED
3 rows: REC_DAILY + CAMT053S → CANCELLED
3 rows: REC_DAILY + CAMT053E → CANCELLED
3 rows: REC_DAILY_V2 + CAMT052  → DRAFT
3 rows: REC_DAILY_V2 + CAMT053S → DRAFT
3 rows: REC_DAILY_V2 + CAMT053E → DRAFT
```

The stored procedure has to identify all rows belonging to the old recipient and cancel them all, then insert matching rows for the new recipient. More work, more risk of partial failure.

**Problem 3 — Recipient identity is scattered**

Currently you can answer "who are the recipients on this agreement version?" with a simple query on `message_recipient`. One row per recipient, clean list.

With `agreement_scope`, answering the same question requires:
```sql
SELECT DISTINCT recipient_id
FROM agreement_scope
WHERE agreement_version_id = ?
AND status = 'ACTIVE'
```

Not complex, but the concept of a recipient as a first-class entity is lost. The recipient exists implicitly across multiple rows rather than explicitly as one row.

**Problem 4 — Screen 3 of the wizard maps to `message_recipient`, not to the combined entity**

The UI in Screen 3 thinks in terms of recipients first, then product parts per recipient. The current model mirrors that mental model exactly:
```
recipient → product parts → accounts
```
The proposed model flattens this to:
```
recipient+productpart → accounts
```
Which means the UI and the data model no longer share the same conceptual hierarchy.

---

## A middle ground worth considering

Keep `message_recipient` as a first-class entity for status tracking and recipient identity, but rename `product_part_assignment` to something more meaningful — `recipient_scope` or `recipient_product` — to make the schema more readable without losing the structural benefits:

```
agreement_version
  └── message_recipient       (identity, status, replace tracking)
        └── recipient_scope   (renamed from product_part_assignment)
              └── account_assignment
```

This gives you better naming without the structural problems the full merge introduces.

---

## Verdict

The idea is understandable — the schema does look simpler at first glance. But the merge creates three real problems:

| Problem | Impact |
|---|---|
| Recipient status scattered across multiple rows | Medium — sync complexity in stored procedures |
| Replace operation touches N rows instead of 1 | Medium — more complex, more fragile |
| Recipient identity lost as a first-class concept | Low-Medium — harder to query, UI model diverges |

The current two-table structure is not accidental overhead — `message_recipient` and `product_part_assignment` represent two genuinely different things: **who receives** vs **what they receive**. Keeping them separate preserves that distinction cleanly.

My recommendation is to keep the two tables but consider renaming `product_part_assignment` to `recipient_scope` if the naming bothers you. Would that address what you were trying to achieve?

# For account_assignment - I was thinking of having accounts table where we hold account numbers and the use account_id from this table to use it in account_assignment table. What do you say?

Good thinking. Let me evaluate it properly.

---

## What you are proposing

Instead of storing account details directly on `account_assignment`:

```
Current:
account_assignment
  account_id      varchar   (raw value from external API)
  account_iban    varchar
  currency        varchar(3)
```

Introduce a separate `account` table:

```
Proposed:
account
  id              uuid      (internal PK)
  account_id      varchar   (from external API)
  account_iban    varchar
  currency        varchar(3)

account_assignment
  account_id      uuid      (FK → account.id)
```

---

## The core question

**Who owns the account data?**

You told me earlier that accounts are **fetched from an external API** — they are not created or managed in this system. This is the most important factor in evaluating your proposal.

---

## The problem with introducing an `account` table

**You would be duplicating a dataset you do not own.**

Every time a customer uses an account, you would need to:

```
1. Call external accounts API → get account details
2. Check: does this account already exist in our account table?
3a. If yes → use existing id
3b. If no  → INSERT into account table first, then use new id
```

This creates several problems:

**Sync problem** — What happens when account details change in the external system? The IBAN, currency, or account number could change. Your local `account` table becomes stale. You now need a sync mechanism you did not need before.

**Ownership ambiguity** — The external API is the source of truth for accounts. Your system is the source of truth for agreements. Introducing an `account` table blurs that boundary and makes your system partially responsible for data it does not own.

**Unnecessary complexity** — You gain a normalisation benefit only if the same account appears many times across many agreements and you want to avoid repeating IBAN and currency. But since accounts are fetched fresh from the API on each wizard session, the duplication is not really a problem — it is just a snapshot at the time of agreement creation.

---

## When an `account` table would make sense

If any of these were true, I would recommend it:

| Condition | Verdict |
|---|---|
| Accounts are created and managed in this system | ✅ Use a table |
| Account details change frequently and need to stay current | ✅ Use a table with sync |
| The same account appears hundreds of times and storage is a concern | ✅ Normalise |
| Accounts need their own lifecycle (active/inactive/blocked) in this system | ✅ Use a table |
| Accounts are just reference data fetched externally and snapshotted | ❌ No table needed |

Your situation is the last case.

---

## What the current design actually does well

`account_assignment` stores a **snapshot** of the account details at the time the agreement version was created. This is intentional and correct for two reasons:

**Audit correctness** — If the external API changes an account's IBAN six months from now, your historical agreement versions still show what was agreed at the time. A foreign key to a shared `account` table would silently reflect the updated IBAN on all historical records.

**Report generation accuracy** — `report_job` denormalises `account_ids` for the same reason. The report that ran on Day 1 should reflect the accounts that were configured on Day 1, not what the account table says today.

---

## Verdict

Keep the current design. Store account details directly on `account_assignment` as a snapshot. Do not introduce an `account` table for externally owned reference data.

The only change I would suggest is making sure the column naming is consistent and clear:

```dbml
Table account_assignment {
  id                         uuid       [pk]
  product_part_assignment_id uuid       [null, ref: > product_part_assignment.id]
  payment_type_assignment_id uuid       [null, ref: > payment_type_assignment.id]
  account_id                 varchar    [not null, note: "External account identifier from accounts API"]
  account_iban               varchar    [not null]
  currency                   varchar(3) [not null]
  created_at                 timestamp  [not null]

  note: "Snapshot of account details at agreement version creation time.
         Intentionally denormalised — changes in the external accounts API
         do not affect historical agreement versions."
}
```

The note makes the intent explicit so future developers understand it is a deliberate snapshot, not an oversight.

# Sync problem — What happens when account details change in the external system? The IBAN, currency, or account number could change. Your local `account` table becomes stale. You now need a sync mechanism you did not need before. What happens to account_assignment table in the above case?

Great challenge. You are right to push back. Let me think through this honestly.

---

## The honest answer

If an account's IBAN or currency changes in the external system, the `account_assignment` table has **exactly the same staleness problem** as a dedicated `account` table would.

```
Agreement Version 3 (ACTIVE):
  account_assignment: account_id=ACC123, iban=GB001, currency=GBP

External API updates ACC123:
  iban=GB002, currency=EUR

account_assignment still shows:
  account_id=ACC123, iban=GB001, currency=GBP  ← stale
```

So my argument against an `account` table on sync grounds was not entirely fair — the staleness risk exists in both designs.

---

## But the staleness means different things in each design

This is the key distinction.

**In `account_assignment` (current design — snapshot):**
```
The stale data is intentional.
It represents what was agreed at the time the agreement version was created.
It is a historical record, not a live reference.
The report generated on Day 1 correctly used GB001/GBP
because that was the account at agreement creation time.
```

**In a shared `account` table (proposed design — live reference):**
```
The stale data is a bug.
Every agreement version that references ACC123 via FK
now silently shows GB002/EUR — including historical versions.
The report that ran on Day 1 now appears to have used GB002/EUR
even though it did not.
You have rewritten history.
```

---

## The fundamental difference

| | Snapshot in `account_assignment` | Shared `account` table |
|---|---|---|
| Data changes externally | Historical records unaffected | All historical records silently updated |
| Audit trail | ✅ Accurate — shows what was configured then | ❌ Broken — shows what is configured now |
| Report replay | ✅ Correct — replays with original account details | ❌ Wrong — replays with current account details |
| Is staleness a problem? | No — it is the point | Yes — it corrupts history |

---

## So when does `account_assignment` actually need updating?

Only in one case — when a customer explicitly edits their agreement and selects updated account details through the wizard. At that point:

```
New agreement_version (N+1) created
New account_assignment rows inserted with fresh data from external API
Old account_assignment rows on version N remain unchanged — correct historical record
```

The external API is consulted **only at agreement edit time**, not continuously. The system never needs to track changes between edits because:

- The agreement is a contract — it reflects what was agreed, not what is current
- If the bank updates an account, the customer initiates a new edit to reflect that change
- That edit creates a new version with fresh account data

---

## Verdict

The staleness argument still holds, just for a different and more important reason than I originally stated.

The real reason to **not** use a shared `account` table is not the sync complexity — it is that a shared table would **corrupt your audit trail and report history** by making historical agreement versions silently reflect current account data rather than the data that was in place when the agreement was created.

The snapshot approach in `account_assignment` is not a workaround — it is the correct design for a versioned agreement system. The data belongs to the version, not to a shared reference table.


