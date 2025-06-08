// Swing interface implementation
// C:\countBeans\beans\GuiWindow.java

import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import java.io.File;

public class GuiWindow extends JFrame {

    public GuiWindow() throws SQLException {
        setTitle("Bean Counter");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        Database db = new Database(); // 🔥 create it once, share it around

        JPanel invoicePanel = buildInvoiceTab(db);       // pass it in
        JPanel browsePanel = buildBrowseTab(db);         // pass it in

        tabbedPane.addTab("New Invoice", invoicePanel);
        tabbedPane.addTab("Build Report", browsePanel);

        add(tabbedPane);
    }

    private JPanel buildInvoiceTab(Database db) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;


        // Vendor dropdown + Add / Delete buttons
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Vendor:"), gbc);

        // Create the vendor combo box, initially empty (we’ll fill it below)
        gbc.gridx = 1;
        JComboBox<String> vendorBox = new JComboBox<>();
        panel.add(vendorBox, gbc);

        // Add Vendor button
        gbc.gridx = 2;
        JButton addVendorBtn = new JButton("Add Vendor");
        panel.add(addVendorBtn, gbc);

        // Add Delete Vendor button right next to Add Vendor
        gbc.gridx = 3;
        JButton delVendorBtn = new JButton("Delete Vendor");
        panel.add(delVendorBtn, gbc);

        row++;

