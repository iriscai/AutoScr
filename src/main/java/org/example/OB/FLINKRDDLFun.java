package org.example.OB;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2023.6.14
 * hetu_flink
 */
public class FLINKRDDLFun {
    static String dateVersion;

    static {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHss");
        dateVersion = formatter.format(date);
    }


    public void getDwiFlinks(String readFileName, String sheetNum, String jhOwner) throws IOException {
        DataSplit dataSplit = new DataSplit();
        YupiDataListener yupiDataListener = new YupiDataListener();
        CheckExcel checkExcel = new CheckExcel();
        checkExcel.checkFile(readFileName, sheetNum, "flink");
        Map<String, Integer> columnsPosiMap = checkExcel.getColumnsPosiMap(readFileName, sheetNum, "flink");
        List<Map<Integer, String>> dataList = yupiDataListener.doRead(readFileName, sheetNum);
        //tablePriKeyMap 为原始数据复合主键 类似dwi_mkt_tbsrnjpricecheck_tg:id&version&channel
        HashMap<String, StringBuffer> tablePriKeyMap = new HashMap<>();
        //tableCommentMap为表注释map 类似 dwi_mkt_tbsrnjpricecheck_tg：特钢日定价版本核准生效
        HashMap<String, String> tableCommentMap = new HashMap<>();
        //tableCommentMap为表注释map 类似 dwi_mkt_tbsrnjpricecheck_tg：RT_MES_BC_FLINK2HUDI_GP_PLATE
        HashMap<String, String> kafkaTopicsMap = new HashMap<>();
        //tableSourceDBNameMap为源系统 类似 dwi_mkt_tbsrnjpricecheck_tg：ERP
        HashMap<String, String> tableSourceDBNameMap = new HashMap<>();
        //tableL2NameMap 类似 dwi_mkt_tbsrnjpricecheck_tg：SAL
        HashMap<String, String> tableL2NameMap = new HashMap<>();
        //columnsCommentMap为字段注释map 类似 dwi_mkt_tbsrnjpricecheck_tg：<versionno:版本号,newgrade:新客户级别>
        HashMap<String, HashMap<String, String>> columnsCommentMap = new HashMap<>();
        for (Map<Integer, String> data : dataList) {
            //添加map数据
            kafkaTopicsMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("TOPIC_NAME")));
            tableSourceDBNameMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("来源")).toLowerCase());
            tableL2NameMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("L1资产编码")));
            tableCommentMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("逻辑实体-中文(L4)")));
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
        }
        dataList.clear();
        int i = 1;
        //添加表头
        HashMap<Integer, String> titleName = new HashMap<>();
        dataList.add(0, titleName);
        //去重
        for (HashMap.Entry<String, String> entry : tableCommentMap.entrySet()) {
            dataSplit.writeDwiKafkaToFlink(entry.getKey(), tableL2NameMap.get(entry.getKey()), kafkaTopicsMap.get(entry.getKey()),columnsCommentMap.get(entry.getKey()), tablePriKeyMap.get(entry.getKey()).toString(), jhOwner);
        }
    }

}
