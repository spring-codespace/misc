# CAMT Canonical Model - Batch Booking & Hierarchical Data Design

## Critical Update: Batch Booking & Data Structure

### Your Scenario (Stored Procedure Output)
```
Single SP Call Returns:
├── Multiple Accounts
│   ├── Account A
│   │   ├── Transaction 1 (batch booked = false)
│   │   ├── Transaction 2 (batch booked = true) ────┐
│   │   ├── Transaction 3 (batch booked = true) ────┤ These belong to same batch
│   │   └── Transaction 4 (batch booked = false)    │
│   └── Account B
│       ├── Transaction 1 (batch booked = false)
│       └── (no more transactions)
```

---

## ISO20022 CAMT Structure with Batch Booking

### Understanding the Hierarchy

The ISO20022 CAMT messages have a **THREE-level hierarchy** for entries:

```
AccountReport/Statement/Notification
└── Entry (Ntry) - ReportEntry2
    └── EntryDetails (NtryDtls) - EntryDetails1
        ├── Batch (Btch) - BatchInformation2 [OPTIONAL]
        └── TransactionDetails (TxDtls) - EntryTransaction2 [0..unbounded]
```

### Key Discovery: Batch Booking Mechanism

**ISO20022 Batch Booking Works As Follows:**

1. **Non-Batch Entry**: Simple entry with no batch info
   ```
   Entry (amount: 100.00)
   └── EntryDetails
       └── TransactionDetails (1 item - the actual transaction)
   ```

2. **Batch-Booked Entry**: Entry represents MULTIPLE transactions
   ```
   Entry (amount: 500.00)  ← Sum of all transactions in batch
   └── EntryDetails
       ├── Batch
       │   ├── MessageId
       │   ├── NumberOfTransactions: 5
       │   └── TotalAmount: 500.00
       └── TransactionDetails (5 items)
           ├── Transaction 1 (100.00)
           ├── Transaction 2 (100.00)
           ├── Transaction 3 (100.00)
           ├── Transaction 4 (100.00)
           └── Transaction 5 (100.00)
   ```

**Critical Insight:**
- When `batch booked = true` in your SP, multiple DB rows should be **aggregated** into a SINGLE Entry
- The Entry amount is the **sum** of all transactions in that batch
- Individual transaction details go into `TransactionDetails` (TxDtls)
- The `Batch` element contains summary information

---

## Revised Canonical Model with Batch Support

### 1. Updated Entry Structure

```java
public class Entry {
    // === Entry Level (the "line" on the statement) ===
    private String entryReference;
    private Amount amount;                      // Sum of all transactions (if batch)
    private CreditDebitIndicator cdtDbtInd;
    private Boolean reversalIndicator;
    private EntryStatus status;
    private LocalDate bookingDate;
    private LocalDate valueDate;
    private String accountServicerReference;
    private List<Availability> availabilities;
    private BankTransactionCode bankTxCode;
    private Boolean commissionWaiverIndicator;
    
    // === Entry Details (can have multiple) ===
    private List<EntryDetails> entryDetails;    // NEW: Support multiple entry details
    
    private String additionalInfo;
}

public class EntryDetails {
    // === Batch Information (present only if batch-booked) ===
    private BatchInformation batch;             // NULL if not batch-booked
    
    // === Transaction Details (1..n) ===
    private List<TransactionDetails> transactions;  // Individual transactions
}

public class BatchInformation {
    private String messageId;                   // Optional
    private String paymentInformationId;        // Optional
    private Integer numberOfTransactions;       // Optional
    private Amount totalAmount;                 // Optional
    private CreditDebitIndicator cdtDbtInd;     // Optional
}

public class TransactionDetails {
    // This is the ACTUAL transaction from your database
    private TransactionReferences references;
    private AmountDetails amountDetails;
    private List<Availability> availabilities;
    private BankTransactionCode bankTxCode;
    private List<Charges> charges;
    private List<TransactionInterest> interests;
    private TransactionParties relatedParties;      // Debtor, Creditor, etc.
    private TransactionAgents relatedAgents;        // Debtor/Creditor agents
    private Purpose purpose;
    private RemittanceInformation remittanceInfo;   // Important: payment references
    private TransactionDates dates;
    private TransactionPrice price;
    private List<TransactionQuantity> quantities;
    private SecurityIdentification financialInstrument;
    private TaxInformation tax;
    private ReturnInformation returnInfo;
    private String additionalInfo;
}
```

