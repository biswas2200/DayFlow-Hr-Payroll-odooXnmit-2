package com.dayflow.hrmtool.payroll;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@Service
public class PayslipPdfService {

    public String generatePdfAndSave(Payslip payslip, SalaryStructure structure, List<SalaryComponent> components, String employeeName) {
        String dirPath = "uploads/payslips/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String fileName = "payslip_" + payslip.getEmployeeId() + "_" + payslip.getYear() + "_" + payslip.getMonth() + ".pdf";
        String filePath = dirPath + fileName;

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            document.add(new Paragraph("Payslip for " + employeeName));
            document.add(new Paragraph("Month/Year: " + payslip.getMonth() + "/" + payslip.getYear()));
            document.add(new Paragraph(" "));
            
            PdfPTable table = new PdfPTable(2);
            table.addCell("Component");
            table.addCell("Amount");
            
            for (SalaryComponent c : components) {
                table.addCell(c.getType().name());
                table.addCell(String.valueOf(c.getComputedAmount()));
            }
            
            table.addCell("Gross Salary");
            table.addCell(String.valueOf(payslip.getGrossSalary()));
            table.addCell("PF Deduction");
            table.addCell(String.valueOf(payslip.getPfEmployee()));
            table.addCell("Professional Tax");
            table.addCell(String.valueOf(payslip.getProfessionalTax()));
            table.addCell("Net Salary");
            table.addCell(String.valueOf(payslip.getNetSalary()));
            
            document.add(table);
            document.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }

        return "payslips/" + fileName;
    }
}
