package org.example.OB;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自动化生成 CDM_Ods_json 文件
 */
public class CDMDwrJsonFun {
    static String dateVersion;

    static {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHss");
        dateVersion = formatter.format(date);
    }

    public void getJsonFiles(String fileName, String sheetNum) throws IOException {
        CheckExcel checkExcel = new CheckExcel();
        YupiDataListener yupiDataListener = new YupiDataListener();
        DataSplit dataSplit = new DataSplit();
        ScriptPathCon scriptPathCon = new ScriptPathCon();
        JSONArray dwrjsonArray = new JSONArray();
        Map<String, Integer> columnsPosiMap = checkExcel.getColumnsPosiMap(fileName, sheetNum, "dwi");
        List<Map<Integer, String>> dataList = yupiDataListener.doRead(fileName, sheetNum);
        //sourceColumnsMap 为原始数据字段串 类似a&b&c&d&e
        HashMap<String, StringBuffer> sourceColumnsMap = new HashMap<>();
        //tableCommentMap为表注释map 类似 dwi_sal_tbsonj_order_main_bc：板材订单主文件
        HashMap<String, String> tableCommentMap = new HashMap<>();
        //tableSourceDBNameMap为源系统 类似 dwi_sal_tbsonj_order_main_bc：ERP
        HashMap<String, String> tableSourceDBNameMap = new HashMap<>();
        //tableL2NameMap为领域 类似 dwi_sal_tbsonj_order_main_bc：MKT
        HashMap<String, String> tableL2NameMap = new HashMap<>();
        //sourcePriKeyMap 为原始数据复合主键 类似id&version&channel
        HashMap<String, StringBuffer> sourcePriKeyMap = new HashMap<>();
        //columnsCommentMap 类似 dwi_sal_tbsonj_order_main_bc：<versionno:版本号,newgrade:新客户级别>
        HashMap<String, HashMap<String, String>> columnsCommentMap = new HashMap<>();
        //columnsLengthMap 类似 dwi_sal_tbsonj_order_main_bc：<versionno:2,newgrade:100>
        HashMap<String, HashMap<String, String>> columnsLengthMap = new HashMap<>();

        for (Map<Integer, String> data : dataList) {
            //添加map数据
            tableL2NameMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("L1资产编码")));
            tableSourceDBNameMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("来源")));
            tableCommentMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("逻辑实体-中文(L4)")));
            //为原始数据字段串 类似a&b&c&d&e
            if (!sourceColumnsMap.containsKey(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")))) {
                StringBuffer sourceColumnsBuffer = new StringBuffer();
                sourceColumnsBuffer.append(data.get(columnsPosiMap.get("属性-英文(L5)")));
                sourceColumnsMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), sourceColumnsBuffer);
            } else {
                StringBuffer sourceColumnsBuffer = sourceColumnsMap.get(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")));
                sourceColumnsBuffer.append("&" + data.get(columnsPosiMap.get("属性-英文(L5)")));
                sourceColumnsMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), sourceColumnsBuffer);
            }
            //主键
            if (!sourcePriKeyMap.containsKey(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")))) {
                if ((data.get(columnsPosiMap.get("是否为主键")) != null) && (data.get(columnsPosiMap.get("是否为主键")).trim().equals("Y") || data.get(columnsPosiMap.get("是否为主键")).trim().equals("是") || data.get(columnsPosiMap.get("是否为主键")).trim().equals("1"))) {
                    StringBuffer priKeytringBuffer = new StringBuffer();
                    priKeytringBuffer.append(data.get(columnsPosiMap.get("属性-英文(L5)")));
                    sourcePriKeyMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), priKeytringBuffer);
                } else {
                }
            } else {
                StringBuffer priKeytringBuffer = sourcePriKeyMap.get(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")));
                if ((data.get(columnsPosiMap.get("是否为主键")) != null) && (data.get(columnsPosiMap.get("是否为主键")).trim().equals("Y") || data.get(columnsPosiMap.get("是否为主键")).trim().equals("是")|| data.get(columnsPosiMap.get("是否为主键")).trim().equals("1"))) {
                    priKeytringBuffer.append("&" + data.get(columnsPosiMap.get("属性-英文(L5)")));
                }
                sourcePriKeyMap.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), priKeytringBuffer);
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
        for (HashMap.Entry<String, String> entry : tableCommentMap.entrySet()) {
            JSONObject cdmdwrJson2 = new JSONObject();
            cdmdwrJson2 = JSONObject.parseObject(dataSplit.writeCDMDwrjson(entry.getKey(), sourceColumnsMap.get(entry.getKey()).toString(), tableL2NameMap.get(entry.getKey())));
            dwrjsonArray.add(cdmdwrJson2);
        }

        /**
         * 写出CDM->DWRJSON
         */
        String cdmWriteFilePath = "src/main/resources/AutoToLake/out/CDM/";
        String cdmDwrWriteFileName = cdmWriteFilePath + "cdm_dwr_" + dateVersion + ".json";
        try {
            scriptPathCon.checkFilePathIfExists(cdmWriteFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter dwrbw = new BufferedWriter(new FileWriter(cdmDwrWriteFileName));
        JSONObject cdmdwrJson = new JSONObject();
        cdmdwrJson.put("jobs", dwrjsonArray);
        dwrbw.write(cdmdwrJson.toJSONString());
        dwrbw.flush();
        dwrbw.close();
    }

}
