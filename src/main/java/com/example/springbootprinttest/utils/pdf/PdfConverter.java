//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package com.example.springbootprinttest.utils.pdf;

import fr.opensagres.poi.xwpf.converter.core.XWPFConverterException;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class PdfConverter extends AbstractXWPFConverter<PdfOptions> {
    private static final IXWPFConverter<PdfOptions> INSTANCE = new PdfConverter();

    public PdfConverter() {
    }

    public static IXWPFConverter<PdfOptions> getInstance() {
        return INSTANCE;
    }

    @Override
    protected void doConvert(XWPFDocument document, OutputStream out, Writer writer, PdfOptions options) throws XWPFConverterException, IOException {
        try {
            ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
            PdfMapper mapper = new PdfMapper(document, tempOut, options, (Integer)null);
            mapper.start();
            if (mapper.useTotalPageField()) {
                Integer actualPageCount = mapper.getPageCount();
                mapper = new PdfMapper(document, out, options, actualPageCount);
                mapper.start();
            } else {
                out.write(tempOut.toByteArray());
            }

        } catch (Exception var8) {
            throw new XWPFConverterException(var8);
        }
    }
}
