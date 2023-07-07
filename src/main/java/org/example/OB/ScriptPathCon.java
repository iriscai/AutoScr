package org.example.OB;

import com.alibaba.excel.EasyExcel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取相关文档路径，文档名
 */
public class ScriptPathCon {

    /**
     * 生成 OdsDDLScriptName  ODS 建表语句脚本
     * @param sourceTable   全量   src/main/resources/AutoToLake/out/NGDG/ODS/MKT/JH/DDL/hive_ods_erp_tbsonjevaluate.script
     * @param system
     * @return
     */
    public String getOdsDDLScriptName(String L2,String sourceTable,String system) {
        return "src/main/resources/AutoToLake/out/NGDG/ODS/"+L2+"/JH/DDL/DDL_F/"+"hive_ods_"+system+"_"+sourceTable+"_f"+".script";
    }

    /**
     * 生成 OdsDeleteDtETLScriptName  ODS 删除分区脚本
     * @param sourceTable     src/main/resources/AutoToLake/out/NGDG/ODS/MKT/JH/ETL/etl_ods_erp_tbsonjevaluate.script
     * @param system
     * @return
     */
    public String getOdsDeleteDtETLScriptName(String L2,String sourceTable,String system) {
        return "src/main/resources/AutoToLake/out/NGDG/ODS/"+L2+"/JH/ETL/"+"etl_ods_"+system+"_"+sourceTable+"_f"+".script";
    }

    /**
     * 生成 DwiETLScriptName DWI_ETL DWI更新数据脚本
     * @param dwiTable    dwi_mkt_tbsrnjpricecheck_bc
     * @return
     */
    public String getDwiETLScriptName(String L2,String dwiTable) {
        return "src/main/resources/AutoToLake/out/NGDG/DWI/"+L2+"/JH/ETL_每日全量/"+"etl_"+dwiTable+".script";
    }


    /**
     * 生成 DwrDDLScriptName DWR_DDL脚本
     * @param dwiTableName    dwr_mkt_tbsrnjpricecheck_bc_jc_f
     * @return
     */
    public String getDwrDDLScriptName(String dwiTableName,String L2) {
        return "src/main/resources/AutoToLake/out/NGDG/DWR/"+L2+"/JH/DDL/"+"dws_"+dwiTableName.replace("dwi","dwr")+"_jc_f.script";
    }

    public String getflinkDDLScriptPath(String dwiTableName) {
        return "src/main/resources/AutoToLake/out/NGDG/FLINK/"+dwiTableName+".sql";
    }


    public String getOdsDDLScriptPath(String L2) {
        return "src/main/resources/AutoToLake/out/NGDG/ODS/"+L2+"/JH/DDL/DDL_F";
    }

    public String getOdsDeleteDtETLScriptPath(String L2) {
        return "src/main/resources/AutoToLake/out/NGDG/ODS/"+L2+"/JH/ETL";
    }

    public String getDwiETLScriptPath(String L2) {
        return "src/main/resources/AutoToLake/out/NGDG/DWI/"+L2+"/JH/ETL_每日全量";
    }

    public String getDwrDDLScriptPath(String L2) {
        return "src/main/resources/AutoToLake/out/NGDG/DWR/"+L2+"/JH/DDL";
    }
    public String getFlinkDDLScriptPath() {
        return "src/main/resources/AutoToLake/out/NGDG/FLINK";
    }
    public void checkFilePathIfExists(String filePath) throws IOException {
        File file = new File(filePath);
        if(file.exists()){
        }else{
            try {
                file.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 生成 OdsETLScriptPath  ODS ETL 脚本 删除7天前分区
     * @param sourceTable
     * @param system
     * @return
     */
    public String getOdsETLScriptName(String sourceTable,String system) {
        return sourceTable;
    }

    /**
     * 生成 JOBPath  JOB 调度 文件
     * @param sourceTable
     * @param system
     * @return
     */
    public String getJOBName(String sourceTable,String system) {
        return "Sheet1";
    }



}
