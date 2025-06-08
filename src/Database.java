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
                                "issued_date INTEGER NOT NULL, " +  // UNIX timestamp
                                "description TEXT, " +
                                "amount REAL NOT NULL, " +
                                "tax_included INTEGER NOT NULL)";
            stmt.execute(invoiceTable);
        }
    }


    // CREATE
    public void insertInvoice(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (vendor, category, issued_date, description, amount, tax_included) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, invoice.getVendor());
            pstmt.setString(2, invoice.getCategory());
            pstmt.setLong(3, invoice.getIssuedDate());
            pstmt.setString(4, invoice.getDescription());
            pstmt.setDouble(5, invoice.getAmount());
            pstmt.setInt(6, invoice.isTaxIncluded() ? 1 : 0);
            pstmt.executeUpdate();
        }
    }

    // READ (basic filter support)
    public List<Invoice> fetchInvoices(String vendorFilter, String categoryFilter, Long fromTimestamp, Long toTimestamp) throws SQLException {
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
                    rs.getInt("tax_included") == 1
                );
                invoices.add(inv);
            }
        }

        return invoices;
    }

    // UPDATE
    public void updateInvoice(Invoice invoice) throws SQLException {
        String sql = "UPDATE invoices SET vendor=?, category=?, issued_date=?, description=?, amount=?, tax_included=? WHERE id=?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, invoice.getVendor());
            pstmt.setString(2, invoice.getCategory());
            pstmt.setLong(3, invoice.getIssuedDate());
            pstmt.setString(4, invoice.getDescription());
            pstmt.setDouble(5, invoice.getAmount());
            pstmt.setInt(6, invoice.isTaxIncluded() ? 1 : 0);
            pstmt.setInt(7, invoice.getId());
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