---

## Mapping Strategy from Stored Procedure

### Scenario 1: Your Stored Procedure Returns Flat Data

**Assumed SP Result Structure:**
```sql
-- Each row represents ONE transaction
ACCOUNT_ID | TXN_ID | BATCH_ID | BATCH_BOOKED | AMOUNT | BOOKING_DATE | ...
-----------+--------+----------+--------------+--------+--------------+----
ACC001     | T001   | NULL     | false        | 100.00 | 2024-01-15   | ...
ACC001     | T002   | B001     | true         | 50.00  | 2024-01-15   | ...
ACC001     | T003   | B001     | true         | 75.00  | 2024-01-15   | ...
ACC001     | T004   | NULL     | false        | 200.00 | 2024-01-16   | ...
ACC002     | T005   | NULL     | false        | 300.00 | 2024-01-15   | ...
```

### Processing Logic (Canonical Mapper)

```java
public class StoredProcedureToCanonicalMapper {
    
    public CamtDocument mapFromStoredProcedure(List<TransactionRow> spResults) {
        
        // Step 1: Group by Account
        Map<String, List<TransactionRow>> byAccount = 
            spResults.stream()
                .collect(Collectors.groupingBy(TransactionRow::getAccountId));
        
        List<AccountReport> reports = new ArrayList<>();
        
        for (Map.Entry<String, List<TransactionRow>> accountEntry : byAccount.entrySet()) {
            String accountId = accountEntry.getKey();
            List<TransactionRow> accountTransactions = accountEntry.getValue();
            
            // Step 2: Within each account, group by batch
            Map<String, List<TransactionRow>> byBatch = groupByBatch(accountTransactions);
            
            List<Entry> entries = new ArrayList<>();
            
            for (Map.Entry<String, List<TransactionRow>> batchEntry : byBatch.entrySet()) {
                String batchId = batchEntry.getKey();
                List<TransactionRow> batchTransactions = batchEntry.getValue();
                
                if (batchId == null || batchTransactions.size() == 1) {
                    // Non-batch entry: one transaction = one entry
                    entries.add(createSimpleEntry(batchTransactions.get(0)));
                } else {
                    // Batch entry: multiple transactions = one entry with batch details
                    entries.add(createBatchEntry(batchId, batchTransactions));
                }
            }
            
            // Create AccountReport with all entries
            AccountReport report = createAccountReport(accountId, entries);
            reports.add(report);
        }
        
        return new CamtDocument(messageType, version, header, reports);
    }
    
    private Map<String, List<TransactionRow>> groupByBatch(List<TransactionRow> transactions) {
        Map<String, List<TransactionRow>> batches = new LinkedHashMap<>();
        
        for (TransactionRow txn : transactions) {
            if (txn.isBatchBooked() && txn.getBatchId() != null) {
                // Batch transaction
                batches.computeIfAbsent(txn.getBatchId(), k -> new ArrayList<>()).add(txn);
            } else {
                // Non-batch transaction: use unique key per transaction
                String uniqueKey = "SINGLE_" + txn.getTransactionId();
                batches.computeIfAbsent(uniqueKey, k -> new ArrayList<>()).add(txn);
            }
        }
        
        return batches;
    }
    
    private Entry createSimpleEntry(TransactionRow txn) {
        Entry entry = new Entry();
        
        // Entry-level data
        entry.setEntryReference(txn.getTransactionId());
        entry.setAmount(new Amount(txn.getAmount(), txn.getCurrency()));
        entry.setCdtDbtInd(txn.getCreditDebitIndicator());
        entry.setStatus(txn.getStatus());
        entry.setBookingDate(txn.getBookingDate());
        entry.setValueDate(txn.getValueDate());
        // ... other entry-level fields
        
        // Create single EntryDetails with single TransactionDetails
        EntryDetails entryDetails = new EntryDetails();
        entryDetails.setBatch(null);  // No batch
        
        TransactionDetails txnDetails = mapTransactionDetails(txn);
        entryDetails.setTransactions(List.of(txnDetails));
        
        entry.setEntryDetails(List.of(entryDetails));
        
        return entry;
    }
    
    private Entry createBatchEntry(String batchId, List<TransactionRow> batchTransactions) {
        Entry entry = new Entry();
        
        // Entry-level data: AGGREGATED from all transactions in batch
        BigDecimal totalAmount = batchTransactions.stream()
            .map(TransactionRow::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        entry.setEntryReference(batchId);  // Use batch ID as entry reference
        entry.setAmount(new Amount(totalAmount, batchTransactions.get(0).getCurrency()));
        entry.setCdtDbtInd(batchTransactions.get(0).getCreditDebitIndicator());
        entry.setStatus(batchTransactions.get(0).getStatus());
        entry.setBookingDate(batchTransactions.get(0).getBookingDate());
        entry.setValueDate(batchTransactions.get(0).getValueDate());
        // ... other entry-level fields (use first transaction or aggregate)
        
        // Create EntryDetails with Batch information
        EntryDetails entryDetails = new EntryDetails();
        
        // Batch information
        BatchInformation batch = new BatchInformation();
        batch.setMessageId(batchId);
        batch.setNumberOfTransactions(batchTransactions.size());
        batch.setTotalAmount(new Amount(totalAmount, batchTransactions.get(0).getCurrency()));
        batch.setCdtDbtInd(batchTransactions.get(0).getCreditDebitIndicator());
        entryDetails.setBatch(batch);
        
        // Individual transaction details
        List<TransactionDetails> txnDetailsList = batchTransactions.stream()
            .map(this::mapTransactionDetails)
            .collect(Collectors.toList());
        entryDetails.setTransactions(txnDetailsList);
        
        entry.setEntryDetails(List.of(entryDetails));
        
        return entry;
    }
    
    private TransactionDetails mapTransactionDetails(TransactionRow txn) {
        TransactionDetails details = new TransactionDetails();
        
        // Map all transaction-specific fields
        details.setReferences(mapReferences(txn));
        details.setAmountDetails(mapAmountDetails(txn));
        details.setBankTxCode(mapBankTxCode(txn));
        details.setRelatedParties(mapParties(txn));  // Debtor, Creditor
        details.setRemittanceInfo(mapRemittance(txn));  // Payment references
        // ... etc
        
        return details;
    }
}
```

