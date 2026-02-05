# CAMT ISO20022 Model Design - Based on XSD Analysis

## Analysis Summary

After analyzing your XSD files (camt.052.001.08, camt.053.001.08, camt.054.001.08), here's what I found:

### Message Structure Comparison

| CAMT Type | Root Element | Main Container | Container Element Name | Key Differences |
|-----------|--------------|----------------|----------------------|-----------------|
| **052** (Account Report) | `BkToCstmrAcctRpt` | `AccountReport25` | `Rpt` | Used for **intraday** account reporting |
| **053** (Statement) | `BkToCstmrStmt` | `AccountStatement9` | `Stmt` | Used for **end-of-day** statements, requires at least 1 balance (`Bal` minOccurs="1") |
| **054** (Notification) | `BkToCstmrDbtCdtNtfctn` | `AccountNotification17` | `Ntfctn` | Used for **real-time** debit/credit notifications, balances optional |

### Common Structure (All 3 Types)

All three message types share:
```
Document
  └─ Message (BkToCstmrAcctRpt/BkToCstmrStmt/BkToCstmrDbtCdtNtfctn)
      ├─ GrpHdr (GroupHeader81) - Message-level header
      ├─ Report/Statement/Notification (1..unbounded)
      │   ├─ Id
      │   ├─ Pagination info
      │   ├─ Acct (CashAccount39) - Account details
      │   ├─ Bal (CashBalance8) - Balances
      │   ├─ TxsSummry (TotalTransactions6) - Transaction summary
      │   ├─ Ntry (ReportEntry10) - Individual entries/transactions
      │   └─ Additional info
      └─ SplmtryData (optional)
```

### Key Observations

1. **Structural Similarity**: All three messages are ~95% identical in structure
2. **Main Differences**:
   - Element names: `Rpt` vs `Stmt` vs `Ntfctn`
   - Balance requirements: 053 requires at least 1 balance, others are optional
   - Additional info field names: `AddtlRptInf` vs `AddtlStmtInf` vs `AddtlNtfctnInf`
3. **Common Complex Types**: They all use the same:
   - `GroupHeader81`
   - `CashAccount39`
   - `CashBalance8`
   - `ReportEntry10`
   - `EntryDetails9`
   - Transaction details structures

## Recommended Canonical Model Strategy

Given the high structural similarity, I recommend a **HYBRID APPROACH**:

### Option 1: Single Unified Canonical Model (RECOMMENDED)

**Benefits:**
- Maximum code reuse (~95% shared structure)
- Single set of mappers from DB
- Easier to maintain
- Single transformation logic for shared elements

**Structure:**
```
canonical/
├── common/
│   ├── CamtGroupHeader.java
│   ├── CamtAccount.java
│   ├── CamtBalance.java
│   ├── CamtEntry.java (transaction)
│   ├── CamtEntryDetails.java
│   ├── CamtParty.java
│   ├── CamtAmount.java
│   └── CamtTransactionSummary.java
│
├── CamtReportData.java  // Single unified model
│   - contains: header, List<CamtReportItem>
│   - ReportType enum (REPORT_052, STATEMENT_053, NOTIFICATION_054)
│
└── CamtReportItem.java  // Represents Rpt/Stmt/Ntfctn
    - contains: account, balances, entries, summary
```

### Option 2: Separate Models with Shared Base (Alternative)

If you prefer type safety for each message type:

```
canonical/
├── common/ (same as Option 1)
├── base/
│   └── BaseCamtReportItem.java  // Abstract base class
│
├── Camt052ReportData.java
│   └── Camt052ReportItem.java extends BaseCamtReportItem
│
├── Camt053StatementData.java
│   └── Camt053StatementItem.java extends BaseCamtReportItem
│
└── Camt054NotificationData.java
    └── Camt054NotificationItem.java extends BaseCamtReportItem
```

## Detailed Class Design (Option 1 - Recommended)

### 1. Canonical Model Classes

