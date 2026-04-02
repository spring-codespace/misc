USE [REPORT];
GO

-- =========================================================
-- TABLE: AgreementVersionStatus (REFERENCE TABLE)
-- =========================================================
CREATE TABLE CAMT.AgreementVersionStatus (
    Code        NVARCHAR(15)  PRIMARY KEY,
    Description NVARCHAR(100) NOT NULL
);
GO

-- =========================================================
-- TABLE: AgreementScopeStatus (REFERENCE TABLE)
-- =========================================================
CREATE TABLE CAMT.AgreementScopeStatus (
    Code        NVARCHAR(15)  PRIMARY KEY,
    Description NVARCHAR(100) NOT NULL
);
GO

-- =========================================================
-- TABLE: ProductPart (REFERENCE TABLE)
-- =========================================================
CREATE TABLE CAMT.ProductPart (
    Code        NVARCHAR(40)  PRIMARY KEY,
    Description NVARCHAR(100) NOT NULL
);
GO

-- =========================================================
-- TABLE: Agreement
-- =========================================================
CREATE TABLE CAMT.Agreement (
    Id           NVARCHAR(20) PRIMARY KEY,
    Name         NVARCHAR(35) NOT NULL,
    BankId       NVARCHAR(5)  NOT NULL,
    CorporateId  NVARCHAR(15) NOT NULL,
    CreatedAt    DATETIME2    NOT NULL,
    Channel      NVARCHAR(20) NOT NULL
);
GO

-- =========================================================
-- TABLE: AgreementContact
-- =========================================================
CREATE TABLE CAMT.AgreementContact (
    Id           BIGINT IDENTITY PRIMARY KEY,
    AgreementId  NVARCHAR(20) NOT NULL,
    ContactName  NVARCHAR(40) NOT NULL,
    ContactEmail NVARCHAR(50) NOT NULL,
    ContactPhone NVARCHAR(15) NOT NULL,
    CreatedAt    DATETIME2    NOT NULL,

    CONSTRAINT FK_AgreementContact_Agreement
        FOREIGN KEY (AgreementId) REFERENCES CAMT.Agreement(Id)
);
GO

-- =========================================================
-- TABLE: AgreementVersion
-- =========================================================
CREATE TABLE CAMT.AgreementVersion (
    Id             BIGINT IDENTITY PRIMARY KEY,
    AgreementId    NVARCHAR(20) NOT NULL,
    Status         NVARCHAR(15) NOT NULL,
    PricingOrderRef NVARCHAR(20) NOT NULL,
    ExpiredAt      DATETIME2    NULL,
    CreatedAt      DATETIME2    NOT NULL,
    ActivatedAt    DATETIME2    NULL,
    SupersededAt   DATETIME2    NULL,
    CancelledAt    DATETIME2    NULL,

    CONSTRAINT FK_AgreementVersion_Agreement
        FOREIGN KEY (AgreementId) REFERENCES CAMT.Agreement(Id),

    CONSTRAINT FK_AgreementVersion_Status
        FOREIGN KEY (Status) REFERENCES CAMT.AgreementVersionStatus(Code)
);
GO

-- Only one ACTIVE version per agreement
CREATE UNIQUE INDEX CAMT.UX_AgreementVersion_Active
ON CAMT.AgreementVersion (AgreementId)
WHERE Status = 'ACTIVE';
GO

-- =========================================================
-- TABLE: AgreementScope
-- =========================================================
CREATE TABLE CAMT.AgreementScope (
    Id                  BIGINT IDENTITY PRIMARY KEY,
    AgreementVersionId  BIGINT NOT NULL,
    MessageRecipientId  NVARCHAR(20) NOT NULL,
    ProductPart         NVARCHAR(40) NOT NULL,
    Status              NVARCHAR(15) NOT NULL,
    CreatedAt           DATETIME2    NOT NULL,
    ActivatedAt         DATETIME2    NULL,
    CancelledAt         DATETIME2    NULL,

    CONSTRAINT FK_AgreementScope_Version
        FOREIGN KEY (AgreementVersionId) REFERENCES CAMT.AgreementVersion(Id),

    CONSTRAINT FK_AgreementScope_Status
        FOREIGN KEY (Status) REFERENCES CAMT.AgreementScopeStatus(Code),

    CONSTRAINT FK_AgreementScope_ProductPart
        FOREIGN KEY (ProductPart) REFERENCES CAMT.ProductPart(Code)
);
GO

