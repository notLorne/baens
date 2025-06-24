public class Invoice implements ExcelExporter.FinancialEntry {
    private int id;
    private String vendor;
    private String category;
    private long issuedDate;  // stored as UNIX timestamp (milliseconds)
    private String description;
    private double amount;
    private boolean taxIncluded;
    private boolean nonTaxable;

    // Constructor
    public Invoice(int id, String vendor, String category, long issuedDate,
                   String description, double amount, boolean taxIncluded, boolean nonTaxable) {
        this.id = id;
        this.vendor = vendor;
        this.category = category;
        this.issuedDate = issuedDate;
        this.description = description;
        this.amount = amount;
        this.taxIncluded = taxIncluded;
        this.nonTaxable = nonTaxable;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getIssuedDate() { return issuedDate; }
    public void setIssuedDate(long issuedDate) { this.issuedDate = issuedDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public boolean isTaxIncluded() { return taxIncluded; }
    public void setTaxIncluded(boolean taxIncluded) { this.taxIncluded = taxIncluded; }

    public boolean isNonTaxable() { return nonTaxable; }
    public void setNonTaxable(boolean nonTaxable) { this.nonTaxable = nonTaxable; }
}
