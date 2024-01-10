package com.example.springbootprinttest;

import com.deepoove.poi.data.RowRenderData;
import com.deepoove.poi.policy.DynamicTableRenderPolicy;
import com.deepoove.poi.policy.TableRenderPolicy;
import com.deepoove.poi.util.TableTools;
import com.example.springbootprinttest.entry.ServerTableData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.List;
import java.util.Map;

public class ServerTablePolicy extends DynamicTableRenderPolicy {
    @Override
    public void render(XWPFTable xwpfTable, Object tableData) throws Exception {
        if (null == tableData) {
            return;
        }

        // 参数数据声明
        ServerTableData serverTableData = (ServerTableData) tableData;
        List<RowRenderData> serverDataList = serverTableData.getServerDataList();
        List<Map<String, Object>> groupDataList = serverTableData.getGroupDataList();
        Integer mergeColumn = serverTableData.getMergeColumn();

        if (CollectionUtils.isNotEmpty(serverDataList)) {
            // 先删除一行, demo中第一行是为了调整 三线表 样式
//            xwpfTable.removeRow(1);//如果表单里面只有一行表头信息的话，这里设置成1
            xwpfTable.removeRow(4);//如果表单里面有两行数据，则设置成2

            // 行从中间插入, 因此采用倒序渲染数据
            for (int i = serverDataList.size() - 1; i >= 0; i--) {
//                XWPFTableRow newRow = xwpfTable.insertNewTableRow(1);//从表单的哪行开始插入数据，一般表单有一个标题，所以这里设置1；
                XWPFTableRow newRow = xwpfTable.insertNewTableRow(4);//从表单的哪行开始插入数据，如果表单里面有两行，这是设置成2
//                newRow.setHeight(400);
                for (int j = 0; j < 4; j++) {//因为我的表单是3列，所以这里是3
                    newRow.createCell();
                }
                // 渲染一行数据
                TableRenderPolicy.Helper.renderRow(newRow, serverDataList.get(i));
            }

            // 处理合并
            for (int i = 0; i < serverDataList.size(); i++) {
                // 获取要合并的名称那一列数据 mergeColumn代表要合并的列，从0开始
                String typeNameData = serverDataList.get(i).getCells().get(mergeColumn).getParagraphs().get(0).getContents().get(0).toString();
                for (int j = 0; j < groupDataList.size(); j++) {
                    String typeNameTemplate = String.valueOf(groupDataList.get(j).get("typeName"));
                    int listSize = Integer.parseInt(String.valueOf(groupDataList.get(j).get("listSize")));
                    if(listSize == 1){
                        continue;
                    }
                    // 若匹配上 就直接合并
                    if (typeNameTemplate.equals(typeNameData)) {
//                        TableTools.mergeCellsVertically(xwpfTable, 0, i + 1, i + listSize);//如果表单里面只有一行表头信息的话，用这个语句
                        TableTools.mergeCellsVertically(xwpfTable, 1, i + 4, i + 3 + listSize);//如果表单里面有两行数据，则用这个语句
                        groupDataList.remove(j);
                        break;
                    }
                }
            }
        }
    }
}