-- No duplicate scopes per version
CREATE UNIQUE INDEX CAMT.UX_AgreementScope_Unique
ON CAMT.AgreementScope (AgreementVersionId, MessageRecipientId, ProductPart);
GO

-- =========================================================
-- TABLE: PaymentType (REFERENCE TABLE)
-- =========================================================
CREATE TABLE CAMT.PaymentType (
    Code        NVARCHAR(40) PRIMARY KEY,
    Description NVARCHAR(100) NOT NULL
);
GO

-- =========================================================
-- TABLE: PaymentTypeAssignment
-- =========================================================
CREATE TABLE CAMT.PaymentTypeAssignment (
    Id              BIGINT IDENTITY PRIMARY KEY,
    AgreementScopeId BIGINT NOT NULL,
    PaymentType     NVARCHAR(40) NOT NULL,
    CreatedAt       DATETIME2    NOT NULL,

    CONSTRAINT FK_PaymentTypeAssignment_Scope
        FOREIGN KEY (AgreementScopeId) REFERENCES CAMT.AgreementScope(Id),

    CONSTRAINT FK_PaymentTypeAssignment_PaymentType
        FOREIGN KEY (PaymentType) REFERENCES CAMT.PaymentType(Code)
);
GO

-- Unique payment type per scope
CREATE UNIQUE INDEX CAMT.UX_PaymentTypeAssignment_Unique
ON CAMT.PaymentTypeAssignment (AgreementScopeId, PaymentType);
GO

-- =========================================================
-- TABLE: AccountAssignment
-- =========================================================
CREATE TABLE CAMT.AccountAssignment (
    Id                   BIGINT IDENTITY PRIMARY KEY,
    PaymentTypeAssignmentId BIGINT NOT NULL,
    AccountBBAN          NVARCHAR(15) NOT NULL,
    AccountIBAN          NVARCHAR(35) NOT NULL,
    Currency             NVARCHAR(3)  NOT NULL,
    CreatedAt            DATETIME2    NOT NULL,

    CONSTRAINT FK_AccountAssignment_PaymentTypeAssignment
        FOREIGN KEY (PaymentTypeAssignmentId) REFERENCES CAMT.PaymentTypeAssignment(Id)
);
GO

-- =========================================================
-- TABLE: AliasAssignment
-- =========================================================
CREATE TABLE CAMT.AliasAssignment (
    Id                   BIGINT IDENTITY PRIMARY KEY,
    PaymentTypeAssignmentId BIGINT NOT NULL,
    AliasId              NVARCHAR(15) NOT NULL,
    CreatedAt            DATETIME2    NOT NULL,

    CONSTRAINT FK_AliasAssignment_PaymentTypeAssignment
        FOREIGN KEY (PaymentTypeAssignmentId) REFERENCES CAMT.PaymentTypeAssignment(Id)
);
GO

-- =========================================================
-- TABLE: CartItem
-- =========================================================
CREATE TABLE CAMT.CartItem (
    Id              BIGINT IDENTITY PRIMARY KEY,
    AgreementVersionId BIGINT NOT NULL,
    CorporateId     NVARCHAR(15) NOT NULL,
    ExpiresAt       DATETIME2    NOT NULL,
    ApprovedAt      DATETIME2    NULL,
    ExpiredAt       DATETIME2    NULL,
    CreatedAt       DATETIME2    NOT NULL,

    CONSTRAINT FK_CartItem_AgreementVersion
        FOREIGN KEY (AgreementVersionId) REFERENCES CAMT.AgreementVersion(Id)
);
GO

-- One cart item per version
CREATE UNIQUE INDEX CAMT.UX_CartItem_Version
ON CAMT.CartItem (AgreementVersionId);
GO

