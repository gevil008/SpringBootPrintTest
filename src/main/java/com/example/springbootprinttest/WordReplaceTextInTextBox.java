package com.example.springbootprinttest;

import org.apache.poi.xwpf.usermodel.IRunBody;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;
import org.springframework.core.io.ClassPathResource;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取word文档中文本框
 */
public class WordReplaceTextInTextBox {

    public static void main(String[] args) throws Exception {

        XWPFDocument document = new XWPFDocument(new ClassPathResource("/templates/test.docx").getInputStream());

        String someWords = "TextBox";

        for (XWPFParagraph paragraph : document.getParagraphs()) {
            XmlCursor cursor = paragraph.getCTP().newCursor();
            // cursor.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//*/w:txbxContent/w:p/w:r");
            cursor.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:drawing/*/w:txbxContent/w:p/w:r");
            // cursor.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main';declare namespace wps='http://schemas.microsoft.com/office/word/2010/wordprocessingShape' .//wps:txbx/w:txbxContent/w:p");

            List<XmlObject> ctrsintxtbx = new ArrayList<XmlObject>();

            while (cursor.hasNextSelection()) {
                cursor.toNextSelection();
                XmlObject obj = cursor.getObject();
                ctrsintxtbx.add(obj);
            }
            for (XmlObject obj : ctrsintxtbx) {
                CTR ctr = CTR.Factory.parse(obj.xmlText());
                XWPFRun bufferrun = new XWPFRun(ctr, (IRunBody) paragraph);
                String text = bufferrun.getText(0);
                if (text != null && text.contains(someWords)) {
                    text = text.replace(someWords, "replaced"+System.currentTimeMillis());
                    bufferrun.setText(text, 0);
                }
                obj.set(bufferrun.getCTR());
            }
        }
        String url = "C:\\Users\\Administrator\\Desktop\\temp"+System.currentTimeMillis()+".docx";
        FileOutputStream out = new FileOutputStream(url);
        document.write(out);
        out.close();
        document.close();
    }
}