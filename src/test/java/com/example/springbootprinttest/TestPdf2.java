package com.example.springbootprinttest;

import com.alibaba.fastjson.JSONObject;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class TestPdf2 {

    @Test
    public void importKeshidaUps() throws Exception {
        try {
            FileInputStream fis = new FileInputStream(new File("C:\\Users\\Administrator\\Desktop\\附件2. 500千伏智能变电站典型二次安措汇编.docx"));
            XWPFDocument document = new XWPFDocument(fis);
            List<XWPFTable> tables = document.getTables();


            List<Map> list = new ArrayList<>();
            Map<String, Object> map1 = new HashMap<>();
            Map<String, Object> map2 = new HashMap<>();
            List<Map> list2 = new ArrayList<>();

            for (XWPFTable table : tables) {
                boolean b = false;
                map1 = new HashMap<>();
                list2 = new ArrayList<>();
                boolean type = false;
                boolean type2 = false;
                for (XWPFTableRow row : table.getRows()) {
                    map2 = new HashMap<>();
                    for (int i = 0,n=row.getTableCells().size(); i < n; i++) {
                        String text = row.getCell(i).getText().trim().replace(" ", "").replace("\\t","");
                        if (text.contains("二次设备状态记录")){
                            type = false;
                            type2 = false;
                        }
                        if (text.contains("被试设备名称")){
                            b = true;
                            int j = i+1;
                            map1.put("deviceName",row.getCell(j).getText());
                        }
                        if (text.contains("工作内容：")){
                            if (null != text && text.split("：").length>=2){
                                map1.put("cont",text.split("：")[1]);
                            }
                        }
                        if ( b && type) {
                            if (n == 1){
                                map2.put("top",row.getCell(i).getText());
                            }
                            if (null != row.getCell(2)){
                                map2.put("type",row.getCell(2).getText());
                            }
                            if (null != row.getCell(3)){
                                map2.put("name",row.getCell(3).getText());
                            }
                            list2.add(map2);
                            type = false;
                        }
                        if ("恢复".equals(text) || "恢 复".equals(text)){
                            type = true;
                            type2 = true;
                        }
                        if ("其它".equals(text) || "其他".equals(text) || "其余".equals(text)){
                            type = false;
                            type2 = false;
                        }
                        System.out.print(row.getCell(i).getText() + "\t");
                    }
                    if (type2) {
                        type = true;
                    }
                    System.out.println();
                }
                if (b) {
                    map1.put("device",list2);
                    list.add(map1);
                }
                System.out.println("--------------------");
            }
            System.out.println(JSONObject.toJSONString(list));

            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