---

## Critical Design Considerations

### 1. **Entry vs Transaction: What Goes Where?**

**Entry Level (ReportEntry2):**
- Represents a **line on the bank statement**
- For batch: contains **aggregate** amount
- Has booking date, value date, status
- **One entry can contain multiple transactions**

**Transaction Level (EntryTransaction2):**
- Represents the **actual payment/transfer**
- Contains debtor/creditor information
- Contains remittance information (invoice references, etc.)
- Contains detailed amounts (instructed, transaction, counter-value)

**Mapping Rule:**
```
If BATCH_BOOKED = false:
    1 Database Row → 1 Entry → 1 EntryDetails → 1 TransactionDetails

If BATCH_BOOKED = true (with same BATCH_ID):
    N Database Rows → 1 Entry → 1 EntryDetails → N TransactionDetails
                                      └── BatchInformation (sum, count)
```

### 2. **Which Fields to Aggregate for Batch Entries?**

When creating a batch entry from multiple transactions:

| Field | Strategy |
|-------|----------|
| **Amount** | **SUM** all transaction amounts |
| **CreditDebitIndicator** | Use from first txn (all should be same) |
| **BookingDate** | Use from first txn (all should be same) |
| **ValueDate** | Use from first txn or MIN/MAX depending on business rule |
| **Status** | Use from first txn (all should be same in batch) |
| **BankTransactionCode** | Use from first txn or generic batch code |
| **EntryReference** | Use batch ID |

**Validation:** Ensure all transactions in a batch have:
- Same currency
- Same credit/debit indicator
- Same booking date (usually)
- Same status

### 3. **Stored Procedure Design Implications**

Your SP should ideally return:

