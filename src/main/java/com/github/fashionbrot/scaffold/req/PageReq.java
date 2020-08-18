package com.github.fashionbrot.scaffold.req;

import lombok.Data;

@Data
public class PageReq {

    //当前页码
    private int start;
    //每页条数
    private int length;

    private String tableName;
}
