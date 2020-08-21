package com.github.fashionbrot.scaffold.req;

import lombok.Data;

@Data
public class CodeReq {

    private String tables;

    private String packagePath;

    private String excludePrefix;

    private String author;

    private String version;

    private String email;

    private int swaggerStatus;
}
