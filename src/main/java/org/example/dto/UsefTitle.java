package org.example.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class UsefTitle {
    @ExcelProperty("L1资产编码")
    private String l1Code;
    @ExcelProperty("L3")
    private String l3;
    @ExcelProperty("路径")
    private String l3Posi;
    @ExcelProperty("逻辑实体-英文(L4)")
    private String l4Code;
    @ExcelProperty("逻辑实体-中文(L4)")
    private String l4Name;
    @ExcelProperty("源系统表名-英文")
    private String sourceTable;
    @ExcelProperty("源系统表名-中文")
    private String sourceTableName;
    @ExcelProperty("表名-英文（规范后）")
    private String sourceTableNor;
    @ExcelProperty("*表名-中文（规范后）")
    private String sourceTableNameNor;
    @ExcelProperty("表业务含义及用途(L4业务含义)")
    private String l4Desc;
    @ExcelProperty("属性-英文（L5）")
    private String column;
    @ExcelProperty("属性-中文（L5）")
    private String columnComent;
    @ExcelProperty("字段长度")
    private String columnSize;
    @ExcelProperty("是否为主键")
    private String IfPreyK;

}
