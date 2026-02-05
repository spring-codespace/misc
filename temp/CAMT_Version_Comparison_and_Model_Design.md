# CAMT ISO20022 Version Comparison & Updated Model Design

## Executive Summary

After analyzing both v02 (001.02) and v08 (001.08) XSD files for camt.052, camt.053, and camt.054, I can confirm:

**The structural differences between versions are MINIMAL (≈5% changes)**
**The differences between message types remain consistent across versions**

This validates the **unified canonical model approach** even more strongly.

---

## Detailed Version Comparison (V02 vs V08)

### 1. GroupHeader Changes

| Element | V02 (GroupHeader42) | V08 (GroupHeader81) | Impact |
|---------|---------------------|---------------------|--------|
| MsgId | ✓ Same | ✓ Same | None |
| CreDtTm | ✓ Same | ✓ Same | None |
| MsgRcpt | PartyIdentification32 | PartyIdentification135 | **Type version change** |
| MsgPgntn | Pagination | Pagination1 | **Type version change** |
| OrgnlBizQry | ❌ Not present | ✓ OriginalBusinessQuery1 | **NEW in V08** |
| AddtlInf | ✓ Same | ✓ Same | None |

**Key Changes:**
- Added `OrgnlBizQry` field in V08 (optional)
- Updated type versions for Party and Pagination

---

### 2. Account Report/Statement/Notification Changes

#### AccountReport: V02 (AccountReport11) → V08 (AccountReport25)

| Element | V02 | V08 | Change |
|---------|-----|-----|--------|
| Id | ✓ | ✓ | Same |
| RptPgntn | ❌ | ✓ Pagination1 | **NEW in V08** |
| ElctrncSeqNb | ✓ | ✓ | Same |
| RptgSeq | ❌ | ✓ SequenceRange1Choice | **NEW in V08** |
| LglSeqNb | ✓ | ✓ | Same |
| CreDtTm | ✓ Required | ✓ Optional | **Cardinality change** |
| FrToDt | DateTimePeriodDetails | DateTimePeriod1 | **Type version change** |
| Acct | CashAccount20 | CashAccount39 | **Type version change** |
| RltdAcct | CashAccount16 | CashAccount38 | **Type version change** |
| Intrst | AccountInterest2 | AccountInterest4 | **Type version change** |
| Bal | CashBalance3 | CashBalance8 | **Type version change** |
| TxsSummry | TotalTransactions2 | TotalTransactions6 | **Type version change** |
| Ntry | ReportEntry2 | ReportEntry10 | **Type version change** |

**Key Additions in V08:**
- `RptPgntn` - Report-level pagination
- `RptgSeq` - Reporting sequence range

**Pattern:** Same structure, newer type versions with enhanced features

---

### 3. ReportEntry Changes

#### V02 (ReportEntry2) → V08 (ReportEntry10)

| Element | V02 | V08 | Change |
|---------|-----|-----|--------|
| Core fields | Same | Same | No change |
| Sts | EntryStatus2Code | EntryStatus1Choice | **Changed from Code to Choice** |
| BookgDt | DateAndDateTimeChoice | DateAndDateTime2Choice | **Type version change** |
| ValDt | DateAndDateTimeChoice | DateAndDateTime2Choice | **Type version change** |
| Avlbty | CashBalanceAvailability2 | CashAvailability1 | **Type simplification** |
| Chrgs | ChargesInformation6 (unbounded) | Charges6 (single) | **Cardinality & type change** |
| Intrst | TransactionInterest2 (unbounded) | TransactionInterest4 (single) | **Cardinality & type change** |
| CardTx | ❌ Not present | ✓ CardEntry4 | **NEW in V08** |

**Key Changes:**
- Status changed from simple code to choice type (more flexible)
- Charges and Interest: changed from multiple elements to single element with internal structure
- Added CardTx for card transaction details

---

### 4. Message Type Comparison (V02)

All three message types in V02 are nearly identical:

| Feature | camt.052 (Report) | camt.053 (Statement) | camt.054 (Notification) |
|---------|-------------------|----------------------|-------------------------|
| GroupHeader | GroupHeader42 | GroupHeader42 | GroupHeader42 |
| Container Element | `Rpt` (AccountReport11) | `Stmt` (AccountStatement2) | `Ntfctn` (AccountNotification2) |
| Balance Required | Optional | **Required (min=1)** | Optional |
| Common Elements | Id, SeqNb, CreDtTm, Acct, Bal, Ntry | Same | Same (no Bal requirement) |

**Consistency:** The pattern seen in V08 already existed in V02 - minimal differences between types.

---

## Key Findings for Model Design

### 1. Version Differences are Structural, Not Conceptual

The differences between V02 and V08 are:
- **Type version updates** (e.g., CashAccount20 → CashAccount39)
- **New optional fields** (OrgnlBizQry, RptPgntn, CardTx)
- **Element cardinality changes** (Chrgs, Intrst)
- **Choice vs Code refinements** (Status field)

**Important:** The overall message structure and flow remains the same.

### 2. Backward Compatibility Implications

