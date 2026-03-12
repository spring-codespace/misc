-- =====================================================
-- Corporate Reporting Product Agreement Database
-- MS SQL Server DDL Scripts with Test Data
-- =====================================================

-- Drop tables if they exist (in reverse order of dependencies)
IF OBJECT_ID('agreement_history', 'U') IS NOT NULL DROP TABLE agreement_history;
IF OBJECT_ID('cart_item', 'U') IS NOT NULL DROP TABLE cart_item;
IF OBJECT_ID('user_session', 'U') IS NOT NULL DROP TABLE user_session;
IF OBJECT_ID('recipient_product_part_account', 'U') IS NOT NULL DROP TABLE recipient_product_part_account;
IF OBJECT_ID('recipient_product_part', 'U') IS NOT NULL DROP TABLE recipient_product_part;
IF OBJECT_ID('message_recipient', 'U') IS NOT NULL DROP TABLE message_recipient;
IF OBJECT_ID('agreement_contact', 'U') IS NOT NULL DROP TABLE agreement_contact;
IF OBJECT_ID('contact', 'U') IS NOT NULL DROP TABLE contact;
IF OBJECT_ID('agreement_product_part', 'U') IS NOT NULL DROP TABLE agreement_product_part;
IF OBJECT_ID('corporate_reporting_agreement', 'U') IS NOT NULL DROP TABLE corporate_reporting_agreement;
IF OBJECT_ID('product_part_lu', 'U') IS NOT NULL DROP TABLE product_part_lu;
IF OBJECT_ID('agreement_status_lu', 'U') IS NOT NULL DROP TABLE agreement_status_lu;

-- =====================================================
-- 1. AGREEMENT_STATUS_LU (Lookup table)
-- =====================================================
CREATE TABLE agreement_status_lu (
    status_code NVARCHAR(20) PRIMARY KEY,
    description NVARCHAR(100),
    is_active BIT DEFAULT 1
);

-- =====================================================
-- 2. PRODUCT_PART_LU (Lookup table for Camt report types)
-- =====================================================
CREATE TABLE product_part_lu (
    product_part_code NVARCHAR(30) PRIMARY KEY,
    product_part_name NVARCHAR(100) NOT NULL,
    description NVARCHAR(255),
    is_active BIT DEFAULT 1
);

