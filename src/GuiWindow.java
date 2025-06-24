// Swing interface implementation
// C:\countBeans\beans\GuiWindow.java

import java.awt.*;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class GuiWindow extends JFrame {

    public GuiWindow() throws SQLException {
        setTitle("Conte Beans 0.1ALPHA");
        setSize(800, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        Database db = new Database();

        DefaultComboBoxModel<String> sharedCategoryModel = new DefaultComboBoxModel<>();
        for (String cat : db.fetchAllCategories()) {
            sharedCategoryModel.addElement(cat);
        }

        JPanel invoicePanel = buildInvoiceTab(db, sharedCategoryModel);
        JPanel incomePanel = buildIncomeTab(db, sharedCategoryModel);
        JPanel browsePanel = buildBrowseTab(db, sharedCategoryModel);

        tabbedPane.addTab("New Invoice", invoicePanel);
        tabbedPane.addTab("New Income", incomePanel);
        tabbedPane.addTab("Build Report", browsePanel);

        add(tabbedPane);
    }

    private JPanel buildInvoiceTab(Database db, DefaultComboBoxModel<String> sharedCategoryModel) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Vendor dropdown + Add / Delete buttons
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Vendor:"), gbc);

        // Vendor dropdown + Add/Delete buttons compacted
        JComboBox<String> vendorBox = new JComboBox<>();
        JButton addVendorBtn = new JButton("Add Vendor");
        JButton delVendorBtn = new JButton("Delete Vendor");

        gbc.gridy = row;
        gbc.insets = new Insets(6, 6, 6, 6); // Keep your padding consistent

        // Vendor label
        gbc.gridx = 0;
        panel.add(new JLabel("Vendor:"), gbc);

        // Vendor combo box
        gbc.gridx = 1;
        panel.add(vendorBox, gbc);

        // Add Vendor button
        gbc.gridx = 2;
        panel.add(addVendorBtn, gbc);

        // Delete Vendor button
        gbc.gridx = 3;
        panel.add(delVendorBtn, gbc);

        row++;

        // --- Populate vendorBox from DB at startup ---
        try {
            List<String> vendors = db.getAllVendors(); // You'll implement this in Database class
            for (String v : vendors) {
                vendorBox.addItem(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // --- Add Vendor button action ---
        addVendorBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panel, "Enter new vendor name:");
            if (input == null)
                return;

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
                    JOptionPane.YES_NO_OPTION);

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
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> categoryBox = new JComboBox<>(sharedCategoryModel);
        try {
            for (String cat : db.fetchAllCategories()) {
                categoryBox.addItem(cat);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        panel.add(categoryBox, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 2; // Allow buttons panel to span 2 columns and get more space
        JPanel catBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton addCategoryBtn = new JButton("Add category");
        JButton delCategoryBtn = new JButton("Remove category");
        catBtnPanel.add(addCategoryBtn);
        catBtnPanel.add(delCategoryBtn);
        panel.add(catBtnPanel, gbc);
        gbc.gridwidth = 1; // Reset gridwidth

        row++;

        // --- Add Category button action ---
        addCategoryBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panel, "Enter new category name:");
            if (input == null)
                return;

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
                    JOptionPane.YES_NO_OPTION);

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
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Issued Date:"), gbc);

        gbc.gridx = 1;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy/MM/dd");
        dateSpinner.setEditor(dateEditor);
        panel.add(dateSpinner, gbc);
        row++;

        // Description field
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JTextField descField = new JTextField();
        panel.add(descField, gbc);
        row++;
        gbc.gridwidth = 1;

        // Amount + Tax checkbox
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Amount ($):"), gbc);

        gbc.gridx = 1;
        JTextField amountField = new JTextField();
        panel.add(amountField, gbc);

        gbc.gridx = 2;
        JCheckBox taxIncluded = new JCheckBox("Tax Included");
        panel.add(taxIncluded, gbc);

        gbc.gridx = 3;
        JCheckBox noTax = new JCheckBox("Non-Taxable");
        panel.add(noTax, gbc);
        row++;

        noTax.addItemListener(e -> {
            if (noTax.isSelected()) {
                taxIncluded.setEnabled(false);
                taxIncluded.setSelected(false);
            } else {
                taxIncluded.setEnabled(true);
            }
        });

        // Submit button
        gbc.gridx = 1;
        gbc.gridy = row;
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
                boolean nonTaxableFlag = noTax.isSelected();

                Invoice invoice = new Invoice(
                        0, // id = 0 or whatever before DB assigns
                        vendor,
                        category,
                        issuedDate.getTime(),
                        description,
                        amount,
                        isTax,
                        nonTaxableFlag);

                db.insertInvoice(invoice);

                JOptionPane.showMessageDialog(panel, "Invoice added!");

                // Clear inputs for next entry
                descField.setText("");
                amountField.setText("");
                taxIncluded.setSelected(false);
                noTax.setSelected(false); // <== reset nonTax checkbox too
                dateModel.setValue(new java.util.Date());

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private JPanel buildIncomeTab(Database db, DefaultComboBoxModel<String> sharedCategoryModel) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Client Dropdown
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Client:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> clientBox = new JComboBox<>();
        panel.add(clientBox, gbc);

        gbc.gridx = 2;
        JButton addClientBtn = new JButton("Add Client");
        panel.add(addClientBtn, gbc);

        gbc.gridx = 3;
        JButton delClientBtn = new JButton("Delete Client");
        panel.add(delClientBtn, gbc);

        row++;

        // -- Load from DB
        try {
            for (String c : db.getAllClients()) {
                clientBox.addItem(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addClientBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(panel, "Enter new client name:");
            if (input == null)
                return;

            String newClient = input.trim();
            if (newClient.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Client name cannot be empty.");
                return;
            }

            if (isInComboBox(clientBox, newClient)) {
                JOptionPane.showMessageDialog(panel, "Client already exists.");
                return;
            }

            try {
                db.insertClient(newClient);
                clientBox.addItem(newClient);
                clientBox.setSelectedItem(newClient);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(panel, "Error adding client: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        delClientBtn.addActionListener(e -> {
            String selectedClient = (String) clientBox.getSelectedItem();
            if (selectedClient == null) {
                JOptionPane.showMessageDialog(panel, "No client selected to delete.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Delete client \"" + selectedClient + "\"?\n(This will NOT delete income records.)",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    db.deleteClient(selectedClient);
                    clientBox.removeItem(selectedClient);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(panel, "Error deleting client: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });

        // --- Category
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> categoryBox = new JComboBox<>(sharedCategoryModel);
        panel.add(categoryBox, gbc);
        gbc.gridx = 2;
        JButton addCatBtn = new JButton("Add Category");
        panel.add(addCatBtn, gbc);
        gbc.gridx = 3;
        JButton delCatBtn = new JButton("Delete Category");
        panel.add(delCatBtn, gbc);
        row++;

        try {
            for (String cat : db.fetchAllCategories()) {
                categoryBox.addItem(cat);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // --- Issued Date
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Issued Date:"), gbc);
        gbc.gridx = 1;
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy/MM/dd"));
        panel.add(dateSpinner, gbc);
        row++;

        // --- Description
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        JTextField descField = new JTextField();
        panel.add(descField, gbc);
        gbc.gridwidth = 1;
        row++;

        // --- Amount, Tax, Non-Taxable
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Amount ($):"), gbc);

        gbc.gridx = 1;
        JTextField amountField = new JTextField();
        panel.add(amountField, gbc);

        gbc.gridx = 2;
        JCheckBox taxIncluded = new JCheckBox("Tax Included");
        panel.add(taxIncluded, gbc);

        gbc.gridx = 3;
        JCheckBox noTax = new JCheckBox("Non-Taxable");
        panel.add(noTax, gbc);

        noTax.addItemListener(e -> {
            if (noTax.isSelected()) {
                taxIncluded.setEnabled(false);
                taxIncluded.setSelected(false);
            } else {
                taxIncluded.setEnabled(true);
            }
        });

        row++;

        // --- Submit button
        gbc.gridx = 1;
        gbc.gridy = row;
        JButton submitBtn = new JButton("Add Income");
        panel.add(submitBtn, gbc);

        submitBtn.addActionListener(e -> {
            try {
                String client = (String) clientBox.getSelectedItem();
                String category = (String) categoryBox.getSelectedItem();
                java.util.Date issuedDate = dateModel.getDate();
                String description = descField.getText();
                double amount = Double.parseDouble(amountField.getText());
                boolean isTax = taxIncluded.isSelected();
                boolean nonTaxable = noTax.isSelected();

                Income income = new Income(
                        0,
                        client,
                        category,
                        issuedDate.getTime(),
                        description,
                        amount,
                        isTax,
                        nonTaxable);

                db.insertIncome(income);
                JOptionPane.showMessageDialog(panel, "Income added!");

                descField.setText("");
                amountField.setText("");
                taxIncluded.setSelected(false);
                noTax.setSelected(false);
                dateModel.setValue(new java.util.Date());

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

private JPanel buildBrowseTab(Database db, DefaultComboBoxModel<String> sharedCategoryModel) {
    JPanel panel = new JPanel(new BorderLayout());

    JPanel filterPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    int row = 0;

    // --- Header info inputs
    gbc.gridx = 0; gbc.gridy = row;
    filterPanel.add(new JLabel("Company Name:"), gbc);
    gbc.gridx = 1;
    JTextField companyField = new JTextField(20);
    filterPanel.add(companyField, gbc);

    gbc.gridx = 2;
    filterPanel.add(new JLabel("Address:"), gbc);
    gbc.gridx = 3;
    JTextField addressField = new JTextField(20);
    filterPanel.add(addressField, gbc);

    row++;
    gbc.gridy = row; gbc.gridx = 0;
    filterPanel.add(new JLabel("Contact:"), gbc);
    gbc.gridx = 1;
    JTextField contactField = new JTextField(20);
    filterPanel.add(contactField, gbc);

    gbc.gridx = 2;
    filterPanel.add(new JLabel("Phone:"), gbc);
    gbc.gridx = 3;
    JTextField phoneField = new JTextField(15);
    filterPanel.add(phoneField, gbc);

    row++;
    gbc.gridy = row; gbc.gridx = 0;
    filterPanel.add(new JLabel("Email:"), gbc);
    gbc.gridx = 1;
    JTextField emailField = new JTextField(20);
    filterPanel.add(emailField, gbc);

    gbc.gridx = 2;
    filterPanel.add(new JLabel("Report Title:"), gbc);
    gbc.gridx = 3;
    JTextField reportTitleField = new JTextField(20);
    filterPanel.add(reportTitleField, gbc);

    row++;
    gbc.gridy = row; gbc.gridx = 1;
    JButton saveInfoBtn = new JButton("Save Info");
    filterPanel.add(saveInfoBtn, gbc);

    row++;
    gbc.gridy = row; gbc.gridx = 0;
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
    gbc.gridy = row; gbc.gridx = 0;
    filterPanel.add(new JLabel("Export Title:"), gbc);
    gbc.gridx = 1;
    JTextField exportTitleField = new JTextField(20);
    filterPanel.add(exportTitleField, gbc);

    gbc.gridx = 3;
    JButton exportBtn = new JButton("Export to Excel");
    filterPanel.add(exportBtn, gbc);

    panel.add(filterPanel, BorderLayout.NORTH);

    // --- Save header info
    saveInfoBtn.addActionListener(e -> {
        try {
            Properties props = new Properties();
            props.setProperty("company", companyField.getText());
            props.setProperty("address", addressField.getText());
            props.setProperty("contact", contactField.getText());
            props.setProperty("phone", phoneField.getText());
            props.setProperty("email", emailField.getText());
            props.setProperty("reportTitle", reportTitleField.getText());
            try (FileOutputStream out = new FileOutputStream("report_info.properties")) {
                props.store(out, "Report Header Info");
            }
            JOptionPane.showMessageDialog(panel, "Info saved successfully!");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(panel, "Failed to save info: " + ex.getMessage());
        }
    });

    // --- Load saved header info
    try {
        File file = new File("report_info.properties");
        if (file.exists()) {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            }
            companyField.setText(props.getProperty("company", ""));
            addressField.setText(props.getProperty("address", ""));
            contactField.setText(props.getProperty("contact", ""));
            phoneField.setText(props.getProperty("phone", ""));
            emailField.setText(props.getProperty("email", ""));
            reportTitleField.setText(props.getProperty("reportTitle", ""));
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }

    // --- Export logic
    exportBtn.addActionListener(e -> {
        try {
            String exportTitle = exportTitleField.getText().trim();
            if (exportTitle.isEmpty()) exportTitle = "Invoices";

            long fromTs = ((java.util.Date) fromDate.getValue()).getTime();
            long toTs = ((java.util.Date) toDate.getValue()).getTime();

            List<Invoice> invoices = db.fetchInvoices(null, null, fromTs, toTs);
            List<Income> incomes = db.fetchIncomes(fromTs, toTs);

            Map<String, String> headerInfo = new LinkedHashMap<>();
            headerInfo.put("Company", companyField.getText());
            headerInfo.put("Address", addressField.getText());
            headerInfo.put("Contact", contactField.getText());
            headerInfo.put("Phone", phoneField.getText());
            headerInfo.put("Email", emailField.getText());
            headerInfo.put("Report Title", reportTitleField.getText());

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(exportTitle + ".xls"));
            int option = fileChooser.showSaveDialog(panel);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                ExcelExporter.exportReport(invoices, incomes, file, headerInfo);
                JOptionPane.showMessageDialog(panel, "Exported to " + file.getAbsolutePath());
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
        String[] columns = { "ID", "Vendor", "Category", "Date", "Description", "Amount", "Tax" };
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
            if (box.getItemAt(i).equalsIgnoreCase(item))
                return true;
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
