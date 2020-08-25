package com.github.fashionbrot.scaffold.req;

import lombok.Data;

@Data
public class CodeReq {

    private String moduleName="example";

    private String projectName="example";

    private String tables;

    private String packagePath;

    private String excludePrefix;

    private String author;

    private String version;

    private String email;

    private int swaggerStatus;

    private String controllerPackage;

    private String commonPackage;

    private String servicePackage;

    private String daoPackage;
}
