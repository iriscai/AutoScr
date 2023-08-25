package org.example.OB;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 调度集成
 */
public class JobJsonFun {
    static String dateVersion;
    static JSONObject jobDemoJson;

    static {
        JSONReadUn jsonReadUn = new JSONReadUn();
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHss");
        dateVersion = formatter.format(date);
        String jobDemofilePath = "src/main/resources/source/job_properties_demo.json";
        jobDemoJson = jsonReadUn.readJsonFile(jobDemofilePath);

    }

    /**
     * 生成入湖调度
     *
     * @param fileName
     * @param sheetNum
     * @param jhOwner
     * @throws IOException
     */
    public void getJsonFiles(String fileName, String sheetNum, String jhOwner) throws IOException {
        CheckExcel checkExcel = new CheckExcel();
        YupiDataListener yupiDataListener = new YupiDataListener();
        DataSplit dataSplit = new DataSplit();
        ScriptPathCon scriptPathCon = new ScriptPathCon();
        JSONArray dwrjsonArray = new JSONArray();
        Map<String, Integer> columnsPosiMap = checkExcel.getColumnsPosiMap(fileName, sheetNum, "job");
        List<Map<Integer, String>> dataList = yupiDataListener.doRead(fileName, sheetNum);


        //jobtitleMaps 为job对应模型job_财务域_全量_test01：dwi_fin_tbacc1
        HashMap<String, ArrayList<String>> jobtitleMaps = new HashMap<>();
        //dwiAndOdsMaps 为模型对应源表  dwi_fin_tbacc1：tbacc1
        HashMap<String, String> dwiAndOdsMaps = new HashMap<>();
        //dwiDBMaps 为模型对应库  dwi_fin_tbacc1：dwi_fin
        HashMap<String, String> dwiDBMaps = new HashMap<>();
        //odsDBMaps 为源表对应库  tbacc1：ods_erp
        HashMap<String, String> odsDBMaps = new HashMap<>();
        for (Map<Integer, String> data : dataList) {
            //添加map数据
            ArrayList<String> jobtitleMaps2 = new ArrayList<>();
            if (jobtitleMaps.containsKey(data.get(columnsPosiMap.get("调度名称")))) {
                jobtitleMaps2 = jobtitleMaps.get(data.get(columnsPosiMap.get("调度名称")));
            }
            jobtitleMaps2.add(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")));
            jobtitleMaps.put(data.get(columnsPosiMap.get("调度名称")), jobtitleMaps2);
            dwiAndOdsMaps.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("源系统表名-英文")));
            dwiDBMaps.put(data.get(columnsPosiMap.get("逻辑实体-英文(L4)")), data.get(columnsPosiMap.get("模型-库名")));
            odsDBMaps.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("源表-库名")));
            //为原始数据字段串 类似a&b&c&d&e
        }
        for (HashMap.Entry<String, ArrayList<String>> entry : jobtitleMaps.entrySet()) {
            ArrayList<Object> nodes = new ArrayList();
            nodes.add(jobDemoJson.get("Dummy_node_demo"));
            for (int i = 0; i < entry.getValue().size(); i++) {
                String dwiTableName = entry.getValue().get(i);
                String dwiEtlName = "etl_" + dwiTableName;
                String odsCdmPre = "开始";
                String odsCdmDB = odsDBMaps.get(dwiAndOdsMaps.get(dwiTableName));
                String odsCdmName = odsCdmDB + "_" + dwiAndOdsMaps.get(dwiTableName) + "_f";
                String odsEtlName = "etl_" + odsCdmName;
                String dwiEtlDB = dwiDBMaps.get(dwiTableName);
                JSONObject dwiETLJson = new JSONObject();
                dwiETLJson = JSONObject.parseObject(dataSplit.writejobnodes(String.valueOf(i * 100), dwiEtlName, odsEtlName, dwiEtlDB, "dwiETL"));
                nodes.add(dwiETLJson);
                JSONObject odsETLJson = new JSONObject();
                odsETLJson = JSONObject.parseObject(dataSplit.writejobnodes(String.valueOf(i * 100), odsEtlName, odsCdmName, odsCdmDB, "odsETL"));
                nodes.add(odsETLJson);
                JSONObject cdmobJson = new JSONObject();
                cdmobJson = JSONObject.parseObject(dataSplit.writejobnodes(String.valueOf(i * 100), odsCdmName, odsCdmPre, odsCdmDB, "CDMJob"));
                nodes.add(cdmobJson);
            }

            /**
             * 写出CDM->DWRJSON
             */
            String jobWriteFilePath = "src/main/resources/AutoToLake/out/JOB/";
            String jobWriteFileName = jobWriteFilePath + "JOB_JSON_" + entry.getKey() + "_" + dateVersion + ".job";
            try {
                scriptPathCon.checkFilePathIfExists(jobWriteFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedWriter dwrbw = new BufferedWriter(new FileWriter(jobWriteFileName));
            JSONObject jobJson = jobDemoJson.getJSONObject("details");
            jobJson.put("nodes", nodes);
            jobJson.put("directory", "/NGDG/ETL/JH_FIN/按天全量");
            jobJson.put("name", entry.getKey());
            dwrbw.write(jobJson.toJSONString());
            dwrbw.flush();
            dwrbw.close();
        }
    }

    /**
     * 仅生成初始化ODS_DDL调度任务
     *
     * @param fileName
     * @param sheetNum
     * @param jhOwner
     * @throws IOException
     */
    public void getJustOdsDDLJob(String fileName, String sheetNum, String jhOwner) throws IOException {

        CheckExcel checkExcel = new CheckExcel();
        YupiDataListener yupiDataListener = new YupiDataListener();
        DataSplit dataSplit = new DataSplit();
        ScriptPathCon scriptPathCon = new ScriptPathCon();
        Map<String, Integer> columnsPosiMap = checkExcel.getColumnsPosiMap(fileName, sheetNum, "job_ddl");
        List<Map<Integer, String>> dataList = yupiDataListener.doRead(fileName, sheetNum);
        //jobtitleMaps 为job对应tableName   job_ods_create_DDL_test01：tbadtcday
        HashMap<String, ArrayList<String>> jobtitleMaps = new HashMap<>();
        //dwiAndOdsMaps 为模型对应源表  tbadtcday：ERP
        HashMap<String, String> dbFrom = new HashMap<>();
        for (Map<Integer, String> data : dataList) {
            //添加map数据
            dbFrom.put(data.get(columnsPosiMap.get("源系统表名-英文")), data.get(columnsPosiMap.get("来源")));
            ArrayList<String> jobtitleMaps2 = new ArrayList<>();
            if (jobtitleMaps.containsKey(data.get(columnsPosiMap.get("调度名称")))) {
                jobtitleMaps2 = jobtitleMaps.get(data.get(columnsPosiMap.get("调度名称")));
            }
            jobtitleMaps2.add(data.get(columnsPosiMap.get("源系统表名-英文")));
            jobtitleMaps.put(data.get(columnsPosiMap.get("调度名称")), jobtitleMaps2);
        }
        for (HashMap.Entry<String, ArrayList<String>> entry : jobtitleMaps.entrySet()) {
            ArrayList<Object> nodes = new ArrayList();
            nodes.add(jobDemoJson.get("Dummy_node_demo"));
            for (int i = 0; i < entry.getValue().size(); i++) {
                String tableName = entry.getValue().get(i);
                String odsCdmDB = dbFrom.get(tableName).toLowerCase();
                String scriptName = "hive_" + odsCdmDB + "_" + tableName + "_f";
                JSONObject ddlNodeJson = new JSONObject();
                ddlNodeJson = JSONObject.parseObject(dataSplit.writejobDDLnodes(String.valueOf(i * 100), scriptName, odsCdmDB));
                nodes.add(ddlNodeJson);
            }
            String jobWriteFilePath = "src/main/resources/AutoToLake/out/JOB/";
            String jobWriteFileName = jobWriteFilePath + "JOB_ODSDDL_" + entry.getKey() + "_" + dateVersion + ".job";
            try {
                scriptPathCon.checkFilePathIfExists(jobWriteFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedWriter dwrbw = new BufferedWriter(new FileWriter(jobWriteFileName));
            JSONObject jobJson = jobDemoJson.getJSONObject("ddl_details");
            jobJson.put("nodes", nodes);
            jobJson.put("directory", "/NGDG/ETL/JH_FIN/按天全量");
            jobJson.put("name", entry.getKey());
            dwrbw.write(jobJson.toJSONString());
            dwrbw.flush();
            dwrbw.close();
        }
    }


}