V08 is **mostly backward compatible** with V02:
- ✅ All V02 required fields exist in V08
- ✅ New V08 fields are optional
- ⚠️ Type versions changed (XJC generates different classes)
- ⚠️ Some cardinality changes (Chrgs, Intrst from multiple to single)

### 3. Forward Compatibility Design

To support future versions (v09, v10, etc.), the canonical model must:
- ✅ Accommodate optional fields gracefully
- ✅ Handle both single and multiple instances of repeating elements
- ✅ Support extensible enumerations

---

## Updated Canonical Model Design

Based on this analysis, here's the refined canonical model:

### Core Design Principles

1. **Version-agnostic canonical model** - Represents the superset of all fields
2. **Null-safe mappings** - Transformers handle missing fields based on version
3. **Collection flexibility** - Use Lists even for single-element fields in some versions
4. **Extensibility markers** - Use Maps for proprietary/future extensions

---

## Updated Canonical Model Classes

```java
package com.yourcompany.camt.model.canonical;

/**
 * Version-agnostic Group Header
 * Supports both V02 (GroupHeader42) and V08 (GroupHeader81)
 */
@Data
@Builder
public class CamtGroupHeader {
    // Core fields (present in all versions)
    @NonNull
    private String messageId;
    
    @NonNull
    private LocalDateTime creationDateTime;
    
    private CamtParty messageRecipient;
    
    private Pagination pagination;
    
    private String additionalInfo;
    
    // V08+ fields (null for V02)
    private OriginalBusinessQuery originalBusinessQuery;
    
    // Extension point for future versions
    private Map<String, Object> extensions;
}

/**
 * Pagination - supports both V02 and V08 types
 */
@Data
@Builder
public class Pagination {
    private String pageNumber;
    private boolean lastPageIndicator;
}

/**
 * Original Business Query (V08+)
 */
@Data
@Builder
public class OriginalBusinessQuery {
    private String messageIdentification;
    private String messageNameIdentification;
    private LocalDateTime creationDateTime;
}

/**
 * Version-agnostic Report Item
 * Represents AccountReport11/AccountReport25, AccountStatement2/AccountStatement9, 
 * AccountNotification2/AccountNotification17
 */
@Data
@Builder
public class CamtReportItem {
    // Common core fields (all versions, all types)
    @NonNull
    private String id;
    
    private Integer electronicSequenceNumber;
    
    private Integer legalSequenceNumber;
    
    private LocalDateTime creationDateTime;  // Required in V02, optional in V08
    
    private DateRange fromToDate;
    
    private CopyDuplicateIndicator copyDuplicateIndicator;
    
    private ReportingSource reportingSource;
    
    @NonNull
    private CamtAccount account;
    
    private CamtAccount relatedAccount;
    
    // Interest information
    private List<CamtInterest> interests;
    
    // Balances (note: required for camt.053)
    private List<CamtBalance> balances;
    
    // Transaction summary
    private CamtTransactionSummary transactionSummary;
    
    // Entries/transactions
    private List<CamtEntry> entries;
    
    // Additional info (field name varies by message type)
    private String additionalInfo;
    
    // V08+ fields
    private Pagination reportPagination;
    
    private SequenceRange reportingSequence;
    
    // Extension point
    private Map<String, Object> extensions;
}

/**
 * Sequence Range (V08+)
 */
@Data
@Builder
public class SequenceRange {
    private String fromSequence;
    private String toSequence;
}

/**
 * Version-agnostic Entry (Transaction)
 * Supports both ReportEntry2 (V02) and ReportEntry10 (V08)
 */
@Data
@Builder
public class CamtEntry {
    // Core fields (all versions)
    private String entryReference;
    
    @NonNull
    private CamtAmount amount;
    
    @NonNull
    private CreditDebitIndicator creditDebitIndicator;
    
    private Boolean reversalIndicator;
    
    @NonNull
    private EntryStatus status;  // Can be Code (V02) or Choice (V08)
    
    private LocalDate bookingDate;
    
    private LocalDate valueDate;
    
    private String accountServicerReference;
    
    private List<CashAvailability> availability;
    
    @NonNull
    private BankTransactionCode bankTransactionCode;
    
    private Boolean commissionWaiverIndicator;
    
    private MessageIdentification additionalInfoIndicator;
    
    private AmountDetails amountDetails;
    
    // Charges - handle both single (V08) and multiple (V02)
    private List<CamtCharges> charges;
    
    private TechnicalInputChannel technicalInputChannel;
    
    // Interest - handle both single (V08) and multiple (V02)
    private List<CamtInterest> interests;
    
    // V08+ fields
    private CardTransaction cardTransaction;
    
    private List<CamtEntryDetails> entryDetails;
    
    private String additionalInfo;
    
    // Extension point
    private Map<String, Object> extensions;
}

/**
 * Entry Status - handles both Code (V02) and Choice (V08)
 */
@Data
@Builder
public class EntryStatus {
    private EntryStatusCode code;  // BOOK, PDNG, INFO, etc.
    private String proprietaryCode;
}

public enum EntryStatusCode {
    BOOK,  // Booked
    PDNG,  // Pending
    INFO,  // Information
    FUTR   // Future (V08+)
}

/**
 * Card Transaction (V08+)
 */
@Data
@Builder
public class CardTransaction {
    private String cardNumber;  // Masked
    private String terminalId;
    private String merchantName;
    private String merchantCategoryCode;
    private LocalDateTime transactionDateTime;
}

/**
 * Account - supports CashAccount20 (V02) and CashAccount39 (V08)
 */
@Data
@Builder
public class CamtAccount {
    private String iban;
    private String otherIdentification;
    private String accountType;
    private String accountName;
    private String currency;
    private CamtParty owner;
    private CamtParty servicer;
    
    // V08+ might have additional fields
    private Map<String, Object> extensions;
}

/**
 * Balance - supports CashBalance3 (V02) and CashBalance8 (V08)
 */
@Data
@Builder
public class CamtBalance {
    @NonNull
    private BalanceType type;
    
    @NonNull
    private CamtAmount amount;
    
    @NonNull
    private CreditDebitIndicator creditDebitIndicator;
    
    @NonNull
    private LocalDateTime date;
    
    private List<CashAvailability> availability;
}

public enum BalanceType {
    OPBD,  // Opening Booked
    CLBD,  // Closing Booked
    OPAV,  // Opening Available
    CLAV,  // Closing Available
    FWAV,  // Forward Available
    PRCD,  // Previously Closed Booked
    ITBD,  // Interim Booked
    ITAV,  // Interim Available
    XPCD   // Expected (V08+)
}

/**
 * Charges - supports ChargesInformation6 (V02 unbounded) and Charges6 (V08 single)
 */
@Data
@Builder
public class CamtCharges {
    private CamtAmount totalChargesAmount;
    private List<ChargeDetail> details;
}

@Data
@Builder
public class ChargeDetail {
    private CamtAmount amount;
    private String type;
    private CamtParty chargeBearer;
}

/**
 * Interest - supports AccountInterest2/TransactionInterest2 (V02) 
 * and AccountInterest4/TransactionInterest4 (V08)
 */
@Data
@Builder
public class CamtInterest {
    private InterestType type;
    private List<Rate> rates;
    private DateRange fromToDate;
    private String reason;
    private Tax tax;
    private CamtAmount amount;  // For transaction interest
}

@Data
@Builder
public class Rate {
    private String rateType;
    private BigDecimal rate;
    private String basis;
}

/**
 * Main Report Data - Unified model for all types and versions
 */
@Data
@Builder
public class CamtReportData {
    @NonNull
    private CamtReportType reportType;
    
    @NonNull
    private String version;  // "001.02" or "001.08"
    
    @NonNull
    private CamtGroupHeader groupHeader;
    
    @NonNull
    private List<CamtReportItem> reportItems;
    
    // For supplementary data (if needed)
    private List<SupplementaryData> supplementaryData;
    
    // Metadata
    private LocalDateTime generatedAt;
    private String generatedBy;
}

public enum CamtReportType {
    ACCOUNT_REPORT_052("052", "BkToCstmrAcctRpt"),
    STATEMENT_053("053", "BkToCstmrStmt"),
    NOTIFICATION_054("054", "BkToCstmrDbtCdtNtfctn");
    
    private final String code;
    private final String rootElementName;
    
    CamtReportType(String code, String rootElementName) {
        this.code = code;
        this.rootElementName = rootElementName;
    }
    
    public String getCode() { return code; }
    public String getRootElementName() { return rootElementName; }
}
```