```java
// Common components
package com.yourcompany.camt.model.canonical.common;

@Data
@Builder
public class CamtGroupHeader {
    private String messageId;
    private LocalDateTime creationDateTime;
    private CamtParty messageRecipient;
    private Pagination pagination;
    private String additionalInfo;
}

@Data
@Builder
public class CamtAccount {
    private String iban;
    private String otherIdentification;
    private String accountName;
    private String currency;
    private CamtParty owner;
    private CamtParty servicer;
}

@Data
@Builder
public class CamtBalance {
    private BalanceType type;  // OPBD, CLBD, PRCD, etc.
    private CamtAmount amount;
    private LocalDateTime date;
    private CreditDebitIndicator creditDebitIndicator;
}

@Data
@Builder
public class CamtEntry {
    private String entryReference;
    private CamtAmount amount;
    private CreditDebitIndicator creditDebitIndicator;
    private EntryStatus status;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private String bankTransactionCode;
    private List<CamtEntryDetails> entryDetails;
    private String additionalInfo;
}

@Data
@Builder
public class CamtEntryDetails {
    private CamtAmount amount;
    private CamtParty debtor;
    private CamtParty creditor;
    private String remittanceInformation;
    private String endToEndId;
    private String transactionId;
}

// Main unified model
package com.yourcompany.camt.model.canonical;

@Data
@Builder
public class CamtReportData {
    private CamtReportType reportType;  // REPORT_052, STATEMENT_053, NOTIFICATION_054
    private CamtGroupHeader groupHeader;
    private List<CamtReportItem> reportItems;
    private String version;  // "001.02" or "001.08"
}

@Data
@Builder
public class CamtReportItem {
    private String id;
    private Pagination pagination;
    private LocalDateTime creationDateTime;
    private DateRange fromToDate;
    private CamtAccount account;
    private CamtAccount relatedAccount;
    private List<CamtBalance> balances;
    private CamtTransactionSummary transactionSummary;
    private List<CamtEntry> entries;
    private String additionalInfo;
}

public enum CamtReportType {
    ACCOUNT_REPORT_052,
    STATEMENT_053,
    NOTIFICATION_054
}
```

### 2. Database Row Mappers

Since you're using stored procedures, create RowMappers:

```java
package com.yourcompany.camt.mapper;

@Component
public class CamtRowMapper {
    
    /**
     * Maps stored procedure result set to canonical model
     * Assumes your SP returns related data in multiple result sets
     */
    public CamtReportData mapStoredProcedureResults(
            CamtReportType reportType,
            SqlParameterSource params) {
        
        // Execute SP and get multiple result sets
        Map<String, Object> results = jdbcTemplate.call(
            connection -> {
                CallableStatement cs = connection.prepareCall("{call sp_get_camt_data(?)}");
                cs.setString(1, reportType.name());
                return cs;
            },
            List.of(
                new SqlReturnResultSet("header", this::mapHeader),
                new SqlReturnResultSet("accounts", this::mapAccounts),
                new SqlReturnResultSet("balances", this::mapBalances),
                new SqlReturnResultSet("entries", this::mapEntries)
            )
        );
        
        // Build canonical model
        return CamtReportData.builder()
            .reportType(reportType)
            .groupHeader(mapToGroupHeader(results.get("header")))
            .reportItems(buildReportItems(results))
            .build();
    }
    
    private CamtGroupHeader mapHeader(ResultSet rs, int rowNum) throws SQLException {
        return CamtGroupHeader.builder()
            .messageId(rs.getString("message_id"))
            .creationDateTime(rs.getTimestamp("creation_date_time").toLocalDateTime())
            .additionalInfo(rs.getString("additional_info"))
            .build();
    }
    
    private CamtAccount mapAccounts(ResultSet rs, int rowNum) throws SQLException {
        return CamtAccount.builder()
            .iban(rs.getString("iban"))
            .accountName(rs.getString("account_name"))
            .currency(rs.getString("currency"))
            .build();
    }
    
    // Similar methods for balances, entries, etc.
}
```

### 3. Version-Specific Transformers

