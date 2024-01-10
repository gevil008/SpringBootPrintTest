package com.example.springbootprinttest.entry;

import com.deepoove.poi.data.RowRenderData;
 
import java.util.List;
import java.util.Map;
 
public class ServerTableData {
 
    /**
     *  携带表格中真实数据
     */
    private List<RowRenderData> serverDataList;
 
    /**
     * 携带要分组的信息
     */
    private List<Map<String, Object>> groupDataList;
 
    /**
     * 需要合并的列，从0开始
     */
    private Integer mergeColumn;
 
    public List<RowRenderData> getServerDataList() {
        return serverDataList;
    }
 
    public void setServerDataList(List<RowRenderData> serverDataList) {
        this.serverDataList = serverDataList;
    }
 
    public List<Map<String, Object>> getGroupDataList() {
        return groupDataList;
    }
 
    public void setGroupDataList(List<Map<String, Object>> groupDataList) {
        this.groupDataList = groupDataList;
    }
 
    public Integer getMergeColumn() {
        return mergeColumn;
    }
 
    public void setMergeColumn(Integer mergeColumn) {
        this.mergeColumn = mergeColumn;
    }
}