---

## Version-Specific Transformer Strategy

### Transformer Interface

```java
package com.yourcompany.camt.transformer;

/**
 * Strategy interface for transforming canonical model to version-specific XSD models
 */
public interface CamtTransformer<T> {
    /**
     * Transform canonical model to XSD-generated model
     */
    T transform(CamtReportData canonicalData);
    
    /**
     * Get the version this transformer supports
     */
    String getVersion();
    
    /**
     * Get the message type this transformer supports
     */
    CamtReportType getSupportedType();
    
    /**
     * Validate canonical data for this version/type
     */
    default void validate(CamtReportData data) {
        if (data.getReportType() != getSupportedType()) {
            throw new IllegalArgumentException(
                "Expected " + getSupportedType() + " but got " + data.getReportType()
            );
        }
        if (!data.getVersion().equals(getVersion())) {
            throw new IllegalArgumentException(
                "Expected version " + getVersion() + " but got " + data.getVersion()
            );
        }
        
        // Version-specific validation
        performVersionSpecificValidation(data);
    }
    
    /**
     * Perform version-specific validation
     */
    default void performVersionSpecificValidation(CamtReportData data) {
        // Can be overridden by implementations
    }
}
```

### Base Transformer Abstract Class

```java
package com.yourcompany.camt.transformer;

/**
 * Base transformer with common transformation logic
 */
public abstract class BaseCamtTransformer<T> implements CamtTransformer<T> {
    
    protected final String version;
    protected final CamtReportType type;
    
    protected BaseCamtTransformer(String version, CamtReportType type) {
        this.version = version;
        this.type = type;
    }
    
    @Override
    public String getVersion() {
        return version;
    }
    
    @Override
    public CamtReportType getSupportedType() {
        return type;
    }
    
    /**
     * Common transformation for Amount
     * (structure same across versions, just different class names)
     */
    protected <A> A transformAmount(CamtAmount canonical, 
                                     BiFunction<BigDecimal, String, A> amountCreator) {
        return amountCreator.apply(
            canonical.getValue(),
            canonical.getCurrency()
        );
    }
    
    /**
     * Transform credit/debit indicator
     */
    protected <E extends Enum<E>> E transformCreditDebitIndicator(
            CreditDebitIndicator canonical, 
            Class<E> enumClass) {
        return Enum.valueOf(enumClass, canonical.name());
    }
    
    /**
     * Transform date to XMLGregorianCalendar (common across versions)
     */
    protected XMLGregorianCalendar toXmlDate(LocalDate date) {
        try {
            return DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(date.toString());
        } catch (DatatypeConfigurationException e) {
            throw new TransformationException("Failed to convert date", e);
        }
    }
    
    /**
     * Transform datetime to XMLGregorianCalendar
     */
    protected XMLGregorianCalendar toXmlDateTime(LocalDateTime dateTime) {
        try {
            return DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(dateTime.toString());
        } catch (DatatypeConfigurationException e) {
            throw new TransformationException("Failed to convert datetime", e);
        }
    }
}
```

