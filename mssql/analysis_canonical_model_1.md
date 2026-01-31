# CAMT Messages Analysis & Canonical Model Design

## Executive Summary

After analyzing the three CAMT XSD schemas (v02), I've identified significant structural similarities that make a unified canonical model highly feasible. The three message types share approximately **85-90% common structure** at the report/statement/notification level.

---

## 1. Message Type Overview

### CAMT.052 - Bank-to-Customer Account Report (Intraday)
- **Purpose**: Intraday account information
- **Root Element**: `BankToCustomerAccountReportV02`
- **Container**: `AccountReport11` (element name: `Rpt`)
- **Key Characteristic**: Real-time/intraday reporting

### CAMT.053 - Bank-to-Customer Statement (End of Day)
- **Purpose**: End-of-day account statement
- **Root Element**: `BankToCustomerStatementV02`
- **Container**: `AccountStatement2` (element name: `Stmt`)
- **Key Characteristic**: Balance is **mandatory** (minOccurs="1")

### CAMT.054 - Bank-to-Customer Debit/Credit Notification
- **Purpose**: Transaction notifications
- **Root Element**: `BankToCustomerDebitCreditNotificationV02`
- **Container**: `AccountNotification2` (element name: `Ntfctn`)
- **Key Characteristic**: No balance requirement, lightweight notification

---

## 2. Structural Comparison

### 2.1 High-Level Structure (100% Common)

All three messages share identical top-level structure:

```
Document
└── [MessageType]V02
    ├── GroupHeader42 (GrpHdr) - IDENTICAL
    └── [Report/Statement/Notification] (1..unbounded)
```

**GroupHeader42 Elements** (Shared by all):
- MsgId (Message ID)
- CreDtTm (Creation DateTime)
- MsgRcpt (Message Recipient) - optional
- MsgPgntn (Message Pagination) - optional
- AddtlInf (Additional Info) - optional

### 2.2 Report/Statement/Notification Level (90% Common)

| Element | AccountReport11<br>(052) | AccountStatement2<br>(053) | AccountNotification2<br>(054) | Notes |
|---------|----------|-------------|--------------|-------|
| **Id** | ✓ Required | ✓ Required | ✓ Required | Report/Statement/Notification ID |
| **ElctrncSeqNb** | ✓ Optional | ✓ Optional | ✓ Optional | Electronic Sequence Number |
| **LglSeqNb** | ✓ Optional | ✓ Optional | ✓ Optional | Legal Sequence Number |
| **CreDtTm** | ✓ Required | ✓ Required | ✓ Required | Creation DateTime |
| **FrToDt** | ✓ Optional | ✓ Optional | ✓ Optional | From-To Date Period |
| **CpyDplctInd** | ✓ Optional | ✓ Optional | ✓ Optional | Copy/Duplicate Indicator |
| **RptgSrc** | ✓ Optional | ✓ Optional | ✓ Optional | Reporting Source |
| **Acct** | ✓ Required | ✓ Required | ✓ Required | Account (CashAccount20) |
| **RltdAcct** | ✓ Optional | ✓ Optional | ✓ Optional | Related Account |
| **Intrst** | ✓ Optional[] | ✓ Optional[] | ✓ Optional[] | Interest (0..unbounded) |
| **Bal** | ✓ Optional[] | ✓ **Required[]** | ❌ Not present | **KEY DIFFERENCE** |
| **TxsSummry** | ✓ Optional | ✓ Optional | ✓ Optional | Transaction Summary |
| **Ntry** | ✓ Optional[] | ✓ Optional[] | ✓ Optional[] | Entries (0..unbounded) |
| **AddtlInfo** | AddtlRptInf | AddtlStmtInf | AddtlNtfctnInf | **Different field names** |

**Key Observations:**
1. **Balance (Bal)**: 
   - 052: Optional (0..unbounded)
   - 053: **Mandatory** (1..unbounded) - Must have at least one balance
   - 054: **Not present** - Notifications don't include balances
   
2. **Additional Info**: Same type (Max500Text), different element names
3. **Entry Structure (Ntry)**: Uses identical `ReportEntry2` type across all three

### 2.3 Entry Level (ReportEntry2) - 100% Identical

The transaction entry structure `ReportEntry2` is **completely identical** across all three message types:

