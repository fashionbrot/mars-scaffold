package com.github.fashionbrot.scaffold.mapper;

import com.github.fashionbrot.scaffold.entity.ColumnEntity;
import com.github.fashionbrot.scaffold.entity.TableEntity;

import java.util.List;
import java.util.Map;

public interface BaseMapper {

    /**
     * 获取数据库表
     * @param map 查询条件
     * @return
     */
    List<TableEntity> tableList(Map<String, Object> map);
    /**
     * 获取表对象
     * @param tableName 查询条件
     * @return
     */
    TableEntity queryTable(String tableName);
    /**
     * 获取表的列集合
     * @param tableName 查询条件
     * @return
     */
    List<ColumnEntity> queryColumns(String tableName);

}
