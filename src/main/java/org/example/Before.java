package org.example;

import org.example.OB.CheckExcel;

/**
 * 功能之前校验入参excel
 */
public class Before {
    public static void main(String[] args) {
        System.out.println("文档校验!");

        CheckExcel checkExcel=new CheckExcel();
        checkExcel.checkFile("src/main/resources/AutoToLake/in/入湖脚本数据.xlsx", "脚本数据", "dwi");

//        checkExcel.checkFile("src/main/resources/AutoToLake/in/供应商有效表清单.xlsx", "蔡怡", "dwi");


    }
}
