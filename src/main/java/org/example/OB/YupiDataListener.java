package org.example.OB;

import com.alibaba.excel.EasyExcel;
import org.example.dto.Head;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 读写主类，需重写表头
 */
public class YupiDataListener {

    public List<Map<Integer, String>> doRead(String fileName,String sheetNum) {
        List<Map<Integer, String>> list = EasyExcel
                .read(fileName)
                .sheet(sheetNum)
                .doReadSync();
        return list;
    }

    //EasyExcel 获取关键字段定位
    public Map<String,Integer> getTitlePosi(String fileName,String sheetNum) {
        List<Map<Integer, String>> list = EasyExcel
                .read(fileName)
                .sheet(sheetNum).headRowNumber(0)
                .doReadSync();
        Map<Integer, String> head=list.get(0);
        Map<String,Integer> posi=new HashMap<>();
        for (int i = 0; i < head.size(); i++) {
            posi.put(head.get(i),i);
        }
        return posi;
    }

    //EasyExcel 获取关键字段定位
    public Map<Integer,String> getTitle(String fileName,String sheetNum) {
        List<Map<Integer, String>> list = EasyExcel
                .read(fileName)
                .sheet(sheetNum).headRowNumber(0)
                .doReadSync();
        Map<Integer, String> head=list.get(0);
        return head;
    }
}