```sql
SELECT 
    -- Account Level
    a.account_id,
    a.account_iban,
    a.account_currency,
    a.account_name,
    
    -- Entry Level (if available)
    e.entry_id,
    e.booking_date,
    e.value_date,
    e.entry_status,
    
    -- Batch Level
    b.batch_id,
    b.is_batch_booked,  -- Boolean flag
    b.batch_message_id,
    
    -- Transaction Level
    t.transaction_id,
    t.amount,
    t.currency,
    t.credit_debit_ind,
    t.debtor_name,
    t.creditor_name,
    t.remittance_info,
    t.end_to_end_id,
    -- ... all other transaction fields
    
FROM accounts a
LEFT JOIN entries e ON ...
LEFT JOIN batches b ON ...
LEFT JOIN transactions t ON ...

WHERE ...
ORDER BY 
    a.account_id,
    b.batch_id NULLS FIRST,  -- Non-batch entries first
    t.transaction_id
```

**Key Points:**
- Each row = one transaction
- Multiple rows with same `batch_id` → grouped into one Entry
- `is_batch_booked` flag indicates whether to aggregate
- Ordering helps with streaming processing

---

## Updated Canonical Model Classes

### Complete Entry Class

```java
@Data
@Builder
public class Entry {
    // ========================================
    // ENTRY LEVEL (the line on statement)
    // ========================================
    
    /** Entry reference (unique within report/statement) */
    private String entryReference;
    
    /** 
     * Entry amount
     * - For single transaction: transaction amount
     * - For batch: SUM of all transaction amounts
     */
    private Amount amount;
    
    /** Credit or Debit indicator */
    private CreditDebitIndicator creditDebitIndicator;
    
    /** Whether this is a reversal */
    private Boolean reversalIndicator;
    
    /** Entry status: BOOK, PDNG, INFO */
    private EntryStatus status;
    
    /** Date when entry was booked to account */
    private LocalDate bookingDate;
    
    /** Value date (date when amount is available) */
    private LocalDate valueDate;
    
    /** Account servicer reference */
    private String accountServicerReference;
    
    /** Availability of funds */
    private List<Availability> availabilities;
    
    /** Bank transaction code (domain/family/subfamily) */
    private BankTransactionCode bankTransactionCode;
    
    /** Commission waiver indicator */
    private Boolean commissionWaiverIndicator;
    
    /** Amount details (instructed, transaction, counter-value) */
    private AmountDetails amountDetails;
    
    /** Charges at entry level */
    private List<Charges> charges;
    
    /** Technical input channel */
    private TechnicalInputChannel technicalInputChannel;
    
    /** Interest at entry level */
    private List<TransactionInterest> interests;
    
    // ========================================
    // ENTRY DETAILS (transaction breakdown)
    // ========================================
    
    /**
     * Entry details containing:
     * - Batch information (if batch-booked)
     * - Individual transaction details (1..n)
     * 
     * Typically there's only ONE EntryDetails per Entry,
     * but ISO20022 allows multiple (0..unbounded)
     */
    private List<EntryDetails> entryDetails;
    
    /** Additional entry information */
    private String additionalInfo;
    
    // ========================================
    // HELPER METHODS
    // ========================================
    
    /**
     * Check if this entry represents a batch booking
     */
    public boolean isBatchBooked() {
        return entryDetails != null 
            && !entryDetails.isEmpty() 
            && entryDetails.get(0).getBatch() != null;
    }
    
    /**
     * Get total number of transactions in this entry
     */
    public int getTransactionCount() {
        if (entryDetails == null || entryDetails.isEmpty()) {
            return 0;
        }
        return entryDetails.stream()
            .mapToInt(ed -> ed.getTransactions() != null ? ed.getTransactions().size() : 0)
            .sum();
    }
    
    /**
     * Get all transactions flattened from all entry details
     */
    public List<TransactionDetails> getAllTransactions() {
        if (entryDetails == null) {
            return List.of();
        }
        return entryDetails.stream()
            .filter(ed -> ed.getTransactions() != null)
            .flatMap(ed -> ed.getTransactions().stream())
            .collect(Collectors.toList());
    }
}

@Data
@Builder
public class EntryDetails {
    /**
     * Batch information
     * Present ONLY if multiple transactions are batch-booked together
     * NULL for single transactions
     */
    private BatchInformation batch;
    
    /**
     * Individual transaction details
     * - Single transaction: List of size 1
     * - Batch: List of size N
     */
    private List<TransactionDetails> transactions;
    
    /**
     * Check if this represents a batch
     */
    public boolean isBatch() {
        return batch != null;
    }
}

@Data
@Builder
public class BatchInformation {
    /** Message identification of the batch */
    private String messageId;
    
    /** Payment information ID */
    private String paymentInformationId;
    
    /** Number of individual transactions in the batch */
    private Integer numberOfTransactions;
    
    /** Total amount of all transactions (should equal Entry.amount) */
    private Amount totalAmount;
    
    /** Credit/Debit indicator for the batch */
    private CreditDebitIndicator creditDebitIndicator;
}

@Data
@Builder
public class TransactionDetails {
    /** Transaction references (end-to-end, mandate, etc.) */
    private TransactionReferences references;
    
    /** Amount details (instructed, transaction, counter-value) */
    private AmountDetails amountDetails;
    
    /** Availability information */
    private List<Availability> availabilities;
    
    /** Bank transaction code (can override entry-level code) */
    private BankTransactionCode bankTransactionCode;
    
    /** Charges specific to this transaction */
    private List<Charges> charges;
    
    /** Interest specific to this transaction */
    private List<TransactionInterest> interests;
    
    /** Related parties (debtor, creditor, ultimate debtor/creditor) */
    private TransactionParties relatedParties;
    
    /** Related agents (debtor agent, creditor agent, intermediaries) */
    private TransactionAgents relatedAgents;
    
    /** Purpose of the transaction */
    private Purpose purpose;
    
    /** Remittance location */
    private List<RemittanceLocation> relatedRemittanceInfo;
    
    /** Remittance information (structured/unstructured payment references) */
    private RemittanceInformation remittanceInfo;
    
    /** Transaction dates (acceptance, interbank settlement, etc.) */
    private TransactionDates dates;
    
    /** Transaction price */
    private TransactionPrice price;
    
    /** Transaction quantities */
    private List<TransactionQuantity> quantities;
    
    /** Financial instrument identification */
    private SecurityIdentification financialInstrumentId;
    
    /** Tax information */
    private TaxInformation tax;
    
    /** Return/reject information */
    private ReturnInformation returnInfo;
    
    /** Corporate action information */
    private CorporateAction corporateAction;
    
    /** Safekeeping account */
    private SecuritiesAccount safekeepingAccount;
    
    /** Cash deposit information */
    private CashDeposit cashDeposit;
    
    /** Card transaction information */
    private CardTransaction cardTransaction;
    
    /** Additional transaction info */
    private String additionalInfo;
}
```

