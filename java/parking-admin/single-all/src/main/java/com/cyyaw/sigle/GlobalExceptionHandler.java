package com.cyyaw.sigle;

import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.common.WebException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResult<?> handleValidException(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        return BaseResult.fail(msg);
    }

    @ExceptionHandler(Exception.class)
    public BaseResult<?> handleException(Exception e) {
        if (e instanceof WebException) {
            WebException webException = (WebException) e;
            BaseResult<String> res = new BaseResult<>();
            res.setCode(webException.getCode());
            res.setMsg(webException.getMsg());
            return res;
        }
        e.printStackTrace();
        return BaseResult.fail();
    }

}