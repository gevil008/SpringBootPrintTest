package com.example.springbootprinttest;

import com.alibaba.fastjson.JSONObject;
import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.data.Rows;
import com.deepoove.poi.util.PoitlIOUtils;
import com.example.springbootprinttest.entry.ServerTableData;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class PreTestExport {

    public static void main(String[] args) throws Exception {
        // 获取模板文件流
        ClassPathResource resource = new ClassPathResource("/templates/变电站倒闸操作作业风险预控措施卡.docx");
        //ClassPathResource resource = new ClassPathResource("/templates/全县烟区.docx");
//        InputStream resourceAsStream = new FileInputStream(new File("E:/word/poi-tl-pre-old2.docx"));
        InputStream resourceAsStream = resource.getInputStream();
        //poi-tl 配置
        ConfigureBuilder builder = Configure.builder();
        builder.useSpringEL(false);

        String str = "{\"operationContent\":\"操作任务111\",\"executors\":\"执行人员222\",\"executeTime\":\"2024-01-10 12:00:00\",\"dangersPreventiveMeasures\":[{\"dangerousAnalysis\":\"危险点分析1\",\"preventionMeasure\":\"预控措施1\",\"isExecute\":\"1\"},{\"dangerousAnalysis\":\"危险点分析1\",\"preventionMeasure\":\"预控措施2\",\"isExecute\":\"1\"},{\"dangerousAnalysis\":\"危险点分析1\",\"preventionMeasure\":\"预控措施3\",\"isExecute\":\"0\"},{\"dangerousAnalysis\":\"危险点分析2\",\"preventionMeasure\":\"预控措施4\",\"isExecute\":\"1\"},{\"dangerousAnalysis\":\"危险点分析3\",\"preventionMeasure\":\"预控措施5\",\"isExecute\":\"0\"},{\"dangerousAnalysis\":\"危险点分析3\",\"preventionMeasure\":\"预控措施6\",\"isExecute\":\"1\"},{\"dangerousAnalysis\":\"1213\",\"preventionMeasure\":\"预控措施7\",\"isExecute\":\"0\"},{\"dangerousAnalysis\":\"123\",\"preventionMeasure\":\"预控措施7\",\"isExecute\":\"1\"},{\"dangerousAnalysis\":\"哈哈哈\",\"preventionMeasure\":\"预控措施6\",\"isExecute\":\"1\"},{\"dangerousAnalysis\":\"0\",\"preventionMeasure\":\"预控措施6\",\"isExecute\":\"1\"}]}";
        Map<String, Object> map = JSONObject.parseObject(str, Map.class);
        //HashMap<String, Object> map = new HashMap<>();
        // 伪造一个表格数据
        //单个表格
        List<Map> maps = JSONObject.parseArray(JSONObject.toJSONString(map.get("dangersPreventiveMeasures")), Map.class);
        ServerTableData oneTable = getServerTableData(maps);
        map.put("oneTable", oneTable);
        //map.put("title", "2023");
        map.remove("dangersPreventiveMeasures");
        System.out.println(JSONObject.toJSONString(map));
        builder.bind("oneTable", new ServerTablePolicy());


        XWPFTemplate template = XWPFTemplate.compile(Objects.requireNonNull(resourceAsStream), builder.build()).render(map);
        // 获取表格对象
        XWPFTable table = template.getXWPFDocument().getTableArray(0);

        // 设置表格边框样式为黑色实线
        CTTblPr tblPr = table.getCTTbl().getTblPr();
        CTTblBorders tblBorders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        CTBorder border = tblBorders.addNewTop();
        border.setVal(STBorder.SINGLE);
        border.setColor("000000");

        // HttpServletResponse response
        OutputStream out = new FileOutputStream(new File("C:/Users/Administrator/Desktop/" + System.currentTimeMillis() + ".docx"));
        BufferedOutputStream bos = new BufferedOutputStream(out);
        template.write(bos);
        bos.flush();
        out.flush();
        PoitlIOUtils.closeQuietlyMulti(template, bos, out);

    }

    private static ServerTableData getServerTableData(List<Map> maps) {
        ServerTableData serverTableData = new ServerTableData();
        List<RowRenderData> serverDataList = new ArrayList<>();
        List<Map<String, Object>> groupDataList = new ArrayList<Map<String, Object>>();
        // 解析危险点数据
        List<String> analysis = maps
                .stream()
                .filter(e -> e.get("dangerousAnalysis") != null && !"".equals(e.get("dangerousAnalysis")))
                .map(e -> e.get("dangerousAnalysis").toString())
                .collect(Collectors.toList())
                .stream()
                .distinct()
                .collect(Collectors.toList());
        Map<Object, List<Map>> dangerousAnalysis = maps.stream().collect(Collectors.groupingBy(e -> e.get("dangerousAnalysis")));
        int j = 1;
        for (String s : analysis) {
            List<Map> entry = dangerousAnalysis.get(s);
            int n = entry.size();
            for (int i = 0; i < n; i++) {
                Map<String, String> map = entry.get(i);
                String isExecute = map.get("isExecute");
                if ("1".equals(isExecute)) {
                    isExecute = "\u2714";
                } else {
                    isExecute = "";
                }
                serverDataList.add(Rows.of(j + "", map.get("dangerousAnalysis"), map.get("preventionMeasure"), isExecute).textFontSize(14).center().create());
                j++;
            }
            Map<String, Object> groupData = new HashMap<>();
            groupData.put("typeName", s);
            groupData.put("listSize", n);
            groupDataList.add(groupData);
        }


        /*RowRenderData serverData1 = Rows.of("广州", "天河", "0.3").textFontSize(14).center().create();
        RowRenderData serverData2 = Rows.of("广州", "白云", "0.2").textFontSize(14).center().create();
        RowRenderData serverData3 = Rows.of("广州", "东山", "0.1").textFontSize(14).center().create();
        serverDataList.add(serverData1);
        serverDataList.add(serverData2);
        serverDataList.add(serverData3);

        RowRenderData fs = Rows.of("佛山", "禅城", "0.3").textFontSize(14).center().create();
        serverDataList.add(fs);

        RowRenderData sz1 = Rows.of("深圳", "南山", "0.3").textFontSize(14).center().create();
        RowRenderData sz2 = Rows.of("深圳", "福田", "0.3").textFontSize(14).center().create();
        serverDataList.add(sz1);
        serverDataList.add(sz2);
        Map<String, Object> groupData1 = new HashMap<String, Object>();
        groupData1.put("typeName", "广州");
        groupData1.put("listSize", "3");
        Map<String, Object> groupData2 = new HashMap<String, Object>();
        groupData2.put("typeName", "深圳");
        groupData2.put("listSize", "2");

        Map<String, Object> groupData3 = new HashMap<String, Object>();
        groupData3.put("typeName", "佛山");
        groupData3.put("listSize", "1");

        groupDataList.add(groupData1);
        groupDataList.add(groupData2);
        groupDataList.add(groupData3);*/


        serverTableData.setServerDataList(serverDataList);
        serverTableData.setGroupDataList(groupDataList);
        serverTableData.setMergeColumn(1);
        return serverTableData;
    }

}