package org.example.OB;

import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * 校验文档准确性　　ｏｄｓ．ｅｘｃｅｌ　　　ｄｗｉ．ｅｘｃｅｌ
 */
public class CheckExcel {
    static JSONObject scriptexcelcolumns;
    static JSONObject datatypejson;
    static JSONObject excelMustColumnsJson;
    static HashMap<String, HashMap<String, String>> datatype = new HashMap<String, HashMap<String, String>>();
    static HashMap<String, String> excelcolumns = new HashMap<String, String>();
    static YupiDataListener yupiDataListener = new YupiDataListener();
    static JSONObject cdmgroups;
    static HashMap<String, HashMap<String, String>> propetriesMap = new HashMap<String, HashMap<String, String>>();

    static {
        JSONReadUn jsonReadUn = new JSONReadUn();
        String scriptexcelcolumnsfilePath = "src/main/resources/source/script_desc.json";
        String datatypefilePath = "src/main/resources/source/datatype.json";
        excelMustColumnsJson = jsonReadUn.readJsonFile("src/main/resources/source/excel_columns.json");
        scriptexcelcolumns = jsonReadUn.readJsonFile(scriptexcelcolumnsfilePath);
        datatypejson = jsonReadUn.readJsonFile(datatypefilePath);

        for (Iterator iterator = scriptexcelcolumns.getJSONObject("excelcolumns").keySet().iterator(); iterator.hasNext(); ) {
            String diagramKey = (String) iterator.next();
            excelcolumns.put(diagramKey, scriptexcelcolumns.getJSONObject("excelcolumns").getString(diagramKey));
        }
        //获取数据类型map
        for (Map.Entry<String, Object> entry : datatypejson.entrySet()) {
            JSONObject dataevetypejson = (JSONObject) entry.getValue();
            HashMap<String, String> dataevetypes = JSONObject.parseObject(JSONObject.toJSONString(dataevetypejson), HashMap.class);
            datatype.put(entry.getKey(), dataevetypes);
        }
        String cdmgroupsfilePath = "src/main/resources/source/cdm_group_properties.json";
        cdmgroups = jsonReadUn.readJsonFile(cdmgroupsfilePath);
        for (Iterator iterator = cdmgroups.keySet().iterator(); iterator.hasNext(); ) {
            String diagramKey = (String) iterator.next();
            HashMap<String, String> groupsMap = new HashMap<String, String>();
            cdmgroups.getJSONArray(diagramKey).forEach(i -> {
                JSONObject jsonObjectname = JSONObject.parseObject(i.toString());
                groupsMap.put(jsonObjectname.getString("name"), jsonObjectname.getString("value"));
            });
            propetriesMap.put(diagramKey, groupsMap);
        }
    }

    /**
     * args[0]  filePath<src/main/resources/AutoToLake/in/xxx.xlsx></>    必填
     * args[1]  SheetName<Sheet1></>   必填
     * args[2]  fileType<ods/dwi></>   必填
     *
     * @param args 用来校验文件所需执行字段完整性
     */
    public void main(String[] args) {
        switch (args[2]) {
            case "ods":
                this.checkFile(args[0], args[1], args[2]);
            case "dwi":
                this.checkFile(args[0], args[1], args[2]);
            default:
                System.out.println("type error");
        }
    }


    /**
     * 校验EXCEL文档准确性
     * @param filePath
     * @param sheetName
     * @param type
     */
    public void checkFile(String filePath, String sheetName, String type) {
        List<Map<Integer, String>> dataList = yupiDataListener.doRead(filePath, sheetName);
        //检验字段缺失已及获取定位
        Map<String, Integer> columnsPosiMap = new HashMap<>();
        try {
            columnsPosiMap = getColumnsPosiMap(filePath, sheetName, type);
            System.out.println("DoneAndSucceed: 必须字段校验");
        } catch (Exception e) {
            System.out.println("ERROR：必须字段缺失,校验EXCEL,与source/excel_columns.json配置内容核对！");
        }
//        checkExcelColumnNums(filePath,sheetName,type);
        //检验字段  是否存在空值
        Map<String, String> nullColumnsMap = new HashMap<>();
        for (Map<Integer, String> data : dataList) {
            for (String i : columnsPosiMap.keySet()) {
                if (data.get(columnsPosiMap.get(i))==null) {
                    nullColumnsMap.put(i, "存在空值");
                    continue;
                }
            }
        }
        if (nullColumnsMap.size() > 0) {
            System.out.println(nullColumnsMap);
        } else {
            System.out.println("DoneAndSucceed: 字段空值校验");
        }
        //检验字段 具体字段内容
        if (type.equals("ods")||type.equals("dwi")) {
            for (Map<Integer, String> data : dataList) {
                //校验源系统字段类型及长度
                checkdatatype(data.get(columnsPosiMap.get("字段类型")), data.get(columnsPosiMap.get("字段长度")));
            }
            System.out.println("Done: 源系统字段类型及长度校验");
        }

    }

