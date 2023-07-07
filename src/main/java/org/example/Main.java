package org.example;

import org.example.OB.*;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Hello world!");


////
//        DWIRDDLFun dwirddlFun = new DWIRDDLFun();
//        dwirddlFun.getDwrDwiSpricts("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "jc_f", "jh_caiyi");
//        CDMDwrJsonFun cdmDwrJsonFun = new CDMDwrJsonFun();
//        cdmDwrJsonFun.getJsonFiles("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "jc_f");
        ODSDDLFun odsddlFun = new ODSDDLFun();
        odsddlFun.getodsSpricts("src/main/resources/AutoToLake/in/财务字段级清单.xlsx", "caiyi_ods",  "jh_caiyi");
        CDMOdsJsonFun cdmOdsJsonFun = new CDMOdsJsonFun();
        cdmOdsJsonFun.getJsonFiles("src/main/resources/AutoToLake/in/财务字段级清单.xlsx", "caiyi_ods");
////
//        FLINKRDDLFun flinkrddlFun=new FLINKRDDLFun();
//        flinkrddlFun.getDwiFlinks("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "hetu", "jh_caiyi");


    }
}