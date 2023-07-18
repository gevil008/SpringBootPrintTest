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

public interface IXWPFConverter<T extends Options> {
    void convert(XWPFDocument var1, OutputStream var2, T var3) throws XWPFConverterException, IOException;
}
