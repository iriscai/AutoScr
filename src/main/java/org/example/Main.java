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
////        cdmDwrJsonFun.getJsonFiles("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "jc_f");
//        ODSDDLFun odsddlFun = new ODSDDLFun();
//        odsddlFun.getodsSpricts("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "陶涛",  "jh_taotao");
//        CDMOdsJsonFun cdmOdsJsonFun = new CDMOdsJsonFun();
//        cdmOdsJsonFun.getJsonFiles("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "陶涛");
        JobJsonFun jobJsonFun = new JobJsonFun();
        jobJsonFun.getJustOdsDDLJob("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "调度ODSDDL","jh_caiyi");
////
//        FLINKRDDLFun flinkrddlFun=new FLINKRDDLFun();
//        flinkrddlFun.getDwiFlinks("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "hetu", "jh_caiyi");


    }
}