```java
package com.yourcompany.camt.transformer;

public interface CamtTransformer<T> {
    T transform(CamtReportData canonicalData);
    String getVersion();
    CamtReportType getSupportedType();
}

// Example V08 implementation for camt.052
package com.yourcompany.camt.transformer.v08;

@Component
public class Camt052TransformerV08 implements CamtTransformer<Document> {
    
    @Override
    public Document transform(CamtReportData canonicalData) {
        validate(canonicalData, CamtReportType.ACCOUNT_REPORT_052);
        
        Document document = new Document();
        BankToCustomerAccountReportV08 report = new BankToCustomerAccountReportV08();
        
        // Map GroupHeader
        report.setGrpHdr(transformGroupHeader(canonicalData.getGroupHeader()));
        
        // Map Reports
        report.getRpt().addAll(
            canonicalData.getReportItems().stream()
                .map(this::transformAccountReport)
                .collect(Collectors.toList())
        );
        
        document.setBkToCstmrAcctRpt(report);
        return document;
    }
    
    private GroupHeader81 transformGroupHeader(CamtGroupHeader canonical) {
        GroupHeader81 header = new GroupHeader81();
        header.setMsgId(canonical.getMessageId());
        header.setCreDtTm(
            DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(canonical.getCreationDateTime().toString())
        );
        return header;
    }
    
    private AccountReport25 transformAccountReport(CamtReportItem item) {
        AccountReport25 report = new AccountReport25();
        report.setId(item.getId());
        report.setAcct(transformAccount(item.getAccount()));
        report.getBal().addAll(transformBalances(item.getBalances()));
        report.getNtry().addAll(transformEntries(item.getEntries()));
        return report;
    }
    
    private CashAccount39 transformAccount(CamtAccount canonical) {
        CashAccount39 account = new CashAccount39();
        
        AccountIdentification4Choice id = new AccountIdentification4Choice();
        id.setIBAN(canonical.getIban());
        account.setId(id);
        
        account.setCcy(canonical.getCurrency());
        // ... more mappings
        
        return account;
    }
    
    // Similar transform methods for balances, entries, etc.
    
    @Override
    public String getVersion() {
        return "001.08";
    }
    
    @Override
    public CamtReportType getSupportedType() {
        return CamtReportType.ACCOUNT_REPORT_052;
    }
}
```

### 4. Transformer Factory

```java
package com.yourcompany.camt.transformer;

@Component
public class CamtTransformerFactory {
    
    private final Map<String, CamtTransformer<?>> transformers;
    
    @Autowired
    public CamtTransformerFactory(List<CamtTransformer<?>> allTransformers) {
        this.transformers = allTransformers.stream()
            .collect(Collectors.toMap(
                t -> t.getVersion() + "_" + t.getSupportedType(),
                Function.identity()
            ));
    }
    
    public CamtTransformer<?> getTransformer(String version, CamtReportType type) {
        String key = version + "_" + type;
        CamtTransformer<?> transformer = transformers.get(key);
        
        if (transformer == null) {
            throw new UnsupportedOperationException(
                "No transformer found for version: " + version + ", type: " + type
            );
        }
        
        return transformer;
    }
}
```

### 5. Service Layer

```java
package com.yourcompany.camt.service;

@Service
public class CamtReportService {
    
    private final CamtRowMapper rowMapper;
    private final CamtTransformerFactory transformerFactory;
    private final CamtXmlGenerator xmlGenerator;
    private final CamtVersionConfig versionConfig;
    
    public String generateReport(CamtReportType reportType, String accountId) {
        // 1. Fetch data from database
        CamtReportData canonicalData = rowMapper.mapStoredProcedureResults(
            reportType, 
            new MapSqlParameterSource("accountId", accountId)
        );
        
        // 2. Get configured version
        String version = versionConfig.getVersion(reportType);
        canonicalData.setVersion(version);
        
        // 3. Transform to XSD model
        CamtTransformer<?> transformer = transformerFactory.getTransformer(version, reportType);
        Object xsdModel = transformer.transform(canonicalData);
        
        // 4. Generate XML
        return xmlGenerator.generateXml(xsdModel, reportType, version);
    }
}
```

### 6. XML Generator

