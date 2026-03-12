-- =====================================================
-- STORED PROCEDURE: Clean up expired data
-- =====================================================
CREATE PROCEDURE sp_CleanupExpiredData
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @cutoff_date DATETIME = DATEADD(month, -3, GETDATE());
    
    -- Begin transaction
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- 1. Archive/delete expired cart items (older than cart_expiry_date)
        DELETE FROM cart_item 
        WHERE cart_expiry_date < GETDATE() 
           OR (is_active = 1 AND cart_expiry_date < GETDATE());
        
        -- 2. Mark inactive user sessions (older than 15 minutes)
        UPDATE user_session 
        SET is_active = 0 
        WHERE last_activity_time < DATEADD(minute, -15, GETDATE()) 
          AND is_active = 1;
        
        -- 3. Delete old history records (older than 3 months)
        -- In production, you might want to archive these to another table first
        DELETE FROM agreement_history 
        WHERE change_date < @cutoff_date;
        
        -- 4. Update expired agreements
        UPDATE corporate_reporting_agreement 
        SET status_code = 'EXPIRED' 
        WHERE expiry_date < CAST(GETDATE() AS DATE) 
          AND status_code = 'ACTIVE';
        
        COMMIT TRANSACTION;
        
        SELECT 'Cleanup completed successfully' AS result;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- Schedule this procedure to run daily
-- EXEC sp_CleanupExpiredData;
