package com.github.fashionbrot.scaffold.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetVo  {

    public static final int fail=-1;

    public static final int success=1;

    private int code;

    private String msg;

    private Object data;
}
