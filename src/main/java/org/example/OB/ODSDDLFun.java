package org.example.OB;

import com.alibaba.excel.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ODSDDLFun {
    static String dateVersion;

    static {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHss");
        dateVersion = formatter.format(date);
    }

    public void getodsSpricts(String readFileName, String sheetNum,String jhOwner) throws IOException {
        DataSplit dataSplit = new DataSplit();
        YupiDataListener yupiDataListener = new YupiDataListener();
        CheckExcel checkExcel=new CheckExcel();
        Map<String, Integer> columnsPosiMap = checkExcel.getColumnsPosiMap(readFileName, sheetNum, "ods");
        List<Map<Integer, String>> dataList = yupiDataListener.doRead(readFileName, sheetNum);
        //odstableCommentMap为表注释map 类似 TBSONJEVALUATE：特钢日定价版本核准生效
        HashMap<String, String> odstableCommentMap = new HashMap<>();
        //odstableSourceDBNameMap为源系统 类似 TBSONJEVALUATE：ERP
        HashMap<String, String> odstableSourceDBNameMap = new HashMap<>();
        //odstableL2NameMap 类似 TBSONJEVALUATE：SAL
        HashMap<String, String> odstableL2NameMap = new HashMap<>();
        //columnsCommentMap为字段注释map 类似 TBSRNJPRICECHECK：<versionno:版本号,newgrade:新客户级别>
        HashMap<String, HashMap<String, String>> odscolumnsCommentMap = new HashMap<>();
        //odscolumnsLengthMap为字段长度map 类似 TBSRNJPRICECHECK：<versionno:VARCHAR(2),newgrade:DECIMAL(10,2)>
        HashMap<String, HashMap<String, String>> odscolumnsLengthMap = new HashMap<>();
        for (Map<Integer, String> data : dataList) {
            //添加map数据
            odstableSourceDBNameMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("来源")).toLowerCase());
            odstableL2NameMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("L1资产编码")));
            odstableCommentMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("源系统表名-中文")));
            //字段备注
            if (!odscolumnsCommentMap.containsKey(data.get(columnsPosiMap.get("源系统表名-英文")))) {
                HashMap<String, String> tableColumnsCommentsMap = new HashMap<>();
                tableColumnsCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")), data.get(columnsPosiMap.get("属性-中文(L5)")));
                odscolumnsCommentMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), tableColumnsCommentsMap);
            } else {
                HashMap<String, String> tableColumnsCommentsMap = odscolumnsCommentMap.get(data.get(columnsPosiMap.get("源系统表名-英文")));
                tableColumnsCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")), data.get(columnsPosiMap.get("属性-中文(L5)")));
                odscolumnsCommentMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), tableColumnsCommentsMap);
            }
            //字段数据长度
            if (!odscolumnsLengthMap.containsKey(data.get(columnsPosiMap.get("源系统表名-英文")))) {
                HashMap<String, String> tablecolumnsLengthCommentsMap = new HashMap<>();
                tablecolumnsLengthCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")), getcolumnsLengthMap(data.get(columnsPosiMap.get("字段类型")),data.get(columnsPosiMap.get("字段长度"))));
                odscolumnsLengthMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), tablecolumnsLengthCommentsMap);
            } else {
                HashMap<String, String> tablecolumnsLengthCommentsMap = odscolumnsLengthMap.get(data.get(columnsPosiMap.get("源系统表名-英文")));
                tablecolumnsLengthCommentsMap.put(data.get(columnsPosiMap.get("属性-英文(L5)")), getcolumnsLengthMap(data.get(columnsPosiMap.get("字段类型")),data.get(columnsPosiMap.get("字段长度"))));
                odscolumnsLengthMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), tablecolumnsLengthCommentsMap);
            }

        }


        dataList.clear();
        int i = 1;
        //添加表头
        HashMap<Integer, String> titleName = new HashMap<>();
        dataList.add(0, titleName);
        //去重
        for (HashMap.Entry<String, String> entry : odstableCommentMap.entrySet()) {
            dataSplit.writeodsDeleteDtETL(odstableL2NameMap.get(entry.getKey()), entry.getKey().toLowerCase(), odstableSourceDBNameMap.get(entry.getKey()), entry.getValue(),jhOwner);
            dataSplit.writeodsDDL(odstableL2NameMap.get(entry.getKey()), entry.getKey().toLowerCase(), odstableSourceDBNameMap.get(entry.getKey()), odscolumnsCommentMap.get(entry.getKey()), odscolumnsLengthMap.get(entry.getKey()), odstableCommentMap.get(entry.getKey()),jhOwner);
        }

    }

    public static String getcolumnsLengthMap(String type, String length) throws IOException {
        boolean flag = length.matches("^[(]\\d{1,3}[,]\\d{1,2}[)]$");
        boolean flag2 = length.matches("^\\d{1,3}[,]\\d{1,2}$");
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
                return caseCheck(flag,flag2,length);
            case "DECIMAL":
                return caseCheck(flag,flag2,length);
            default:
                return "VARCHAR(10)";
        }
    }


    public static String caseCheck(Boolean flag,Boolean flag2, String length) throws IOException {
        if(flag){
            return "DECIMAL" + length;
        } else if (flag2)
        {
            return "DECIMAL" + "("+length+")";
        }
        else if (StringUtils.isNumeric(length) && new Integer(length) > 0)
        {
            return "DECIMAL" + "("+length+",2)";
        }
        else {
            return "VARCHAR(10)";}
    }
}
