package org.example;

import org.example.OB.CheckExcel;

/**
 * 功能之前校验入参excel
 */
public class Before {
    public static void main(String[] args) {
        System.out.println("文档校验!");

        CheckExcel checkExcel=new CheckExcel();
        checkExcel.checkFile("src/main/resources/AutoToLake/in/财务字段级清单.xlsx", "caiyi_ods", "ods");

//        checkExcel.checkFile("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "蔡怡", "dwi");


    }
}