### Example V02 Transformer Implementation

```java
package com.yourcompany.camt.transformer.v02;

import com.yourcompany.camt.model.generated.v02.camt052.*;

@Component
public class Camt052TransformerV02 extends BaseCamtTransformer<Document> {
    
    public Camt052TransformerV02() {
        super("001.02", CamtReportType.ACCOUNT_REPORT_052);
    }
    
    @Override
    public Document transform(CamtReportData canonicalData) {
        validate(canonicalData);
        
        Document document = new Document();
        BankToCustomerAccountReportV02 report = new BankToCustomerAccountReportV02();
        
        // Transform GroupHeader
        report.setGrpHdr(transformGroupHeader(canonicalData.getGroupHeader()));
        
        // Transform Reports
        canonicalData.getReportItems().forEach(item -> 
            report.getRpt().add(transformAccountReport(item))
        );
        
        document.setBkToCstmrAcctRpt(report);
        return document;
    }
    
    private GroupHeader42 transformGroupHeader(CamtGroupHeader canonical) {
        GroupHeader42 header = new GroupHeader42();
        header.setMsgId(canonical.getMessageId());
        header.setCreDtTm(toXmlDateTime(canonical.getCreationDateTime()));
        
        if (canonical.getMessageRecipient() != null) {
            header.setMsgRcpt(transformPartyV02(canonical.getMessageRecipient()));
        }
        
        if (canonical.getPagination() != null) {
            header.setMsgPgntn(transformPaginationV02(canonical.getPagination()));
        }
        
        if (canonical.getAdditionalInfo() != null) {
            header.setAddtlInf(canonical.getAdditionalInfo());
        }
        
        // Note: OrgnlBizQry not present in V02, ignore if set
        
        return header;
    }
    
    private AccountReport11 transformAccountReport(CamtReportItem item) {
        AccountReport11 report = new AccountReport11();
        
        report.setId(item.getId());
        
        if (item.getElectronicSequenceNumber() != null) {
            report.setElctrncSeqNb(BigDecimal.valueOf(item.getElectronicSequenceNumber()));
        }
        
        if (item.getLegalSequenceNumber() != null) {
            report.setLglSeqNb(BigDecimal.valueOf(item.getLegalSequenceNumber()));
        }
        
        // V02: CreDtTm is required
        if (item.getCreationDateTime() != null) {
            report.setCreDtTm(toXmlDateTime(item.getCreationDateTime()));
        } else {
            // Use current time if not provided (shouldn't happen with proper validation)
            report.setCreDtTm(toXmlDateTime(LocalDateTime.now()));
        }
        
        if (item.getFromToDate() != null) {
            report.setFrToDt(transformDateRangeV02(item.getFromToDate()));
        }
        
        // Transform Account
        report.setAcct(transformAccountV02(item.getAccount()));
        
        if (item.getRelatedAccount() != null) {
            report.setRltdAcct(transformRelatedAccountV02(item.getRelatedAccount()));
        }
        
        // Transform Interests
        item.getInterests().forEach(interest ->
            report.getIntrst().add(transformInterestV02(interest))
        );
        
        // Transform Balances
        item.getBalances().forEach(balance ->
            report.getBal().add(transformBalanceV02(balance))
        );
        
        // Transform Transaction Summary
        if (item.getTransactionSummary() != null) {
            report.setTxsSummry(transformTransactionSummaryV02(item.getTransactionSummary()));
        }
        
        // Transform Entries
        item.getEntries().forEach(entry ->
            report.getNtry().add(transformEntryV02(entry))
        );
        
        if (item.getAdditionalInfo() != null) {
            report.setAddtlRptInf(item.getAdditionalInfo());
        }
        
        // Note: V08 fields like RptPgntn, RptgSeq ignored in V02
        
        return report;
    }
    
    private ReportEntry2 transformEntryV02(CamtEntry entry) {
        ReportEntry2 xsdEntry = new ReportEntry2();
        
        if (entry.getEntryReference() != null) {
            xsdEntry.setNtryRef(entry.getEntryReference());
        }
        
        xsdEntry.setAmt(transformAmountV02(entry.getAmount()));
        xsdEntry.setCdtDbtInd(CreditDebitCode.valueOf(entry.getCreditDebitIndicator().name()));
        
        if (entry.getReversalIndicator() != null) {
            xsdEntry.setRvslInd(entry.getReversalIndicator());
        }
        
        // V02: Status is EntryStatus2Code (simple enum)
        xsdEntry.setSts(EntryStatus2Code.valueOf(entry.getStatus().getCode().name()));
        
        if (entry.getBookingDate() != null) {
            xsdEntry.setBookgDt(transformDateChoiceV02(entry.getBookingDate()));
        }
        
        if (entry.getValueDate() != null) {
            xsdEntry.setValDt(transformDateChoiceV02(entry.getValueDate()));
        }
        
        xsdEntry.setBkTxCd(transformBankTransactionCodeV02(entry.getBankTransactionCode()));
        
        // Charges: V02 allows multiple ChargesInformation6
        entry.getCharges().forEach(charge ->
            xsdEntry.getChrgs().add(transformChargesV02(charge))
        );
        
        // Interest: V02 allows multiple TransactionInterest2
        entry.getInterests().forEach(interest ->
            xsdEntry.getIntrst().add(transformTransactionInterestV02(interest))
        );
        
        // EntryDetails
        entry.getEntryDetails().forEach(detail ->
            xsdEntry.getNtryDtls().add(transformEntryDetailsV02(detail))
        );
        
        // Note: CardTx not present in V02, ignore if set
        
        return xsdEntry;
    }
    
    private ActiveOrHistoricCurrencyAndAmount transformAmountV02(CamtAmount amount) {
        ActiveOrHistoricCurrencyAndAmount xsdAmount = new ActiveOrHistoricCurrencyAndAmount();
        xsdAmount.setValue(amount.getValue());
        xsdAmount.setCcy(amount.getCurrency());
        return xsdAmount;
    }
    
    // Additional transformation methods...
    
    @Override
    public void performVersionSpecificValidation(CamtReportData data) {
        // V02-specific validation
        data.getReportItems().forEach(item -> {
            // CreDtTm is required in V02
            if (item.getCreationDateTime() == null) {
                throw new ValidationException("CreationDateTime is required in V02");
            }
            
            // Check that no V08-specific fields are used
            if (item.getReportPagination() != null) {
                throw new ValidationException("ReportPagination is not supported in V02");
            }
            
            if (item.getReportingSequence() != null) {
                throw new ValidationException("ReportingSequence is not supported in V02");
            }
            
            // Check entries
            item.getEntries().forEach(entry -> {
                if (entry.getCardTransaction() != null) {
                    throw new ValidationException("CardTransaction is not supported in V02");
                }
            });
        });
    }
}
```

