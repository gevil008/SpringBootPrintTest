//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.springbootprinttest.utils.pdf;

import fr.opensagres.poi.xwpf.converter.core.Options;
import fr.opensagres.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public abstract class AbstractXWPFConverter<T extends Options> implements IXWPFConverter<T> {
    public AbstractXWPFConverter() {
    }

    @Override
    public void convert(XWPFDocument XWPFDocument, OutputStream out, T options) throws XWPFConverterException, IOException {
        try {
            this.doConvert(XWPFDocument, out, (Writer)null, options);
        } finally {
            if (out != null) {
                out.close();
            }

        }

    }

    public void convert(XWPFDocument XWPFDocument, Writer writer, T options) throws XWPFConverterException, IOException {
        try {
            this.doConvert(XWPFDocument, (OutputStream)null, writer, options);
        } finally {
            if (writer != null) {
                writer.close();
            }

        }

    }

    protected abstract void doConvert(XWPFDocument var1, OutputStream var2, Writer var3, T var4) throws XWPFConverterException, IOException;
}
