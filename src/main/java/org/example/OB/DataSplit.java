package org.example.OB;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 生成 OdsDDLScriptPath  ODS 建表语句脚本
 */
public class DataSplit {
    static JSONObject scriptdesc;
    static JSONObject kafkaSource;
    static JSONObject flinkSource;
    static String scripttitletips;
    static String flinkJobTips;
    static String scriptOwnerTips;
    static JSONObject scriptexcelcolumns;
    static JSONObject datatypejson;
    static JSONObject excelMustColumnsJson;
    static JSONObject cdmOdsDemoJson;
    static JSONObject jobDemoJson;
//    static JSONObject nodeDemoJson;
    static JSONObject cdmDwrDemoJson;
    static JSONObject cdmProperties;                         //cdm_group_properties.json
    static HashMap<String, String> cdmlinkdbsMap;          //ERP_ZAIBEI_NEW:DB
    static HashMap<String, String> cdmlinkgroupsMap;       //erp:ERP_ZAIBEI_NEW
    static HashMap<String, String> cdmgroups;              //SAL:36
    static HashMap<String, String> cdmextendedconfigs;              //"eyLCYXRjaEpvYiI6ImZhbHNlIn0="
    static HashMap<String, String> cdmextetldtfunMap;      //MYSQL:${dateformat(yyyy-MM-dd HH:mm:ss)}
    static HashMap<String, String> cdmdtfunMap;            //MYSQL:DATE_SUB(current_date,INTERVAL 1 DAY)
    static HashMap<String, String> cdmlinkdbtypegroupsMap; //ERP_ZAIBEI_NEW:MYSQL

    static {
        JSONReadUn jsonReadUn = new JSONReadUn();
        String cdmOdsDemofilePath = "src/main/resources/source/cdm_ods_demo.json";
        String cdmDwrDemofilePath = "src/main/resources/source/cdm_dwr_demo.json";
        String jobDemofilePath = "src/main/resources/source/job_properties_demo.json";
        cdmOdsDemoJson = jsonReadUn.readJsonFile(cdmOdsDemofilePath);
        jobDemoJson = jsonReadUn.readJsonFile(jobDemofilePath);
        cdmDwrDemoJson = jsonReadUn.readJsonFile(cdmDwrDemofilePath);
        String scriptexcelcolumnsfilePath = "src/main/resources/source/script_desc.json";
        String datatypefilePath = "src/main/resources/source/datatype.json";
        excelMustColumnsJson = jsonReadUn.readJsonFile("src/main/resources/source/excel_columns.json");
        scriptexcelcolumns = jsonReadUn.readJsonFile(scriptexcelcolumnsfilePath);
        datatypejson = jsonReadUn.readJsonFile(datatypefilePath);
        String cdmgroupsfilePath = "src/main/resources/source/cdm_group_properties.json";
        cdmProperties = jsonReadUn.readJsonFile(cdmgroupsfilePath);
        String scriptPath = "src/main/resources/source/script_desc.json";
        scriptdesc = jsonReadUn.readJsonFile(scriptPath).getJSONObject("scriptDesc");
        kafkaSource = jsonReadUn.readJsonFile(scriptPath).getJSONObject("kafkaSource");
        flinkSource = jsonReadUn.readJsonFile(scriptPath).getJSONObject("flinkSource");
        scripttitletips = jsonReadUn.readJsonFile(scriptPath).getString("scripttitletips");
        flinkJobTips = jsonReadUn.readJsonFile(scriptPath).getString("flinkJobTips");
        scriptOwnerTips = jsonReadUn.readJsonFile(scriptPath).getString("scriptOwnerTips");
        HashMap<String, HashMap<String, String>> propetriesMap = new HashMap<>();
        for (Iterator iterator = cdmProperties.keySet().iterator(); iterator.hasNext(); ) {
            String diagramKey = (String) iterator.next();
            HashMap<String, String> groupsMap = new HashMap<String, String>();
            cdmProperties.getJSONArray(diagramKey).forEach(i -> {
                JSONObject jsonObjectname = JSONObject.parseObject(i.toString());
                groupsMap.put(jsonObjectname.getString("name"), jsonObjectname.getString("value"));
            });
            propetriesMap.put(diagramKey, groupsMap);
        }
        cdmlinkdbtypegroupsMap = propetriesMap.get("cdm_linkdbtypegroups");
        cdmlinkdbsMap = propetriesMap.get("cdm_linkdbs");
        cdmlinkgroupsMap = propetriesMap.get("cdm_linkgroups");
        cdmgroups = propetriesMap.get("cdm_groups");
        cdmextendedconfigs = propetriesMap.get("cdm_extended-configs");
        cdmextetldtfunMap = propetriesMap.get("cdm_extetldtfun");
        cdmdtfunMap = propetriesMap.get("cdm_dtfun");
    }

    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String currentDate = formatter.format(date);
    ScriptPathCon scriptPathCon = new ScriptPathCon();


