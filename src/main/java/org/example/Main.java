package org.example;

import org.example.OB.*;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");


//////
        DWIRDDLFun dwirddlFun = new DWIRDDLFun();
        dwirddlFun.getDwrDwiSpricts("src/main/resources/AutoToLake/in/入湖脚本数据.xlsx", "毛凯乐", "jh_maokaile");
//        CDMDwrJsonFun cdmDwrJsonFun = new CDMDwrJsonFun();
////        cdmDwrJsonFun.getJsonFiles("src/main/resources/AutoToLake/in/入湖脚本数据.xlsx", "jc_f");
//        ODSDDLFun odsddlFun = new ODSDDLFun();
//        odsddlFun.getodsSpricts("src/main/resources/AutoToLake/in/入湖脚本数据.xlsx", "蔡怡_1",  "jh_taotao");
//        CDMOdsJsonFun cdmOdsJsonFun = new CDMOdsJsonFun();
//        cdmOdsJsonFun.getJsonFiles("src/main/resources/AutoToLake/in/入湖脚本数据.xlsx", "蔡怡_1");
//        JobJsonFun jobJsonFun = new JobJsonFun();
//        jobJsonFun.getJustOdsDDLJob("src/main/resources/AutoToLake/in/入湖脚本数据.xlsx", "初始化建表_蔡怡_1","jh_taotao");
//        jobJsonFun.getJsonFiles("src/main/resources/AutoToLake/in/入湖脚本数据.xlsx", "调度配置_蔡怡_1","jh_taotao");
//        FLINKRDDLFun flinkrddlFun=new FLINKRDDLFun();
//        flinkrddlFun.getDwiFlinks("src/main/resources/AutoToLake/in/入湖脚本数据.xlsx", "hetu", "jh_caiyi");


    }
}