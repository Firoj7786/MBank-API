package com.mbank.util;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.mbank.dto.TransactionDTO;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.EncryptionConstants;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;


public class pdfGenerator {

    private pdfGenerator() {
        // prevent instantiation
    }

    public static byte[] generateStatementPdf(List<TransactionDTO> transactions,
                                              String accountNumber,
                                              String password) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // 🔐 Encryption (safe encoding)
            WriterProperties writerProperties = new WriterProperties()
                    .setStandardEncryption(
                            password.getBytes(StandardCharsets.UTF_8),
                            accountNumber.getBytes(StandardCharsets.UTF_8),
                            EncryptionConstants.ALLOW_PRINTING,
                            EncryptionConstants.ENCRYPTION_AES_256
                    );

            PdfWriter writer = new PdfWriter(baos, writerProperties);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // 🏦 Title
            document.add(new Paragraph("M Bank Statement")
                    .setBold()
                    .setFontSize(18));

            document.add(new Paragraph("Account Number: " + safe(accountNumber)));
            document.add(new Paragraph("\n"));

            // 📊 Table with full width
            Table table = new Table(UnitValue.createPercentArray(new float[]{3, 3, 3}))
                    .useAllAvailableWidth();

            // Headers
            table.addHeaderCell("Date");
            table.addHeaderCell("Type");
            table.addHeaderCell("Amount");

            // Data rows
            if (transactions != null && !transactions.isEmpty()) {
                for (TransactionDTO txn : transactions) {

                    table.addCell(safe(txn.getTransactionDate()));
                    table.addCell(safe(txn.getTransactionType()));
                    table.addCell(String.valueOf(txn.getAmount()));
                }
            } else {
                table.addCell("No transactions found");
                table.addCell("-");
                table.addCell("-");
            }

            document.add(table);
            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // ✅ Null-safe helper (prevents crashes)
    private static String safe(Object value) {
        return value == null ? "-" : value.toString();
    }
}