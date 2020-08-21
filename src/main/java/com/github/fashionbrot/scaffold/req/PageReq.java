package com.github.fashionbrot.scaffold.req;

import lombok.Data;

@Data
public class PageReq {

    //当前页码n
    private int start;
    //每页条数
    private int length;

    private String tableName;
}
