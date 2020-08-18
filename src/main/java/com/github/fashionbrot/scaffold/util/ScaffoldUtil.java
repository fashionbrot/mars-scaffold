package com.github.fashionbrot.scaffold.util;

import com.github.fashionbrot.scaffold.entity.ColumnEntity;
import com.github.fashionbrot.scaffold.entity.TableEntity;
import com.github.fashionbrot.scaffold.exception.ScaffoldException;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ScaffoldUtil {

    @Autowired
    private Environment config;

    public static List<String> getTemplates(){
        List<String> templates = new ArrayList<>();
        templates.add("template/Entity.java.vm");
        templates.add("template/Dao.java.vm");
        templates.add("template/Dao.xml.vm");
        templates.add("template/Service.java.vm");
        templates.add("template/ServiceImpl.java.vm");
        templates.add("template/Controller.java.vm");
        templates.add("template/Excel.java.vm");
        templates.add("template/Redis.java.vm");
        templates.add("template/DTO.java.vm");
        return templates;
    }



    public  void generator(TableEntity tableEntity, List<ColumnEntity> columns, ZipOutputStream zip){

        boolean hasBigDecimal = false;
        //表名转换成Java类名
        String className = tableToJava(tableEntity.getTableName(), config.getProperty("tablePrefix"));
        tableEntity.setClassName(className);
        tableEntity.setVariableClassName(StringUtils.uncapitalize(className));

        //列信息
        for(ColumnEntity columnEntity : columns){
            //列名转换成Java属性名
            String attrName = columnToJava(columnEntity.getColumnName());
            columnEntity.setAttrName(attrName);
            columnEntity.setVariableAttrName(StringUtils.uncapitalize(attrName));

            //列的数据类型，转换成Java类型
            if("NUMBER".equals(columnEntity.getDataType()) && columnEntity.getDataScale() > 0){
                columnEntity.setAttrType("Double");
            }else if("NUMBER".equals(columnEntity.getDataType()) && columnEntity.getDataPrecision()>14){
                columnEntity.setAttrType("Long");
            }else{
                String attrType = config.getProperty(columnEntity.getDataType(), "unknowType");
                columnEntity.setAttrType(attrType);
                if (!hasBigDecimal && attrType.equals("BigDecimal" )) {
                    hasBigDecimal = true;
                }
            }

            //是否主键
            if("PRI".equalsIgnoreCase(columnEntity.getColumnKey()) && tableEntity.getPrimaryKeyColumnEntity() == null){
                tableEntity.setPrimaryKeyColumnEntity(columnEntity);
            }
        }
        tableEntity.setColumns(columns);

        //没主键，则第一个字段为主键
        if(tableEntity.getPrimaryKeyColumnEntity() == null){
            tableEntity.setPrimaryKeyColumnEntity(tableEntity.getColumns().get(0));
        }

        //设置velocity资源加载器
        Properties prop = new Properties();
        prop.put("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(prop);

        String main = config.getProperty("main" );
        main = StringUtils.isBlank(main) ? config.getProperty("package" ) : main;

        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", tableEntity.getTableName());
        // 处理注释
        if(StringUtils.isNotBlank(tableEntity.getComments())){
            map.put("comments", tableEntity.getComments().replace("表","信息"));
            map.put("commentsEntity", tableEntity.getComments().replace("表","信息实体"));
            map.put("commentsService", tableEntity.getComments().replace("表","信息服务层"));
            map.put("commentsController", tableEntity.getComments().replace("表","信息控制层"));
            map.put("commentsApi", tableEntity.getComments().replace("表","信息接口"));
            map.put("commentsDao", tableEntity.getComments().replace("表","信息数据层"));
        }
        map.put("pk", tableEntity.getPrimaryKeyColumnEntity());
        map.put("className", tableEntity.getClassName());
        map.put("variableClassName", tableEntity.getVariableClassName());
        map.put("columns", tableEntity.getColumns());
        map.put("hasBigDecimal", hasBigDecimal);
        map.put("main", main);
        // API接口排序
        if(tableEntity.getCreateTime()!= null){
            map.put("apiSort", StringUtils.substring(String.valueOf(tableEntity.getCreateTime().getTime() + System.currentTimeMillis()), 1, 9));
        }

        String moduleName = config.getProperty("moduleName" );
        if(StringUtils.isNotBlank(moduleName)){
            map.put("package", config.getProperty("package" ) + "." + moduleName);
        }else {
            map.put("package", config.getProperty("package" ));
        }
        if(StringUtils.isNotBlank(tableEntity.getTableName())){
            String tableName = StringUtils.lowerCase(tableEntity.getTableName());
            map.put("pathName", tableName.replace("_","/"));

            // 前端权限标识
            map.put("apiPermission", tableName.replace("_",":"));
            // className.toLowerCase()
            map.put("vueFileName", className.toLowerCase());
        }
        map.put("moduleName", config.getProperty("moduleName" ));
        map.put("author", config.getProperty("author"));
        map.put("version", config.getProperty("version"));
        map.put("email", config.getProperty("email"));
        map.put("datetime", DateUtil.format(new Date(), DateUtil.DATE_TIME_PATTERN));
        map.put("date", DateUtil.format(new Date(), DateUtil.DATE_PATTERN));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        for(String template : templates){
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);

            try {
                //添加到zip
                zip.putNextEntry(new ZipEntry(getFileName(template, tableEntity.getClassName(), config.getProperty("package"), config.getProperty("moduleName"))));
                IOUtils.write(sw.toString(), zip, "UTF-8");
                IOUtils.closeQuietly(sw);
                zip.closeEntry();
            } catch (IOException e) {
                throw new ScaffoldException("渲染模板失败，表名：" + tableEntity.getTableName());
            }
        }
    }

    /**
     * 列名转换成Java属性名
     */
    public static String columnToJava(String columnName) {
        return WordUtils.capitalizeFully(columnName, new char[]{'_'}).replace("_", "");
    }

    /**
     * 表名转换成Java类名
     */
    public static String tableToJava(String tableName, String tablePrefix) {
        if(StringUtils.isNotBlank(tablePrefix)){
            tableName = tableName.replaceFirst(tablePrefix, "");
        }
        return columnToJava(tableName);
    }


    /**
     * 获取文件名
     */
    public static String getFileName(String template, String className, String packageName, String moduleName) {
        String packagePath = "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator + moduleName + File.separator;
        }

        if (template.contains("Entity.java.vm" )) {
            return packagePath + "entity" + File.separator + className + "Entity.java";
        }

        if (template.contains("Excel.java.vm" )) {
            return packagePath + "excel" + File.separator + className + "Excel.java";
        }

        if (template.contains("Dao.java.vm" )) {
            return packagePath + "dao" + File.separator + className + "Dao.java";
        }

        if (template.contains("Service.java.vm" )) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }

        if (template.contains("ServiceImpl.java.vm" )) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains("Controller.java.vm" )) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }

        if (template.contains("Redis.java.vm" )) {
            return packagePath + "redis" + File.separator + className + "Redis.java";
        }

        if (template.contains("DTO.java.vm" )) {
            return moduleName + File.separator + "dto" + File.separator + className + "DTO.java";
        }

        if (template.contains("Dao.xml.vm" )) {
            return "main" + File.separator + "resources" + File.separator + "mapper" + File.separator + className + "Dao.xml";
        }


        return null;
    }


}