**ReportEntry2 Elements:**
- NtryRef (Entry Reference)
- Amt (Amount) - Required
- CdtDbtInd (Credit/Debit Indicator) - Required
- RvslInd (Reversal Indicator)
- Sts (Status) - Required
- BookgDt (Booking Date)
- ValDt (Value Date)
- AcctSvcrRef (Account Servicer Reference)
- Avlbty (Availability) - 0..unbounded
- BkTxCd (Bank Transaction Code) - Required
- ComssnWvrInd (Commission Waiver Indicator)
- AddtlInfInd (Additional Info Indicator)
- AmtDtls (Amount Details)
- Chrgs (Charges) - 0..unbounded
- TechInptChanl (Technical Input Channel)
- Intrst (Interest) - 0..unbounded
- NtryDtls (Entry Details) - 0..unbounded
- AddtlNtryInf (Additional Entry Info)

---

## 3. Recommended Canonical Model Structure

### 3.1 Design Principles

1. **Single Source of Truth**: One canonical model for all CAMT types
2. **Version Agnostic**: Abstract from version-specific details
3. **Superset Approach**: Include all possible fields from all versions
4. **Business-Oriented**: Structure around business concepts, not ISO20022 technical naming
5. **Nullable/Optional Fields**: Use Optional<> or nullable types for version differences
6. **Mapper Pattern**: Separate mappers for each version and message type

### 3.2 Proposed Canonical Model Layers

```
┌─────────────────────────────────────────────────┐
│  Database Layer (Stored Procedures)              │
└───────────────┬─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│  Canonical Model (Business Objects)              │
│  - CamtDocument                                  │
│  - Header (GroupHeader)                          │
│  - AccountReport (unified)                       │
│  - Entry                                         │
│  - Balance                                       │
│  - Party, Account, etc.                          │
└───────────────┬─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│  ISO20022 Mappers (Version + Type Specific)     │
│  - Camt052V02Mapper                              │
│  - Camt053V02Mapper                              │
│  - Camt054V02Mapper                              │
│  - Camt052V08Mapper                              │
│  - ... (future versions)                         │
└───────────────┬─────────────────────────────────┘
                │
                ▼
┌─────────────────────────────────────────────────┐
│  ISO20022 Generated Classes (JAXB)              │
│  - One package per version                       │
└─────────────────────────────────────────────────┘
```

### 3.3 Core Canonical Classes (Proposed)

#### **1. CamtDocument** (Top Level)
```java
class CamtDocument {
    - MessageType messageType          // ENUM: CAMT_052, CAMT_053, CAMT_054
    - String messageVersion            // "02", "08", etc.
    - Header header                    // Group header info
    - List<AccountReport> reports      // Unified reports/statements/notifications
}
```

#### **2. Header** (GroupHeader)
```java
class Header {
    - String messageId
    - LocalDateTime creationDateTime
    - Party messageRecipient           // Optional
    - Pagination pagination            // Optional
    - String additionalInfo            // Optional
}
```

#### **3. AccountReport** (Unified Report/Statement/Notification)
```java
class AccountReport {
    - String id
    - Long electronicSequenceNumber    // Optional
    - Long legalSequenceNumber         // Optional
    - LocalDateTime creationDateTime
    - DateTimePeriod period            // Optional (from/to)
    - CopyDuplicateIndicator indicator // Optional
    - ReportingSource reportingSource  // Optional
    - Account account                  // Required
    - Account relatedAccount           // Optional
    - List<Interest> interests         // Optional
    - List<Balance> balances           // Optional (mandatory for 053)
    - TransactionSummary summary       // Optional
    - List<Entry> entries              // Optional
    - String additionalInformation     // Optional
}
```

#### **4. Entry** (Transaction Entry)
```java
class Entry {
    - String entryReference            // Optional
    - Amount amount                    // Required
    - CreditDebitIndicator cdtDbtInd   // Required: CRDT or DBIT
    - Boolean reversalIndicator        // Optional
    - EntryStatus status               // Required: BOOK, PDNG, INFO
    - LocalDate bookingDate            // Optional
    - LocalDate valueDate              // Optional
    - String accountServicerReference  // Optional
    - List<Availability> availabilities // Optional
    - BankTransactionCode bankTxCode   // Required
    - Boolean commissionWaiverInd      // Optional
    - AmountDetails amountDetails      // Optional
    - List<Charges> charges            // Optional
    - TechnicalInputChannel channel    // Optional
    - List<TransactionInterest> interests // Optional
    - List<EntryDetails> entryDetails  // Optional
    - String additionalInfo            // Optional
}
```

#### **5. Balance**
```java
class Balance {
    - BalanceType type                 // ENUM: OPBD, CLBD, ITBD, etc.
    - Amount amount                    // Required
    - CreditDebitIndicator cdtDbtInd   // Required
    - LocalDate date                   // Required
    - List<Availability> availabilities // Optional
}
```

