import java.io.File;
import java.util.List;

import jxl.*;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;



public class ExcelExporter {

    // Tax rates for Quebec
    private static final double GST_RATE = 0.05;
    private static final double QST_RATE = 0.09975;

    // Helper method to calculate taxes based on base amount and tax included flag
    private static TaxBreakdown calculateTaxes(double amount, boolean taxIncluded) {
        double baseAmount, gstAmount, qstAmount;

        if (taxIncluded) {
            // Tax included in amount, reverse calculate base and taxes

            // Let total amount = amount = B + GST + QST
            // Solve for base B:
            // amount = B + (B*GST_RATE) + ((B + B*GST_RATE)*QST_RATE)
            // amount = B * (1 + GST_RATE + (1 + GST_RATE)*QST_RATE)
            double factor = 1 + GST_RATE + (1 + GST_RATE) * QST_RATE;
            baseAmount = amount / factor;

            gstAmount = baseAmount * GST_RATE;
            qstAmount = (baseAmount + gstAmount) * QST_RATE;
        } else {
            // Tax not included, calculate taxes from base amount
            baseAmount = amount;
            gstAmount = baseAmount * GST_RATE;
            qstAmount = (baseAmount + gstAmount) * QST_RATE;
        }

        return new TaxBreakdown(baseAmount, gstAmount, qstAmount);
    }

    public static void exportInvoices(List<Invoice> invoices, File file) throws Exception {
        WritableWorkbook workbook = Workbook.createWorkbook(file);
        WritableSheet sheet = workbook.createSheet("Invoices", 0);

        // Totals for summary
        double totalBase = 0.0;
        double totalGst = 0.0;
        double totalQst = 0.0;
        double totalAmount = 0.0;

        // Calculate totals
        for (Invoice inv : invoices) {
            TaxBreakdown tb = calculateTaxes(inv.getAmount(), inv.isTaxIncluded());
            totalBase += tb.base;
            totalGst += tb.gst;
            totalQst += tb.qst;
            totalAmount += tb.base + tb.gst + tb.qst;
        }

        // Write totals summary at top (row 0,1,2,3)
        sheet.addCell(new Label(0, 0, "TOTAL BASE AMOUNT"));
        sheet.addCell(new jxl.write.Number(1, 0, totalBase));

        sheet.addCell(new Label(0, 1, "TOTAL GST (5%)"));
        sheet.addCell(new jxl.write.Number(1, 1, totalGst));

        sheet.addCell(new Label(0, 2, "TOTAL QST (9.975%)"));
        sheet.addCell(new jxl.write.Number(1, 2, totalQst));

        sheet.addCell(new Label(0, 3, "TOTAL AMOUNT"));
        sheet.addCell(new jxl.write.Number(1, 3, totalAmount));

        // Headers start at row 5
        int startRow = 5;
        sheet.addCell(new Label(0, startRow, "Vendor"));
        sheet.addCell(new Label(1, startRow, "Category"));
        sheet.addCell(new Label(2, startRow, "Issued Date"));
        sheet.addCell(new Label(3, startRow, "Description"));
        sheet.addCell(new Label(4, startRow, "Base Amount"));
        sheet.addCell(new Label(5, startRow, "GST (5%)"));
        sheet.addCell(new Label(6, startRow, "QST (9.975%)"));
        sheet.addCell(new Label(7, startRow, "Total Amount"));
        sheet.addCell(new Label(8, startRow, "Tax Included"));

        int row = startRow + 1;
        for (Invoice inv : invoices) {
            TaxBreakdown tb = calculateTaxes(inv.getAmount(), inv.isTaxIncluded());

            sheet.addCell(new Label(0, row, inv.getVendor()));
            sheet.addCell(new Label(1, row, inv.getCategory()));
            sheet.addCell(new Label(2, row, new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(inv.getIssuedDate()))));
            sheet.addCell(new Label(3, row, inv.getDescription()));
            sheet.addCell(new jxl.write.Number(4, row, tb.base));
            sheet.addCell(new jxl.write.Number(5, row, tb.gst));
            sheet.addCell(new jxl.write.Number(6, row, tb.qst));
            sheet.addCell(new jxl.write.Number(7, row, tb.base + tb.gst + tb.qst));
            sheet.addCell(new Label(8, row, inv.isTaxIncluded() ? "Yes" : "No"));
            row++;
        }

        workbook.write();
        workbook.close();
    }

    // Helper class to store tax breakdown
    private static class TaxBreakdown {
        double base, gst, qst;
        TaxBreakdown(double base, double gst, double qst) {
            this.base = base;
            this.gst = gst;
            this.qst = qst;
        }
    }
}