    /**
     * 解析生成ODS_DDL.script   全量_F
     *
     * @param L2             领域：MKT
     * @param tablename      表名  tbscnjclientrequest
     * @param system         系统：erp
     * @param columnsComents 字段备注mapping tbscnjclientrequest：<versionno:版本号,newgrade:新客户级别>
     * @param columnsLengths 字段长度mapping  tbscnjclientrequest：<versionno:2,newgrade:100>
     * @param tablecomment   表注释  特钢日定价版本核准生效
     * @throws IOException
     */
    public void writeodsDDL(String L2, String tablename, String system, Map<String, String> columnsComents, Map<String, String> columnsLengths, String tablecomment, String jhOwner) throws IOException {
        String OdsDDLScriptName = scriptPathCon.getOdsDDLScriptName(L2, tablename, system);
        String OdsDDLScriptPath = scriptPathCon.getOdsDDLScriptPath(L2);
        try {
            scriptPathCon.checkFilePathIfExists(OdsDDLScriptPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(OdsDDLScriptName));
        JSONObject jsonObject = scriptdesc;
        jsonObject.put("connectionName", "MRS_Spark_Agent");
        jsonObject.put("type", "SparkSQL");
        String currentDatabase = "ods_" + system;
        String directory = OdsDDLScriptPath.replace("src/main/resources/AutoToLake/out", "");
        String scriptame = "hive_ods_" + system + "_" + tablename + "_f";
        String tableame = "ods_" + system + "_" + tablename + "_f";

        String tips = scripttitletips;
        tips = tips.replace("tableName", tableame)
                .replace("jh_name", jhOwner)
                .replace("tableComment", tablecomment)
                .replace("currentDate", currentDate)
                .replace("checkfirst", scriptOwnerTips);
        String createFirst = "CREATE TABLE IF NOT EXISTS " + currentDatabase + "." + tableame + "( \n";
        String createend = "   ext_etl_dt timestamp COMMENT 'ETL时间',\n" +
                "   ext_src_sys_id varchar(20) COMMENT '源系统'\n" +
                ") COMMENT '"
                + tablecomment + "'\n" +
                "partitioned by(dt varchar(10) COMMENT '时间分区') \n" +
                "stored as orc tblproperties(\"orc.compress\" = \"SNAPPY\");";
        StringBuffer columns = new StringBuffer();
        for (HashMap.Entry<String, String> entry : columnsComents.entrySet()) {
            columns.append("   " + entry.getKey() + "   " + columnsLengths.get(entry.getKey()) + " COMMENT '" + columnsComents.get(entry.getKey()) + "',\n");
        }
        jsonObject.put("content", tips + createFirst + columns.toString() + createend);
        jsonObject.put("currentDatabase", currentDatabase);
        jsonObject.put("directory", directory);
        jsonObject.put("name", scriptame);
        bw.write(jsonObject.toJSONString());
        bw.flush();
        bw.close();
        System.out.println("脚本： " + scriptame + "    写入成功！");
    }


    /**
     * 解析生成  ODS_ETL.script   删除7天前分区脚本
     */
    public void writeodsDeleteDtETL(String L2, String tablename, String system, String tablecomment, String jhOwner) throws IOException {
        String odsDeleteDtETLScriptName = scriptPathCon.getOdsDeleteDtETLScriptName(L2, tablename, system);
        String odsDeleteDtETLScriptPath = scriptPathCon.getOdsDeleteDtETLScriptPath(L2);
        try {
            scriptPathCon.checkFilePathIfExists(odsDeleteDtETLScriptPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(odsDeleteDtETLScriptName));
        JSONObject jsonObject = scriptdesc;
        jsonObject.put("connectionName", "MRS_Spark_Agent");
        jsonObject.put("type", "SparkSQL");
        String currentDatabase = "ods_" + system;
        String directory = odsDeleteDtETLScriptPath.replace("src/main/resources/AutoToLake/out", "");
        String scriptame = "etl_ods_" + system + "_" + tablename + "_f";
        String tableame = "ods_" + system + "_" + tablename + "_f";

        String tips = scripttitletips;
        tips = tips.replace("tableName", tableame)
                .replace("jh_name", jhOwner)
                .replace("tableComment", tablecomment)
                .replace("currentDate", currentDate)
                .replace("checkfirst", scriptOwnerTips);
        String content = "alter table " + currentDatabase + "." + tableame + " drop if exists partition (dt = '${ETL_TODAY_MINUS8}');";
        jsonObject.put("content", tips + content);
        jsonObject.put("currentDatabase", currentDatabase);
        jsonObject.put("directory", directory);
        jsonObject.put("name", scriptame);
        bw.write(jsonObject.toJSONString());
        bw.flush();
        bw.close();
        System.out.println("脚本： " + scriptame + "    写入成功！");
    }

    /**
     * 解析生成  DWI_INSERT_ETL脚本   抽取ＯＤＳ 数据覆盖DWI表
     */
    public void writeInsertDwiETL(String dwiTableName, String L2, String sourceTableName, String system, Map<String, String> columnsComents, String tablecomment, String jhOwner) throws IOException {
        String dwiETLScriptName = scriptPathCon.getDwiETLScriptName(L2, dwiTableName);
        String dwiETLScriptPath = scriptPathCon.getDwiETLScriptPath(L2);
        try {
            scriptPathCon.checkFilePathIfExists(dwiETLScriptPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(dwiETLScriptName));
        JSONObject jsonObject = scriptdesc;
        jsonObject.put("connectionName", "MRS_Spark_Agent");
        jsonObject.put("type", "SparkSQL");
        String currentDatabase = "dwi_" + L2.toLowerCase();
        String directory = dwiETLScriptPath.replace("src/main/resources/AutoToLake/out", "");
        String scriptame = "etl_" + dwiTableName;
        String odstableName = "ods_" + system + "." + "ods_" + system + "_" + sourceTableName.toLowerCase() + "_f";

        String tips = scripttitletips;
        tips = tips.replace("tableName", dwiTableName)
                .replace("jh_name", jhOwner)
                .replace("tableComment", tablecomment)
                .replace("currentDate", currentDate)
                .replace("checkfirst", scriptOwnerTips);


        String createend = "WHERE\n" +
                "    dt = '${ETL_TODAY_MINUS1}'";
        StringBuffer columns = new StringBuffer();
        for (HashMap.Entry<String, String> entry : columnsComents.entrySet()) {
            columns.append("   " + entry.getKey() + ",\n");
        }
        String columnsin = "(" + columns + "ext_etl_dt,\n ext_src_sys_id) \n";
        String createFirst = "INSERT\n" +
                "    OVERWRITE TABLE " + currentDatabase + "." + dwiTableName + " \n" + columnsin
                + "SELECT \n";
        String columnsto = columns + ("from_unixtime(to_unix_timestamp(now())) as ext_etl_dt,\n" +
                "    ext_src_sys_id\n" +
                "FROM    " + odstableName + "\n");
        jsonObject.put("content", tips + createFirst + columnsto + createend);
        jsonObject.put("currentDatabase", currentDatabase);
        jsonObject.put("directory", directory);
        jsonObject.put("name", scriptame);
        bw.write(jsonObject.toJSONString());
        bw.flush();
        bw.close();
        System.out.println("脚本： " + scriptame + "    写入成功！");
    }


    /**
     * 解析生成  DWRJCFDDL脚本   DWR　ｊｃ＿ｆ表建表语句
     */
    public void writeDwrJcFDDLWithPriKeys(String dwiTableName, String L2, Map<String, String> columnsComents, String tablecomment, String primaryKeys, HashMap<String, HashMap<String, String>> columnsIfNullMap, String jhOwner) throws IOException {
        String dwrDDLScriptName = scriptPathCon.getDwrDDLScriptName(dwiTableName, L2);
        String dwiETLScriptPath = scriptPathCon.getDwrDDLScriptPath(L2);
        try {
            scriptPathCon.checkFilePathIfExists(dwiETLScriptPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(dwrDDLScriptName));
        JSONObject jsonObject = scriptdesc;
        jsonObject.put("connectionName", "DWS_NGDG");
        jsonObject.put("type", "DWSSQL");
        String currentDatabase = "dwr_" + L2.toLowerCase();
        String directory = dwiETLScriptPath.replace("src/main/resources/AutoToLake/out", "");
        String tableName = dwiTableName.replace("dwi", "dwr") + "_jc_f";
        String scriptame = "dws_" + tableName;

        String tips = scripttitletips;
        tips = tips.replace("tableName", tableName)
                .replace("jh_name", jhOwner)
                .replace("tableComment", tablecomment)
                .replace("currentDate", currentDate)
                .replace("checkfirst", scriptOwnerTips);
        String createFirst = "CREATE TABLE IF NOT EXISTS " + currentDatabase + "." + tableName + "( \n";
        StringBuffer columns = new StringBuffer();
        StringBuffer columnsComments = new StringBuffer();
        for (HashMap.Entry<String, String> entry : columnsComents.entrySet()) {
            columns.append("   " + entry.getKey() + " VARCHAR" + columnsIfNullMap.get(dwiTableName).get(entry.getKey()) + ",\n");
            columnsComments.append("COMMENT ON COLUMN ")
                    .append(currentDatabase + "." + tableName + "." + entry.getKey() + " IS '" + entry.getValue() + "';\n");
        }
        columns.append("    ext_etl_dt TIMESTAMP,\n" +
                "    ext_src_sys_id VARCHAR,\n");
        String newprimaryKeys = primaryKeys.replace("&", ",");
        String createPrimaryKeys = "    primary key (" + newprimaryKeys + ")\n";
        String distribute = ") WITH (ORIENTATION = COLUMN) distribute by hash(" + newprimaryKeys + ");\n";
        String tableComments = "COMMENT ON TABLE " + currentDatabase + "." + tableName + " IS '" + tablecomment + "';\n";
        jsonObject.put("content", tips + createFirst + columns + createPrimaryKeys + distribute + tableComments + columnsComments);
        jsonObject.put("directory", directory);
        jsonObject.put("name", scriptame);
        jsonObject.put("currentDatabase", "ngdg");
        bw.write(jsonObject.toJSONString());
        bw.flush();
        bw.close();
        System.out.println("脚本： " + scriptame + "    写入成功！");
    }

    public void writeDwrJcFDDLWithOutPriKey(String dwiTableName, String L2, Map<String, String> columnsComents, String tablecomment, HashMap<String, HashMap<String, String>> columnsIfNullMap, String jhOwner) throws IOException {
        String dwrDDLScriptName = scriptPathCon.getDwrDDLScriptName(dwiTableName, L2);
        String dwiETLScriptPath = scriptPathCon.getDwrDDLScriptPath(L2);
        try {
            scriptPathCon.checkFilePathIfExists(dwiETLScriptPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(dwrDDLScriptName));
        JSONObject jsonObject = scriptdesc;
        jsonObject.put("connectionName", "DWS_NGDG");
        jsonObject.put("type", "DWSSQL");
        String currentDatabase = "dwr_" + L2.toLowerCase();
        String directory = dwiETLScriptPath.replace("src/main/resources/AutoToLake/out", "");
        String tableName = dwiTableName.replace("dwi", "dwr") + "_jc_f";
        String scriptame = "dws_" + tableName;

        String tips = scripttitletips;
        tips = tips.replace("tableName", tableName)
                .replace("jh_name", jhOwner)
                .replace("tableComment", tablecomment)
                .replace("currentDate", currentDate)
                .replace("checkfirst", scriptOwnerTips);
        String createFirst = "CREATE TABLE IF NOT EXISTS " + currentDatabase + "." + tableName + "( \n";
        StringBuffer columns = new StringBuffer();
        StringBuffer columnsComments = new StringBuffer();
        for (HashMap.Entry<String, String> entry : columnsComents.entrySet()) {
            columns.append("   " + entry.getKey() + " VARCHAR" + columnsIfNullMap.get(dwiTableName).get(entry.getKey()) + ",\n");
            columnsComments.append("COMMENT ON COLUMN ")
                    .append(currentDatabase + "." + tableName + "." + entry.getKey() + " IS '" + entry.getValue() + "';\n");
        }
        columns.append("    ext_etl_dt TIMESTAMP,\n" +
                "    ext_src_sys_id VARCHAR\n");
        String distribute = ") WITH (ORIENTATION = COLUMN);\n";
        String tableComments = "COMMENT ON TABLE " + currentDatabase + "." + tableName + " IS '" + tablecomment + "';\n";
        jsonObject.put("content", tips + createFirst + columns + distribute + tableComments + columnsComments);
        jsonObject.put("directory", directory);
        jsonObject.put("name", scriptame);
        jsonObject.put("currentDatabase", "ngdg");
        bw.write(jsonObject.toJSONString());
        bw.flush();
        bw.close();
        System.out.println("脚本： " + scriptame + "    写入成功！");
    }


    /**
     * @param dwiTableName
     * @param L2
     * @param columnsComents
     * @param jhOwner
     * @throws IOException
     */
    public void writeDwiKafkaToFlink(String dwiTableName, String L2, String kafkaTopicName, Map<String, String> columnsComents, String tablePriKeys, String jhOwner) throws IOException {
        String dwiFlinkDDLScriptName = scriptPathCon.getflinkDDLScriptPath(dwiTableName);
        String dwiFlinkScriptPath = scriptPathCon.getFlinkDDLScriptPath();
        try {
            scriptPathCon.checkFilePathIfExists(dwiFlinkScriptPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = new BufferedWriter(new FileWriter(dwiFlinkDDLScriptName));
        String currentDatabase = "dwi_" + L2.toLowerCase();
        String kafkaTableName = "kafka_" + dwiTableName;
        String createkafkaFirst = "CREATE TABLE IF NOT EXISTS " + kafkaTableName + "( \n";
        String createflinkFirst = "CREATE TABLE IF NOT EXISTS " + dwiTableName + "( \n";
        String insertflink = "insert into \n";
        StringBuffer kafkaColumns = new StringBuffer();
        StringBuffer selectColumns = new StringBuffer();
        for (HashMap.Entry<String, String> entry : columnsComents.entrySet()) {
            //kafka流表字段/ /Hudi表字段
            kafkaColumns.append("\n" + entry.getKey() + " STRING COMMENT '" + entry.getValue() + "',");
            //insert字段
            selectColumns.append("\n" + entry.getKey() + ",");
        }
        kafkaColumns.deleteCharAt(kafkaColumns.length() - 1);
        selectColumns.deleteCharAt(selectColumns.length() - 1);
        String kafkaComment = "\n ) WITH (\n" +
                "    'connector' = 'kafka',\n" +
                "    'topic' = 'kafka_topic_name',\n" +
                "    'properties.bootstrap.servers' = 'kafka_servers',\n" +
                "    'properties.group.id' = 'kafka_group_id',\n" +
                "    'scan.startup.mode' = 'kafka_scan_mode',\n" +
                "    'format' = 'debezium-json'\n" +
                ");\n";
        String flinkSource = "\n) WITH (\n" +
                "'connector' = 'hudi',\n" +
                "    'path' = 'hdfs://hacluster/user/hive/warehouse/db_db/dwi_table_name',\n" +
                "    'table.type' = 'MERGE_ON_READ',\n" +
                "    'hoodie.datasource.write.recordkey.field' = 'primaryKeys'\n" +
                ");\n";

        System.out.printf("", kafkaSource.toJSONString());
        kafkaComment = kafkaComment.replace("kafka_servers", kafkaSource.getString("properties.bootstrap.servers"))
                .replace("kafka_group_id", kafkaSource.getString("properties.group.id"))
                .replace("kafka_scan_mode", kafkaSource.getString("scan.startup.mode"))
                .replace("kafka_topic_name", kafkaTopicName);
        flinkSource = flinkSource.replace("db_db", currentDatabase + ".db")
                .replace("dwi_table_name", dwiTableName)
                .replace("primaryKeys", tablePriKeys.replace("&", ","));
        String dwiFlinkJobsDec = flinkJobTips.replace("jh_name", jhOwner)
                .replace("currentDate", currentDate);
        String dwiFlinkJobs = dwiFlinkJobsDec + createkafkaFirst + kafkaColumns + kafkaComment + createflinkFirst + kafkaColumns + flinkSource + insertflink + dwiTableName + " \nselect \n" + selectColumns + "\n" + "from \n" + kafkaTableName + ";\n";
        bw.write(dwiFlinkJobs);
        bw.flush();
        bw.close();
        System.out.println("kafla_flink_job 脚本： " + dwiTableName + "    写入成功！");
    }


    /**
     * 写出CDM->DWRJSON
     */
    public String writeCDMDwrjson(String tableName, String sourceColumns, String L2) throws IOException {
        Random r = new Random();
        String dwiTableName = tableName;
        String dwrTableName = dwiTableName.replace("dwi", "dwr") + "_jc_f";
        String cdm_dwi_dwr_json_name = dwrTableName;
        String toJobConfig_schemaName = "dwr_" + L2.toLowerCase();
        String toJobConfig_tableName = dwrTableName;
        String groupJobConfigGroupName = L2;
        String groupJobConfigGroupId = cdmgroups.get(L2);
        String toJobConfig_columnList = sourceColumns;
        String fromJobConfig_table = dwiTableName;
        String fromJobConfig_database = "dwi_" + L2.toLowerCase();
        String fromJobConfig_columnList = sourceColumns;
        String extendedconfigvalue = cdmextendedconfigs.get(String.valueOf(r.nextInt(10)));
        String json = cdmDwrDemoJson.toJSONString().replace("cdm_dwi_dwr_json_name", cdm_dwi_dwr_json_name)
                .replace("fromJobConfig_extendedFields", extendedconfigvalue)
                .replace("toJobConfig_schemaName", toJobConfig_schemaName)
                .replace("toJobConfig_tableName", toJobConfig_tableName)
                .replace("toJobConfig_columnList", toJobConfig_columnList)
                .replace("fromJobConfig_table", fromJobConfig_table)
                .replace("groupJobConfig_groupName", groupJobConfigGroupName)
                .replace("groupJobConfig_groupId", groupJobConfigGroupId)
                .replace("fromJobConfig_database", fromJobConfig_database)
                .replace("fromJobConfig_columnList", fromJobConfig_columnList);
        return json;
    }

    /**
     * 写出CDM->ODSJSON
     * cdmOdsJsonString
     */
    public String writeCDMOdsjson(String tableName, String sourceDB, Map<String, StringBuffer> sourceColumnsMap, String L2, String odstablecdmfromlink, String odstablecdmfromlinkschema) throws IOException {
        Random r = new Random();
        String toJobConfigDatabase = "ods_" + sourceDB.toLowerCase();   //ods_erp
        String toJobConfigTable = "ods_" + sourceDB.toLowerCase() + "_" + tableName.toLowerCase() + "_f";   //ods_erp_tbsrnjpricecheck_f
        String toJobConfigColumnList = sourceColumnsMap.get(tableName).toString().toLowerCase();
        String fromJobConfigTableName = tableName;                          //TBSRNJPRICECHECK
        String fromJobConfigColumnList = sourceColumnsMap.get(tableName).toString();
        String groupJobConfigGroupName = L2;                                //SAL
        String groupJobConfigGroupId = cdmgroups.get(L2);     //MKT:33
//        String fromLinkName = cdmlinkgroupsMap.get(sourceDB.toLowerCase()); //ERP_ZAIBEI_NEW
        String fromLinkName = odstablecdmfromlink; //ERP_ZAIBEI_NEW
//        String fromJobConfigSchemaName =cdmlinkdbsMap.get(fromLinkName);    //DB
        String fromJobConfigSchemaName = odstablecdmfromlinkschema;    //DB
        String dbType = cdmlinkdbtypegroupsMap.get(fromLinkName);            //MYSQL
        String extetldtfun = cdmextetldtfunMap.get(dbType);                 //MYSQL:${dateformat(yyyy-MM-dd HH:mm:ss)}
        String extsrcsyssource = sourceDB.toLowerCase();                    //erp
        String dtfun = cdmdtfunMap.get(dbType);                             //MYSQL:DATE_SUB(current_date,INTERVAL 1 DAY)
        String cdmSourceOdsJsonName = toJobConfigTable + "";
        String extendedconfigvalue = cdmextendedconfigs.get(String.valueOf(r.nextInt(10)));
        String json = cdmOdsDemoJson.toJSONString().replace("toJobConfig_database", toJobConfigDatabase)
                .replace("fromJobConfig_extendedFields", extendedconfigvalue)
                .replace("toJobConfig_table", toJobConfigTable)
                .replace("extetldtfun", extetldtfun)
                .replace("extsrcsyssource", extsrcsyssource)
                .replace("dtfun", dtfun)
                .replace("toJobConfig_columnList", toJobConfigColumnList)
                .replace("fromJobConfig_schemaName", fromJobConfigSchemaName)
                .replace("fromJobConfig_tableName", fromJobConfigTableName)
                .replace("fromJobConfig_columnList", fromJobConfigColumnList)
                .replace("groupJobConfig_groupName", groupJobConfigGroupName)
                .replace("groupJobConfig_groupId", groupJobConfigGroupId)
                .replace("from_link_name", fromLinkName)
                .replace("cdmSourceOdsJsonName", cdmSourceOdsJsonName);
        return json;
    }

    /**
     * 获取节点json
     *
     * @return
     * @throws IOException
     *///生成CDM_ODS 节点  传参 locationXY   etlScriptName   preNodeName    scriptDatabase scriptType
    public String writejobnodes(String locationY, String etlScriptName, String preNodeName, String scriptDatabase, String scriptType) throws IOException {
        JSONObject nodeDemoJson = jobDemoJson.getJSONObject("etl_node_demo");
        nodeDemoJson.put("name", etlScriptName);
        ArrayList<String> preNode = new ArrayList<>();
        preNode.add(preNodeName);
        JSONArray nodePros=setNodePros(etlScriptName,scriptDatabase,scriptType);
        nodeDemoJson.put("preNodeName", preNode);
        JSONObject dwiEtlPosition=new JSONObject();
        dwiEtlPosition.put("x","900");dwiEtlPosition.put("y",locationY);
        JSONObject odsEtlPosition=new JSONObject();
        odsEtlPosition.put("x","600");odsEtlPosition.put("y",locationY);
        JSONObject odsCdmPosition=new JSONObject();
        odsCdmPosition.put("x","300");odsCdmPosition.put("y",locationY);
        if (scriptType.equals("dwiETL")) {
            nodeDemoJson.put("properties", nodePros);
            nodeDemoJson.put("location", dwiEtlPosition);
            nodeDemoJson.put("type", "SparkSQL");
        } else if (scriptType.equals("odsETL")) {

            nodeDemoJson.put("properties", nodePros);
            nodeDemoJson.put("location", odsEtlPosition);
            nodeDemoJson.put("type", "SparkSQL");
        } else {
            nodeDemoJson.put("properties", nodePros);
            nodeDemoJson.put("location", odsCdmPosition);
            nodeDemoJson.put("type", "CDMJob");
        }
        return nodeDemoJson.toString();
    }

    /**
     * 生成job初始化建表语句中 ods_DDL   node信息
     * @param locationY
     * @param scriptName
     * @param dbName
     * @return
     * @throws IOException
     */
    public String writejobDDLnodes(String locationY, String scriptName, String dbName) throws IOException {
        JSONObject nodeDemoJson = jobDemoJson.getJSONObject("ods_ddl_node_demo");
        nodeDemoJson.put("name", scriptName);
        JSONArray properties = new JSONArray();
        JSONObject object = new JSONObject();
        object.put("name", "database");
        object.put("value", dbName);
        JSONObject object1 = new JSONObject();
        object1.put("name", "connectionName");
        object1.put("value", "MRS_Spark_Agent");
        JSONObject object2 = new JSONObject();
        object2.put("name", "scriptName");
        object2.put("value", scriptName);
        JSONObject object4 = new JSONObject();
        object4.put("name", "scriptArgs");
        object4.put("value", "");
        JSONObject object3 = new JSONObject();
        object3.put("name", "emptyRunningJob");
        object3.put("value", "0");
        properties.add(object);
        properties.add(object2);
        properties.add(object1);
        properties.add(object3);
        properties.add(object4);
        JSONObject position=new JSONObject();
        position.put("x","900");position.put("y",locationY);
        nodeDemoJson.put("properties", properties);
        nodeDemoJson.put("location", position);
        return nodeDemoJson.toString();
    }


    public JSONArray setNodePros(String etlScriptName, String scriptDatabase, String scriptType) throws IOException {
        JSONArray properties = new JSONArray();
        if (scriptType.equals("CDMJob")) {
            JSONObject object = new JSONObject();
            object.put("name", "jobType");
            object.put("value", "existsJob");
            JSONObject object1 = new JSONObject();
            object1.put("name", "clusterName");
            object1.put("value", "CDM-hcyt");
            JSONObject object2 = new JSONObject();
            object2.put("name", "jobName");
            object2.put("value", etlScriptName);
            JSONObject object3 = new JSONObject();
            object3.put("name", "emptyRunningJob");
            object3.put("value", "0");
            properties.add(object);
            properties.add(object2);
            properties.add(object1);
            properties.add(object3);
        } else if (scriptType.equals("odsETL")) {
            JSONObject object = new JSONObject();
            object.put("name", "scriptName");
            object.put("value", etlScriptName);
            JSONObject object1 = new JSONObject();
            object1.put("name", "connectionName");
            object1.put("value", "MRS_Spark_Agent");
            JSONObject object2 = new JSONObject();
            object2.put("name", "database");
            object2.put("value", scriptDatabase);
            JSONObject object3 = new JSONObject();
            object3.put("name", "scriptArgs");
            object3.put("value", "ETL_TODAY_MINUS8=");
            JSONObject object4 = new JSONObject();
            object4.put("name", "emptyRunningJob");
            object4.put("value", "0");
            properties.add(object);
            properties.add(object2);
            properties.add(object1);
            properties.add(object3);
            properties.add(object4);
        } else if (scriptType.equals("dwiETL")) {
            JSONObject object = new JSONObject();
            object.put("name", "scriptName");
            object.put("value", etlScriptName);
            JSONObject object1 = new JSONObject();
            object1.put("name", "connectionName");
            object1.put("value", "MRS_Spark_Agent");
            JSONObject object2 = new JSONObject();
            object2.put("name", "database");
            object2.put("value", scriptDatabase);
            JSONObject object3 = new JSONObject();
            object3.put("name", "scriptArgs");
            object3.put("value", "ETL_TODAY_MINUS1=");
            JSONObject object4 = new JSONObject();
            object4.put("name", "emptyRunningJob");
            object4.put("value", "0");
            properties.add(object);
            properties.add(object2);
            properties.add(object1);
            properties.add(object3);
            properties.add(object4);
        } else {
        }
        return properties;

    }
    }