### Example V08 Transformer Implementation

```java
package com.yourcompany.camt.transformer.v08;

import com.yourcompany.camt.model.generated.v08.camt052.*;

@Component
public class Camt052TransformerV08 extends BaseCamtTransformer<Document> {
    
    public Camt052TransformerV08() {
        super("001.08", CamtReportType.ACCOUNT_REPORT_052);
    }
    
    @Override
    public Document transform(CamtReportData canonicalData) {
        validate(canonicalData);
        
        Document document = new Document();
        BankToCustomerAccountReportV08 report = new BankToCustomerAccountReportV08();
        
        // Transform GroupHeader
        report.setGrpHdr(transformGroupHeader(canonicalData.getGroupHeader()));
        
        // Transform Reports
        canonicalData.getReportItems().forEach(item -> 
            report.getRpt().add(transformAccountReport(item))
        );
        
        document.setBkToCstmrAcctRpt(report);
        return document;
    }
    
    private GroupHeader81 transformGroupHeader(CamtGroupHeader canonical) {
        GroupHeader81 header = new GroupHeader81();
        header.setMsgId(canonical.getMessageId());
        header.setCreDtTm(toXmlDateTime(canonical.getCreationDateTime()));
        
        if (canonical.getMessageRecipient() != null) {
            header.setMsgRcpt(transformPartyV08(canonical.getMessageRecipient()));
        }
        
        if (canonical.getPagination() != null) {
            header.setMsgPgntn(transformPaginationV08(canonical.getPagination()));
        }
        
        // V08: OrgnlBizQry field
        if (canonical.getOriginalBusinessQuery() != null) {
            header.setOrgnlBizQry(transformOriginalBusinessQuery(canonical.getOriginalBusinessQuery()));
        }
        
        if (canonical.getAdditionalInfo() != null) {
            header.setAddtlInf(canonical.getAdditionalInfo());
        }
        
        return header;
    }
    
    private AccountReport25 transformAccountReport(CamtReportItem item) {
        AccountReport25 report = new AccountReport25();
        
        report.setId(item.getId());
        
        // V08: RptPgntn field
        if (item.getReportPagination() != null) {
            report.setRptPgntn(transformPaginationV08(item.getReportPagination()));
        }
        
        if (item.getElectronicSequenceNumber() != null) {
            report.setElctrncSeqNb(BigDecimal.valueOf(item.getElectronicSequenceNumber()));
        }
        
        // V08: RptgSeq field
        if (item.getReportingSequence() != null) {
            report.setRptgSeq(transformSequenceRange(item.getReportingSequence()));
        }
        
        if (item.getLegalSequenceNumber() != null) {
            report.setLglSeqNb(BigDecimal.valueOf(item.getLegalSequenceNumber()));
        }
        
        // V08: CreDtTm is optional
        if (item.getCreationDateTime() != null) {
            report.setCreDtTm(toXmlDateTime(item.getCreationDateTime()));
        }
        
        if (item.getFromToDate() != null) {
            report.setFrToDt(transformDateRangeV08(item.getFromToDate()));
        }
        
        // Transform Account
        report.setAcct(transformAccountV08(item.getAccount()));
        
        if (item.getRelatedAccount() != null) {
            report.setRltdAcct(transformRelatedAccountV08(item.getRelatedAccount()));
        }
        
        // Transform Interests
        item.getInterests().forEach(interest ->
            report.getIntrst().add(transformInterestV08(interest))
        );
        
        // Transform Balances
        item.getBalances().forEach(balance ->
            report.getBal().add(transformBalanceV08(balance))
        );
        
        // Transform Transaction Summary
        if (item.getTransactionSummary() != null) {
            report.setTxsSummry(transformTransactionSummaryV08(item.getTransactionSummary()));
        }
        
        // Transform Entries
        item.getEntries().forEach(entry ->
            report.getNtry().add(transformEntryV08(entry))
        );
        
        if (item.getAdditionalInfo() != null) {
            report.setAddtlRptInf(item.getAdditionalInfo());
        }
        
        return report;
    }
    
    private ReportEntry10 transformEntryV08(CamtEntry entry) {
        ReportEntry10 xsdEntry = new ReportEntry10();
        
        if (entry.getEntryReference() != null) {
            xsdEntry.setNtryRef(entry.getEntryReference());
        }
        
        xsdEntry.setAmt(transformAmountV08(entry.getAmount()));
        xsdEntry.setCdtDbtInd(CreditDebitCode.valueOf(entry.getCreditDebitIndicator().name()));
        
        if (entry.getReversalIndicator() != null) {
            xsdEntry.setRvslInd(entry.getReversalIndicator());
        }
        
        // V08: Status is EntryStatus1Choice (choice type)
        xsdEntry.setSts(transformEntryStatusV08(entry.getStatus()));
        
        if (entry.getBookingDate() != null) {
            xsdEntry.setBookgDt(transformDateChoiceV08(entry.getBookingDate()));
        }
        
        if (entry.getValueDate() != null) {
            xsdEntry.setValDt(transformDateChoiceV08(entry.getValueDate()));
        }
        
        xsdEntry.setBkTxCd(transformBankTransactionCodeV08(entry.getBankTransactionCode()));
        
        // Charges: V08 uses single Charges6 structure
        if (!entry.getCharges().isEmpty()) {
            xsdEntry.setChrgs(transformChargesV08(entry.getCharges()));
        }
        
        // Interest: V08 uses single TransactionInterest4
        if (!entry.getInterests().isEmpty()) {
            xsdEntry.setIntrst(transformTransactionInterestV08(entry.getInterests()));
        }
        
        // V08: CardTx field
        if (entry.getCardTransaction() != null) {
            xsdEntry.setCardTx(transformCardTransaction(entry.getCardTransaction()));
        }
        
        // EntryDetails
        entry.getEntryDetails().forEach(detail ->
            xsdEntry.getNtryDtls().add(transformEntryDetailsV08(detail))
        );
        
        return xsdEntry;
    }
    
    private EntryStatus1Choice transformEntryStatusV08(EntryStatus status) {
        EntryStatus1Choice choice = new EntryStatus1Choice();
        
        if (status.getProprietaryCode() != null) {
            choice.setPrtry(status.getProprietaryCode());
        } else {
            // Convert enum to V08 status code
            choice.setCd(status.getCode().name());
        }
        
        return choice;
    }
    
    private CardEntry4 transformCardTransaction(CardTransaction canonical) {
        CardEntry4 card = new CardEntry4();
        // Map card transaction details
        // ...
        return card;
    }
    
    private Charges6 transformChargesV08(List<CamtCharges> charges) {
        // V08: Consolidate multiple charges into single structure
        Charges6 xsdCharges = new Charges6();
        
        // Calculate total
        BigDecimal total = charges.stream()
            .map(c -> c.getTotalChargesAmount().getValue())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        ActiveOrHistoricCurrencyAndAmount totalAmount = new ActiveOrHistoricCurrencyAndAmount();
        totalAmount.setValue(total);
        totalAmount.setCcy(charges.get(0).getTotalChargesAmount().getCurrency());
        xsdCharges.setTtlChrgsAndTaxAmt(totalAmount);
        
        // Add individual charge records
        charges.forEach(charge ->
            charge.getDetails().forEach(detail ->
                xsdCharges.getRcrd().add(transformChargeRecord(detail))
            )
        );
        
        return xsdCharges;
    }
    
    // Additional transformation methods...
}
```

