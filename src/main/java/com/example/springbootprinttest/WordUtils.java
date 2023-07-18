package com.example.springbootprinttest;

import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import com.aspose.words.License;
import com.aspose.words.SaveFormat;
import lombok.SneakyThrows;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.xwpf.usermodel.TableWidthType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;


/**
 * 通过word模板生成新的word工具类
 */
public class WordUtils {


    /**
     * 加载license 用于破解 不生成水印
     *
     */
    @SneakyThrows
    private static void getLicense() {
        try (InputStream inputStream = WordUtils.class.getClassLoader().getResourceAsStream("license.xml")) {
            License license = new License();
            license.setLicense(inputStream );
            //乱码问题
            garbledCode();
        }
    }

    /**
     * 乱码问题
     */
    @SneakyThrows
    private static void  garbledCode() {
        //把用到的字体包从windows的C:\Windows\Fonts里所有文件，复制到linux的/usr/share/fonts/windows下。
        OsInfo osInfo = SystemUtil.getOsInfo();
        if(osInfo.isLinux()){
            FontSettings.setFontsFolder("/usr/share/fonts/windows"+File.separator, true);
        }
    }

    /**
     * 获取Resource路径
     * @return
     */
    public static String getResourcePath(){
        String path = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            return path.substring(1);
        }else{
            path="/usr/local";
        }
        return path;
    }



    /**
     * word转pdf
     * @param outputUrl word文件保存的路径
     * @return pdf生成的路径
     */
    @SneakyThrows
    public static String wordToPdf(String outputUrl) {
        //生成pdf
        String outputPdf = outputUrl.replace("docx", "pdf");
        File filePdf = new File(outputPdf);
        FileOutputStream streamPdf = new FileOutputStream(filePdf);

        //凭证 不然切换后有水印
        getLicense();

        //开始渲染转化,并且输出
        Document doc = new Document(outputUrl); // Address是将要被转化的word文档
        doc.save(streamPdf, SaveFormat.PDF);// 全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF,  EPUB, XPS, SWF 相互转换
        streamPdf.close();

       return outputPdf;
    }

    /**
     * web下载文件
     * @param path pdf临时保存路径
     * @param response
     */
    public static void downResult(String  path, HttpServletResponse response){
        try{

            File file = new File(path);
            boolean exists = file.exists();
            if (!exists){
                throw new Exception("文件不存在！");
            }
            // 获取文件名
            String filename = file.getName();

            // 获取文件后缀名
            String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();

            // 将文件写入输入流
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStream fis = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);
            fis.close();

            // 清空response
            response.reset();
            // 设置response的Header
            response.setCharacterEncoding("UTF-8");
            //Content-Disposition的作用：告知浏览器以何种方式显示响应返回的文件，用浏览器打开还是以附件的形式下载到本地保存
            //attachment表示以附件方式下载 inline表示在线打开 "Content-Disposition: inline; filename=文件名.mp3"
            // filename表示文件的默认名称，因为网络传输只支持URL编码的相关支付，因此需要将文件名URL编码后进行传输,前端收到后需要反编码才能获取到真正的名称
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));
            // 告知浏览器文件的大小
            response.addHeader("Content-Length", "" + file.length());
            OutputStream outputStream = new BufferedOutputStream(response.getOutputStream());

            //设置响应格式，已文件流的方式返回给前端。
            response.setContentType("application/octet-stream");
            outputStream.write(buffer);
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断文本中时候包含$
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
     * 根据模板生成新word文档
     * 判断表格是需要替换还是需要插入，判断逻辑有$为替换，表格无$为插入
     *
     * @param inputUrl  模板存放地址
     * @param outputUrl 新文档存放地址
     * @param textMap   需要替换的信息集合
     * @param tableList 需要插入的表格信息集合
     */
    public static boolean changWord(String inputUrl, String outputUrl,
                                    Map<String, String> textMap, List<List<String[]>> tableList) {
        //模板转换默认成功
        boolean changeFlag = true;
        try {
            //获取docx解析对象
            XWPFDocument document = new XWPFDocument(POIXMLDocument.openPackage(inputUrl));
            //解析替换文本段落对象
            WordUtils.changeText(document, textMap);
            //解析替换表格对象
            // WordUtils.changeTable(document, textMap, tableList);
            //生成新的word
            File file = new File(outputUrl);
            FileOutputStream stream = new FileOutputStream(file);
            document.write(stream);
            stream.close();
            System.out.println("成功生成！" + outputUrl);
        } catch (IOException e) {
            e.printStackTrace();
            changeFlag = false;
        }

        return changeFlag;

    }


    // word表格跨列合并单元格
    //row 指定行、fromCell 开始列数、toCell 结束列数。
    public static void mergeColumn(XWPFTable table, int row, int fromCell, int toCell) {
        for (int cellIndex = fromCell; cellIndex <= toCell; cellIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if (cellIndex == fromCell) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    // word表格跨行并单元格
    //col 指定列、fromRow 开始行、toRow 结束行。
    public static void mergeLine(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            if (rowIndex == fromRow) {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }


    /**
     * 替换段落文本
     *
     * @param document docx解析对象
     * @param textMap  需要替换的信息集合
     */
    public static void changeText(XWPFDocument document, Map<String, String> textMap) {
        //获取段落集合
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        for (XWPFParagraph paragraph : paragraphs) {
            //判断此段落时候需要进行替换
            String text = paragraph.getText();
            if (checkText(text)) {
                List<XWPFRun> runs = paragraph.getRuns();
                for (XWPFRun run : runs) {
                    //替换模板原来位置
                    // run.setText(changeValue(run.toString(), textMap),0);
                    String textValue = changeValue(run.toString(), textMap);
                    run.setText(textValue, 0);
                }
            }
        }
    }


    /**
     * 遍历表格
     *
     * @param rows    表格行对象
     * @param textMap 需要替换的信息集合
     */
    public static void eachTable(List<XWPFTableRow> rows, Map<String, String> textMap) {
        for (XWPFTableRow row : rows) {
            List<XWPFTableCell> cells = row.getTableCells();
            for (XWPFTableCell cell : cells) {
                //判断单元格是否需要替换
                if (checkText(cell.getText())) {
                    List<XWPFParagraph> paragraphs = cell.getParagraphs();
                    for (XWPFParagraph paragraph : paragraphs) {
                        List<XWPFRun> runs = paragraph.getRuns();
                        for (XWPFRun run : runs) {
                            run.setText(changeValue(run.toString(), textMap), 0);
                        }
                    }
                }
            }
        }
    }



    /**
     * 为表格插入数据，行数不够添加新行
     *
     * @param table     需要插入数据的表格
     * @param tableList 插入数据集合
     */
    public static void insertTable(XWPFTable table, List<List<String>> tableList) {

        //创建行,根据需要插入的数据添加新行，不处理表头
        // for (int i = 1; i < tableList.size(); i++) {
        //     XWPFTableRow row = table.createRow();
        // }
        //遍历表格插入数据
        List<XWPFTableRow> rows = table.getRows();
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow newRow = table.getRow(i);
            List<XWPFTableCell> cells = newRow.getTableCells();
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                cell.setText(tableList.get(i-1).get(j));
            }
        }
    }

    /**
     * 替换表格对象方法
     *
     * @param document  docx解析对象
     * @param textMap   需要替换的信息集合
     * @param tableList 需要插入的表格信息集合
     */
    public static void changeTableDynamic(XWPFDocument document, Map<String, String> textMap,
                                          List<List<String>> tableList, List<List<int[]>> mergeLineTable, List<List<int[]>> mergeColumnTable) {
        //获取表格对象集合
        List<XWPFTable> tables = document.getTables();
        for (int i = 0; i < tables.size(); i++) {
            XWPFTable table = tables.get(i);
            if (mergeLineTable == null && mergeColumnTable == null) {
                insertTableDynamicCol(table, tableList);
            } else {
                insertTableMergeDynamicCol(table, tableList, mergeLineTable.get(i), mergeColumnTable.get(i));
            }
        }
    }
    /**
     * 替换表格对象方法-->(带合并单元格方法)
     *
     * @param document  docx解析对象
     * @param textMap   需要替换的信息集合
     * @param tableList 需要插入的表格信息集合
     */
    public static void changeTable(XWPFDocument document, Map<String, String> textMap,
                                   List<List<String>> tableList) {
        //获取表格对象集合
        List<XWPFTable> tables = document.getTables();
        for (int i = 0; i < tables.size(); i++) {
            //只处理行数大于等于2的表格，且不循环表头
            XWPFTable table = tables.get(i);
            if (table.getRows().size() > 1) {
                //判断表格是需要替换还是需要插入，判断逻辑有$为替换，表格无$为插入
                if (checkText(table.getText())) {
                    List<XWPFTableRow> rows = table.getRows();
                    //遍历表格,并替换模板
                    eachTable(rows, textMap);
                } else {
//                  System.out.println("插入"+table.getText());
                    insertTable(table, tableList);
                }
            }
        }
    }


    /**
     * 为表格插入数据，行数不够添加新行 列不够添加新列
     *
     * @param table     需要插入数据的表格
     * @param tableList 插入数据集合
     */
    public static void insertTableDynamicCol(XWPFTable table, List<List<String>> tableList) {
        //遍历表格插入数据
        List<XWPFTableRow> rows = table.getRows();
        // 列
        int col = rows.get(0).getTableCells().size();
        //创建列,根据需要插入的数据添加新列
        // for (int i = col; i < tableList.get(0).length; i++) {
        //     table.addNewCol();
        // }

        //创建行,根据需要插入的数据添加新行
        for (int i = rows.size() - 1; i < tableList.size(); i++) {
            XWPFTableRow row = table.createRow();
        }

        table.removeRow(0);

        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow newRow = table.getRow(i);
            List<XWPFTableCell> cells = newRow.getTableCells();
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                //设置表格内容水平居中
                CTTc cttc = cell.getCTTc();
                CTTcPr ctPr = cttc.addNewTcPr();
                ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
                cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
                // cell.setText(tableList.get(i)[j]);
            }
        }
    }


    /**
     * 为表格插入数据，行数不够添加新行 列不够添加新列--->带合并单元格
     *
     * @param table     需要插入数据的表格
     * @param tableList 插入数据集合
     */
    public static void insertTableMergeDynamicCol(XWPFTable table, List<List<String>> tableList, List<int[]> mergeLine, List<int[]> mergeColumn) {
        //遍历表格插入数据
        List<XWPFTableRow> rows = table.getRows();
        // 列
        int col = rows.get(0).getTableCells().size();
        //创建列,根据需要插入的数据添加新列
        // if (tableList.size() > 0) {
        //     for (int i = col; i < tableList.get(0).length; i++) {
        //         table.addNewCol();
        //     }
        // }

        //创建行,根据需要插入的数据添加新行
        for (int i = rows.size() - 1; i < tableList.size(); i++) {
            XWPFTableRow row = table.createRow();
        }

        table.removeRow(0);

        for (int i = 0; i < rows.size(); i++) {
            XWPFTableRow newRow = table.getRow(i);
            List<XWPFTableCell> cells = newRow.getTableCells();
            for (int j = 0; j < cells.size(); j++) {
                XWPFTableCell cell = cells.get(j);
                //设置表格内容水平居中
                CTTc cttc = cell.getCTTc();
                CTTcPr ctPr = cttc.addNewTcPr();
                ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
                cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
                // cell.setText(tableList.get(i)[j]);
            }
        }
        if (tableList.size() > 0) {
            if (mergeLine != null && !mergeLine.isEmpty()) {
                for (int i = 0; i < mergeLine.size(); i++) {
                    mergeLine(table, mergeLine.get(i)[0], mergeLine.get(i)[1], mergeLine.get(i)[2]);
                }
            }
            if (mergeColumn != null && !mergeColumn.isEmpty()) {
                for (int i = 0; i < mergeColumn.size(); i++) {
                    mergeColumn(table, mergeColumn.get(i)[0], mergeColumn.get(i)[1], mergeColumn.get(i)[2]);
                }
            }
            // 设置列宽
            table.setWidthType(TableWidthType.DXA);



//            //手动添加新的一行
//            XWPFTableCell cell = table.getRow(rows.size() - 1).getCell(0);
//            /** 设置水平居中 */
//            CTTc cttc = cell.getCTTc();
//            CTTcPr ctPr = cttc.addNewTcPr();
//            ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
//            cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.RIGHT);
//
//
//            XWPFParagraph para = cell.getParagraphArray(0);
//
//            //一个XWPFRun代表具有相同属性的一个区域。
//            XWPFRun run = para.createRun();
//
//            run.setText("小计：");
//            run = para.createRun();
//            run.setColor("FF0000");
//            run.setText(tableList.get(tableList.size() - 1)[tableList.get(0).length - 1]);
//            run = para.createRun();
//            run.setColor("000000");
//            run.setText("元");
        }

    }



    /**
     * 匹配传入信息集合与模板
     *
     * @param value   模板需要替换的区域
     * @param textMap 传入信息集合
     * @return 模板需要替换区域信息集合对应值
     */
    public static String changeValue(String value, Map<String, String> textMap) {
        Set<Entry<String, String>> textSets = textMap.entrySet();
        for (Entry<String, String> textSet : textSets) {
            //匹配模板与替换值 格式${key}
            String key = "${" + textSet.getKey() + "}";
            if (value.indexOf(key) != -1) {
                //全部参数替换
                // value = textSet.getValue();
                //仅替换参数
                value = value.replace(key, textSet.getValue()==null?"无":textSet.getValue());
            }
        }
        //模板未匹配到区域替换为空
        if (checkText(value)) {
            value = "";
        }
        return value;
    }


    /**
     * 关闭输入流
     *
     * @param is
     */
    private static void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭输出流
     *
     * @param os
     */
    private static void close(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 实例1
     *
     * 根据模板生成Pdf并下载
     * 判断表格是需要替换还是需要插入，判断逻辑有$为替换，表格无$为插入
     * @param dynamic  模板存放地址 true 有合并的内容
     * @param inputUrl  模板存放地址
     * @param response
     * @param textMap   需要替换的信息集合
     * @param tableList 需要插入的表格信息集合
     */
    public static String changWordToPdfDownloadUrl(boolean dynamic, String inputUrl, String outputUrl, String fileName, HttpServletResponse response,
                                                   Map<String, String> textMap, List<List<String>> tableList, List<List<int[]>> mergeLineTable, List<List<int[]>> mergeColumnTable) {
        //模板转换默认成功
        boolean changeFlag = true;
        try {
            //获取docx解析对象
            File inputWordFile = new File(inputUrl);
            InputStream source = new FileInputStream(inputWordFile);
            XWPFDocument document = new XWPFDocument(source);

            //解析替换文本段落对象
            WordUtils.changeText(document, textMap);
            //解析替换表格对象 true 有合并的内容
            if (!dynamic) {
                WordUtils.changeTable(document, textMap, tableList);
            } else {
               // true 有合并的内容
               //  WordUtils.changeTableDynamic(document, textMap, tableList, mergeLineTable, mergeColumnTable);
            }
            //生成新的word
            File file = new File(outputUrl);
            FileOutputStream stream = new FileOutputStream(file);
            document.write(stream);
            stream.close();


            //word转化pdf 渲染
            String outputPdf= wordToPdf(outputUrl);

            //web下载
            // downResult(outputPdf,response);

            // 删除过渡word文件
            // if (file.exists()) {
            //     file.delete();
            // }
            // 删除过渡pdf文件
            // File filePdf=  new File(outputPdf);
            // if (filePdf.exists()) {
            //     filePdf.delete();
            // }
            close(source);
            return outputPdf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 实例2
     *
     * @param dynamic
     * @param inputUrl
     * @param outputUrl
     * @param fileName
     * @param response
     * @param textMap
     * @param tableList
     * @param mergeLineTable
     * @param mergeColumnTable
     * @return
     */
    public static Map<String, String> changWordToPdfDownloadUrlAndMD5(boolean dynamic, String inputUrl, String outputUrl, String fileName, HttpServletResponse response,
                                                                      Map<String, String> textMap, List<List<String[]>> tableList, List<List<int[]>> mergeLineTable, List<List<int[]>> mergeColumnTable) {
        //模板转换默认成功
        boolean changeFlag = true;
        try {
            //获取docx解析对象
            File inputWordFile = new File(inputUrl);
            InputStream source = new FileInputStream(inputWordFile);
            XWPFDocument document = new XWPFDocument(source);

            //解析替换文本段落对象
            WordUtils.changeText(document, textMap);
            //解析替换表格对象
            if (!dynamic) {
                // WordUtils.changeTable(document, textMap, tableList);
            } else {
                /**
                 * mergeLineTable，行设置
                 * mergeColumnTable，列设置
                 */
                // WordUtils.changeTableDynamic(document, textMap, tableList, mergeLineTable, mergeColumnTable);
            }
            //生成新的word
            File file = new File(outputUrl);
            FileOutputStream stream = new FileOutputStream(file);
            document.write(stream);
            stream.close();


            //word转化pdf 渲染
            String outputPdf= wordToPdf(outputUrl);

            //web下载
            downResult(outputPdf,response);


            File filePdf=  new File(outputPdf);
            String stringMD5 = MD5Util.MD5File(filePdf);
            Map<String, String> result = new HashMap<>();
            result.put("url", outputPdf);
            result.put("MD5", stringMD5);

            // 删除过渡word文件
            if (file.exists()) {
                file.delete();
            }
            // 删除过渡pdf文件
            if (filePdf.exists()) {
                filePdf.delete();
            }
            close(source);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }






    /**
     * 预付款证明单pdf
     * @param dynamic
     * @param inputStream 原始文件输入流
     * @param outputUrl 输出文件 路径和名称定义
     * @param response
     * @param textMap 替换${XX}内容
     * @param tableList  插入表数据
     * @param mergeLineTable 行合并
     * @param mergeColumnTable 列合并
     * @return
     */
    public static Map<String, String> exportProofOfAdvancePayment(boolean dynamic, InputStream inputStream, String outputUrl, HttpServletResponse response,
                                                                                Map<String, String> textMap, List<List<String[]>> tableList, List<List<int[]>> mergeLineTable, List<List<int[]>> mergeColumnTable) {
        //模板转换默认成功
        boolean changeFlag = true;
        try {

            InputStream source = inputStream;
            XWPFDocument document = new XWPFDocument(inputStream);

            //解析替换文本段落对象
            WordUtils.changeText(document, textMap);
            //解析替换表格对象
            if (!dynamic) {
                // WordUtils.changeTable(document, textMap, tableList);
            } else {
                /**
                 * mergeLineTable，行设置
                 * mergeColumnTable，列设置
                 */
                // WordUtils.changeTableDynamic(document, textMap, tableList, mergeLineTable, mergeColumnTable);
            }
            //生成新的word
            File file = new File(outputUrl);
            FileOutputStream stream = new FileOutputStream(file);
            document.write(stream);
            stream.close();


            //word转化pdf 渲染
            String outputPdf= wordToPdf(outputUrl);

            //web下载
            downResult(outputPdf,response);


            File filePdf=  new File(outputPdf);
            String stringMD5 = MD5Util.MD5File(filePdf);
            Map<String, String> result = new HashMap<>();
            result.put("url", outputPdf);
            result.put("MD5", stringMD5);
            // 删除过渡word文件
            if (file.exists()) {
                file.delete();
            }
            // 删除过渡pdf文件
            if (filePdf.exists()) {
                filePdf.delete();
            }
            close(source);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}

