package com.mayvel.pdfGenerator;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

public class PageNumberEvent extends PdfPageEventHelper {
    protected PdfTemplate total;
    protected BaseFont baseFont;

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        total = writer.getDirectContent().createTemplate(30, 16);
        try {
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();
        int pageN = writer.getPageNumber();
        String text = "Page " + pageN + " of ";
        float len = baseFont.getWidthPoint(text, 10);

        Rectangle pageSize = document.getPageSize();
        float x = (pageSize.getLeft() + pageSize.getRight()) / 2;
        float y = document.bottom() - 20;

        cb.beginText();
        cb.setFontAndSize(baseFont, 10);
        cb.setTextMatrix(x - len / 2, y);
        cb.showText(text);
        cb.endText();
        cb.addTemplate(total, x - len / 2 + len, y);
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        total.beginText();
        total.setFontAndSize(baseFont, 10);
        total.setTextMatrix(0, 0);
        total.showText(String.valueOf(writer.getPageNumber() - 1));
        total.endText();
    }
}