---

## Example: Processing Flow

### Input: Stored Procedure Results

```
Row 1: ACC001, T001, NULL,  false, 100.00, DBIT, "Supplier A"
Row 2: ACC001, T002, B001,  true,   50.00, CRDT, "Customer X"
Row 3: ACC001, T003, B001,  true,   75.00, CRDT, "Customer Y"
Row 4: ACC001, T004, B001,  true,   25.00, CRDT, "Customer Z"
Row 5: ACC002, T005, NULL,  false, 300.00, DBIT, "Supplier B"
```

### Canonical Model Output

```
CamtDocument
└── AccountReport (ACC001)
    ├── Entry 1 (entryRef: "T001", amount: 100.00 DBIT)
    │   └── EntryDetails
    │       ├── batch: NULL
    │       └── transactions: [1]
    │           └── TransactionDetails (T001, Supplier A)
    │
    ├── Entry 2 (entryRef: "B001", amount: 150.00 CRDT) ← Sum of batch!
    │   └── EntryDetails
    │       ├── batch: BatchInformation
    │       │   ├── messageId: "B001"
    │       │   ├── numberOfTransactions: 3
    │       │   └── totalAmount: 150.00 CRDT
    │       └── transactions: [3]
    │           ├── TransactionDetails (T002, Customer X, 50.00)
    │           ├── TransactionDetails (T003, Customer Y, 75.00)
    │           └── TransactionDetails (T004, Customer Z, 25.00)
    
└── AccountReport (ACC002)
    └── Entry 1 (entryRef: "T005", amount: 300.00 DBIT)
        └── EntryDetails
            ├── batch: NULL
            └── transactions: [1]
                └── TransactionDetails (T005, Supplier B)
```

### ISO20022 XML Output (for CAMT.053)

