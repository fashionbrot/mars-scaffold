package com.github.fashionbrot.scaffold.controller;

import com.github.fashionbrot.scaffold.entity.ColumnEntity;
import com.github.fashionbrot.scaffold.req.PageReq;
import com.github.fashionbrot.scaffold.service.ScaffoldService;
import com.github.fashionbrot.scaffold.vo.PageVo;
import com.github.fashionbrot.scaffold.vo.RetVo;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/scaffold")
public class ScaffoldController {




    @Autowired
    private ScaffoldService scaffoldService;

    @GetMapping("/table/index")
    public String tableIndex(){
        return "/table/index";
    }

    /**
     * 列表
     */
    @ResponseBody
    @RequestMapping("/list")
    public PageVo list(PageReq req){
        return scaffoldService.queryList(req);

    }

    /**
     * 字段列表
     */
    @ResponseBody
    @RequestMapping("/columnList")
    public RetVo columnList(@RequestParam Map<String, Object> params){
        List<ColumnEntity> columnList = scaffoldService.queryColumns(params.get("tableName").toString());

        return RetVo.builder()
                .data(columnList)
                .build();
    }

    /**
     * 生成代码
     */
    @RequestMapping("/code")
    public void code(String tables, HttpServletResponse response) throws IOException {
        byte[] data = scaffoldService.generatorCode(tables.split(","));

        response.reset();
        response.setHeader("Content-Disposition", ("attachment; filename=\"scaffold-"+System.currentTimeMillis()+".zip\""));
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");

        IOUtils.write(data, response.getOutputStream());
    }

}
