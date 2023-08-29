package com.example.springbootprinttest;

import com.example.springbootprinttest.utils.DocxUtils;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import lombok.Data;
import lombok.SneakyThrows;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@SpringBootTest
class springbootprinttestApplicationTests {
    private static final String BRACE_REGEX_STR = "\\$\\{(.*?)\\}";
    private static final String CHECKBOX_START_REGEX_STR = "@checkBoxStart:(.*?)@";
    private static final String CHECKBOX_END_REGEX_STR = "@checkBoxEnd@";
    private static final Pattern compile = Pattern.compile(BRACE_REGEX_STR);
    private static final Pattern checkBoxCompile = Pattern.compile(CHECKBOX_START_REGEX_STR);

    @Test
    public void TempDirExample() {
        // 获取临时目录并打印。
        String tempDir = System.getProperty("java.io.tmpdir");
        File file = new File(tempDir + "" + System.currentTimeMillis() + ".pdf");
        System.out.println(file.getPath());
        System.out.println("OS temporary directory is " + tempDir);
    }
    @Data
    class table{
        private String id;
        private String name;
        private String score;
    }

    /**
     * 读取模板
     * @param
     * @throws Exception
     */
    @Test
    public void operatorWord() throws Exception {
        //获得模板文件
        // InputStream docis = new FileInputStream("C:\Users\16630\Desktop\模板A.docx");
        ClassPathResource resource = new ClassPathResource("/templates/测试.docx");
        //转成word
        // CustomXWPFDocument document = new CustomXWPFDocument(resource.getInputStream());

        Map textMap = new HashMap();
        textMap.put("maxName","张三");
        textMap.put("maxScore", "100");
        textMap.put("minName","李四");
        textMap.put("minScore", "10");
        textMap.put("image1", "/templates/1_1.jpg");
        textMap.put("image2", "/templates/1_2.jpg");
        textMap.put("paragraphs", "哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈" +
                "哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈");

        table table = new table();
        table.setId("1");
        table.setName("张三");
        table.setScore("100");
        table table2 = new table();
        table2.setId("2");
        table2.setName("李四 ");
        table2.setScore("99");
        List<table> tables = new ArrayList<>();
        tables.add(table);
        tables.add(table2);
        textMap.put("table", tables);

        CustomXWPFDocument xwpfDocument = DocxUtils.replaceAndFill(resource.getInputStream(), textMap, false, 83);
        PdfOptions options = PdfOptions.create();
        //中文字体处理
        options.fontProvider(new IFontProvider() {
            @SneakyThrows
            @Override
            public Font getFont(String familyName, String encoding, float size, int style, Color color) {
                // if ("Calibri".equals(familyName)) {
                //     return new Font(FontUtils.getFont("Calibri"), size, style, color);
                // }
                // if ("Wingdings 2".equals(familyName)) {
                //     return new Font(FontUtils.getFont("Wingdings 2"), size, style, color);
                // }
                // Font font = new Font(FontUtils.getFont("宋体"), size, style, color);
                // return font;

                try {
                    BaseFont bfChinese = BaseFont.createFont("/font/simsun.ttc,0", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    // 使用iTextAsian.jar中的字体 需要引入itext-asian
                    // BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                    // 使用资源字体(ClassPath)
                    // BaseFont bfChinese = BaseFont.createFont("/SIMYOU.TTF", BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
                    // 使用其他的字体
                    // BaseFont bfChinese = BaseFont.createFont("/Users/wangjiahao/Downloads/simfang.ttf", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
                    Font fontChinese = new Font(bfChinese, size, style, color);
                    if (familyName != null)
                        fontChinese.setFamily(familyName);
                    return fontChinese;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        });

        String url = "C:\\Users\\Administrator\\Desktop\\temp"+System.currentTimeMillis()+".pdf";
        File file = new File(url);
        FileOutputStream stream = new FileOutputStream(file);
        // xwpfDocument.write(stream);
        // stream.close();
        // aspose word模板转pdf
        // WordUtils.wordToPdf(url);

        // poi word模板转pdf
        PdfConverter.getInstance().convert(xwpfDocument, stream, options);
        xwpfDocument.close();

    }

    /**
     * 为表格插入数据，行数不够添加新行
     *
     * @param table     需要插入数据的表格
     * @param tableList 插入数据集合
     */
    public static void insertTable(XWPFTable table, List<List<String>> tableList) {
        //创建行,根据需要插入的数据添加新行，不处理表头
        for (int i = 1; i <= tableList.size(); i++) {
            table.createRow();
        }
        //遍历表格插入数据
        List<XWPFTableRow> rows = table.getRows();
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow newRow = table.getRow(i);
            List<XWPFTableCell> cells = newRow.getTableCells();
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                cell.setText(tableList.get(i - 1).get(j));
                //表格样式一致-->没有此段表格会默认左对齐
                //有此段会使表格格式一致
                CTTc cttc = cell.getCTTc();
                CTTcPr ctPr = cttc.addNewTcPr();
                ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
                cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
            }
        }
    }

    private void insertText(CustomXWPFDocument document) {
        //声明替换模板对象
        Map textMap = new HashMap();
        textMap.put("maxName","张三");
        textMap.put("maxScore", "100");
        textMap.put("minName","李四");
        textMap.put("minScore", "10");
        //替换模板数据
        WordUtils.changeText(document,textMap);
    }

    /**
     * 替换段落文本
     *
     * @param document docx解析对象
     * @param textMap  需要替换的信息集合
     */
    public static void changeText(XWPFDocument document, Map<String, Object> textMap) {
        //获取段落集合
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
            //获取到段落中的所有文本内容
            String text = paragraph.getText();
            //判断此段落中是否有需要进行替换的文本
            if (checkText(text)) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    //替换模板原来位置
                    run.setText(changeValue(run.toString(), textMap).toString(), 0);
                }
            }
        }
    }
    /**
     * 判断文本中是否包含$
     *
     * @param text 文本
     * @return 包含返回true, 不包含返回false
     */
    public static boolean checkText(String text) {
        boolean check = false;
        if (text.indexOf("$") != -1) {
            check = true;
        }
        return check;
    }
    /**
     * 替换模板${}
     */
    private static Object changeValue(String value, Map<String, Object> textMap) {
        Set<Map.Entry<String, Object>> textSets = textMap.entrySet();
        Object valu = "";
        for (Map.Entry<String, Object> textSet : textSets) {
            // 匹配模板与替换值 格式${key}
            String key = textSet.getKey();
            if (value.contains(key)) {
                valu = textSet.getValue();
            }
        }
        return valu;
    }

    private static void replaceSpecialWord2(XWPFDocument document, Map<String, String> params) {
        int findCount = 0, executeCount = 0;
        //获取段落集合
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        String paragraphText = null, value = null, targetStr = null, endValue = null;
        List<XWPFRun> runs = null;
        for (XWPFParagraph paragraph : paragraphs) {
            if (paragraph == null || "".equals(paragraph.getText().trim())) {
                continue;
            }
        }
        System.out.println("");
        System.out.println("替换结束，总共发现特殊含义字符：" + findCount + " 处，执行替换：" + executeCount + "次。");
    }

    private static String getCurrentValueByKey(String key, Map<String, String> params) {
        if (params.isEmpty() || key == null || key.isEmpty()) {
            return null;
        }
        if (params.containsKey(key)) {
            return params.get(key);
        }
        return null;
    }

    private static boolean isIncludeTarget(String paragraphText) {
        if (paragraphText.contains("${")) {
            return true;
        }
        return false;
    }

}
