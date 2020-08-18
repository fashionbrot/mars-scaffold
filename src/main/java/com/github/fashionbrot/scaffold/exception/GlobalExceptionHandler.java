package com.github.fashionbrot.scaffold.exception;

import com.github.fashionbrot.scaffold.vo.RetVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ScaffoldException.class)
    public RetVo handleRRException(ScaffoldException e){
        log.info(e.getMsg());
        return RetVo.builder()
                .code(e.getCode())
                .msg(e.getMsg())
                .build();
    }

    @ExceptionHandler(Exception.class)
    public RetVo handleException(Exception e){
        log.error(e.getMessage(), e);
        return RetVo.builder()
                .code(RetVo.fail)
                .msg("未知异常")
                .build();
    }

}
