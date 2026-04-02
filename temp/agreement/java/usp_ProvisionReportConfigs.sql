-- =============================================================================
-- STORED PROCEDURE: usp_ProvisionReportConfigs
--
-- Purpose:
--   Nightly job that creates a ReportConfig + ReportAgreementScope row for
--   every ACTIVE AgreementScope that does not already have one.
--   Fully idempotent — safe to re-run any number of times on the same night.
--
-- Granularity:
--   One ReportConfig per active AgreementScope
--   (i.e. per unique MessageRecipientId + ProductPart combination per version).
--
-- Derived from agreement data:
--   ReportType          ← AgreementScope.ProductPart
--   MessageRecipientId  ← AgreementScope.MessageRecipientId
--
-- Controlled via parameters (all have defaults, override at call time):
--   @ReportFrequency, @ReportVersion, @AccountFormat,
--   @MessageRecipientType, @ReportDescription,
--   @IsActive, @IsPaginated, @IsEmptyReportAllowed, @IsBundled
--
-- Usage:
--   EXEC usp_ProvisionReportConfigs;                          -- all defaults
--   EXEC usp_ProvisionReportConfigs @IsActive = 1;           -- activate immediately
--   EXEC usp_ProvisionReportConfigs @ReportFrequency = '4_HOURS', @AccountFormat = 'BBAN';
-- =============================================================================

CREATE OR ALTER PROCEDURE usp_ProvisionReportConfigs
    @ReportFrequency        NVARCHAR(35)  = '1_HOUR',
    @ReportVersion          NVARCHAR(3)   = '1',
    @AccountFormat          NVARCHAR(4)   = 'IBAN',
    @MessageRecipientType   NVARCHAR(15)  = 'SIGNER',
    @ReportDescription      NVARCHAR(80)  = 'Auto-provisioned by nightly job',
    @IsActive               BIT           = 0,
    @IsPaginated            BIT           = 0,
    @IsEmptyReportAllowed   BIT           = 0,
    @IsBundled              BIT           = 0
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;   -- auto-rollback entire transaction on any error

    -- -------------------------------------------------------------------------
    -- Validate parameter values against allowed reference data
    -- -------------------------------------------------------------------------

    IF @AccountFormat NOT IN ('IBAN', 'BBAN')
    BEGIN
        RAISERROR('Invalid @AccountFormat. Allowed values: IBAN, BBAN.', 16, 1);
        RETURN;
    END

    IF @ReportFrequency NOT IN ('30_MIN', '1_HOUR', '2_HOUR', '4_HOURS')
    BEGIN
        RAISERROR('Invalid @ReportFrequency. Allowed values: 30_MIN, 1_HOUR, 2_HOUR, 4_HOURS.', 16, 1);
        RETURN;
    END

    -- -------------------------------------------------------------------------
    -- Working variables
    -- -------------------------------------------------------------------------

    DECLARE @Now            DATETIME2       = SYSUTCDATETIME();
    DECLARE @CreatedBy      NVARCHAR(20)    = 'SYSTEM';
    DECLARE @InsertedConfigs INT            = 0;
    DECLARE @InsertedLinks   INT            = 0;

    BEGIN TRANSACTION;

    BEGIN TRY

        -- ---------------------------------------------------------------------
        -- STEP 1: Identify all ACTIVE scopes that have no ReportConfig yet.
        --
        -- A scope is considered "already provisioned" when a ReportAgreementScope
        -- row exists linking it to any ReportConfig.  We use this join rather
        -- than querying ReportConfig directly so the check stays consistent with
        -- the ReportAgreementScope unique index (ReportConfigId, AgreementScopeId).
        -- ---------------------------------------------------------------------

        DROP TABLE IF EXISTS #ScopesToProvision;

        SELECT
            sc.Id                   AS AgreementScopeId,
            sc.MessageRecipientId,
            sc.ProductPart          AS ReportTypeCode,
            av.AgreementId
        INTO #ScopesToProvision
        FROM AgreementScope         sc
        JOIN AgreementVersion       av  ON av.Id     = sc.AgreementVersionId
        JOIN AgreementVersionStatus avs ON avs.Code  = av.Status
        JOIN AgreementScopeStatus   ass ON ass.Code  = sc.Status
        WHERE avs.Code  = 'ACTIVE'
          AND ass.Code  = 'ACTIVE'
          -- Skip scopes that already have a ReportConfig linked
          AND NOT EXISTS (
              SELECT 1
              FROM   ReportAgreementScope ras
              WHERE  ras.AgreementScopeId = sc.Id
          );

        -- Nothing to do — exit cleanly
        IF NOT EXISTS (SELECT 1 FROM #ScopesToProvision)
        BEGIN
            COMMIT TRANSACTION;
            PRINT 'usp_ProvisionReportConfigs: no new scopes to provision.';
            RETURN;
        END

        -- ---------------------------------------------------------------------
        -- STEP 2: Insert one ReportConfig per qualifying scope.
        --
        -- OUTPUT clause captures the new Id alongside the source AgreementScopeId
        -- so we can insert ReportAgreementScope rows in step 3 without a
        -- second lookup.
        -- ---------------------------------------------------------------------

        DROP TABLE IF EXISTS #InsertedReportConfigs;

        CREATE TABLE #InsertedReportConfigs (
            ReportConfigId      BIGINT          NOT NULL,
            AgreementScopeId    BIGINT          NOT NULL,
            AgreementId         NVARCHAR(20)    NOT NULL
        );

        INSERT INTO ReportConfig (
            ReportType,
            ReportVersion,
            ReportFrequency,
            Description,
            MessageRecipientId,
            MessageRecipientType,
            AccountFormat,
            IsActive,
            IsPaginated,
            IsEmptyReportAllowed,
            IsBundled,
            CreatedAt,
            CreatedBy,
            UpdatedAt,
            UpdatedBy
        )
        OUTPUT
            INSERTED.Id,
            src.AgreementScopeId,
            src.AgreementId
        INTO #InsertedReportConfigs (ReportConfigId, AgreementScopeId, AgreementId)
        SELECT
            stp.ReportTypeCode,
            @ReportVersion,
            @ReportFrequency,
            @ReportDescription,
            stp.MessageRecipientId,
            @MessageRecipientType,
            @AccountFormat,
            @IsActive,
            @IsPaginated,
            @IsEmptyReportAllowed,
            @IsBundled,
            @Now,
            @CreatedBy,
            @Now,
            @CreatedBy
        FROM #ScopesToProvision stp;

        SET @InsertedConfigs = @@ROWCOUNT;

        -- ---------------------------------------------------------------------
        -- STEP 3: Insert corresponding ReportAgreementScope rows.
        --
        -- The unique index UX_ReportAgreementScope_Unique (ReportConfigId,
        -- AgreementScopeId) guards against duplicates at the DB level as a
        -- second safety net.
        -- ---------------------------------------------------------------------

        INSERT INTO ReportAgreementScope (
            ReportConfigId,
            AgreementScopeId,
            AgreementId
        )
        SELECT
            irc.ReportConfigId,
            irc.AgreementScopeId,
            irc.AgreementId
        FROM #InsertedReportConfigs irc;

        SET @InsertedLinks = @@ROWCOUNT;

        COMMIT TRANSACTION;

        -- ---------------------------------------------------------------------
        -- Summary log — visible in SQL Server Agent job history and SSMS output
        -- ---------------------------------------------------------------------

        PRINT 'usp_ProvisionReportConfigs completed successfully.';
        PRINT '  ReportConfig rows inserted    : ' + CAST(@InsertedConfigs AS NVARCHAR(10));
        PRINT '  ReportAgreementScope rows inserted : ' + CAST(@InsertedLinks  AS NVARCHAR(10));

    END TRY
    BEGIN CATCH

        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        DECLARE
            @ErrMessage  NVARCHAR(4000) = ERROR_MESSAGE(),
            @ErrSeverity INT            = ERROR_SEVERITY(),
            @ErrState    INT            = ERROR_STATE(),
            @ErrLine     INT            = ERROR_LINE();

        PRINT 'usp_ProvisionReportConfigs FAILED at line ' + CAST(@ErrLine AS NVARCHAR(10));
        PRINT 'Error: ' + @ErrMessage;

        RAISERROR(@ErrMessage, @ErrSeverity, @ErrState);

    END CATCH;

    -- Cleanup temp tables (defensive — DROP TABLE IF EXISTS already handles session end)
    DROP TABLE IF EXISTS #ScopesToProvision;
    DROP TABLE IF EXISTS #InsertedReportConfigs;

