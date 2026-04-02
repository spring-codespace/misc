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