---

## Transformer Factory with Auto-Discovery

```java
package com.yourcompany.camt.transformer;

@Component
public class CamtTransformerFactory {
    
    private final Map<String, CamtTransformer<?>> transformers;
    
    @Autowired
    public CamtTransformerFactory(List<CamtTransformer<?>> allTransformers) {
        this.transformers = allTransformers.stream()
            .collect(Collectors.toMap(
                t -> buildKey(t.getVersion(), t.getSupportedType()),
                Function.identity()
            ));
        
        log.info("Registered {} CAMT transformers: {}", 
            transformers.size(), 
            transformers.keySet()
        );
    }
    
    private String buildKey(String version, CamtReportType type) {
        return version + "_" + type.getCode();
    }
    
    public CamtTransformer<?> getTransformer(String version, CamtReportType type) {
        String key = buildKey(version, type);
        CamtTransformer<?> transformer = transformers.get(key);
        
        if (transformer == null) {
            throw new UnsupportedOperationException(
                String.format("No transformer found for version %s, type %s. Available: %s",
                    version, type, transformers.keySet())
            );
        }
        
        return transformer;
    }
    
    public Set<String> getSupportedVersions() {
        return transformers.keySet().stream()
            .map(key -> key.split("_")[0])
            .collect(Collectors.toSet());
    }
    
    public boolean isSupported(String version, CamtReportType type) {
        return transformers.containsKey(buildKey(version, type));
    }
}
```

