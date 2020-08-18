package com.github.fashionbrot.scaffold.exception;


import lombok.Data;

@Data
public class ScaffoldException  extends RuntimeException  {

    private int code=-1;

    private String msg;


    public ScaffoldException( String msg) {
        super(msg);
        this.msg = msg;
    }
}
