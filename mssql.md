Got it — **no `sqlcmd`, no `bcp`, only SSMS access in environment A**. That constraint changes things a bit, but you still have a couple of *clean, practical* options 👍
I’ll walk you from **most practical → most robust**, given real-world SSMS-only access.

---

## 🏆 **Best approach with SSMS-only access: “Results to File” (CSV) → BULK INSERT**

This is honestly the most common solution in locked-down environments.

---

## **Step 1: Export stored procedure results WITH headers from SSMS**

### In SSMS (Environment A):

1. Open a **New Query**
2. Run:

   ```sql
   EXEC dbo.YourStoredProcedure;
   ```
3. **Before executing**, do:

   * `Query` → `Results To` → **Results to File** (Ctrl+Shift+F)
4. Click **Execute**
5. Save as:

   ```
   output.csv
   ```

### ✔ Why this works

* SSMS **includes column headers**
* File is standard CSV (or tab-delimited depending on settings)
* No extra permissions required

---

### 🔧 (Important) Make sure SSMS is set to CSV-friendly output

Go to:

```
Tools → Options → Query Results → SQL Server → Results to Text
```

Set:

* **Output format:** Custom delimiter
* **Delimiter:** `,`
* **Include column headers in the result set:** ✅ checked
* **Maximum characters per column:** 65535

This avoids truncated data and weird formatting.

---

## **Step 2: Copy file into Docker SQL Server container**

```bash
docker cp output.csv mssql_container:/var/opt/mssql/data/output.csv
```

---

## **Step 3: Import into local Docker SQL Server**

### Create target table first

```sql
CREATE TABLE dbo.TargetTable (
    col1 INT,
    col2 VARCHAR(200),
    col3 DATETIME
);
```

### Import data

```sql
BULK INSERT dbo.TargetTable
FROM '/var/opt/mssql/data/output.csv'
WITH (
    FIRSTROW = 2,            -- skip header
    FIELDTERMINATOR = ',',
    ROWTERMINATOR = '\n',
    TABLOCK
);
```

✔ Fast
✔ Container-friendly
✔ Repeatable

---

## 🧠 **If CSV gives you trouble (commas, quotes, newlines)**

### Safer alternative: **Tab-delimited**

In SSMS options:

* Delimiter → `\t` (tab)

Then:

```sql
BULK INSERT dbo.TargetTable
FROM '/var/opt/mssql/data/output.tsv'
WITH (
    FIRSTROW = 2,
    FIELDTERMINATOR = '\t',
    ROWTERMINATOR = '\n'
);
```

💡 **Tab-delimited is MUCH more reliable** than CSV for SQL exports.

---

## ⚠️ Common pitfalls to watch for

| Issue          | Fix                                                       |
| -------------- | --------------------------------------------------------- |
| Data truncated | Increase “Maximum characters per column”                  |
| Extra spaces   | Enable `-W` equivalent (trim spaces) → SSMS already trims |
| Unicode text   | Use `VARCHAR` or switch to `DATAFILETYPE = 'widechar'`    |
| CRLF issues    | Use `ROWTERMINATOR = '0x0A'`                              |

---

## 🔁 **If this is recurring (not one-time)**

Best long-term approach:

* Create a **view** that mirrors the SP output
* Export via SSMS
* Import via script

This avoids SP logic changes breaking exports later.

---

## 🧩 **Summary**

Given your constraints, the **best approach is**:

> **SSMS “Results to File” → CSV/TSV → docker cp → BULK INSERT**

It’s:

* Permission-friendly
* Simple
* Proven in locked-down environments

---

If you want, tell me:

* Approx row count
* Any text columns with commas/newlines
* Windows or Mac laptop

I’ll fine-tune the import settings so it works first try 👌
