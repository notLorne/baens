import java.io.File;
import java.util.List;
import java.util.Map;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Colour;
import jxl.write.*;

public class ExcelExporter {

    private static final double GST_RATE = 0.05;
    private static final double QST_RATE = 0.09975;

    // Create reusable styles
    private static WritableCellFormat headerFormat;
    private static WritableCellFormat moneyFormat;
    private static WritableCellFormat moneyBoldFormat;
    private static WritableCellFormat normalFormat;

    static {
        try {
            WritableFont headerFont = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD);
            headerFormat = new WritableCellFormat(headerFont);
            headerFormat.setBackground(Colour.GRAY_25);
            headerFormat.setAlignment(Alignment.CENTRE);
            headerFormat.setWrap(true);

            NumberFormat nf = new NumberFormat("$#,##0.00");
            moneyFormat = new WritableCellFormat(nf);
            moneyFormat.setAlignment(Alignment.RIGHT);

            WritableFont moneyBoldFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
            moneyBoldFormat = new WritableCellFormat(moneyBoldFont, nf);
            moneyBoldFormat.setAlignment(Alignment.RIGHT);

            WritableFont normalFont = new WritableFont(WritableFont.ARIAL, 10, WritableFont.NO_BOLD);
            normalFormat = new WritableCellFormat(normalFont);
            normalFormat.setWrap(true);

        } catch (Exception e) {
            // fail quietly, fallback to defaults
            e.printStackTrace();
        }
    }

    private static TaxBreakdown calculateTaxes(double amount, boolean taxIncluded, boolean nonTaxable) {
        if (nonTaxable) return new TaxBreakdown(amount, 0, 0);
        double base, gst, qst;
        if (taxIncluded) {
            double factor = 1 + GST_RATE + (1 + GST_RATE) * QST_RATE;
            base = amount / factor;
        } else {
            base = amount;
        }
        gst = base * GST_RATE;
        qst = (base + gst) * QST_RATE;
        return new TaxBreakdown(base, gst, qst);
    }

    public static void exportReport(List<Invoice> invoices, List<Income> incomes, File file, Map<String, String> headerInfo) throws Exception {
        WritableWorkbook workbook = Workbook.createWorkbook(file);

        // --- Summary Sheet ---
        WritableSheet summary = workbook.createSheet("Summary", 0);
        int row = 0;

        Label title = new Label(0, row++, "Quarterly Financial Report Summary", headerFormat);
        summary.addCell(title);
        summary.mergeCells(0, 0, 3, 0);

        for (Map.Entry<String, String> entry : headerInfo.entrySet()) {
            summary.addCell(new Label(0, row, entry.getKey(), headerFormat));
            summary.addCell(new Label(1, row++, entry.getValue(), normalFormat));
        }

        row++;
        Label invoiceTitle = new Label(0, row++, "INVOICE TOTALS", headerFormat);
        summary.addCell(invoiceTitle);

        TaxAccumulator invoiceTotals = new TaxAccumulator();
        for (Invoice i : invoices) invoiceTotals.add(calculateTaxes(i.getAmount(), i.isTaxIncluded(), i.isNonTaxable()), i.isNonTaxable());
        invoiceTotals.writeToSheet(summary, row, "Invoice");

        row += 6;
        Label incomeTitle = new Label(0, row++, "INCOME TOTALS", headerFormat);
        summary.addCell(incomeTitle);

        TaxAccumulator incomeTotals = new TaxAccumulator();
        for (Income i : incomes) incomeTotals.add(calculateTaxes(i.getAmount(), i.isTaxIncluded(), i.isNonTaxable()), i.isNonTaxable());
        incomeTotals.writeToSheet(summary, row, "Income");

        // --- Invoices Sheet ---
        WritableSheet invoiceSheet = workbook.createSheet("Invoices", 1);
        writeSheet(invoiceSheet, invoices);

        // --- Incomes Sheet ---
        WritableSheet incomeSheet = workbook.createSheet("Incomes", 2);
        writeSheet(incomeSheet, incomes);

        workbook.write();
        workbook.close();
    }

    private static void writeSheet(WritableSheet sheet, List<? extends FinancialEntry> list) throws Exception {
        // Set headers with formatting
        String[] headers = {"Vendor", "Category", "Issued Date", "Description", "Base Amount", "GST (5%)", "QST (9.975%)", "Total Amount", "Tax Included", "Non-Taxable"};

        for (int i = 0; i < headers.length; i++) {
            sheet.addCell(new Label(i, 0, headers[i], headerFormat));
        }

        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd");

        for (int row = 0; row < list.size(); row++) {
            FinancialEntry item = list.get(row);
            TaxBreakdown tb = calculateTaxes(item.getAmount(), item.isTaxIncluded(), item.isNonTaxable());
            int excelRow = row + 1;

            sheet.addCell(new Label(0, excelRow, item.getVendor(), normalFormat));
            sheet.addCell(new Label(1, excelRow, item.getCategory(), normalFormat));
            sheet.addCell(new Label(2, excelRow, fmt.format(new java.util.Date(item.getIssuedDate())), normalFormat));
            sheet.addCell(new Label(3, excelRow, item.getDescription(), normalFormat));

            sheet.addCell(new jxl.write.Number(4, excelRow, tb.base, moneyFormat));
            sheet.addCell(new jxl.write.Number(5, excelRow, tb.gst, moneyFormat));
            sheet.addCell(new jxl.write.Number(6, excelRow, tb.qst, moneyFormat));
            sheet.addCell(new jxl.write.Number(7, excelRow, tb.base + tb.gst + tb.qst, moneyFormat));

            sheet.addCell(new Label(8, excelRow, item.isTaxIncluded() ? "Yes" : "No", normalFormat));
            sheet.addCell(new Label(9, excelRow, item.isNonTaxable() ? "Yes" : "No", normalFormat));
        }

        // Set some reasonable column widths (chars)
        sheet.setColumnView(0, 15); // Vendor
        sheet.setColumnView(1, 15); // Category
        sheet.setColumnView(2, 12); // Date
        sheet.setColumnView(3, 30); // Description
        sheet.setColumnView(4, 12); // Base Amount
        sheet.setColumnView(5, 12); // GST
        sheet.setColumnView(6, 12); // QST
        sheet.setColumnView(7, 14); // Total
        sheet.setColumnView(8, 12); // Tax Included
        sheet.setColumnView(9, 12); // Non-Taxable
    }

    private static class TaxBreakdown {
        double base, gst, qst;
        TaxBreakdown(double base, double gst, double qst) {
            this.base = base;
            this.gst = gst;
            this.qst = qst;
        }
    }

    private static class TaxAccumulator {
        double base = 0, gst = 0, qst = 0, total = 0, nonTaxable = 0;

        void add(TaxBreakdown t, boolean isNonTaxable) {
            base += t.base;
            gst += t.gst;
            qst += t.qst;
            total += t.base + t.gst + t.qst;
            if (isNonTaxable) nonTaxable += t.base;
        }

        void writeToSheet(WritableSheet sheet, int startRow, String label) throws Exception {
            sheet.addCell(new Label(0, startRow++, label + " Base Total", headerFormat));
            sheet.addCell(new jxl.write.Number(1, startRow - 1, base, moneyBoldFormat));
            sheet.addCell(new Label(0, startRow++, label + " GST Total", headerFormat));
            sheet.addCell(new jxl.write.Number(1, startRow - 1, gst, moneyBoldFormat));
            sheet.addCell(new Label(0, startRow++, label + " QST Total", headerFormat));
            sheet.addCell(new jxl.write.Number(1, startRow - 1, qst, moneyBoldFormat));
            sheet.addCell(new Label(0, startRow++, label + " Grand Total", headerFormat));
            sheet.addCell(new jxl.write.Number(1, startRow - 1, total, moneyBoldFormat));
            sheet.addCell(new Label(0, startRow++, label + " Non-Taxable Total", headerFormat));
            sheet.addCell(new jxl.write.Number(1, startRow - 1, nonTaxable, moneyBoldFormat));
        }
    }

    public interface FinancialEntry {
        String getVendor();
        String getCategory();
        long getIssuedDate();
        String getDescription();
        double getAmount();
        boolean isTaxIncluded();
        boolean isNonTaxable();
    }
}