    /**
     * 获取配置必须字段与EXCEL数据定位集合
     * @param filePath
     * @param sheet
     * @param type
     * @return
     */
    public Map<String, Integer> getColumnsPosiMap(String filePath, String sheet, String type) {
        Map<String, Integer> getColumnsPosiMap = new HashMap<>();
        HashMap<String, String> mustColumnsMap = getFunColumnsMap(type);
        Map<String, Integer> getColumnsPosi = getExcelColumnsMap(filePath, sheet);
        System.out.println("配置所需字段：    "+mustColumnsMap);
        System.out.println("EXCEL提供字段：    "+getColumnsPosi);
        //将excel提供字段与功能所需字段对比
        if (getColumnsPosi.keySet().containsAll(mustColumnsMap.keySet())) {
            for (String key : mustColumnsMap.keySet()) {
                getColumnsPosiMap.put(key, new Integer(getColumnsPosi.get(key)));
            }
            return getColumnsPosiMap;
        } else {
            System.out.println("ERROR：必须字段缺失,校验EXCEL,与source/excel_columns.json配置内容核对！");
            return null;
        }
    }
    /**
     * 获取excel字段集合
     * @param filePath
     * @param sheet
     * @return
     */
    public Map<String, Integer> getExcelColumnsMap(String filePath, String sheet) {
        Map<Integer, String> getTitlePosi = yupiDataListener.getTitle(filePath, sheet);
        Map<String, Integer> columnsPosi = new HashMap<>();
        for (Integer key : getTitlePosi.keySet()) {
            columnsPosi.put(getTitlePosi.get(key), key);
        }
        return columnsPosi;
    }

    /**
     * 获取配置文件必须字段集合
     * @param type
     * @return
     */
    public HashMap<String, String> getFunColumnsMap(String type) {
        HashMap<String, String> returnExcelColumnsMap = new HashMap<String, String>();
        for (Iterator iterator = excelMustColumnsJson.getJSONObject(type).keySet().iterator(); iterator.hasNext(); ) {
            String diagramKey = (String) iterator.next();
            returnExcelColumnsMap.put(diagramKey, excelMustColumnsJson.getJSONObject(type).getString(diagramKey));
        }
        return returnExcelColumnsMap;
    }

    /**
     *判断EXCEL里
     * @param type
     * @param dataLength
     * @return
     */
    public Boolean checkdatatype(String type, String dataLength) {
        String oldType=type.toUpperCase();
        Boolean out = true;
        boolean flag = dataLength.matches("^\\d{1,3}[,]\\d{1,2}$");
        boolean flag2 = dataLength.matches("^[(]\\d{1,3}[,]\\d{1,2}[)]$");

        if (oldType.equals("BIGDECIMAL") ||oldType.equals("DECIMAL") || oldType.equals("NUMERIC")) {
            if (flag||flag2) {
            }
            else if (StringUtils.isNumeric(dataLength) && new Integer(dataLength) > 0)
            {
            } else {
                System.out.println("ERROR：EXCEL字段<源系统字段类型><字段长度>匹配错误！ 详细内容： " + oldType + " " + dataLength);
                out = false;
            }
        } else if (oldType.equals("TIMESTAMP") || oldType.equals("DATE") || oldType.equals("YEAR") || oldType.equals("TIME") || oldType.equals("CLOB")) {
        } else if (oldType.equals("STRING") || oldType.equals("CHAR") || oldType.equals("VARCHAR") ||
                oldType.equals("TINYINT") || oldType.equals("SMALLINT") || oldType.equals("INTEGER") ||
                oldType.equals("MEDIUMINT") || oldType.equals("INT") || oldType.equals("FLOAT") ||
                oldType.equals("DOUBLE") || oldType.equals("BIGINT")) {
            if (StringUtils.isNumeric(dataLength) && new Integer(dataLength) > 0) {
            } else {
                System.out.println("ERROR：EXCEL字段<源系统字段类型><字段长度>匹配错误！ 详细内容： " + oldType + " " + dataLength);
                out = false;
            }
        } else {
            System.out.println("ERROR：EXCEL字段<源系统字段类型><字段长度>匹配错误！ 详细内容： " + oldType + " " + dataLength);
            out = false;
        }
        return out;
    }



}
