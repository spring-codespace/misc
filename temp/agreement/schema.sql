-- =========================================================
-- TABLE: AgreementVersionStatus (REFERENCE TABLE)
-- =========================================================
CREATE TABLE AgreementVersionStatus (
    Code        NVARCHAR(15)  PRIMARY KEY,
    Description NVARCHAR(100) NOT NULL
);

-- =========================================================
-- TABLE: AgreementScopeStatus (REFERENCE TABLE)
-- =========================================================
CREATE TABLE AgreementScopeStatus (
    Code        NVARCHAR(15)  PRIMARY KEY,
    Description NVARCHAR(100) NOT NULL
);

-- =========================================================
-- TABLE: ProductPart (REFERENCE TABLE)
-- =========================================================
CREATE TABLE ProductPart (
    Code        NVARCHAR(40)  PRIMARY KEY,
    Description NVARCHAR(100) NOT NULL
);

-- =========================================================
-- TABLE: Agreement
-- =========================================================
CREATE TABLE Agreement (
    Id NVARCHAR(20) PRIMARY KEY,
    Name NVARCHAR(35) NOT NULL,
    BankId NVARCHAR(5) NOT NULL,
    CorporateId NVARCHAR(15) NOT NULL,
    CreatedAt DATETIME2 NOT NULL,
    Channel NVARCHAR(20) NOT NULL
);

-- =========================================================
-- TABLE: AgreementContact
-- =========================================================
CREATE TABLE AgreementContact (
    Id BIGINT IDENTITY PRIMARY KEY,
    AgreementId NVARCHAR(20) NOT NULL,
    ContactName NVARCHAR(40) NOT NULL,
    ContactEmail NVARCHAR(50) NOT NULL,
    ContactPhone NVARCHAR(15) NOT NULL,
    CreatedAt DATETIME2 NOT NULL,

    CONSTRAINT FK_AgreementContact_Agreement
        FOREIGN KEY (AgreementId) REFERENCES Agreement(Id)
);

-- =========================================================
-- TABLE: AgreementVersion
-- =========================================================
CREATE TABLE AgreementVersion (
    Id BIGINT IDENTITY PRIMARY KEY,
    AgreementId NVARCHAR(20) NOT NULL,
    Status NVARCHAR(15) NOT NULL,
    PricingOrderRef NVARCHAR(20) NOT NULL,
    ExpiredAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL,
    ActivatedAt DATETIME2 NULL,
    SupersededAt DATETIME2 NULL,
    CancelledAt DATETIME2 NULL,

    CONSTRAINT FK_AgreementVersion_Agreement
        FOREIGN KEY (AgreementId) REFERENCES Agreement(Id),

    CONSTRAINT FK_AgreementVersion_Status
        FOREIGN KEY (Status) REFERENCES AgreementVersionStatus(Code)
);

-- Only one ACTIVE version per agreement
CREATE UNIQUE INDEX UX_AgreementVersion_Active
ON AgreementVersion (AgreementId)
WHERE Status = 'ACTIVE';

-- =========================================================
-- TABLE: AgreementScope
-- =========================================================
CREATE TABLE AgreementScope (
    Id BIGINT IDENTITY PRIMARY KEY,
    AgreementVersionId BIGINT NOT NULL,
    MessageRecipientId NVARCHAR(20) NOT NULL,
    ProductPart NVARCHAR(40) NOT NULL,
    Status NVARCHAR(15) NOT NULL,
    CreatedAt DATETIME2 NOT NULL,
    ActivatedAt DATETIME2 NULL,
    CancelledAt DATETIME2 NULL,

    CONSTRAINT FK_AgreementScope_Version
        FOREIGN KEY (AgreementVersionId) REFERENCES AgreementVersion(Id),

    CONSTRAINT FK_AgreementScope_Status
        FOREIGN KEY (Status) REFERENCES AgreementScopeStatus(Code),

    CONSTRAINT FK_AgreementScope_ProductPart
        FOREIGN KEY (ProductPart) REFERENCES ProductPart(Code)
);

-- No duplicate scopes per version
CREATE UNIQUE INDEX UX_AgreementScope_Unique
ON AgreementScope (AgreementVersionId, MessageRecipientId, ProductPart);

-- =========================================================
-- TABLE: PaymentType (REFERENCE TABLE)
-- =========================================================
CREATE TABLE PaymentType (
    Code NVARCHAR(40) PRIMARY KEY,
    Description NVARCHAR(100) NOT NULL
);

-- =========================================================
-- TABLE: PaymentTypeAssignment
-- =========================================================
CREATE TABLE PaymentTypeAssignment (
    Id BIGINT IDENTITY PRIMARY KEY,
    AgreementScopeId BIGINT NOT NULL,
    PaymentType NVARCHAR(40) NOT NULL,
    CreatedAt DATETIME2 NOT NULL,

    CONSTRAINT FK_PaymentTypeAssignment_Scope
        FOREIGN KEY (AgreementScopeId) REFERENCES AgreementScope(Id),

    CONSTRAINT FK_PaymentTypeAssignment_PaymentType
        FOREIGN KEY (PaymentType) REFERENCES PaymentType(Code)
);

-- Unique payment type per scope
CREATE UNIQUE INDEX UX_PaymentTypeAssignment_Unique
ON PaymentTypeAssignment (AgreementScopeId, PaymentType);