#### **6. Supporting Classes**
```java
class Amount {
    - BigDecimal value
    - String currency                  // ISO 4217 (e.g., "EUR", "USD")
}

class Account {
    - AccountIdentification identification  // IBAN or Other
    - String accountType               // Optional
    - String currency                  // Optional
    - String accountName               // Optional
    - Party owner                      // Optional
    - Party servicer                   // Optional
}

class Party {
    - String name
    - PostalAddress address            // Optional
    - AccountIdentification identification // Optional
    - String countryOfResidence        // Optional
}

class BankTransactionCode {
    - Domain domain                    // Optional
    - String proprietaryCode           // Optional
}
```

---

## 4. Mapping Strategy

### 4.1 Database to Canonical (Input Mapper)

**Stored Procedure Output → Canonical Model**

Each stored procedure will have its own mapper:
- `Camt052StoredProcMapper`
- `Camt053StoredProcMapper`
- `Camt054StoredProcMapper`

These mappers will:
1. Parse stored procedure result sets
2. Handle NULL values appropriately
3. Create canonical objects with business validations
4. Set message type automatically

### 4.2 Canonical to ISO20022 (Output Mapper)

**Canonical Model → ISO20022 JAXB Classes**

Version and type-specific mappers:
- `Camt052V02Mapper extends AbstractCamtMapper`
- `Camt053V02Mapper extends AbstractCamtMapper`
- `Camt054V02Mapper extends AbstractCamtMapper`
- `Camt052V08Mapper extends AbstractCamtMapper`
- etc.

Each mapper will:
1. Transform canonical model to version-specific JAXB classes
2. Handle version differences (new/removed fields)
3. Apply ISO20022 validation rules
4. Generate proper XML namespaces

### 4.3 Mapper Factory Pattern

```java
interface CamtMapperFactory {
    CamtMapper getMapper(MessageType type, String version);
}

// Usage:
CamtMapper mapper = factory.getMapper(CAMT_052, "08");
Document052V08 isoDocument = mapper.toIso20022(canonicalDoc);
```

---

## 5. Key Design Decisions

### 5.1 Handle Balance Differences

**Problem**: Balance is:
- Optional in 052
- Mandatory in 053
- Absent in 054

**Solution**: 
```java
class AccountReport {
    private List<Balance> balances;  // Always present in canonical
    
    // Validation at mapper level
    // 053 mapper: validates at least one balance exists
    // 054 mapper: ignores balances completely
}
```

### 5.2 Handle Additional Info Field Name Variations

**Problem**: Three different field names for same concept:
- 052: `AddtlRptInf`
- 053: `AddtlStmtInf`
- 054: `AddtlNtfctnInf`

**Solution**: Single canonical field, mappers handle naming:
```java
class AccountReport {
    private String additionalInformation;  // Generic name
}

// In mapper:
// 052: maps to AddtlRptInf
// 053: maps to AddtlStmtInf
// 054: maps to AddtlNtfctnInf
```

### 5.3 Version Evolution Strategy

When new versions (v08, v09, etc.) are released:

1. **Generate new JAXB classes** from new XSD
2. **Compare with canonical model** - identify new/changed fields
3. **Update canonical model** if genuinely new business concepts exist
4. **Create new mapper** (e.g., `Camt052V09Mapper`)
5. **Keep old mappers unchanged** - backward compatibility maintained

### 5.4 Enum Strategy

Use Java enums for controlled vocabularies:
```java
enum MessageType {
    CAMT_052,  // Account Report
    CAMT_053,  // Statement
    CAMT_054   // Notification
}

enum EntryStatus {
    BOOK,   // Booked
    PDNG,   // Pending
    INFO    // Information
}

enum CreditDebitIndicator {
    CRDT,   // Credit
    DBIT    // Debit
}
```

---

## 6. Benefits of This Approach

### 6.1 Maintainability
✓ Single canonical model to maintain
✓ Version changes isolated to mappers
✓ Database changes don't affect ISO20022 generation

### 6.2 Testability
✓ Test canonical model independently
✓ Test mappers with canonical fixtures
✓ Test stored procedures separately

### 6.3 Flexibility
✓ Easy to add new versions (just new mapper)
✓ Easy to support new message types
✓ Can generate multiple versions from same canonical instance

### 6.4 Business Logic Separation
✓ Business rules operate on canonical model
✓ ISO20022 is just a serialization format
✓ Can add other output formats (JSON, CSV) easily

