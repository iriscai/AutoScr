# AutoScr
说明：
org/example/Before.java
功能前：校验EXCEL
org/example/Main.java
入口：生成脚本,cdm.json
需要修改的配置：
<source/excel_columns.json  cdm_group_properties.json>



类参考：
org/example/OB/CDMOdsJsonFun.java
生成cdm_ods.json
org/example/OB/CDMDwrJsonFun.java
生成cdm_dwr.json
org/example/OB/ODSDDLFun.java
生成ods表建表DDL，清除dt=-8ETL 脚本
org/example/OB/DWIRDDLFun.java
生成dwi_ETL脚本<insert into dwi from ods>
生成dwr_jc_f建表DDL<with primaryKeys>
org/example/OB/DataSplit.java
逻辑处理
org/example/CheckExcel.java
校验excel准确性


配置解释：
source/excel_columns.json
为校验excel表格内容，配置
存在校验：
① ERROR：必须字段缺失,校验EXCEL,与source/excel_columns.json配置内容核对！
② ERROR：必须字段为NULL,校验EXCEL,填补空值！
③ ERROR：EXCEL字段<源系统字段类型>内容错误！
④ ERROR：EXCEL字段<字段长度>内容错误！
source/cdm_dwr_demo.json
cdm_dwr json demo
source/cdm_ods_demo.json
cdm_ods json demo
source/cdm_group_properties.json
cdm 相关配置
cdm_groups：分组信息
cdm_linkgroups：系统对应连接器
cdm_linkdbtypegroups：连接器对应数据库类型
cdm_linkdbs：连接器对应库名<动态修改>
cdm_extetldtfun：不同数据库类型对应添加dt形态
cdm_dtfun：不同数据库类型对应添加dt形态
source/script_desc.json
脚本相关配置内容
source/datatype.json
数据库字段类型










note:
半自动化脚本
一.CDM 入湖
1.抽取业务数据至ODS入湖生成导入JSON文件
2.抽取DWI数据至DWR生成导入JSON文件

二.EXCEL清洗转换匹配字段
        1.修改责任人等功能

三.DDL ETL 脚本自动化生成设计
        1.生成ods表建表DDL，清除dt=-8ETL 脚本
        2.生成dwi_ETL脚本<insert into dwi from ods>
        3.生成dwr_jc_f建表DDL<with primaryKeys>

四.添加辅助功能EXCEL 内容校验
        1.字段内容IFNULL
        2.字段名校验
        3.字段类型校验
        4.是否主键/空值字段补齐


