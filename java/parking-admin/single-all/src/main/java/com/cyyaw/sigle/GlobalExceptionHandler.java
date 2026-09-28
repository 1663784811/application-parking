package com.cyyaw.sigle;

import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.common.WebException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
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
        // 走日志而不用 printStackTrace()：stderr 不受 logback 接管，
        // 异常堆栈只落在控制台、日志文件里什么都没有，事后排查不了。
        // 响应体仍统一是 4000 操作失败，不向调用方泄露内部细节。
        log.error("接口未捕获异常", e);
        return BaseResult.fail();
    }

}