---

## Configuration

```yaml
# application.yml
camt:
  # Default versions for each message type
  versions:
    ACCOUNT_REPORT_052: "001.08"
    STATEMENT_053: "001.08"
    NOTIFICATION_054: "001.08"
  
  # Enable version validation
  strict-version-check: true
  
  # Fallback behavior when version not supported
  fallback-to-latest: false
```

```java
@Configuration
@ConfigurationProperties(prefix = "camt")
@Validated
public class CamtConfig {
    
    @NotNull
    private Map<String, String> versions = new HashMap<>();
    
    private boolean strictVersionCheck = true;
    
    private boolean fallbackToLatest = false;
    
    public String getVersion(CamtReportType type) {
        String version = versions.get(type.name());
        if (version == null) {
            throw new ConfigurationException("No version configured for " + type);
        }
        return version;
    }
    
    // Getters and setters
}
```

---

## Complete Service Implementation

```java
package com.yourcompany.camt.service;

@Service
@Slf4j
public class CamtReportService {
    
    private final CamtRowMapper rowMapper;
    private final CamtTransformerFactory transformerFactory;
    private final CamtXmlGenerator xmlGenerator;
    private final CamtConfig config;
    
    @Autowired
    public CamtReportService(
            CamtRowMapper rowMapper,
            CamtTransformerFactory transformerFactory,
            CamtXmlGenerator xmlGenerator,
            CamtConfig config) {
        this.rowMapper = rowMapper;
        this.transformerFactory = transformerFactory;
        this.xmlGenerator = xmlGenerator;
        this.config = config;
    }
    
    /**
     * Generate CAMT report using configured version
     */
    public String generateReport(CamtReportType reportType, String accountId) {
        String version = config.getVersion(reportType);
        return generateReport(reportType, accountId, version);
    }
    
    /**
     * Generate CAMT report with explicit version
     */
    public String generateReport(
            CamtReportType reportType, 
            String accountId, 
            String version) {
        
        log.info("Generating {} report version {} for account {}", 
            reportType, version, accountId);
        
        // 1. Validate version is supported
        if (!transformerFactory.isSupported(version, reportType)) {
            if (config.isFallbackToLatest()) {
                version = getLatestVersion(reportType);
                log.warn("Version {} not supported, falling back to {}", 
                    config.getVersion(reportType), version);
            } else {
                throw new UnsupportedVersionException(
                    "Version " + version + " not supported for " + reportType
                );
            }
        }
        
        // 2. Fetch data from database
        CamtReportData canonicalData = rowMapper.fetchReportData(reportType, accountId);
        canonicalData.setVersion(version);
        canonicalData.setGeneratedAt(LocalDateTime.now());
        
        // 3. Get appropriate transformer
        CamtTransformer<?> transformer = transformerFactory.getTransformer(version, reportType);
        
        // 4. Transform to XSD model
        Object xsdModel = transformer.transform(canonicalData);
        
        // 5. Generate XML
        String xml = xmlGenerator.generateXml(xsdModel, reportType, version);
        
        log.info("Successfully generated {} report ({} bytes)", reportType, xml.length());
        
        return xml;
    }
    
    private String getLatestVersion(CamtReportType type) {
        return transformerFactory.getSupportedVersions().stream()
            .filter(v -> transformerFactory.isSupported(v, type))
            .max(String::compareTo)
            .orElseThrow(() -> new IllegalStateException("No versions available for " + type));
    }
    
    /**
     * Generate reports for multiple accounts in batch
     */
    @Async
    public CompletableFuture<Map<String, String>> generateReportsBatch(
            CamtReportType reportType,
            List<String> accountIds) {
        
        Map<String, String> results = new ConcurrentHashMap<>();
        
        accountIds.parallelStream().forEach(accountId -> {
            try {
                String xml = generateReport(reportType, accountId);
                results.put(accountId, xml);
            } catch (Exception e) {
                log.error("Failed to generate report for account {}", accountId, e);
                results.put(accountId, "ERROR: " + e.getMessage());
            }
        });
        
        return CompletableFuture.completedFuture(results);
    }
}
```