END;
GO


-- =============================================================================
-- SQL Server Agent Job (template — run after procedure is created)
-- =============================================================================
--
-- To schedule this via SQL Server Agent:
--
--   1. Open SQL Server Agent → Jobs → New Job
--   2. General tab   : Name = 'Nightly ReportConfig Provisioning'
--   3. Steps tab     : New Step
--                      Type    = Transact-SQL script (T-SQL)
--                      Command = EXEC usp_ProvisionReportConfigs;
--   4. Schedule tab  : New Schedule
--                      Frequency  = Daily
--                      Start time = 00:05:00
--   5. Notifications : Alert operator on failure (recommended)
--
-- Or schedule via T-SQL:
-- =============================================================================

/*
DECLARE @JobId UNIQUEIDENTIFIER;

EXEC msdb.dbo.sp_add_job
    @job_name       = N'Nightly ReportConfig Provisioning',
    @enabled        = 1,
    @description    = N'Provisions ReportConfig rows for newly ACTIVE AgreementScopes.',
    @job_id         = @JobId OUTPUT;

EXEC msdb.dbo.sp_add_jobstep
    @job_id         = @JobId,
    @step_name      = N'Run usp_ProvisionReportConfigs',
    @command        = N'EXEC usp_ProvisionReportConfigs;',
    @database_name  = N'camt_db';

EXEC msdb.dbo.sp_add_schedule
    @schedule_name  = N'Daily 00:05',
    @freq_type      = 4,        -- Daily
    @freq_interval  = 1,
    @active_start_time = 000500; -- 00:05:00

EXEC msdb.dbo.sp_attach_schedule
    @job_id         = @JobId,
    @schedule_name  = N'Daily 00:05';

EXEC msdb.dbo.sp_add_jobserver
    @job_id         = @JobId;
*/
