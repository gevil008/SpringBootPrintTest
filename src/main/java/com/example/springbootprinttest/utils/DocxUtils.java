package com.example.springbootprinttest.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.springbootprinttest.CustomXWPFDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocxUtils {
    private static final Logger logger = LoggerFactory.getLogger(DocxUtils.class);

    private static final String BRACE_REGEX_STR = "\\$\\{(.*?)\\}";
    private static final String CHECKBOX_START_REGEX_STR = "@checkBoxStart:(.*?)@";
    private static final String CHECKBOX_END_REGEX_STR = "@checkBoxEnd@";
    private static final Pattern compile = Pattern.compile(BRACE_REGEX_STR);
    private static final Pattern checkBoxCompile = Pattern.compile(CHECKBOX_START_REGEX_STR);

    /**
     * 读取docx
     *
     * @param in
     * @return
     * @throws Exception
     */
    public static CustomXWPFDocument read(InputStream in) throws Exception {
        return new CustomXWPFDocument(in);
    }

    /**
     * 替换模板变量
     *
     * @param in
     * @param data
     * @return
     */
    public static CustomXWPFDocument replaceAndFill(InputStream in, Map<String, Object> data, Boolean needFill, Integer rowSize) throws Exception {
        long start = System.currentTimeMillis();

        CustomXWPFDocument document = read(in);
        //普通文本
        List<XWPFParagraph> paragraphs = document.getParagraphs();
        logger.debug("--------------替换普通段落文本start--------------");
        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph p = paragraphs.get(i);
            //处理段落类数据
            Matcher matcher1 = compile.matcher(p.getText());
            if (matcher1.find()) {
                String s = matcher1.group(1);
                if (s.indexOf(":") != -1 && s.split(":")[1].endsWith("R")) {
                    String[] split1 = s.split(":", 3);
                    int length = null == split1[2] ? ChineseUtils.getLength(p.getText()) : Integer.parseInt(split1[2]);
                    Integer allLine = Integer.valueOf(s.split(":")[1].replace("R", ""));
                    logger.debug("开始处理段落");
                    logger.debug("段落处理一行字符数" + length);

                    List<XWPFParagraph> allP = new ArrayList<>();
                    allP.add(p);
                    logger.debug("配置段落行数" + allLine);
                    String key = null;
                    String field = null;
                    //实际数据
                    if (s.indexOf(".") != -1) {
                        String[] split = s.split("\\.");
                        key = split[0].split(":")[0];
                        field = split[1].split(":")[0];
                    } else {
                        key = s.split(":")[0];
                    }

                    String realVal = "";
                    if (field == null && data.get(key) != null) {
                        Object o = data.get(key);
                        realVal = o == null ? "" : o.toString();
                    }
                    if (field != null && data.get(key) != null) {
                        Object o = data.get(key);
                        JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(o));
                        Object field1 = jsonObject.get(field);
                        realVal = field1 == null ? "" : field1.toString();
                    }
                    logger.debug("实际数据：" + realVal);
                    //特殊处理换行
                    realVal = "" + realVal;
                    realVal = realVal.replaceAll("\\r\\n", "\n");

                    //切割换行
                    String[] split = realVal.split("\\n", 999);
                    ArrayList tempText = new ArrayList(allLine);
                    for (int i1 = 0; i1 < split.length; i1++) {
                        String nRow = split[i1];
                        //每一个换行下单独计算所占行
                        int length1 = ChineseUtils.getLength(nRow);
                        logger.debug("实际数据所占英文字符大小：" + length1);
                        if (length1 > length) {
                            //超过每行所能容纳的字符数，进行切割
                            StringBuilder sb = new StringBuilder();
                            int currentLength = 0;
                            for (int j = 0; j < nRow.length(); j++) {
                                char currentChar = nRow.charAt(j);
                                int currentCharLength = ChineseUtils.getLength(String.valueOf(currentChar));
                                if (currentLength + currentCharLength > length) {
                                    tempText.add(sb.toString());
                                    sb.delete(0, sb.length());
                                    currentLength = 0;
                                    sb.append(currentChar);
                                    currentLength += currentCharLength;
                                } else {
                                    sb.append(currentChar);
                                    currentLength += currentCharLength;
                                }
                                if (j == nRow.length() - 1) {
                                    tempText.add(sb.toString());
                                }
                            }
                        } else {
                            //如果不超过每行所能容纳的字符数，直接加入到临时集合
                            tempText.add(nRow);
                        }

                    }
                    //补全字符数
                    for (int i1 = 0; i1 < tempText.size(); i1++) {
                        String str = tempText.get(i1).toString();
                        tempText.set(i1, ChineseUtils.fillStr(str, length, ChineseUtils.FillPosition.LEFT, null, null));
                    }
                    //不足固定行数则补足
                    if (tempText.size() < allLine) {
                        int fillRowSize = allLine - tempText.size();
                        for (int j = 0; j < fillRowSize; j++) {
                            tempText.add(ChineseUtils.fillStr("", length, ChineseUtils.FillPosition.LEFT, null, null));
                        }
                    }
                    //添加到word
                    XWPFParagraph cur = p;
                    int realRowSize = tempText.size();
                    for (int j = 0; j < realRowSize - 1; j++) {
                        XmlCursor xmlCursor = cur.getCTP().newCursor();
                        xmlCursor.toNextSibling();
                        CTRPr rPr = cur.getRuns().get(0).getCTR().getRPr();
                        XWPFParagraph xwpfParagraph = document.insertNewParagraph(xmlCursor);
                        cur = xwpfParagraph;
                        XWPFRun run = xwpfParagraph.createRun();
                        run.getCTR().setRPr(rPr);
                        i++;
                        allP.add(xwpfParagraph);
                    }
                    for (int j = 0; j < allP.size(); j++) {
                        XWPFParagraph xwpfParagraph = allP.get(j);
                        String willFill = tempText.size() > j ? tempText.get(j).toString() : "";
                        String s1 = ChineseUtils.fillStr(willFill, length, ChineseUtils.FillPosition.LEFT, null, null);
                        xwpfParagraph.getRuns().get(0).setText(s1, 0);
                    }
                    continue;
                }
            }

            List<XWPFRun> runs = p.getRuns();
            for (XWPFRun run : runs) {
                Matcher matcher = compile.matcher(run.text());
                while (matcher.find()) {
                    String replace = matcher.group(1);
                    String resVar = "";
                    if (replace.startsWith("img_")) {
                        resVar = replaceBaseVarible(replace.replace("img_", ""), data, false);
                        ClassPathResource imageResource = new ClassPathResource(resVar);
                        try (InputStream inputStream = imageResource.getInputStream()) {
                            //设置图片
                            String blipId = document.addPictureData(inputStream, XWPFDocument.PICTURE_TYPE_PNG);
                            //创建一个word图片，并插入到文档中-->像素可改
                            document.createPicture(blipId, document.getAllPictures().size() - 1, 100, 100, run);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        resVar="";
                    } else if (!replace.startsWith("img_") && replace.indexOf(".") == -1) {
                        //普通变量
                        resVar = replaceBaseVarible(replace, data, needFill);
                    } else {
                        //对象变量
                        resVar = replaceObjectVarible(replace, data, needFill);
                    }
                    String replace1 = run.text().replaceFirst(BRACE_REGEX_STR, resVar);
                    run.setText(replace1, 0);
                }
            }
        }
        logger.debug("--------------替换普通段落文本end--------------");
        //表格
        logger.debug("--------------替换表格文本start--------------");
        List<XWPFTable> tables = document.getTables();
        tables.stream().forEach(table -> {
            List<XWPFTableRow> rows = table.getRows();
            //循环每一行进行填充
            AtomicInteger rowLine = new AtomicInteger(0);
            Iterator<XWPFTableRow> iterator = rows.iterator();
            HashMap<Integer, Object> temp = new HashMap<>();
            while (iterator.hasNext()) {
                XWPFTableRow row = iterator.next();
                List<XWPFTableCell> cells = row.getTableCells();
                if (isLoopRow(cells, data)) {//循环表格对象
                    XWPFTableRow xwpfTableRow = row;
                    try {
                        renderTable(table, xwpfTableRow, data, rowLine.get(), temp);
                    } catch (Exception e) {
                        logger.error("操作失败", e);
                    }
                } else {//普通表单对象
                    cells.forEach(cell -> {
                        List<XWPFParagraph> paragraphs1 = cell.getParagraphs();
                        for (int i = 0; i < paragraphs1.size(); i++) {
                            XWPFParagraph xwpfParagraph = paragraphs1.get(i);
                            List<XWPFRun> runs = xwpfParagraph.getRuns();
                            for (int j = 0; j < runs.size(); j++) {
                                XWPFRun xwpfRun = runs.get(j);
                                String text = xwpfRun.text();
                                Matcher matcher = compile.matcher(text);
                                while (matcher.find()) {
                                    String group = matcher.group();
                                    String group1 = matcher.group(1);
                                    logger.debug("匹配${}:" + group1);
                                    String tep = "";
                                    if (group1.indexOf(".") == -1) {
                                        //普通变量
                                        tep = replaceBaseVarible(group1, data, false);
                                    } else {
                                        //对象变量
                                        tep = replaceObjectVarible(group1, data, false);
                                    }
                                    text = text.replace(group, tep);
                                }
                                xwpfRun.setText(text, 0);
                                //表格样式一致-->没有此段表格会默认左对齐
                                //有此段会使表格格式一致
                                // CTTc cttc = cell.getCTTc();
                                // CTTcPr ctPr = cttc.addNewTcPr();
                                // ctPr.addNewVAlign().setVal(STVerticalJc.CENTER);
                                // cttc.getPList().get(0).addNewPPr().addNewJc().setVal(STJc.CENTER);
                            }
                        }
                    });
                }
                rowLine.addAndGet(1);
            }

            //
            if (temp.size() != 0) {
//                AtomicInteger atomicInteger = new AtomicInteger(0);
                Iterator<Map.Entry<Integer, Object>> iterator1 = temp.entrySet().iterator();

                //一个key代表一个循环
                int changeLine = 0;
                while (iterator1.hasNext()) {
                    Map.Entry<Integer, Object> entry = iterator1.next();
                    int mapRowLine = entry.getKey() + changeLine;
                    if (entry.getValue() == null) {

                    } else {
                        ArrayList<XWPFTableRow> value = (ArrayList<XWPFTableRow>) entry.getValue();
                        for (int i = 0; i < value.size(); i++) {
                            table.addRow(value.get(i), mapRowLine + i + 1);
                            changeLine++;
                        }
                    }
                    table.removeRow(mapRowLine);
                    changeLine--;
                }
            }
            BigInteger bigInteger = new BigInteger("1");
            BigInteger bigIntegerSpace = new BigInteger("0");
            List<XWPFTableRow> tableRows = table.getRows();
            for (int i = 0; i < tableRows.size(); i++) {
                XWPFTableRow tableRow = tableRows.get(i);
                List<XWPFTableCell> tableCells = tableRow.getTableCells();
                for (int i1 = 0; i1 < tableCells.size(); i1++) {
                    XWPFTableCell tableCell = tableCells.get(i1);

                    if (tableCell.getCTTc().getTcPr().getTcBorders() != null) {
                        CTTcBorders tcBorders = tableCell.getCTTc().getTcPr().getTcBorders();
                        CTBorder left = tcBorders.getLeft();
                        CTBorder top = tcBorders.getTop();
                        CTBorder right = tcBorders.getRight();
                        CTBorder bottom = tcBorders.getBottom();
//                        if(left != null){
//                            left.setSz(bigInteger);
//                            left.setSpace(bigIntegerSpace);
//                        }else{
                        left = tcBorders.addNewLeft();
                        left.setSz(bigInteger);
                        left.setSpace(bigIntegerSpace);
//                        }
//                        if(right != null){
//                            right.setSz(bigInteger);
//                            right.setSpace(bigIntegerSpace);
//                        }else{
                        right = tcBorders.addNewRight();
                        right.setSz(bigInteger);
                        right.setSpace(bigIntegerSpace);
//                        }
//                        if(top != null){
//                            top.setSz(bigInteger);
//                            top.setSpace(bigIntegerSpace);
//                        }else{
                        top = tcBorders.addNewTop();
                        top.setSz(bigInteger);
                        top.setSpace(bigIntegerSpace);
//                        }
//                        if(bottom != null){
//                            bottom.setSz(bigInteger);
//                            bottom.setSpace(bigIntegerSpace);
//                        }else{
                        bottom = tcBorders.addNewBottom();
                        bottom.setSz(bigInteger);
                        bottom.setSpace(bigIntegerSpace);
//                        }
                        tableCell.getCTTc().getTcPr().setTcBorders(tcBorders);
                    } else {
                        CTTcBorders borders = tableCell.getCTTc().getTcPr().addNewTcBorders();
                        CTBorder left = borders.addNewLeft();
                        CTBorder top = borders.addNewTop();
                        CTBorder right = borders.addNewRight();
                        CTBorder bottom = borders.addNewBottom();
                        if (left != null) {
                            left.setSz(bigInteger);
                            left.setSpace(bigIntegerSpace);
                        }
                        if (right != null) {
                            right.setSz(bigInteger);
                            right.setSpace(bigIntegerSpace);
                        }
                        if (top != null) {
                            top.setSz(bigInteger);
                            top.setSpace(bigIntegerSpace);
                        }
                        if (bottom != null) {
                            bottom.setSz(bigInteger);
                            bottom.setSpace(bigIntegerSpace);
                        }
                        tableCell.getCTTc().getTcPr().setTcBorders(borders);
                    }
//                    if(i != 0){
//                        //不是第一行
//                        if(tableCell.getCTTc().getTcPr().getTcBorders() != null){
//                            CTTcBorders tcBorders = tableCell.getCTTc().getTcPr().getTcBorders();
//                            if(tcBorders.getTop() != null){
//                                tcBorders.getTop().setVal(STBorder.NIL);
//                                tcBorders.getTop().setSz(new BigInteger("1"));
//                            }else{
//                                CTBorder ctBorder = tcBorders.addNewTop();
//                                ctBorder.setVal(STBorder.NIL);
//                                ctBorder.setSz(new BigInteger("1"));
//                                tcBorders.setTop(ctBorder);
//                            }
//                            tableCell.getCTTc().getTcPr().setTcBorders(tcBorders);
//                        }else{
//                            CTTcBorders borders = tableCell.getCTTc().getTcPr().addNewTcBorders();
//                            CTBorder ctBorder = borders.addNewTop();
//                            ctBorder.setVal(STBorder.NIL);
//                            ctBorder.setSz(new BigInteger("1"));
//                            borders.setTop(ctBorder);
//                            tableCell.getCTTc().getTcPr().setTcBorders(borders);
//                        }
//                    }
                    //前一个单元格
                    if (i1 != 0) {
                        if (tableCell.getCTTc().getTcPr().getTcBorders() != null) {
                            CTTcBorders tcBorders = tableCell.getCTTc().getTcPr().getTcBorders();
                            if (tcBorders.getLeft() != null) {
                                tcBorders.getLeft().setVal(STBorder.NONE);
                                tcBorders.getLeft().setSz(new BigInteger("0"));
                            } else {
                                CTBorder ctBorder = tcBorders.addNewLeft();
                                ctBorder.setVal(STBorder.NONE);
                                ctBorder.setSz(new BigInteger("0"));
                                tcBorders.setLeft(ctBorder);
                            }
                            tableCell.getCTTc().getTcPr().setTcBorders(tcBorders);
                        } else {
                            CTTcBorders borders = tableCell.getCTTc().getTcPr().addNewTcBorders();
                            CTBorder ctBorder = borders.addNewLeft();
                            ctBorder.setVal(STBorder.NONE);
                            ctBorder.setSz(new BigInteger("0"));
                            borders.setLeft(ctBorder);
                            tableCell.getCTTc().getTcPr().setTcBorders(borders);
                        }
                    }
                }
            }
        });

        logger.debug("--------------替换表格文本end--------------");
        long end = System.currentTimeMillis();
        System.out.println("docx变量替换结束，耗时" + (end - start) + "毫秒");

        return document;
    }

    /**
     * 表格行是否是循环行
     *
     * @param cells
     * @param data
     * @return
     */
    private static boolean isLoopRow(List<XWPFTableCell> cells, Map<String, Object> data) {
        for (int i = 0; i < cells.size(); i++) {
            String text = cells.get(i).getText();
            Matcher matcher = compile.matcher(text);
            if (matcher.find()) {
                String trim = matcher.group().replace("${", "").replace("}", "").trim();
                String[] split = trim.split("\\.");
                if (!data.containsKey(split[0])) {
                    return false;
                }
                Object o = data.get(split[0]);
                if (o instanceof List) {
                    //此行是循环数据
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是循环表格
     *
     * @param rows
     * @param data
     * @return
     */
    private static boolean isLoop(List<XWPFTableRow> rows, Map<String, Object> data) {
        for (int i = 0; i < rows.size(); i++) {
            List<XWPFTableCell> tableCells = rows.get(i).getTableCells();
            for (int j = 0; j < tableCells.size(); j++) {
                XWPFTableCell xwpfTableCell = tableCells.get(j);
                String text = xwpfTableCell.getText();
                Matcher matcher = compile.matcher(text);
                if (matcher.find()) {
                    String trim = matcher.group().replace("${", "").replace("}", "").trim();
                    String[] split = trim.split("\\.");
                    if (!data.containsKey(split[0])) {
                        continue;
                        //throw new RuntimeException("键值" + split[0] + "在结果中未找到");
                    }
                    Object o = data.get(split[0]);
                    if (o instanceof List) {
                        return true;
                    }
                } else {
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * 渲染表格
     *
     * @param table
     * @param xwpfTableRow
     * @param data
     * @param temp
     * @throws IOException
     * @throws XmlException
     */
    private static void renderTable(XWPFTable table, XWPFTableRow xwpfTableRow, Map<String, Object> data, int rowLine, HashMap<Integer, Object> temp) throws IOException, XmlException {
        List<XWPFTableCell> tableCells = xwpfTableRow.getTableCells();
        ArrayList<CellProperty> conf = new ArrayList();
        String key = "";
        for (int i = 0; i < tableCells.size(); i++) {
            XWPFTableCell cell = tableCells.get(i);
            String text = cell.getText();
            Matcher matcher = compile.matcher(text);
            if (matcher.find()) {
                String trim = matcher.group().replace("${", "").replace("}", "").trim();
                if (trim.indexOf(".") == -1) {
                    throw new RuntimeException("表格的属性key必须包含.");
                }
                String[] split = trim.split("\\.");
                key = split[0];
                conf.add(new CellProperty(i, split[1], false));
            } else {
                conf.add(new CellProperty(i, text, true));
            }
        }

        if (data.containsKey(key) && data.get(key) != null) {
            Object o = data.get(key);
            JSONArray datas = JSONObject.parseArray(JSONObject.toJSONString(o));
            if (datas.size() == 0) {
                temp.put(rowLine, null);
            }
            for (Object each : datas) {
                CTRow ctrow = CTRow.Factory.parse(xwpfTableRow.getCtRow().newInputStream());
                XWPFTableRow row = new XWPFTableRow(ctrow, table);

                List<XWPFTableCell> tableCells1 = row.getTableCells();
                for (int i = 0; i < tableCells1.size(); i++) {
                    XWPFTableCell xwpfTableCell = tableCells1.get(i);
                    CellProperty cellProperty = conf.get(i);
                    if (!cellProperty.isText) {
                        String sRes = "";
                        JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(each));
                        if (null == each) {
                            continue;
                        } else {

                            //sRes = jsonObject.get(cellProperty.text) == null ? "" : String.valueOf(jsonObject.get(cellProperty.text));
                        }
                        List<XWPFParagraph> paragraphs = xwpfTableCell.getParagraphs();
                        String finalSRes = sRes;
                        paragraphs.stream().forEach(p -> {
                            List<XWPFRun> runs = p.getRuns();
                            runs.stream().forEach(run -> {
                                String text = run.text();
                                String realText = "";
                                Matcher matcherT = compile.matcher(text);
                                Matcher matcher = compile.matcher(text);
                                if (matcherT.find()) {
                                    while (matcher.find()) {
                                        String group = matcher.group(1);
                                        String[] split = group.split(":");
                                        //循环表格匹配必然时   xxx.aaa
                                        String[] split1 = group.split("\\.");
                                        String s = split1[1];
                                        String realVal = "";
                                        if (s.indexOf(":") != -1) {
                                            String field = s.split(":")[0];
                                            int length = Integer.parseInt(s.split(":")[1]);
                                            String o1 = jsonObject.get(field) == null ? "" : (jsonObject.get(field).toString());
                                            String s1 = ChineseUtils.fillStr(o1, length, null, null);
                                            realText = text.replace(matcher.group(0), s1);
                                        } else {
                                            String field = s;
                                            String o1 = jsonObject.get(field) == null ? "" : (jsonObject.get(field).toString());
                                            realText = text.replace(matcher.group(0), o1);
                                        }
                                    }
                                    run.setText(realText, 0);
                                } else {
                                    run.setText(run.text(), 0);
                                }
                            });
                        });
                    }
                }

                if (temp.containsKey(rowLine)) {
                    ArrayList arrayList = (ArrayList) temp.get(rowLine);
                    arrayList.add(row);
                } else {
                    temp.put(rowLine, new ArrayList() {{
                        add(row);
                    }});
                }
            }
        }
    }

    /**
     * 获取表格变量初始行
     *
     * @param rows
     * @return
     */
    private static int getInitRow(List<XWPFTableRow> rows) {
        int from = 0;
        for (int i = 0; i < rows.size(); i++) {
            List<XWPFTableCell> tableCells = rows.get(i).getTableCells();
            for (int j = 0; j < tableCells.size(); j++) {
                if (compile.matcher(tableCells.get(j).getText()).find()) {
                    return from;
                }
            }

            from++;
        }

        return from;
    }

    /**
     * 替换对象变量属性值
     *
     * @param fieldName
     * @param data
     * @param needFill
     * @return
     */
    private static String replaceObjectVarible(String fieldName, Map<String, Object> data, Boolean needFill) {
        int count = 0;
        String[] split = fieldName.split("\\.");
        String obj = split[0];
        String field = split[1];
        if (field.indexOf(":") != -1) {
            count = Integer.parseInt(field.split(":")[1]);
            field = field.split(":")[0];
        }
        String res = "";
        if (data.containsKey(obj) && data.get(obj) != null) {
            Object o = data.get(obj);
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(o));

            res = String.valueOf(jsonObject.get(field) == null ? "" : jsonObject.get(field));
        }
        if (needFill || count != 0) {
            res = ChineseUtils.fillStr(res, count, null, null);
        }
        return res;
    }

    /**
     * 替换不同变量
     *
     * @param fieldName
     * @param data
     * @param needFill
     * @return
     */
    private static String replaceBaseVarible(String fieldName, Map<String, Object> data, Boolean needFill) {

        String count = "0";
        //一行最大容纳字符数
        String rowSize = null;
        //不包含尾行最大容纳字符数
        String rowSizeNoInclude = null;
        if (fieldName.indexOf(":") != -1) {
            String[] split = fieldName.split(":", 999);
            switch (split.length) {
                case 1:
                    fieldName = split[0];
                    break;
                case 2:
                    fieldName = split[0];
                    count = split[1];
                    break;
                case 3:
                    fieldName = split[0];
                    count = split[1];
                    rowSize = split[2];
                    break;
                case 4:
                    fieldName = split[0];
                    count = split[1];
                    rowSize = split[2];
                    rowSizeNoInclude = split[3];
                    break;
            }
        }
        String res = "";
        if (data.containsKey(fieldName)) {
            res = String.valueOf(data.get(fieldName) == null ? "" : data.get(fieldName));
        }
        if (needFill || count != "0") {
            if (count.endsWith("L")) {//左填充
                res = ChineseUtils.fillStr(res, Integer.parseInt(count.substring(0, count.lastIndexOf("L"))), ChineseUtils.FillPosition.LEFT, rowSize, rowSizeNoInclude);
            } else if (count.endsWith("RF")) {
                res = ChineseUtils.fillStr(res, Integer.parseInt(count.substring(0, count.lastIndexOf("RF"))), ChineseUtils.FillPosition.RIGHT, rowSize, rowSizeNoInclude);
            } else {
                res = ChineseUtils.fillStr(res, Integer.parseInt(count), rowSize, rowSizeNoInclude);
            }
        }
        return res;
    }

//    public static void main(String[] args) throws Exception {
//        File file = new File("E:/tmp/new.docx");
//
//
//        if (file.exists()) {
//            file.delete();
//        }
//        file.createNewFile();
//
//        FileOutputStream out = new FileOutputStream(file);
//
//        HashMap data = new HashMap();
//        data.put("danwei", "南京市");
//
//        data.put("skg", new ArrayList() {{
//            add(new HashMap() {{
//                put("id", "11");
//                put("name", "zhangsan");
//            }});
//            add(new HashMap() {{
//                put("id", "22");
//                put("name", "lisi");
//            }});
//        }});
//
//        DocxUtils.replaceAndFill(new FileInputStream("E:/tmp/aaaa.docx"), data, true);
//    }

    /**
     * 合并文档
     *
     * @param src
     * @throws Exception
     */
    public static XWPFDocument mergeTwo(List<XWPFDocument> src) throws Exception {
        XWPFDocument document = src.get(0);

        CTBody body = src.get(0).getDocument().getBody();
        for (int i = 1; i < src.size(); i++) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setPageBreak(true);
            CTBody src2Body = src.get(i).getDocument().getBody();
            appendBody(body, src2Body);
        }
        return document;
    }

    /**
     * 合并文档
     *
     * @param src
     * @param out
     * @throws Exception
     */
    public static void merge(List<XWPFDocument> src, OutputStream out) throws Exception {
        XWPFDocument document = src.get(0);

        CTBody body = src.get(0).getDocument().getBody();
        for (int i = 1; i < src.size(); i++) {
            XWPFParagraph paragraph = document.createParagraph();
            paragraph.setPageBreak(true);
            CTBody src2Body = src.get(i).getDocument().getBody();
            appendBody(body, src2Body);
        }
        document.write(out);
    }

    /**
     * 合并文档内容
     *
     * @param src    目标文档
     * @param append 要合并的文档
     * @throws Exception
     */
    private static void appendBody(CTBody src, CTBody append) throws Exception {
        XmlOptions optionsOuter = new XmlOptions();
        optionsOuter.setSaveOuter();
        String appendString = append.xmlText(optionsOuter);
        String srcString = src.xmlText();
        String prefix = srcString.substring(0, srcString.indexOf(">") + 1);
        String mainPart = srcString.substring(srcString.indexOf(">") + 1,
                srcString.lastIndexOf("<"));
        String sufix = srcString.substring(srcString.lastIndexOf("<"));
        String addPart = appendString.substring(appendString.indexOf(">") + 1,
                appendString.lastIndexOf("<"));
        CTBody makeBody = CTBody.Factory.parse(prefix + mainPart + addPart
                + sufix);
        src.set(makeBody);

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class CellProperty {
        int index;
        String text;
        Boolean isText;
    }
}
