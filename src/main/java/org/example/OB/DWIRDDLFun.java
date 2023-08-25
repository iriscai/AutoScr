package org.example.OB;

import com.alibaba.excel.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DWIRDDLFun {
    static String dateVersion;

    static {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHss");
        dateVersion = formatter.format(date);
    }

    public void getDwrDwiSpricts(String readFileName, String sheetNum, String jhOwner) throws IOException {
        DataSplit dataSplit = new DataSplit();
        YupiDataListener yupiDataListener = new YupiDataListener();
        CheckExcel checkExcel = new CheckExcel();
        checkExcel.checkFile(readFileName,sheetNum,"dwi");
        Map<String, Integer> columnsPosiMap = checkExcel.getColumnsPosiMap(readFileName, sheetNum, "dwi");
        List<Map<Integer, String>> dataList = yupiDataListener.doRead(readFileName, sheetNum);
        //tablePriKeyMap 为原始数据复合主键 类似dwi_mkt_tbsrnjpricecheck_tg:id&version&channel
        HashMap<String, StringBuffer> tablePriKeyMap = new HashMap<>();
        //tableCommentMap为表注释map 类似 dwi_mkt_tbsrnjpricecheck_tg：特钢日定价版本核准生效
        HashMap<String, String> tableCommentMap = new HashMap<>();
        //tableSourceTableNameMap 类似 dwi_mkt_tbsrnjpricecheck_tg：TBSRNJPRICECHECK
        HashMap<String, String> tableSourceTableNameMap = new HashMap<>();
        //tableSourceDBNameMap为源系统 类似 dwi_mkt_tbsrnjpricecheck_tg：ERP
        HashMap<String, String> tableSourceDBNameMap = new HashMap<>();
        //tableL2NameMap 类似 dwi_mkt_tbsrnjpricecheck_tg：SAL
        HashMap<String, String> tableL2NameMap = new HashMap<>();
        //columnsCommentMap为字段注释map 类似 dwi_mkt_tbsrnjpricecheck_tg：<versionno:版本号,newgrade:新客户级别>
        HashMap<String, HashMap<String, String>> columnsCommentMap = new HashMap<>();
        //columnsIfNullMap 类似 dwi_mkt_tbsrnjpricecheck_tg：<versionno:"  NOT NUL",newgrade:"">
        HashMap<String, HashMap<String, String>> columnsIfNullMap = new HashMap<>();
        //odscolumnsLengthMap为字段长度map 类似 dwi_mkt_tbsrnjpricecheck_tg：<versionno:VARCHAR(2),newgrade:DECIMAL(10,2)>
        HashMap<String, HashMap<String, String>> columnsLengthMap = new HashMap<>();
        for (Map<Integer, String> data : dataList) {
            //添加map数据
            tableSourceTableNameMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("源系统表名-英文")).toLowerCase());
            tableSourceDBNameMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("来源")).toLowerCase());
            tableL2NameMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("L1资产编码")));
            tableCommentMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("逻辑实体-中文(L4)")));
            //字段是否为空
            if (!columnsIfNullMap.containsKey(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")))) {
                HashMap<String, String> IfNullCommentsMap = new HashMap<>();
                if (data.get(columnsPosiMap.get("是否为空")).trim().equals("Y")||
                        data.get(columnsPosiMap.get("是否为空")).trim().equals("是")||
                        data.get(columnsPosiMap.get("是否为空")).trim().equals("1")){
                    IfNullCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")),"");
                }
                else {IfNullCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)"))," NOT NULL");}
                columnsIfNullMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), IfNullCommentsMap);
            } else{
                HashMap<String, String> IfNullCommentsMap = columnsIfNullMap.get(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")));
                if (data.get(columnsPosiMap.get("是否为空")).trim().equals("Y")||
                        data.get(columnsPosiMap.get("是否为空")).trim().equals("是")||
                        data.get(columnsPosiMap.get("是否为空")).trim().equals("1")){
                    IfNullCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")),"");
                }
                else {IfNullCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)"))," NOT NULL");}
                columnsIfNullMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), IfNullCommentsMap);
            }
            //主键
            if (!tablePriKeyMap.containsKey(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")))) {

                if ((data.get(columnsPosiMap.get("是否为主键")) != null) && (data.get(columnsPosiMap.get("是否为主键")).trim().equals("Y") || data.get(columnsPosiMap.get("是否为主键")).trim().equals("是") || data.get(columnsPosiMap.get("是否为主键")).trim().equals("1"))) {
                    StringBuffer priKeytringBuffer = new StringBuffer();
                    priKeytringBuffer.append(data.get(columnsPosiMap.get("属性-英文(L5)")));
                    tablePriKeyMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), priKeytringBuffer);
                } else {
                }
            } else {
                StringBuffer priKeytringBuffer = tablePriKeyMap.get(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")));
                if ((data.get(columnsPosiMap.get("是否为主键")) != null) && (data.get(columnsPosiMap.get("是否为主键")).trim().equals("Y") || data.get(columnsPosiMap.get("是否为主键")).trim().equals("是"))) {
                    priKeytringBuffer.append("&" + data.get(columnsPosiMap.get("属性-英文(L5)")));
                }
                tablePriKeyMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), priKeytringBuffer);
            }
            //字段备注
            if (!columnsCommentMap.containsKey(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")))) {
                HashMap<String, String> tableColumnsCommentsMap = new HashMap<>();
                tableColumnsCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")), data.get(columnsPosiMap.get("属性-中文(L5)")));
                columnsCommentMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), tableColumnsCommentsMap);
            } else {
                HashMap<String, String> tableColumnsCommentsMap = columnsCommentMap.get(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")));
                tableColumnsCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")), data.get(columnsPosiMap.get("属性-中文(L5)")));
                columnsCommentMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), tableColumnsCommentsMap);
            }
            //字段数据长度
            if (!columnsLengthMap.containsKey(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")))) {
                HashMap<String, String> tablecolumnsLengthCommentsMap = new HashMap<>();
                tablecolumnsLengthCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")), getcolumnsLengthMap(data.get(columnsPosiMap.get("字段类型")), data.get(columnsPosiMap.get("字段长度"))));
                columnsLengthMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), tablecolumnsLengthCommentsMap);
            } else {
                HashMap<String, String> tablecolumnsLengthCommentsMap = columnsLengthMap.get(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")));
                tablecolumnsLengthCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")), getcolumnsLengthMap(data.get(columnsPosiMap.get("字段类型")), data.get(columnsPosiMap.get("字段长度"))));
                columnsLengthMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), tablecolumnsLengthCommentsMap);
            }
        }
        System.out.println(tableCommentMap);

        dataList.clear();
        int i = 1;
        //添加表头
        HashMap<Integer, String> titleName = new HashMap<>();
        dataList.add(0, titleName);
        //去重
        for (HashMap.Entry<String, String> entry : tableCommentMap.entrySet()) {
            dataSplit.writeInsertDwiETL(entry.getKey(), tableL2NameMap.get(entry.getKey()), tableSourceTableNameMap.get(entry.getKey()).toLowerCase(), tableSourceDBNameMap.get(entry.getKey()).toLowerCase(), columnsCommentMap.get(entry.getKey()), tableCommentMap.get(entry.getKey()), jhOwner);
            try {
                tablePriKeyMap.get(entry.getKey());
                dataSplit.writeDwrJcFDDLWithPriKeys(entry.getKey(), tableL2NameMap.get(entry.getKey()), columnsCommentMap.get(entry.getKey()), entry.getValue(), tablePriKeyMap.get(entry.getKey()).toString(),columnsIfNullMap, jhOwner);
            } catch (Exception e) {
                dataSplit.writeDwrJcFDDLWithOutPriKey(entry.getKey(), tableL2NameMap.get(entry.getKey()), columnsCommentMap.get(entry.getKey()), entry.getValue(), columnsIfNullMap,jhOwner);
            }
        }
    }

    public static String getcolumnsLengthMap(String type, String length) throws IOException {
        boolean flag = length.matches("^[(]\\d{1,3}[,]\\d{1,2}[)]$");
        switch (type.toUpperCase()) {
            case "VARCHAR":
                return "VARCHAR" + "(" + length + ")";
            case "STRING":
                return "VARCHAR" + "(" + length + ")";
            case "CLOB":
                return "STRING";
            case "DATE":
                return "DATE";
            case "SMALLINT":
                return "SMALLINT";
            case "TINYINT":
                return "TINYINT";
            case "NUMERIC":
                return  "DECIMAL "+length;
            case "BIGDECIMAL":
                return caseCheck(flag,length);
            case "DECIMAL":
                return caseCheck(flag,length);
            default:
                return "VARCHAR(10)";
        }
    }
    public static String caseCheck(Boolean flag, String length) throws IOException {
        if(flag){
            return "DECIMAL" + length;
        }
        else if (StringUtils.isNumeric(length) && new Integer(length) > 0)
        {
            return "DECIMAL" + "("+length+",2)";
        }
        else {
            return "VARCHAR(10)";}
    }

}
