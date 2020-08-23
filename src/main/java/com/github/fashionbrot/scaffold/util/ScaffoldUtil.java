package com.github.fashionbrot.scaffold.util;

import com.github.fashionbrot.scaffold.entity.ColumnEntity;
import com.github.fashionbrot.scaffold.entity.TableEntity;
import com.github.fashionbrot.scaffold.exception.ScaffoldException;
import com.github.fashionbrot.scaffold.req.CodeReq;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class ScaffoldUtil {

    @Autowired
    private Environment config;

    public static List<String> getTemplates(){
        List<String> templates = new ArrayList<>();
        templates.add("template/Entity.java.vm");
        templates.add("template/Mapper.java.vm");
        templates.add("template/Mapper.xml.vm");
        templates.add("template/Service.java.vm");
        templates.add("template/ServiceImpl.java.vm");
        templates.add("template/Controller.java.vm");
        templates.add("template/DTO.java.vm");
        templates.add("template/Req.java.vm");
        return templates;
    }

    /**
     * key 是 path
     * value 是vm 模板
     * @return
     */
    public  Map<String,String> getFixedTemplates(CodeReq req){
        String packagePath = getPackagePath(req);
        String projectName = req.getProjectName();
        Map<String,String> map=new HashMap<>();
        map.put(packagePath+File.separator+"entity"+File.separator+"BaseEntity.java","fixed/BaseEntity.java.vm");
        map.put(packagePath+File.separator+"service"+File.separator+"BaseService.java","fixed/BaseService.java.vm");
        map.put(packagePath+File.separator+"service"+File.separator+"impl"+File.separator+"BaseServiceImpl.java","fixed/BaseServiceImpl.java.vm");
        map.put(packagePath+File.separator+"config"+File.separator+"GlobalExceptionHandler.java","fixed/config/GlobalExceptionHandler.java.vm");
        map.put(packagePath+File.separator+"consts"+File.separator+"GlobalConst.java","fixed/consts/GlobalConst.java.vm");
        map.put(packagePath+File.separator+"enums"+File.separator+"RespCode.java","fixed/enums/RespCode.java.vm");
        map.put(packagePath+File.separator+"exception"+File.separator+"CurdException.java","fixed/exception/CurdException.java.vm");
        map.put(packagePath+File.separator+"vo"+File.separator+"RespVo.java","fixed/vo/RespVo.java.vm");
        map.put(packagePath+File.separator+"vo"+File.separator+"PageVo.java","fixed/vo/PageVo.java.vm");
        map.put(packagePath+File.separator+"util"+File.separator+"ConvertUtils.java","fixed/util/ConvertUtils.java.vm");
        map.put(packagePath+File.separator+"req"+File.separator+"PageReq.java","fixed/req/PageReq.java.vm");

        if (req.getSwaggerStatus()==1){
             map.put(packagePath+File.separator+"config"+File.separator+"SwaggerConfig.java","fixed/config/SwaggerConfig.java.vm");
        }
        map.put(packagePath+File.separator+"Application.java","fixed/Application.java.vm");
        map.put(projectName+File.separator+"pom.xml","fixed/pom.xml.vm");

        String resource =getResource(projectName);
        map.put(resource+File.separator+"application.properties","fixed/application.properties.vm");
        map.put(resource+File.separator+"application.yml","fixed/application.yml.vm");
        return map;
    }

    public String getPackagePath(CodeReq req){
        String projectName = req.getProjectName();
        String packageName = req.getPackagePath();
        String packagePath =projectName+File.separator+"src"+File.separator+ "main" + File.separator + "java" + File.separator;
        if (StringUtils.isNotBlank(packageName)) {
            packagePath += packageName.replace(".", File.separator) + File.separator ;
        }
        return  packagePath;
    }

    public String getResource(String projectName){
        return projectName+File.separator+"src"+File.separator+ "main" + File.separator + "resources";
    }



    public  void generator(CodeReq req,TableEntity tableEntity, List<ColumnEntity> columns, ZipOutputStream zip, Flag flag){

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


        //封装模板数据
        Map<String, Object> map = new HashMap<>();
        map.put("oldTableName", tableEntity.getTableName());
        // 处理注释
        if(StringUtils.isNotBlank(tableEntity.getComments())){
            map.put("comments", tableEntity.getComments());
            map.put("commentsEntity", tableEntity.getComments());
            map.put("commentsService", tableEntity.getComments());
            map.put("commentsController", tableEntity.getComments());
            map.put("commentsApi", tableEntity.getComments());
            map.put("commentsDao", tableEntity.getComments());
        }
        map.put("pk", tableEntity.getPrimaryKeyColumnEntity());
        map.put("className", tableEntity.getClassName().replace(captureName(req.getExcludePrefix()),""));
        map.put("variableClassName", tableEntity.getVariableClassName());
        map.put("columns", tableEntity.getColumns());
        StringBuilder sb=new StringBuilder();
        for(ColumnEntity c:tableEntity.getColumns()){
            if (StringUtils.isNotBlank(sb.toString())) {
                sb.append(",").append(c.getColumnName());
            }else{
                sb.append(c.getColumnName());
            }
        }
        map.put("allColumnNames",sb.toString());

        map.put("hasBigDecimal", hasBigDecimal);
       /* map.put("main", main);*/
        // API接口排序
        if(tableEntity.getCreateTime()!= null){
            map.put("apiSort", StringUtils.substring(String.valueOf(tableEntity.getCreateTime().getTime() + System.currentTimeMillis()), 1, 9));
        }

        String packagePath = req.getPackagePath();
        map.put("package", packagePath);
        if(StringUtils.isNotBlank(tableEntity.getTableName())){
            String tableName = StringUtils.lowerCase(tableEntity.getTableName());
            map.put("pathName", tableName.replace("_","/"));

            // 前端权限标识
            map.put("apiPermission", tableName.replace("_",":"));
            // className.toLowerCase()
            map.put("vueFileName", className.toLowerCase());
        }
        map.put("moduleName", req.getModuleName());
        map.put("author", req.getAuthor());
        map.put("version", req.getVersion());
        map.put("email", req.getEmail());
        map.put("datetime", DateUtil.format(new Date(), DateUtil.DATE_TIME_PATTERN));
        map.put("date", DateUtil.format(new Date(), DateUtil.DATE_PATTERN));
        map.put("projectName",req.getProjectName());
        map.put("package2",map.get("package").toString().replace(".","/"));
        VelocityContext context = new VelocityContext(map);

        //获取模板列表
        List<String> templates = getTemplates();
        Map<String,String> mapTemplate = getFixedTemplates(req);
        try {
            generateTemplate(tableEntity, zip, context, templates,req);

            if(flag.isFlag()==false) {
                generateTemplate( zip, context, mapTemplate);
                flag.setFlag(true);
            }
        } catch (IOException e) {
            log.error("error:",e);
            throw new ScaffoldException("渲染模板失败，表名：" + tableEntity.getTableName());
        }

    }

    private void generateTemplate( ZipOutputStream zip, VelocityContext context,Map<String,String> map ) throws IOException {
        Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, String> m = iterator.next();
            //vm 模板
            String template=m.getValue();
            //vm 模板对应的path
            String path = m.getKey();
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            //添加到zip
            log.info("generateTemplate path:{}",path);
            ZipEntry zipEntry = new ZipEntry(path);
            zip.putNextEntry(zipEntry);
            IOUtils.write(sw.toString(), zip, "UTF-8");
            IOUtils.closeQuietly(sw);
            zip.closeEntry();
        }
    }

    private void generateTemplate(TableEntity tableEntity, ZipOutputStream zip, VelocityContext context, List<String> templates,CodeReq req) throws IOException {
        for(String template : templates){
            log.info("template:{}",template);
            //渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, "UTF-8");
            tpl.merge(context, sw);
            //添加到zip
            //替换表前缀
            String className = tableEntity.getClassName().replace(captureName(req.getExcludePrefix()),"");
            String fileName =getFileName(template, className, req);
            ZipEntry zipEntry = new ZipEntry(fileName);

            zip.putNextEntry(zipEntry);
            IOUtils.write(sw.toString(), zip, "UTF-8");
            IOUtils.closeQuietly(sw);
            zip.closeEntry();
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
    public  String getFileName(String template, String className,CodeReq req) {
        String packagePath =getPackagePath(req);

        if (template.contains("Entity.java.vm" )) {
            return packagePath + "entity" + File.separator + className + "Entity.java";
        }
        if (template.contains("Req.java.vm" )) {
            return packagePath + "req" + File.separator + className + "Req.java";
        }

        if (template.contains("Mapper.java.vm" )) {
            return packagePath + "mapper" + File.separator + className + "Mapper.java";
        }

        if (template.contains("Service.java.vm" ) ) {
            return packagePath + "service" + File.separator + className + "Service.java";
        }
        if (template.contains("ServiceImpl.java.vm" )) {
            return packagePath + "service" + File.separator + "impl" + File.separator + className + "ServiceImpl.java";
        }

        if (template.contains("Controller.java.vm" )) {
            return packagePath + "controller" + File.separator + className + "Controller.java";
        }
        if (template.contains("DTO.java.vm" )) {
            return packagePath + File.separator + "dto" + File.separator + className + "DTO.java";
        }
        if (template.contains("Mapper.xml.vm" )) {
            return packagePath + File.separator + "mapper" + File.separator + "xml" + File.separator + className + "Mapper.xml";
        }

        return null;
    }
    /**
     * 将字符串的首字母转大写
     * @param str 需要转换的字符串
     * @return
     */
    private static String captureName(String str) {
        // 进行字母的ascii编码前移，效率要高于截取字符串进行转换的操作
        char[] cs=str.toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);
    }

}