```xml
<Stmt>
  <Id>STMT001</Id>
  <Acct>
    <Id><IBAN>ACC001</IBAN></Id>
  </Acct>
  
  <!-- Entry 1: Non-batch -->
  <Ntry>
    <Amt Ccy="EUR">100.00</Amt>
    <CdtDbtInd>DBIT</CdtDbtInd>
    <NtryDtls>
      <!-- No Btch element -->
      <TxDtls>
        <!-- Transaction details for T001 -->
        <RltdPties>
          <Cdtr><Nm>Supplier A</Nm></Cdtr>
        </RltdPties>
      </TxDtls>
    </NtryDtls>
  </Ntry>
  
  <!-- Entry 2: Batch -->
  <Ntry>
    <Amt Ccy="EUR">150.00</Amt>
    <CdtDbtInd>CRDT</CdtDbtInd>
    <NtryDtls>
      <Btch>
        <MsgId>B001</MsgId>
        <NbOfTxs>3</NbOfTxs>
        <TtlAmt Ccy="EUR">150.00</TtlAmt>
        <CdtDbtInd>CRDT</CdtDbtInd>
      </Btch>
      <TxDtls>
        <!-- Transaction 1: Customer X -->
        <AmtDtls><TxAmt><Amt Ccy="EUR">50.00</Amt></TxAmt></AmtDtls>
        <RltdPties><Dbtr><Nm>Customer X</Nm></Dbtr></RltdPties>
      </TxDtls>
      <TxDtls>
        <!-- Transaction 2: Customer Y -->
        <AmtDtls><TxAmt><Amt Ccy="EUR">75.00</Amt></TxAmt></AmtDtls>
        <RltdPties><Dbtr><Nm>Customer Y</Nm></Dbtr></RltdPties>
      </TxDtls>
      <TxDtls>
        <!-- Transaction 3: Customer Z -->
        <AmtDtls><TxAmt><Amt Ccy="EUR">25.00</Amt></TxAmt></AmtDtls>
        <RltdPties><Dbtr><Nm>Customer Z</Nm></Dbtr></RltdPties>
      </TxDtls>
    </NtryDtls>
  </Ntry>
</Stmt>
```

---

## Key Takeaways

### ✅ **Yes, batch booking absolutely matters!**

1. **Data Aggregation Required**: Multiple DB rows must be aggregated into single Entry
2. **Two-Level Structure**: Entry (aggregate) + TransactionDetails (individual)
3. **Complex Mapping Logic**: Need grouping, summing, validation
4. **Canonical Model Updated**: Must support both batch and non-batch scenarios

### 🎯 **Canonical Model Benefits Even More Important Now**

1. **Consistent Grouping Logic**: Handle batch aggregation once in canonical mapper
2. **Version Independence**: Batch structure is same across v02, v08, etc.
3. **Testing Easier**: Test batch logic independently from ISO20022 generation
4. **Business Rules**: Apply batch validation rules at canonical level

### 📋 **Implementation Checklist**

- [ ] Update Entry class with EntryDetails list
- [ ] Add BatchInformation class
- [ ] Add TransactionDetails class (detailed transaction info)
- [ ] Implement batch grouping logic in SP mapper
- [ ] Validate batch consistency (same currency, date, etc.)
- [ ] Calculate aggregate amounts correctly
- [ ] Test with mixed batch/non-batch scenarios
- [ ] Ensure ISO20022 mappers handle both cases

---

## Questions for You

1. **Batch Identification**: In your SP, what determines the batch grouping?
   - Is there a `BATCH_ID` column?
   - Or grouped by date + other criteria?
   - How do you identify which transactions belong together?

2. **Batch Constraints**: In your business logic:
   - Must all transactions in a batch have the same currency?
   - Same booking date?
   - Same credit/debit indicator?
   - Any other constraints?

3. **Entry-Level Data**: For batch entries, how do you determine:
   - Which BankTransactionCode to use (first txn, or specific batch code)?
   - ValueDate (min, max, first, last)?
   - Status (should all be same in batch)?

4. **Zero Transactions**: You mentioned "zero to multiple transactions per account"
   - Can an account have NO entries at all in the report?
   - Is that valid for all three message types (052, 053, 054)?

Would you like me to create the complete Java implementation with batch handling logic?