```java
package com.yourcompany.camt.generator;

@Component
public class CamtXmlGenerator {
    
    public String generateXml(Object xsdModel, CamtReportType type, String version) {
        try {
            JAXBContext context = getJaxbContext(type, version);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            
            StringWriter writer = new StringWriter();
            marshaller.marshal(xsdModel, writer);
            
            return writer.toString();
        } catch (JAXBException e) {
            throw new CamtGenerationException("Failed to generate XML", e);
        }
    }
    
    private JAXBContext getJaxbContext(CamtReportType type, String version) 
            throws JAXBException {
        String packageName = String.format(
            "com.yourcompany.camt.model.generated.v%s.camt%s",
            version.replace(".", ""),
            type.getCode()  // "052", "053", or "054"
        );
        return JAXBContext.newInstance(packageName);
    }
}
```

### 7. Configuration

```java
package com.yourcompany.camt.config;

@Configuration
@ConfigurationProperties(prefix = "camt")
public class CamtVersionConfig {
    
    private Map<String, String> versions = new HashMap<>();
    
    public String getVersion(CamtReportType type) {
        return versions.getOrDefault(
            type.name(), 
            "001.08"  // default version
        );
    }
    
    // Getters and setters
}
```

**application.yml:**
```yaml
camt:
  versions:
    ACCOUNT_REPORT_052: "001.08"
    STATEMENT_053: "001.08"
    NOTIFICATION_054: "001.08"
```

## Project Structure

```
src/main/java/com/yourcompany/camt/
├── model/
│   ├── canonical/
│   │   ├── common/
│   │   │   ├── CamtGroupHeader.java
│   │   │   ├── CamtAccount.java
│   │   │   ├── CamtBalance.java
│   │   │   ├── CamtEntry.java
│   │   │   ├── CamtEntryDetails.java
│   │   │   ├── CamtParty.java
│   │   │   ├── CamtAmount.java
│   │   │   └── CamtTransactionSummary.java
│   │   ├── CamtReportData.java
│   │   ├── CamtReportItem.java
│   │   └── enums/
│   │       ├── CamtReportType.java
│   │       ├── BalanceType.java
│   │       ├── CreditDebitIndicator.java
│   │       └── EntryStatus.java
│   │
│   └── generated/  # XJC generated classes
│       ├── v02/
│       │   ├── camt052/
│       │   ├── camt053/
│       │   └── camt054/
│       └── v08/
│           ├── camt052/
│           ├── camt053/
│           └── camt054/
│
├── mapper/
│   └── CamtRowMapper.java
│
├── transformer/
│   ├── CamtTransformer.java (interface)
│   ├── CamtTransformerFactory.java
│   ├── v02/
│   │   ├── Camt052TransformerV02.java
│   │   ├── Camt053TransformerV02.java
│   │   └── Camt054TransformerV02.java
│   └── v08/
│       ├── Camt052TransformerV08.java
│       ├── Camt053TransformerV08.java
│       └── Camt054TransformerV08.java
│
├── generator/
│   └── CamtXmlGenerator.java
│
├── service/
│   └── CamtReportService.java
│
├── config/
│   └── CamtVersionConfig.java
│
└── exception/
    └── CamtGenerationException.java
```

## Benefits of This Approach

1. ✅ **Single canonical model** - 95% code reuse across all CAMT types
2. ✅ **Version flexibility** - Easy to add v09, v10, etc.
3. ✅ **Type safety** - Enum-based type discrimination
4. ✅ **Testability** - Each layer independently testable
5. ✅ **Maintainability** - Clear separation of concerns
6. ✅ **Configuration-driven** - Version selection via config
7. ✅ **Extensibility** - New transformers auto-discovered via Spring

## Migration Path for Future Versions

To add a new version (e.g., 001.09):

1. Generate Java classes from new XSD using XJC
2. Create new transformer classes (`Camt052TransformerV09`, etc.)
3. Update configuration to use new version
4. No changes needed to canonical model or service layer

## Next Steps

Would you like me to:
1. Generate complete working code for any specific classes?
2. Create unit tests for the transformer logic?
3. Design the stored procedure result set structure?
4. Create a sample Spring Boot application configuration?