### 6.5 Future-Proofing
✓ New ISO20022 versions: add new mapper
✓ New database schema: update input mapper
✓ New message types: extend canonical model minimally

---

## 7. Implementation Roadmap

### Phase 1: Foundation
1. Define canonical model classes (core entities)
2. Create base mapper interfaces and abstract classes
3. Set up JAXB generation from XSDs

### Phase 2: Version 02 Implementation
1. Implement Camt052V02Mapper
2. Implement Camt053V02Mapper
3. Implement Camt054V02Mapper
4. Implement stored procedure input mappers

### Phase 3: Version 08 Support
1. Generate JAXB classes from v08 XSDs
2. Analyze differences vs v02
3. Extend canonical model if needed
4. Implement v08 mappers

### Phase 4: Testing & Validation
1. Unit tests for each mapper
2. Integration tests with actual stored procedures
3. ISO20022 XML validation against XSDs
4. Performance testing

### Phase 5: Production & Monitoring
1. Deployment
2. Monitoring and logging
3. Error handling and reporting

---

## 8. Technology Stack Recommendations

### Core Technologies
- **Java 17+**: For records, pattern matching, modern features
- **Spring Boot**: Dependency injection, configuration
- **JAXB**: XML binding (generate classes from XSD)
- **MapStruct**: For complex mappings (optional but recommended)
- **Lombok**: Reduce boilerplate (optional)

### XML Handling
- **JAXB (Jakarta XML Binding)**: Primary choice
- **XJC**: Generate Java classes from XSD
- **XML Schema Validation**: Built-in validation

### Testing
- **JUnit 5**: Unit testing
- **AssertJ**: Fluent assertions
- **Mockito**: Mocking
- **XMLUnit**: XML comparison in tests

### Build & Generation
- **Maven/Gradle**: Build tool
- **JAXB Maven Plugin**: Automatic JAXB class generation
- **Separate packages per version**: `com.yourapp.iso20022.camt.v02`, `...v08`

---

## 9. Critical Considerations

### 9.1 Mandatory vs Optional Fields
- Canonical model: All fields optional unless business-critical
- Validation happens at mapper level (version-specific)
- Clear documentation of which fields are required per message type/version

### 9.2 Data Type Precision
- Use `BigDecimal` for amounts (never `double`/`float`)
- Use `LocalDate`/`LocalDateTime` for dates
- Use `String` for codes (with enum validation)

### 9.3 Performance
- Lazy loading for large collections where appropriate
- Streaming for large reports (if needed)
- Connection pooling for database access
- XML generation optimization (reuse JAXB context)

### 9.4 Error Handling
- Clear exception hierarchy
- Validation exceptions with field-level details
- Mapping exceptions with source/target info
- Business rule violations vs technical errors

### 9.5 Documentation
- JavaDoc for all canonical classes
- Mapping documentation (canonical ↔ ISO20022)
- Version differences documented
- Business rules documented

---

## 10. Next Steps & Questions to Address

Before starting implementation, clarify:

1. **Stored Procedure Structure**
   - What columns do they return?
   - Are there separate SPs per version, or version-agnostic?
   - What's the data volume per report?

2. **Business Requirements**
   - Do you need to generate multiple versions simultaneously?
   - Are there custom validations beyond ISO20022 standard?
   - What's the error handling strategy (fail fast vs collect all errors)?

3. **Technical Environment**
   - Spring Boot version?
   - Database type (Oracle, SQL Server, PostgreSQL)?
   - Java version constraints?
   - Existing libraries/frameworks in use?

4. **Non-Functional Requirements**
   - Expected throughput (reports per second)?
   - Maximum report size?
   - Response time requirements?
   - Logging/auditing requirements?

5. **Future Roadmap**
   - Timeline for v08 support?
   - Other CAMT types planned (055, 056, etc.)?
   - Other ISO20022 message types (pacs, pain)?

---

## Conclusion

The canonical model approach is **highly recommended** for your use case because:

1. **High structural similarity** (85-90%) between CAMT types
2. **Version evolution** is predictable and incremental
3. **Separation of concerns** provides flexibility
4. **Testability** is greatly improved
5. **Maintenance burden** is minimized

The main differences (balance cardinality, field names) are easily handled through the mapper pattern, while the vast similarities are leveraged through the shared canonical model.

Would you like me to proceed with:
- **Detailed canonical model class definitions** (with all fields)?
- **Example mapper implementation** (one complete mapper)?
- **Stored procedure result set mapping strategy**?
- **All of the above**?
