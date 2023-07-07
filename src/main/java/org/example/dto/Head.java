package org.example.dto;

import com.alibaba.excel.annotation.ExcelProperty;

public class Head {
    @ExcelProperty("所属主题")
    private String 所属主题;
    @ExcelProperty("*逻辑实体名称")
    private String 逻辑实体名称;
    @ExcelProperty("*表名称")
    private String 表名称;
    @ExcelProperty("*表别名")
    private String 表别名;
    @ExcelProperty("表级标签")
    private String 表级标签;
    @ExcelProperty("*描述")
    private String 描述;
    @ExcelProperty("资产责任人")
    private String 资产责任人;
    @ExcelProperty("父表")
    private String 父表;
    @ExcelProperty("*属性名称(CHN)")
    private String 属性名称;
    @ExcelProperty("*属性名称(ENG)")
    private String 属性名称2;
    @ExcelProperty("属性别名")
    private String 属性别名;
    @ExcelProperty("顺序")
    private String 顺序;
    @ExcelProperty("属性描述")
    private String 属性描述;
    @ExcelProperty("*数据类型")
    private String 数据类型;
    @ExcelProperty("数据长度")
    private String 数据长度;
    @ExcelProperty("是否分区")
    private String 是否分区;
    @ExcelProperty("是否主键")
    private String 是否主键;
    @ExcelProperty("不为空")
    private String 不为空;
    @ExcelProperty("引用的数据标准编码")
    private String 引用的数据标准编码;
    @ExcelProperty("属性标签")
    private String 属性标签;

}
