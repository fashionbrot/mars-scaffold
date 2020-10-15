package com.github.fashionbrot.scaffold.service;

import com.github.fashionbrot.scaffold.mapper.BaseMapper;
import com.github.fashionbrot.scaffold.entity.ColumnEntity;
import com.github.fashionbrot.scaffold.entity.TableEntity;
import com.github.fashionbrot.scaffold.req.CodeReq;
import com.github.fashionbrot.scaffold.req.PageReq;
import com.github.fashionbrot.scaffold.util.Flag;
import com.github.fashionbrot.scaffold.util.ScaffoldUtil;
import com.github.fashionbrot.scaffold.vo.PageVo;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

@Service
public class ScaffoldService {

    @Autowired
    private BaseMapper baseMapper;

    @Autowired
    private ScaffoldUtil scaffoldUtil;

    public PageVo queryList(PageReq req) {
        Page<?> page = PageHelper.startPage(req.getPage(),req.getPageSize());
        Map<String,Object> map=new HashMap();

        map.put("tableName",req.getTableName());
        List<TableEntity> list = baseMapper.tableList(map);

        return PageVo.builder()
                    .data(list)
                    .iTotalDisplayRecords(page.getTotal())
                    .build();
    }


    public TableEntity queryTable(String tableName) {
        return baseMapper.queryTable(tableName);
    }

    public List<ColumnEntity> queryColumns(String tableName) {
        return baseMapper.queryColumns(tableName);
    }

    public byte[] generatorCode(CodeReq req) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        Flag flag=new Flag();
        String[] tableNames=req.getTables().split(",");
        for(String tableName : tableNames){
            scaffoldUtil.generator( req,queryTable(tableName), queryColumns(tableName), zip,flag);
        }

        IOUtils.closeQuietly(zip);
        return outputStream.toByteArray();
    }

}