-- =====================================================
-- 3. CORPORATE_REPORTING_AGREEMENT (Main agreement table)
-- =====================================================
CREATE TABLE corporate_reporting_agreement (
    agreement_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    agreement_name NVARCHAR(200) NOT NULL,
    customer_id NVARCHAR(50) NOT NULL,
    status_code NVARCHAR(20) REFERENCES agreement_status_lu(status_code),
    pricing_order_ref NVARCHAR(100) UNIQUE,
    pricing_type NVARCHAR(20) CHECK (pricing_type IN ('STANDARD', 'INDIVIDUAL')),
    fixed_price DECIMAL(15,2) NULL,
    version_number INT NOT NULL DEFAULT 1,
    parent_agreement_id BIGINT NULL REFERENCES corporate_reporting_agreement(agreement_id),
    is_current_version BIT DEFAULT 1,
    submitted_date DATETIME NULL,
    approved_date DATETIME NULL,
    effective_date DATE NULL,
    expiry_date DATE NULL,
    cart_expiry_date DATETIME NULL,
    created_by NVARCHAR(100),
    created_date DATETIME DEFAULT GETDATE(),
    last_updated_by NVARCHAR(100),
    last_updated_date DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_customer_status ON corporate_reporting_agreement(customer_id, status_code);
CREATE INDEX idx_pricing_ref ON corporate_reporting_agreement(pricing_order_ref);

-- =====================================================
-- 4. AGREEMENT_PRODUCT_PART (Junction - product parts in agreement)
-- =====================================================
CREATE TABLE agreement_product_part (
    agreement_product_part_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    agreement_id BIGINT REFERENCES corporate_reporting_agreement(agreement_id),
    product_part_code NVARCHAR(30) REFERENCES product_part_lu(product_part_code),
    created_date DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_agreement_product ON agreement_product_part(agreement_id);

-- =====================================================
-- 5. CONTACT (Contacts can be shared across agreements)
-- =====================================================
CREATE TABLE contact (
    contact_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id NVARCHAR(50) NOT NULL,
    contact_name NVARCHAR(200) NOT NULL,
    email NVARCHAR(200) NOT NULL,
    is_active BIT DEFAULT 1,
    created_date DATETIME DEFAULT GETDATE(),
    last_updated_date DATETIME DEFAULT GETDATE(),
    CONSTRAINT unique_customer_email UNIQUE (customer_id, email)
);

CREATE INDEX idx_contact_customer ON contact(customer_id, is_active);

-- =====================================================
-- 6. AGREEMENT_CONTACT (Junction - contacts for agreement)
-- =====================================================
CREATE TABLE agreement_contact (
    agreement_contact_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    agreement_id BIGINT REFERENCES corporate_reporting_agreement(agreement_id),
    contact_id BIGINT REFERENCES contact(contact_id),
    is_primary BIT DEFAULT 0,
    created_date DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_agreement_contact ON agreement_contact(agreement_id);

-- =====================================================
-- 7. MESSAGE_RECIPIENT
-- =====================================================
CREATE TABLE message_recipient (
    recipient_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    agreement_id BIGINT REFERENCES corporate_reporting_agreement(agreement_id),
    recipient_name NVARCHAR(200),
    recipient_order INT NOT NULL,
    created_date DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_recipient_agreement ON message_recipient(agreement_id);

-- =====================================================
-- 8. RECIPIENT_PRODUCT_PART
-- =====================================================
CREATE TABLE recipient_product_part (
    recipient_product_part_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    recipient_id BIGINT REFERENCES message_recipient(recipient_id),
    product_part_code NVARCHAR(30) REFERENCES product_part_lu(product_part_code),
    created_date DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_recipient_product ON recipient_product_part(recipient_id);

-- =====================================================
-- 9. RECIPIENT_PRODUCT_PART_ACCOUNT
-- =====================================================
CREATE TABLE recipient_product_part_account (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    recipient_product_part_id BIGINT REFERENCES recipient_product_part(recipient_product_part_id),
    account_number NVARCHAR(50) NOT NULL,
    created_date DATETIME DEFAULT GETDATE()
);

CREATE INDEX idx_account ON recipient_product_part_account(account_number);
CREATE INDEX idx_recipient_product_account ON recipient_product_part_account(recipient_product_part_id);

-- =====================================================
-- 10. CART_ITEM
-- =====================================================
CREATE TABLE cart_item (
    cart_item_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_id NVARCHAR(50) NOT NULL,
    agreement_id BIGINT REFERENCES corporate_reporting_agreement(agreement_id),
    added_to_cart_date DATETIME DEFAULT GETDATE(),
    cart_expiry_date DATETIME NOT NULL,
    is_active BIT DEFAULT 1
);

CREATE INDEX idx_cart_cleanup ON cart_item(cart_expiry_date, is_active);
CREATE INDEX idx_cart_customer ON cart_item(customer_id, is_active);

-- =====================================================
-- 11. USER_SESSION
-- =====================================================
CREATE TABLE user_session (
    session_id NVARCHAR(100) PRIMARY KEY,
    customer_id NVARCHAR(50) NOT NULL,
    login_time DATETIME DEFAULT GETDATE(),
    last_activity_time DATETIME DEFAULT GETDATE(),
    session_data NVARCHAR(MAX) NULL, -- JSON data
    is_active BIT DEFAULT 1
);

CREATE INDEX idx_session_cleanup ON user_session(last_activity_time, is_active);

-- =====================================================
-- 12. AGREEMENT_HISTORY
-- =====================================================
CREATE TABLE agreement_history (
    history_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    agreement_id BIGINT REFERENCES corporate_reporting_agreement(agreement_id),
    action_type NVARCHAR(50),
    old_status NVARCHAR(20),
    new_status NVARCHAR(20),
    changed_by NVARCHAR(100),
    change_date DATETIME DEFAULT GETDATE(),
    change_reason NVARCHAR(500),
    snapshot_data NVARCHAR(MAX) NULL -- JSON data
);

CREATE INDEX idx_history_agreement ON agreement_history(agreement_id);
CREATE INDEX idx_history_archive ON agreement_history(change_date);

-- =====================================================
-- INSERT LOOKUP DATA
-- =====================================================

-- Insert agreement status values
INSERT INTO agreement_status_lu (status_code, description, is_active) VALUES
('DRAFT', 'Agreement in draft, not yet active', 1),
('ACTIVE', 'Agreement is active and generating reports', 1),
('EXPIRED', 'Agreement has passed expiry date', 1),
('CANCELLED', 'Agreement was cancelled by customer', 1),
('SUPERSEDED', 'Agreement has been replaced by newer version', 1);

-- Insert product parts (Camt report types)
INSERT INTO product_part_lu (product_part_code, product_part_name, description, is_active) VALUES
('C052_BAL_ONLY', 'Camt.052 Balances Only', 'Balance information only', 1),
('C052_BAL_TXN', 'Camt.052 Balances & Transactions', 'Balance and transaction details', 1),
('C053_STD', 'Camt.053 Standard', 'Standard account statement', 1),
('C053_EXT', 'Camt.053 Extended', 'Extended account statement with additional details', 1),
('C054_DEBIT', 'Camt.054 Debit Notifications', 'Debit entry notifications', 1),
('C053_CREDIT', 'Camt.053 Credit Notifications', 'Credit entry notifications', 1);

-- =====================================================
-- INSERT TEST DATA
-- =====================================================

-- Test customer: ACME Corporation (customer_id = 'CUST001')
-- Test customer: Beta Industries (customer_id = 'CUST002')

-- 1. Insert contacts
INSERT INTO contact (customer_id, contact_name, email, is_active) VALUES
('CUST001', 'John Smith', 'john.smith@acme.com', 1),
('CUST001', 'Jane Doe', 'jane.doe@acme.com', 1),
('CUST001', 'Bob Johnson', 'bob.johnson@acme.com', 1),
('CUST002', 'Alice Brown', 'alice.brown@betaind.com', 1),
('CUST002', 'Charlie Wilson', 'charlie.wilson@betaind.com', 1);

-- 2. Insert agreements

-- Agreement 1: ACME - Standard pricing (ACTIVE)
INSERT INTO corporate_reporting_agreement (
    agreement_name, customer_id, status_code, pricing_order_ref, 
    pricing_type, fixed_price, version_number, is_current_version,
    submitted_date, approved_date, effective_date, expiry_date,
    created_by, created_date, last_updated_by, last_updated_date
) VALUES (
    'ACME Q1 2024 Reporting', 'CUST001', 'ACTIVE', 'PRC-2024-0001',
    'STANDARD', NULL, 1, 1,
    '2024-01-15 10:30:00', '2024-01-16 14:20:00', '2024-01-01', '2024-12-31',
    'john.smith@acme.com', '2024-01-15 10:30:00', 'system', '2024-01-16 14:20:00'
);

-- Agreement 2: ACME - Draft version (updating agreement 1)
INSERT INTO corporate_reporting_agreement (
    agreement_name, customer_id, status_code, pricing_order_ref,
    pricing_type, fixed_price, version_number, parent_agreement_id, is_current_version,
    submitted_date, cart_expiry_date, created_by, created_date
) VALUES (
    'ACME Q1 2024 Reporting - UPDATED', 'CUST001', 'DRAFT', 'PRC-2024-0025',
    'STANDARD', NULL, 2, 1, 0,
    '2024-02-20 09:15:00', DATEADD(day, 30, '2024-02-20 09:15:00'),
    'jane.doe@acme.com', '2024-02-20 09:15:00'
);

-- Agreement 3: Beta Industries - Individual pricing (ACTIVE)
INSERT INTO corporate_reporting_agreement (
    agreement_name, customer_id, status_code, pricing_order_ref,
    pricing_type, fixed_price, version_number, is_current_version,
    submitted_date, approved_date, effective_date, expiry_date,
    created_by, created_date, last_updated_by, last_updated_date
) VALUES (
    'Beta Monthly Statements', 'CUST002', 'ACTIVE', 'PRC-2024-0089',
    'INDIVIDUAL', 2500.00, 1, 1,
    '2024-02-01 11:00:00', '2024-02-02 09:30:00', '2024-02-01', '2025-01-31',
    'alice.brown@betaind.com', '2024-02-01 11:00:00', 'system', '2024-02-02 09:30:00'
);

-- Agreement 4: Beta Industries - Draft in cart
INSERT INTO corporate_reporting_agreement (
    agreement_name, customer_id, status_code, pricing_order_ref,
    pricing_type, fixed_price, version_number, is_current_version,
    submitted_date, cart_expiry_date, created_by, created_date
) VALUES (
    'Beta Enhanced Reporting', 'CUST002', 'DRAFT', 'PRC-2024-0150',
    'STANDARD', NULL, 1, 1,
    '2024-03-01 13:45:00', DATEADD(day, 30, '2024-03-01 13:45:00'),
    'charlie.wilson@betaind.com', '2024-03-01 13:45:00'
);

-- 3. Insert agreement_product_part (which product parts in each agreement)
-- Agreement 1 (ACME ACTIVE) - has multiple product parts
INSERT INTO agreement_product_part (agreement_id, product_part_code) VALUES
(1, 'C052_BAL_ONLY'),
(1, 'C052_BAL_TXN'),
(1, 'C053_STD');

-- Agreement 2 (ACME DRAFT) - updated product parts
INSERT INTO agreement_product_part (agreement_id, product_part_code) VALUES
(2, 'C052_BAL_ONLY'),
(2, 'C052_BAL_TXN'),
(2, 'C053_STD'),
(2, 'C053_EXT');

-- Agreement 3 (Beta ACTIVE)
INSERT INTO agreement_product_part (agreement_id, product_part_code) VALUES
(3, 'C053_STD'),
(3, 'C054_DEBIT'),
(3, 'C053_CREDIT');

-- Agreement 4 (Beta DRAFT)
INSERT INTO agreement_product_part (agreement_id, product_part_code) VALUES
(4, 'C052_BAL_TXN'),
(4, 'C053_EXT'),
(4, 'C054_DEBIT');

-- 4. Insert agreement_contact (contacts for each agreement)
-- Agreement 1 - John is primary, Jane is secondary
INSERT INTO agreement_contact (agreement_id, contact_id, is_primary) VALUES
(1, 1, 1),  -- John Smith
(1, 2, 0);  -- Jane Doe

-- Agreement 2 - Jane is primary (updated version)
INSERT INTO agreement_contact (agreement_id, contact_id, is_primary) VALUES
(2, 2, 1),  -- Jane Doe
(2, 3, 0);  -- Bob Johnson

-- Agreement 3 - Alice is primary
INSERT INTO agreement_contact (agreement_id, contact_id, is_primary) VALUES
(3, 4, 1),  -- Alice Brown
(3, 5, 0);  -- Charlie Wilson

-- Agreement 4 - Charlie is primary
INSERT INTO agreement_contact (agreement_id, contact_id, is_primary) VALUES
(4, 5, 1);  -- Charlie Wilson

-- 5. Insert message recipients
-- Agreement 1 - Two recipients
INSERT INTO message_recipient (agreement_id, recipient_name, recipient_order) VALUES
(1, 'Treasury Department', 1),
(1, 'Accounting Department', 2);

-- Agreement 2 - Two recipients (same structure, updated)
INSERT INTO message_recipient (agreement_id, recipient_name, recipient_order) VALUES
(2, 'Treasury Department', 1),
(2, 'Accounting Department', 2),
(2, 'Compliance Department', 3);

-- Agreement 3 - One recipient
INSERT INTO message_recipient (agreement_id, recipient_name, recipient_order) VALUES
(3, 'Finance Team', 1);

-- Agreement 4 - Two recipients
INSERT INTO message_recipient (agreement_id, recipient_name, recipient_order) VALUES
(4, 'Operations', 1),
(4, 'Management', 2);

-- 6. Insert recipient_product_part (which product parts for each recipient)
-- Agreement 1, Recipient 1 (Treasury)
INSERT INTO recipient_product_part (recipient_id, product_part_code) VALUES
(1, 'C052_BAL_ONLY'),  -- Treasury gets Balances Only
(1, 'C053_STD');       -- Treasury gets Standard statements

-- Agreement 1, Recipient 2 (Accounting)
INSERT INTO recipient_product_part (recipient_id, product_part_code) VALUES
(2, 'C052_BAL_TXN');    -- Accounting gets Balances & Transactions

-- Agreement 2, Recipient 3 (Treasury - updated)
INSERT INTO recipient_product_part (recipient_id, product_part_code) VALUES
(3, 'C052_BAL_ONLY'),
(3, 'C053_STD'),
(3, 'C053_EXT');        -- Treasury now gets Extended too

-- Agreement 2, Recipient 4 (Accounting - updated)
INSERT INTO recipient_product_part (recipient_id, product_part_code) VALUES
(4, 'C052_BAL_TXN');

-- Agreement 2, Recipient 5 (Compliance - new)
INSERT INTO recipient_product_part (recipient_id, product_part_code) VALUES
(5, 'C053_STD'),
(5, 'C053_EXT');

-- Agreement 3, Recipient 6 (Finance Team)
INSERT INTO recipient_product_part (recipient_id, product_part_code) VALUES
(6, 'C053_STD'),
(6, 'C054_DEBIT'),
(6, 'C053_CREDIT');

-- Agreement 4, Recipient 7 (Operations)
INSERT INTO recipient_product_part (recipient_id, product_part_code) VALUES
(7, 'C052_BAL_TXN'),
(7, 'C053_EXT');

-- Agreement 4, Recipient 8 (Management)
INSERT INTO recipient_product_part (recipient_id, product_part_code) VALUES
(8, 'C053_EXT');

-- 7. Insert recipient_product_part_account (accounts for each product part per recipient)
-- Accounts for ACME: 'ACC001', 'ACC002', 'ACC003', 'ACC004', 'ACC005'
-- Accounts for Beta: 'ACC101', 'ACC102', 'ACC103'

-- Agreement 1, Recipient 1 (Treasury) - C052_BAL_ONLY
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(1, 'ACC001'),
(1, 'ACC002'),
(1, 'ACC003');

-- Agreement 1, Recipient 1 (Treasury) - C053_STD
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(2, 'ACC001'),
(2, 'ACC004');

-- Agreement 1, Recipient 2 (Accounting) - C052_BAL_TXN
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(3, 'ACC001'),
(3, 'ACC002'),
(3, 'ACC003'),
(3, 'ACC004'),
(3, 'ACC005');

-- Agreement 2, Recipient 3 (Treasury) - C052_BAL_ONLY
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(4, 'ACC001'),
(4, 'ACC002'),
(4, 'ACC003');

-- Agreement 2, Recipient 3 (Treasury) - C053_STD
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(5, 'ACC001'),
(5, 'ACC004');

-- Agreement 2, Recipient 3 (Treasury) - C053_EXT
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(6, 'ACC001'),
(6, 'ACC005');

-- Agreement 2, Recipient 4 (Accounting) - C052_BAL_TXN
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(7, 'ACC001'),
(7, 'ACC002'),
(7, 'ACC003'),
(7, 'ACC004'),
(7, 'ACC005');

-- Agreement 2, Recipient 5 (Compliance) - C053_STD
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(8, 'ACC001'),
(8, 'ACC002'),
(8, 'ACC003');

-- Agreement 2, Recipient 5 (Compliance) - C053_EXT
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(9, 'ACC001'),
(9, 'ACC002'),
(9, 'ACC003'),
(9, 'ACC004'),
(9, 'ACC005');

-- Agreement 3, Recipient 6 (Finance) - C053_STD
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(10, 'ACC101'),
(10, 'ACC102');

-- Agreement 3, Recipient 6 (Finance) - C054_DEBIT
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(11, 'ACC101'),
(11, 'ACC103');

-- Agreement 3, Recipient 6 (Finance) - C053_CREDIT
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(12, 'ACC102'),
(12, 'ACC103');

-- Agreement 4, Recipient 7 (Operations) - C052_BAL_TXN
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(13, 'ACC101'),
(13, 'ACC102'),
(13, 'ACC103');

-- Agreement 4, Recipient 7 (Operations) - C053_EXT
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(14, 'ACC101'),
(14, 'ACC102');

-- Agreement 4, Recipient 8 (Management) - C053_EXT
INSERT INTO recipient_product_part_account (recipient_product_part_id, account_number) VALUES
(15, 'ACC101'),
(15, 'ACC102'),
(15, 'ACC103');

-- 8. Insert cart items
INSERT INTO cart_item (customer_id, agreement_id, added_to_cart_date, cart_expiry_date, is_active) VALUES
('CUST001', 2, '2024-02-20 09:15:00', DATEADD(day, 30, '2024-02-20 09:15:00'), 1),
('CUST002', 4, '2024-03-01 13:45:00', DATEADD(day, 30, '2024-03-01 13:45:00'), 1);

-- 9. Insert user sessions
INSERT INTO user_session (session_id, customer_id, login_time, last_activity_time, session_data, is_active) VALUES
('sess_abc123', 'CUST001', DATEADD(minute, -5, GETDATE()), GETDATE(), 
 '{"current_screen": 3, "form_data": {"selected_product_parts": ["C052_BAL_ONLY", "C053_STD"]}}', 1),
('sess_def456', 'CUST002', DATEADD(hour, -2, GETDATE()), DATEADD(hour, -2, GETDATE()), 
 NULL, 0); -- Inactive session

-- 10. Insert agreement history
INSERT INTO agreement_history (agreement_id, action_type, old_status, new_status, changed_by, change_date, change_reason, snapshot_data) VALUES
(1, 'CREATED', NULL, 'DRAFT', 'john.smith@acme.com', '2024-01-15 10:30:00', 'Initial creation', 
 '{"agreement_name": "ACME Q1 2024 Reporting", "product_parts": ["C052_BAL_ONLY", "C052_BAL_TXN", "C053_STD"]}'),
(1, 'SUBMITTED', 'DRAFT', 'DRAFT', 'john.smith@acme.com', '2024-01-15 10:30:00', 'Submitted for approval', NULL),
(1, 'APPROVED', 'DRAFT', 'ACTIVE', 'approver@bank.com', '2024-01-16 14:20:00', 'Approved by compliance', NULL),
(2, 'CREATED', NULL, 'DRAFT', 'jane.doe@acme.com', '2024-02-20 09:15:00', 'Updated version of agreement 1', 
 '{"parent_agreement_id": 1, "changes": ["Added C053_EXT product part"]}'),
(3, 'CREATED', NULL, 'DRAFT', 'alice.brown@betaind.com', '2024-02-01 11:00:00', 'Initial creation', NULL),
(3, 'SUBMITTED', 'DRAFT', 'DRAFT', 'alice.brown@betaind.com', '2024-02-01 11:00:00', 'Submitted for approval', NULL),
(3, 'APPROVED', 'DRAFT', 'ACTIVE', 'approver@bank.com', '2024-02-02 09:30:00', 'Approved with individual pricing', NULL),
(4, 'CREATED', NULL, 'DRAFT', 'charlie.wilson@betaind.com', '2024-03-01 13:45:00', 'Initial creation', NULL);

-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================

-- Check all tables have data
SELECT 'agreement_status_lu' AS table_name, COUNT(*) AS row_count FROM agreement_status_lu UNION ALL
SELECT 'product_part_lu', COUNT(*) FROM product_part_lu UNION ALL
SELECT 'corporate_reporting_agreement', COUNT(*) FROM corporate_reporting_agreement UNION ALL
SELECT 'agreement_product_part', COUNT(*) FROM agreement_product_part UNION ALL
SELECT 'contact', COUNT(*) FROM contact UNION ALL
SELECT 'agreement_contact', COUNT(*) FROM agreement_contact UNION ALL
SELECT 'message_recipient', COUNT(*) FROM message_recipient UNION ALL
SELECT 'recipient_product_part', COUNT(*) FROM recipient_product_part UNION ALL
SELECT 'recipient_product_part_account', COUNT(*) FROM recipient_product_part_account UNION ALL
SELECT 'cart_item', COUNT(*) FROM cart_item UNION ALL
SELECT 'user_session', COUNT(*) FROM user_session UNION ALL
SELECT 'agreement_history', COUNT(*) FROM agreement_history
ORDER BY table_name;

-- Sample query: Get complete agreement details for Agreement 1
SELECT 
    cra.agreement_id,
    cra.agreement_name,
    cra.customer_id,
    asl.description AS status,
    cra.pricing_type,
    cra.fixed_price,
    cra.effective_date,
    cra.expiry_date,
    c.contact_name,
    c.email,
    CASE WHEN ac.is_primary = 1 THEN 'Primary' ELSE 'Secondary' END AS contact_role
FROM corporate_reporting_agreement cra
JOIN agreement_status_lu asl ON cra.status_code = asl.status_code
JOIN agreement_contact ac ON cra.agreement_id = ac.agreement_id
JOIN contact c ON ac.contact_id = c.contact_id
WHERE cra.agreement_id = 1;