-- =========================================================
-- TABLE: AccountAssignment
-- =========================================================
CREATE TABLE AccountAssignment (
    Id BIGINT IDENTITY PRIMARY KEY,
    PaymentTypeAssignmentId BIGINT NOT NULL,
    AccountBBAN NVARCHAR(15) NOT NULL,
    AccountIBAN NVARCHAR(35) NOT NULL,
    Currency NVARCHAR(3) NOT NULL,
    CreatedAt DATETIME2 NOT NULL,

    CONSTRAINT FK_AccountAssignment_PaymentTypeAssignment
        FOREIGN KEY (PaymentTypeAssignmentId) REFERENCES PaymentTypeAssignment(Id)
);

-- =========================================================
-- TABLE: AliasAssignment
-- =========================================================
CREATE TABLE AliasAssignment (
    Id BIGINT IDENTITY PRIMARY KEY,
    PaymentTypeAssignmentId BIGINT NOT NULL,
    AliasId NVARCHAR(15) NOT NULL,
    CreatedAt DATETIME2 NOT NULL,

    CONSTRAINT FK_AliasAssignment_PaymentTypeAssignment
        FOREIGN KEY (PaymentTypeAssignmentId) REFERENCES PaymentTypeAssignment(Id)
);

-- =========================================================
-- TABLE: CartItem
-- =========================================================
CREATE TABLE CartItem (
    Id BIGINT IDENTITY PRIMARY KEY,
    AgreementVersionId BIGINT NOT NULL,
    CorporateId NVARCHAR(15) NOT NULL,
    ExpiresAt DATETIME2 NOT NULL,
    ApprovedAt DATETIME2 NULL,
    ExpiredAt DATETIME2 NULL,
    CreatedAt DATETIME2 NOT NULL,

    CONSTRAINT FK_CartItem_AgreementVersion
        FOREIGN KEY (AgreementVersionId) REFERENCES AgreementVersion(Id)
);

-- One cart item per version
CREATE UNIQUE INDEX UX_CartItem_Version
ON CartItem (AgreementVersionId);

-- =========================================================
-- TABLE: ReportConfig
-- =========================================================
CREATE TABLE ReportConfig (
    Id BIGINT IDENTITY PRIMARY KEY,
    ReportType NVARCHAR(40) NOT NULL,
    ReportVersion NVARCHAR(3) NOT NULL,
    ReportFrequency NVARCHAR(35) NOT NULL,
    Description NVARCHAR(80) NOT NULL,
    MessageRecipientId NVARCHAR(20) NOT NULL,
    MessageRecipientType NVARCHAR(15) NOT NULL,
    AccountFormat NVARCHAR(4) NOT NULL,
    IsActive BIT NOT NULL DEFAULT 0,
    IsPaginated BIT NOT NULL DEFAULT 0,
    IsEmptyReportAllowed BIT NOT NULL DEFAULT 0,
    IsBundled BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL,
    CreatedBy NVARCHAR(20) NOT NULL,
    UpdatedAt DATETIME2 NOT NULL,
    UpdatedBy NVARCHAR(20) NOT NULL,

    CONSTRAINT FK_ReportConfig_ReportType
        FOREIGN KEY (ReportType) REFERENCES ProductPart(Code)
);

-- =========================================================
-- TABLE: ReportAgreementScope
-- =========================================================
CREATE TABLE ReportAgreementScope (
    Id BIGINT IDENTITY PRIMARY KEY,
    ReportConfigId BIGINT NOT NULL,
    AgreementScopeId BIGINT NOT NULL,
    AgreementId NVARCHAR(20) NOT NULL,

    CONSTRAINT FK_ReportAgreementScope_Config
        FOREIGN KEY (ReportConfigId) REFERENCES ReportConfig(Id),

    CONSTRAINT FK_ReportAgreementScope_Scope
        FOREIGN KEY (AgreementScopeId) REFERENCES AgreementScope(Id),

    CONSTRAINT FK_ReportAgreementScope_Agreement
        FOREIGN KEY (AgreementId) REFERENCES Agreement(Id)
);

-- Unique mapping
CREATE UNIQUE INDEX UX_ReportAgreementScope_Unique
ON ReportAgreementScope (ReportConfigId, AgreementScopeId);

-- =========================================================
-- INDEXES (PERFORMANCE)
-- =========================================================

-- Agreement / Version
CREATE INDEX IX_AgreementVersion_Agreement
ON AgreementVersion (AgreementId, Status);

-- Scope
CREATE INDEX IX_AgreementScope_Version
ON AgreementScope (AgreementVersionId);

CREATE INDEX IX_AgreementScope_Recipient
ON AgreementScope (MessageRecipientId);

-- Cart expiration job
CREATE INDEX IX_CartItem_ExpiresAt
ON CartItem (ExpiresAt);

-- Reporting linkage
CREATE INDEX IX_ReportAgreementScope_Agreement
ON ReportAgreementScope (AgreementId);

CREATE INDEX IX_ReportAgreementScope_Scope
ON ReportAgreementScope (AgreementScopeId);

-- Payment + Accounts
CREATE INDEX IX_PaymentTypeAssignment_Scope
ON PaymentTypeAssignment (AgreementScopeId);

CREATE INDEX IX_AccountAssignment_PaymentType
ON AccountAssignment (PaymentTypeAssignmentId);

CREATE INDEX IX_ReportConfig_MessageRecipientId
ON ReportConfig (MessageRecipientId);