        // --- Populate vendorBox from DB at startup ---
        try {
            List<String> vendors = db.getAllVendors();  // You'll implement this in Database class
            for (String v : vendors) {
                vendorBox.addItem(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // --- Add Vendor button action ---
        addVendorBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panel, "Enter new vendor name:");
            if (input == null) return;

            String newVendor = input.trim();
            if (newVendor.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Vendor name cannot be empty.");
                return;
            }

            if (isInComboBox(vendorBox, newVendor)) {
                JOptionPane.showMessageDialog(panel, "Vendor already exists.");
                return;
            }

            try {
                db.insertVendor(newVendor);
                vendorBox.addItem(newVendor);
                vendorBox.setSelectedItem(newVendor);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error adding vendor: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // --- Delete Vendor button action ---
        delVendorBtn.addActionListener(e -> {
            String selectedVendor = (String) vendorBox.getSelectedItem();
            if (selectedVendor == null) {
                JOptionPane.showMessageDialog(panel, "No vendor selected to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel,
                "Delete vendor \"" + selectedVendor + "\"?\n(This will NOT delete invoices.)",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    db.deleteVendor(selectedVendor);
                    vendorBox.removeItem(selectedVendor);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, "Error deleting vendor: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // --- Category dropdown + Add/Delete buttons ---
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> categoryBox = new JComboBox<>();
        // Load from DB
        try {
            for (String cat : db.fetchAllCategories()) {
                categoryBox.addItem(cat);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        panel.add(categoryBox, gbc);

        gbc.gridx = 2;
        JPanel catBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JButton addCategoryBtn = new JButton("+");
        JButton delCategoryBtn = new JButton("–");
        catBtnPanel.add(addCategoryBtn);
        catBtnPanel.add(delCategoryBtn);
        panel.add(catBtnPanel, gbc);
        row++;

        // --- Add Category button action ---
        addCategoryBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panel, "Enter new category name:");
            if (input == null) return;

            String newCat = input.trim();
            if (newCat.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Category name cannot be empty.");
                return;
            }

            if (isInComboBox(categoryBox, newCat)) {
                JOptionPane.showMessageDialog(panel, "Category already exists.");
                return;
            }

            try {
                db.insertCategory(newCat);
                categoryBox.addItem(newCat);
                categoryBox.setSelectedItem(newCat);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error adding category: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // --- Delete Category button action ---
        delCategoryBtn.addActionListener(e -> {
            String selectedCat = (String) categoryBox.getSelectedItem();
            if (selectedCat == null) {
                JOptionPane.showMessageDialog(panel, "No category selected to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel,
                "Delete category \"" + selectedCat + "\"?\n(This will NOT delete invoices.)",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    db.deleteCategory(selectedCat);
                    categoryBox.removeItem(selectedCat);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, "Error deleting category: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

  
        // Date Picker
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Issued Date:"), gbc);

        gbc.gridx = 1;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy");
        dateSpinner.setEditor(dateEditor);
        panel.add(dateSpinner, gbc);
        row++;

        // Description field
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 2;
        JTextField descField = new JTextField();
        panel.add(descField, gbc);
        row++; gbc.gridwidth = 1;

        // Amount + Tax checkbox
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(new JLabel("Amount ($):"), gbc);

        gbc.gridx = 1;
        JTextField amountField = new JTextField();
        panel.add(amountField, gbc);

        gbc.gridx = 2;
        JCheckBox taxIncluded = new JCheckBox("Tax Included");
        panel.add(taxIncluded, gbc);
        row++;

        // Submit button
        gbc.gridx = 1; gbc.gridy = row;
        JButton submitBtn = new JButton("Add Invoice");
        panel.add(submitBtn, gbc);

        // Functional action listener for submit
        submitBtn.addActionListener(e -> {
            try {
                String vendor = (String) vendorBox.getSelectedItem();
                String category = (String) categoryBox.getSelectedItem();
                java.util.Date issuedDate = dateModel.getDate();
                String description = descField.getText();
                double amount = Double.parseDouble(amountField.getText());
                boolean isTax = taxIncluded.isSelected();

                Invoice invoice = new Invoice(
                    0, // id = 0 or whatever you want before DB assigns it
                    vendor,
                    category,
                    issuedDate.getTime(),  // <-- raw long milliseconds here
                    description,
                    amount,
                    isTax
                );

                db.insertInvoice(invoice);

                JOptionPane.showMessageDialog(panel, "Invoice added!");
                
                // Clear inputs for next entry
                descField.setText("");
                amountField.setText("");
                taxIncluded.setSelected(false);
                dateModel.setValue(new java.util.Date());

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel buildBrowseTab(Database db) {
        JPanel panel = new JPanel(new BorderLayout());

        // -- Filter Panel
        JPanel filterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Vendor
        gbc.gridx = 0; gbc.gridy = row;
        filterPanel.add(new JLabel("Vendor:"), gbc);
        gbc.gridx = 1;
        JComboBox<String> vendorBox = new JComboBox<>();
        vendorBox.addItem(""); // Empty for no filter
        // Fill with data from DB
        // ... (we’ll handle this below)
        filterPanel.add(vendorBox, gbc);

        // Category
        gbc.gridx = 2;
        filterPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 3;
        JComboBox<String> categoryBox = new JComboBox<>();
        categoryBox.addItem("");
        // Fill with data from DB
        filterPanel.add(categoryBox, gbc);
        row++;

        // Amount
        gbc.gridx = 0; gbc.gridy = row;
        filterPanel.add(new JLabel("Min Amount:"), gbc);
        gbc.gridx = 1;
        JTextField minAmountField = new JTextField();
        filterPanel.add(minAmountField, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("Max Amount:"), gbc);
        gbc.gridx = 3;
        JTextField maxAmountField = new JTextField();
        filterPanel.add(maxAmountField, gbc);
        row++;

        // Dates
        gbc.gridx = 0; gbc.gridy = row;
        filterPanel.add(new JLabel("From Date:"), gbc);
        gbc.gridx = 1;
        JSpinner fromDate = new JSpinner(new SpinnerDateModel());
        fromDate.setEditor(new JSpinner.DateEditor(fromDate, "yyyy-MM-dd"));
        filterPanel.add(fromDate, gbc);

        gbc.gridx = 2;
        filterPanel.add(new JLabel("To Date:"), gbc);
        gbc.gridx = 3;
        JSpinner toDate = new JSpinner(new SpinnerDateModel());
        toDate.setEditor(new JSpinner.DateEditor(toDate, "yyyy-MM-dd"));
        filterPanel.add(toDate, gbc);
        row++;

        // Search button
        gbc.gridx = 1; gbc.gridy = row;
        JButton searchBtn = new JButton("Search");
        filterPanel.add(searchBtn, gbc);

        gbc.gridy = row + 1;   // one row below your search button line

        // Label
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.EAST;
        filterPanel.add(new JLabel("Export Title:"), gbc);

        // Text field right next to label
        gbc.gridx = 2;
        gbc.anchor = GridBagConstraints.WEST;
        JTextField exportTitleField = new JTextField(20);  // width can be adjusted
        filterPanel.add(exportTitleField, gbc);

        // Export button next to text field
        gbc.gridx = 3;
        JButton exportBtn = new JButton("Export to Excel");
        filterPanel.add(exportBtn, gbc);

        panel.add(filterPanel, BorderLayout.NORTH);

        // Result table
        JTable resultTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(resultTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Hook up filter logic
        searchBtn.addActionListener(e -> {
            try {
                List<Invoice> results = getFilteredInvoices(db, vendorBox, categoryBox, fromDate, toDate, minAmountField, maxAmountField);
                updateTable(resultTable, results);  // Already raw, no tax stuff here
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });


        exportBtn.addActionListener(e -> {
            try {
                // Grab the export title from the textbox if you want to use it for something
                String exportTitle = exportTitleField.getText().trim();
                if (exportTitle.isEmpty()) {
                    exportTitle = "Invoices";  // fallback title
                }

                // Fetch your invoices from the table or your model.
                // If you have your last search results cached somewhere, use those.
                List<Invoice> invoices = getFilteredInvoices(db, vendorBox, categoryBox, fromDate, toDate, minAmountField, maxAmountField);

                // Choose where to save the file
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setSelectedFile(new File(exportTitle + ".xls"));  // default filename
                int option = fileChooser.showSaveDialog(panel);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    ExcelExporter.exportInvoices(invoices, file);
                    JOptionPane.showMessageDialog(panel, "Exported successfully to " + file.getAbsolutePath());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Export failed: " + ex.getMessage());
            }
        });





        return panel;
    }

    // HELPER CLASSES
    private double parseDouble(String val, double fallback) {
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            return fallback;
        }
    }

    private void updateTable(JTable table, List<Invoice> data) {
        String[] columns = {"ID", "Vendor", "Category", "Date", "Description", "Amount", "Tax"};
        String[][] rows = new String[data.size()][columns.length];
        for (int i = 0; i < data.size(); i++) {
            Invoice inv = data.get(i);
            rows[i][0] = String.valueOf(inv.getId());
            rows[i][1] = inv.getVendor();
            rows[i][2] = inv.getCategory();
            rows[i][3] = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(inv.getIssuedDate()));
            rows[i][4] = inv.getDescription();
            rows[i][5] = String.format("%.2f", inv.getAmount());
            rows[i][6] = inv.isTaxIncluded() ? "Yes" : "No";
        }
        table.setModel(new javax.swing.table.DefaultTableModel(rows, columns));
    }

    private boolean isInComboBox(JComboBox<String> box, String item) {
        for (int i = 0; i < box.getItemCount(); i++) {
            if (box.getItemAt(i).equalsIgnoreCase(item)) return true;
        }
        return false;
    }

    private List<Invoice> getFilteredInvoices(Database db, JComboBox<String> vendorBox, JComboBox<String> categoryBox,
                                            JSpinner fromDate, JSpinner toDate,
                                            JTextField minAmountField, JTextField maxAmountField) throws Exception {

        String vendor = vendorBox.getSelectedItem().toString();
        String category = categoryBox.getSelectedItem().toString();
        long fromTs = ((Date) fromDate.getValue()).getTime();
        long toTs = ((Date) toDate.getValue()).getTime();
        double minAmt = parseDouble(minAmountField.getText(), 0);
        double maxAmt = parseDouble(maxAmountField.getText(), Double.MAX_VALUE);

        List<Invoice> results = db.fetchInvoices(vendor, category, fromTs, toTs);
        results.removeIf(inv -> inv.getAmount() < minAmt || inv.getAmount() > maxAmt);
        return results;
    }


}
