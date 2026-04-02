# Java code
```
com.camt.reporting
├── CamtReportingApplication.java
│
├── common/exception
│   ├── ResourceNotFoundException
│   ├── BusinessException
│   └── GlobalExceptionHandler
│
├── reference                          ← lookup tables, no business logic
│   ├── entity  (AgreementVersionStatus, AgreementScopeStatus, ProductPart, PaymentType)
│   └── repository  (one per entity)
│
├── agreement                          ← Phase A core
│   ├── entity  (Agreement, AgreementContact, AgreementVersion, CartItem)
│   ├── repository
│   ├── dto     (AgreementDto, AgreementVersionDto)
│   ├── mapper  (AgreementMapper)
│   └── service (AgreementService, AgreementController)
│
├── scope                              ← Phase A assignments
│   ├── entity  (AgreementScope, PaymentTypeAssignment, AccountAssignment, AliasAssignment)
│   ├── repository
│   ├── dto     (ScopeDto)
│   ├── mapper  (ScopeMapper)
│   └── service (AgreementScopeService, AgreementScopeController)
│
├── cart                               ← Cart lifecycle
│   ├── repository  (CartItemRepository)
│   ├── dto         (CartItemDto)
│   ├── mapper      (CartItemMapper)
│   └── service     (CartItemService, CartItemController)
│
├── report                             ← Phase B
│   ├── entity  (ReportConfig, ReportAgreementScope)
│   ├── repository
│   ├── dto     (ReportDto)
│   ├── mapper  (ReportMapper)
│   └── service (ReportConfigService, ReportConfigController)
│
└── scheduler
    └── CartExpiryScheduler            ← @Scheduled cron 00:05 nightly
```

Those will be input parameters to the stored procedure — that way you can call it with different defaults without changing the procedure itself. Let me confirm the allowed values before writing:Responses didn't come through again — I'll write the procedure with sensible defaults baked in as parameter defaults, all of which you can override at call time. Based on your schema and the earlier discussion I'll use `1_HOUR`, `IBAN`, and `IsActive = 0`. Correct me if any of those are wrong.Here's a walkthrough of the key design decisions:

**All hardcoded defaults are parameters.** The procedure signature is:
```sql
EXEC usp_ProvisionReportConfigs;                                     -- all defaults
EXEC usp_ProvisionReportConfigs @IsActive = 1;                       -- activate immediately
EXEC usp_ProvisionReportConfigs @ReportFrequency = '4_HOURS', @AccountFormat = 'BBAN';
```
This means you never need to alter the procedure to change defaults — just change the call.

**Idempotency via `NOT EXISTS` on `ReportAgreementScope`.** Rather than checking `ReportConfig` directly, the skip check uses `ReportAgreementScope.AgreementScopeId`. This is the correct anchor — it's possible a `ReportConfig` exists but the link row doesn't (or vice versa), so checking the join table keeps both tables consistent.

**`OUTPUT` clause bridges Step 2 → Step 3.** When inserting into `ReportConfig`, the `OUTPUT` clause captures the new auto-generated `Id` alongside the source `AgreementScopeId` and `AgreementId` into a temp table. This avoids a second lookup query in Step 3 and keeps the two inserts atomically correct.

**`SET XACT_ABORT ON`.** If anything fails mid-run (e.g. a FK violation on `ProductPart`), the entire transaction rolls back automatically. No partial provisioning.

**Both `AgreementVersion` and `AgreementScope` must be `ACTIVE`.** The procedure joins both status tables explicitly, so a scope on a superseded version won't accidentally get provisioned.

**SQL Server Agent job template included** at the bottom — both the GUI steps and the T-SQL `sp_add_job` script are included so you can automate the scheduling however you prefer.

The stored procedure already matches all three of those answers exactly:

- `@ReportFrequency = '1_HOUR'` ✓
- `@AccountFormat = 'IBAN'` ✓
- `@IsActive = 0` ✓

No changes needed — the file already generated is correct as-is. You're good to deploy it.

One thing worth doing before the first production run is a dry-run check query to see exactly what the procedure *would* insert, without committing anything:

```sql
-- Preview scopes that would be provisioned
SELECT
    sc.Id                   AS AgreementScopeId,
    sc.MessageRecipientId,
    sc.ProductPart          AS ReportTypeCode,
    av.AgreementId,
    av.Id                   AS AgreementVersionId
FROM AgreementScope         sc
JOIN AgreementVersion       av  ON av.Id    = sc.AgreementVersionId
WHERE av.Status = 'ACTIVE'
  AND sc.Status = 'ACTIVE'
  AND NOT EXISTS (
      SELECT 1
      FROM   ReportAgreementScope ras
      WHERE  ras.AgreementScopeId = sc.Id
  );
```

Run that first — if the row count and data look right, then execute the procedure.
