// Database implementation for managing invoices
// C:\countBeans\beans\Database.java

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String DB_URL = "jdbc:sqlite:invoices.db";
    private Connection conn;

    public Database() throws SQLException {
        connect();
        createTableIfNotExists();
    }

    private void connect() throws SQLException {
        conn = DriverManager.getConnection(DB_URL);
    }

    private void createTableIfNotExists() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Vendors table
            String vendorTable = "CREATE TABLE IF NOT EXISTS vendors (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL)";
            stmt.execute(vendorTable);

            // Categories table
            String categoryTable = "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL)";
            stmt.execute(categoryTable);

            // Invoices table
            String invoiceTable = "CREATE TABLE IF NOT EXISTS invoices (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "vendor TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "issued_date INTEGER NOT NULL, " + // UNIX timestamp
                    "description TEXT, " +
                    "amount REAL NOT NULL, " +
                    "tax_included INTEGER NOT NULL, " +
                    "non_taxable INTEGER NOT NULL DEFAULT 0)"; // <== added here
            stmt.execute(invoiceTable);

            // Clients table
            String clientTable = "CREATE TABLE IF NOT EXISTS clients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT UNIQUE NOT NULL)";
            stmt.execute(clientTable);

            // Income table
            String incomeTable = "CREATE TABLE IF NOT EXISTS income_invoices (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "client TEXT NOT NULL, " +
                    "category TEXT NOT NULL, " +
                    "issued_date INTEGER NOT NULL, " +
                    "description TEXT, " +
                    "amount REAL NOT NULL, " +
                    "tax_included INTEGER NOT NULL, " +
                    "non_taxable INTEGER NOT NULL DEFAULT 0)";
            stmt.execute(incomeTable);

        }
    }

    // INVOICES
    // CREATE
    public void insertInvoice(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (vendor, category, issued_date, description, amount, tax_included, non_taxable) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, invoice.getVendor());
            pstmt.setString(2, invoice.getCategory());
            pstmt.setLong(3, invoice.getIssuedDate());
            pstmt.setString(4, invoice.getDescription());
            pstmt.setDouble(5, invoice.getAmount());
            pstmt.setInt(6, invoice.isTaxIncluded() ? 1 : 0);
            pstmt.setInt(7, invoice.isNonTaxable() ? 1 : 0); // <== new field here
            pstmt.executeUpdate();
        }

    }

    // READ (basic filter support)
    public List<Invoice> fetchInvoices(String vendorFilter, String categoryFilter, Long fromTimestamp, Long toTimestamp)
            throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM invoices WHERE 1=1");

        if (vendorFilter != null && !vendorFilter.isEmpty()) {
            sql.append(" AND vendor = ?");
        }
        if (categoryFilter != null && !categoryFilter.isEmpty()) {
            sql.append(" AND category = ?");
        }
        if (fromTimestamp != null) {
            sql.append(" AND issued_date >= ?");
        }
        if (toTimestamp != null) {
            sql.append(" AND issued_date <= ?");
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int idx = 1;
            if (vendorFilter != null && !vendorFilter.isEmpty()) {
                pstmt.setString(idx++, vendorFilter);
            }
            if (categoryFilter != null && !categoryFilter.isEmpty()) {
                pstmt.setString(idx++, categoryFilter);
            }
            if (fromTimestamp != null) {
                pstmt.setLong(idx++, fromTimestamp);
            }
            if (toTimestamp != null) {
                pstmt.setLong(idx++, toTimestamp);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Invoice inv = new Invoice(
                        rs.getInt("id"),
                        rs.getString("vendor"),
                        rs.getString("category"),
                        rs.getLong("issued_date"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getInt("tax_included") == 1,
                        rs.getInt("non_taxable") == 1 // <== here
                );

                invoices.add(inv);
            }
        }

        return invoices;
    }

    // UPDATE
    public void updateInvoice(Invoice invoice) throws SQLException {
        String sql = "UPDATE invoices SET vendor=?, category=?, issued_date=?, description=?, amount=?, tax_included=?, non_taxable=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, invoice.getVendor());
            pstmt.setString(2, invoice.getCategory());
            pstmt.setLong(3, invoice.getIssuedDate());
            pstmt.setString(4, invoice.getDescription());
            pstmt.setDouble(5, invoice.getAmount());
            pstmt.setInt(6, invoice.isTaxIncluded() ? 1 : 0);
            pstmt.setInt(7, invoice.isNonTaxable() ? 1 : 0); // <== new field
            pstmt.setInt(8, invoice.getId());
            pstmt.executeUpdate();
        }

    }

    // DELETE
    public void deleteInvoice(int id) throws SQLException {
        String sql = "DELETE FROM invoices WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // INCOMES
    // CREATE
    public void insertIncome(Income income) throws SQLException {
        String sql = "INSERT INTO income_invoices (client, category, issued_date, description, amount, tax_included, non_taxable) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, income.getClient());
            pstmt.setString(2, income.getCategory());
            pstmt.setLong(3, income.getIssuedDate());
            pstmt.setString(4, income.getDescription());
            pstmt.setDouble(5, income.getAmount());
            pstmt.setInt(6, income.isTaxIncluded() ? 1 : 0);
            pstmt.setInt(7, income.isNonTaxable() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }

    // READ
    public List<Income> fetchIncomes(Long fromTimestamp, Long toTimestamp) throws SQLException {
        List<Income> incomes = new ArrayList<>();
        String sql = "SELECT * FROM income_invoices WHERE issued_date >= ? AND issued_date <= ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, fromTimestamp);
            pstmt.setLong(2, toTimestamp);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Income income = new Income(
                        rs.getInt("id"),
                        rs.getString("client"),
                        rs.getString("category"),
                        rs.getLong("issued_date"),
                        rs.getString("description"),
                        rs.getDouble("amount"),
                        rs.getInt("tax_included") == 1,
                        rs.getInt("non_taxable") == 1);
                incomes.add(income);
            }
        }

        return incomes;
    }

    // UPDATE
    public void updateIncome(Income income) throws SQLException {
        String sql = "UPDATE income_invoices SET client=?, category=?, issued_date=?, description=?, amount=?, tax_included=?, non_taxable=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, income.getClient());
            pstmt.setString(2, income.getCategory());
            pstmt.setLong(3, income.getIssuedDate());
            pstmt.setString(4, income.getDescription());
            pstmt.setDouble(5, income.getAmount());
            pstmt.setInt(6, income.isTaxIncluded() ? 1 : 0);
            pstmt.setInt(7, income.isNonTaxable() ? 1 : 0);
            pstmt.setInt(8, income.getId());
            pstmt.executeUpdate();
        }
    }

    // DELETE
    public void deleteIncome(int id) throws SQLException {
        String sql = "DELETE FROM income_invoices WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // CLOSE CONNECTION
    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    // Get all vendors as list of strings
    public List<String> getAllVendors() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM vendors ORDER BY name";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        }
        return list;
    }

    public List<String> getAllClients() throws SQLException {
        List<String> list = new ArrayList<>();
        String sql = "SELECT name FROM clients ORDER BY name";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(rs.getString("name"));
            }
        }
        return list;
    }

    public void insertClient(String name) throws SQLException {
        String sql = "INSERT OR IGNORE INTO clients (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public void deleteClient(String name) throws SQLException {
        String sql = "DELETE FROM clients WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    // Insert a new vendor
    public void insertVendor(String name) throws SQLException {
        String sql = "INSERT OR IGNORE INTO vendors (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    // Delete vendor by name
    public void deleteVendor(String name) throws SQLException {
        String sql = "DELETE FROM vendors WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public void insertCategory(String name) throws SQLException {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public void deleteCategory(String name) throws SQLException {
        String sql = "DELETE FROM categories WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
        }
    }

    public List<String> fetchAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories ORDER BY name ASC";
        try (Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        }
        return categories;
    }

}