-- =========================================================
-- TABLE: ReportConfig
-- =========================================================
CREATE TABLE CAMT.ReportConfig (
    Id               BIGINT IDENTITY PRIMARY KEY,
    ReportType       NVARCHAR(40) NOT NULL,
    ReportVersion    NVARCHAR(3)  NOT NULL,
    ReportFrequency  NVARCHAR(35) NOT NULL,
    Description      NVARCHAR(80) NOT NULL,
    MessageRecipientId NVARCHAR(20) NOT NULL,
    MessageRecipientType NVARCHAR(15) NOT NULL,
    AccountFormat    NVARCHAR(4)  NOT NULL,
    IsActive         BIT NOT NULL DEFAULT 0,
    IsPaginated      BIT NOT NULL DEFAULT 0,
    IsEmptyReportAllowed BIT NOT NULL DEFAULT 0,
    IsBundled        BIT NOT NULL DEFAULT 0,
    CreatedAt        DATETIME2    NOT NULL,
    CreatedBy        NVARCHAR(20) NOT NULL,
    UpdatedAt        DATETIME2    NOT NULL,
    UpdatedBy        NVARCHAR(20) NOT NULL,

    CONSTRAINT FK_ReportConfig_ReportType
        FOREIGN KEY (ReportType) REFERENCES CAMT.ProductPart(Code)
);
GO

-- =========================================================
-- TABLE: ReportAgreementScope
-- =========================================================
CREATE TABLE CAMT.ReportAgreementScope (
    Id               BIGINT IDENTITY PRIMARY KEY,
    ReportConfigId   BIGINT NOT NULL,
    AgreementScopeId BIGINT NOT NULL,
    AgreementId      NVARCHAR(20) NOT NULL,

    CONSTRAINT FK_ReportAgreementScope_Config
        FOREIGN KEY (ReportConfigId) REFERENCES CAMT.ReportConfig(Id),

    CONSTRAINT FK_ReportAgreementScope_Scope
        FOREIGN KEY (AgreementScopeId) REFERENCES CAMT.AgreementScope(Id),

    CONSTRAINT FK_ReportAgreementScope_Agreement
        FOREIGN KEY (AgreementId) REFERENCES CAMT.Agreement(Id)
);
GO

-- Unique mapping
CREATE UNIQUE INDEX CAMT.UX_ReportAgreementScope_Unique
ON CAMT.ReportAgreementScope (ReportConfigId, AgreementScopeId);
GO

-- =========================================================
-- INDEXES (PERFORMANCE)
-- =========================================================
GO

-- Agreement / Version
CREATE INDEX CAMT.IX_AgreementVersion_Agreement
ON CAMT.AgreementVersion (AgreementId, Status);
GO

-- Scope
CREATE INDEX CAMT.IX_AgreementScope_Version
ON CAMT.AgreementScope (AgreementVersionId);
GO

CREATE INDEX CAMT.IX_AgreementScope_Recipient
ON CAMT.AgreementScope (MessageRecipientId);
GO

-- Cart expiration job
CREATE INDEX CAMT.IX_CartItem_ExpiresAt
ON CAMT.CartItem (ExpiresAt);
GO

-- Reporting linkage
CREATE INDEX CAMT.IX_ReportAgreementScope_Agreement
ON CAMT.ReportAgreementScope (AgreementId);
GO

CREATE INDEX CAMT.IX_ReportAgreementScope_Scope
ON CAMT.ReportAgreementScope (AgreementScopeId);
GO

-- Payment + Accounts
CREATE INDEX CAMT.IX_PaymentTypeAssignment_Scope
ON CAMT.PaymentTypeAssignment (AgreementScopeId);
GO

CREATE INDEX CAMT.IX_AccountAssignment_PaymentType
ON CAMT.AccountAssignment (PaymentTypeAssignmentId);
GO

CREATE INDEX CAMT.IX_ReportConfig_MessageRecipientId
ON CAMT.ReportConfig (MessageRecipientId);
GO
