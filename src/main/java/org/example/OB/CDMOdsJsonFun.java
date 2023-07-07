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
public class CDMOdsJsonFun {
    static String dateVersion;
    static {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHss");
        dateVersion = formatter.format(date);
    }

    public void getJsonFiles(String fileName, String sheetNum) throws IOException {
        CheckExcel checkExcel=new CheckExcel();
        YupiDataListener yupiDataListener=new YupiDataListener();
        DataSplit dataSplit=new DataSplit();
        ScriptPathCon scriptPathCon=new ScriptPathCon();
        JSONArray odsjsonArray=new JSONArray();
        Map<String, Integer>  columnsPosiMap = checkExcel.getColumnsPosiMap(fileName, sheetNum, "ods");
        List<Map<Integer, String>> dataList = yupiDataListener.doRead(fileName, sheetNum);
        //sourceColumnsMap 为原始数据字段串 类似A&B&C&D&E
        HashMap<String, StringBuffer> sourceColumnsMap = new HashMap<>();
        //odstableCommentMap为表注释map 类似 TBSONJEVALUATE：特钢日定价版本核准生效
        HashMap<String, String> odstableCommentMap = new HashMap<>();
        //odstableSourceDBNameMap为源系统 类似 TBSONJEVALUATE：ERP
        HashMap<String, String> odstableSourceDBNameMap = new HashMap<>();
        //odstableL2NameMap为领域 类似 TBSRNJPRICECHECK：MKT
        HashMap<String, String> odstableL2NameMap = new HashMap<>();
        //odstablecdmfromlink 类似 TBSRNJPRICECHECK：ERP_ZAIBEI_NEW
        HashMap<String, String> odstablecdmfromlink = new HashMap<>();
        //odstablecdmfromlinkschema 类似 TBSRNJPRICECHECK：DB
        HashMap<String, String> odstablecdmfromlinkschema = new HashMap<>();
        for (Map<Integer, String> data : dataList) {
            //添加map数据
            odstableL2NameMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("L1资产编码")));
            odstablecdmfromlink.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("CDM-ODS连接器")));
            odstablecdmfromlinkschema.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("CDM-ODS空间")));
            odstableSourceDBNameMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("来源")));
            odstableCommentMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("源系统表名-中文")));
            //为原始数据字段串 类似A&B&C&D&E
            if (!sourceColumnsMap.containsKey(data.get(columnsPosiMap.get("源系统表名-英文")))) {
                StringBuffer sourceColumnsBuffer = new StringBuffer();
                sourceColumnsBuffer.append(data.get(columnsPosiMap.get("属性-英文(L5)")));
                sourceColumnsMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), sourceColumnsBuffer);
            } else {
                StringBuffer sourceColumnsBuffer = sourceColumnsMap.get(data.get(columnsPosiMap.get("源系统表名-英文")));
                sourceColumnsBuffer.append("&" + data.get(columnsPosiMap.get("属性-英文(L5)")));
                sourceColumnsMap.put(data.get(columnsPosiMap.get("源系统表名-英文")), sourceColumnsBuffer);
            }
        }
        for (HashMap.Entry<String, String> entry : odstableCommentMap.entrySet()) {
            JSONObject cdmOdsJson2 = new JSONObject();
            cdmOdsJson2 = JSONObject.parseObject(dataSplit.writeCDMOdsjson(entry.getKey(), odstableSourceDBNameMap.get(entry.getKey()), sourceColumnsMap, odstableL2NameMap.get(entry.getKey()),odstablecdmfromlink.get(entry.getKey()),odstablecdmfromlinkschema.get(entry.getKey())));
            odsjsonArray.add(cdmOdsJson2);
        }

        /**
         * 写出CDM->ODSJSON
         */
        String cdmWriteFilePath = "src/main/resources/AutoToLake/out/CDM/";
        String cdmOdsWriteFileName = cdmWriteFilePath + "cdm_ods_" + dateVersion + ".json";
        try {
            scriptPathCon.checkFilePathIfExists(cdmWriteFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter odsbw = new BufferedWriter(new FileWriter(cdmOdsWriteFileName));
        JSONObject cdmodsJson = new JSONObject();
        cdmodsJson.put("jobs", odsjsonArray);
        odsbw.write(cdmodsJson.toJSONString());
        odsbw.flush();
        odsbw.close();
    }



}
