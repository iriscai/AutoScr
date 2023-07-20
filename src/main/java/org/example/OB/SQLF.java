package org.example.OB;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLF {
    public static void main(String[] args) throws IOException {
    YupiDataListener yupiDataListener=new YupiDataListener();
        ScriptPathCon scriptPathCon=new ScriptPathCon();
        List<Map<Integer, String>> dataList = yupiDataListener.doRead("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "SQL_1");
        StringBuffer stringBuffer=new StringBuffer();
        String cdmWriteFilePath = "src/main/resources/AutoToLake/out/test";
        String cdmOdsWriteFileName = cdmWriteFilePath + "sql" + ".json";
        try {
            scriptPathCon.checkFilePathIfExists(cdmWriteFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedWriter odsbw = new BufferedWriter(new FileWriter(cdmOdsWriteFileName));


        for (Map<Integer, String> data : dataList) {
            String table_name=data.get(0);
            System.out.println(table_name);
            String colum_name=data.get(1);
            System.out.println(colum_name);
            String sql="SELECT \'"+table_name+"\' as table_name,\'"+colum_name+"\' as colum_name, count(1) as null_count from "+table_name+" where "+colum_name+" is NULL or "+colum_name+"='' \n        union all\n";
            odsbw.write(sql);
        }
        odsbw.flush();
        odsbw.close();
        }
}
