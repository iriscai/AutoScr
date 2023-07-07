package org.example.OB;

import com.alibaba.fastjson.JSONObject;

import java.io.*;

/**
 * json读取功能
 */
public class JSONReadUn {
    public JSONObject readJsonFile(String filename){
        String jsonString = "";
        File jsonFile = new File(filename);
        try {
            FileReader fileReader = new FileReader(jsonFile);
            Reader reader = new InputStreamReader(new FileInputStream(jsonFile),"utf-8");
            int ch = 0;
            StringBuffer stringBuffer = new StringBuffer();
            while ((ch = reader.read()) != -1){
                stringBuffer.append((char) ch);
            }
            fileReader.close();
            reader.close();
            jsonString = stringBuffer.toString();
        } catch (FileNotFoundException e){
            JSONObject notFoundJson = new JSONObject();
            notFoundJson.put("msg","！");
            return notFoundJson;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSONObject.parseObject(jsonString);
    }
}
