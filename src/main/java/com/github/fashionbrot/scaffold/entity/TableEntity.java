package com.github.fashionbrot.scaffold.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TableEntity {
    /**
     * 表名称
     */
    private String tableId;
    /**
     * 表名称
     */
    private String tableName;
    /**
     * 表注释
     */
    private String comments;
    /**
     * 表主键字段
     */
    private ColumnEntity primaryKeyColumnEntity;
    /**
     * 表字段集合（不含主键）
     */
    private List<ColumnEntity> columns;
    /**
     * 生成的类名称，首字母大写
     */
    private String className;
    /**
     * 类引用的变量名称
     */
    private String variableClassName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 最后变更时间
     */
    private Date updateTime;
}