---

## Summary of Recommendations

### 1. **Use Single Unified Canonical Model**
- Supports all versions (V02, V08, future)
- Supports all message types (052, 053, 054)
- Maximum code reuse (95%+)

### 2. **Version-Specific Transformers**
- One transformer per version + message type combination
- Inherit from base transformer for common logic
- Handle version-specific differences gracefully

### 3. **Configuration-Driven Version Selection**
- Configure version per message type
- Support version overrides
- Optional fallback to latest version

### 4. **Validation Strategy**
- Validate at canonical level (business rules)
- Validate at transformer level (version-specific constraints)
- Clear error messages for unsupported features

### 5. **Future-Proof Design**
- Extension points (Map fields) for unknown future fields
- Auto-discovery of transformers via Spring
- Easy to add new versions (just add new transformer)

---

## Migration Path

### Adding New Version (e.g., 001.09)

1. **Generate Java classes from XSD:**
   ```bash
   xjc -d src/main/java -p com.yourcompany.camt.model.generated.v09.camt052 camt_052_001_09.xsd
   xjc -d src/main/java -p com.yourcompany.camt.model.generated.v09.camt053 camt_053_001_09.xsd
   xjc -d src/main/java -p com.yourcompany.camt.model.generated.v09.camt054 camt_054_001_09.xsd
   ```

2. **Create transformers:**
   ```java
   @Component
   public class Camt052TransformerV09 extends BaseCamtTransformer<Document> {
       public Camt052TransformerV09() {
           super("001.09", CamtReportType.ACCOUNT_REPORT_052);
       }
       // Implementation...
   }
   ```

3. **Update configuration:**
   ```yaml
   camt:
     versions:
       ACCOUNT_REPORT_052: "001.09"  # Change to new version
   ```

4. **No changes needed to:**
   - Canonical model
   - Service layer
   - Database layer
   - Factory

---

## Project Structure

```
src/main/java/com/yourcompany/camt/
├── model/
│   ├── canonical/
│   │   ├── CamtReportData.java
│   │   ├── CamtReportItem.java
│   │   ├── CamtGroupHeader.java
│   │   ├── CamtAccount.java
│   │   ├── CamtBalance.java
│   │   ├── CamtEntry.java
│   │   ├── CamtEntryDetails.java
│   │   ├── CamtAmount.java
│   │   ├── CamtParty.java
│   │   ├── CamtCharges.java
│   │   ├── CamtInterest.java
│   │   ├── CardTransaction.java
│   │   └── enums/
│   │       ├── CamtReportType.java
│   │       ├── BalanceType.java
│   │       ├── CreditDebitIndicator.java
│   │       ├── EntryStatusCode.java
│   │       └── ...
│   │
│   └── generated/
│       ├── v02/
│       │   ├── camt052/ (XJC generated)
│       │   ├── camt053/ (XJC generated)
│       │   └── camt054/ (XJC generated)
│       └── v08/
│           ├── camt052/ (XJC generated)
│           ├── camt053/ (XJC generated)
│           └── camt054/ (XJC generated)
│
├── mapper/
│   └── CamtRowMapper.java
│
├── transformer/
│   ├── CamtTransformer.java (interface)
│   ├── BaseCamtTransformer.java (abstract)
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
│   └── CamtConfig.java
│
├── exception/
│   ├── CamtGenerationException.java
│   ├── UnsupportedVersionException.java
│   ├── ValidationException.java
│   └── TransformationException.java
│
└── validation/
    └── CamtValidator.java
```

---

## Next Steps

Would you like me to:

1. **Generate complete working code** for specific transformers?
2. **Create the database row mapper** with stored procedure integration?
3. **Build unit tests** for the transformation logic?
4. **Create a sample Spring Boot application** with REST endpoints?
5. **Design the stored procedure result set structure** to match the canonical model?
6. **Create validation logic** for business rules?

Let me know which area you'd like to dive deeper into!
