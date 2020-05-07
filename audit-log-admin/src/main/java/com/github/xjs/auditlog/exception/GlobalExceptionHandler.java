package com.github.xjs.auditlog.exception;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import com.github.xjs.auditlog.vo.common.CodeMsg;
import com.github.xjs.auditlog.vo.common.ResVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Set;

@RestControllerAdvice
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
	
    @ExceptionHandler(value=Exception.class)
    public ResVo<String> allExceptionHandler(HttpServletRequest request, Exception exception) throws Exception{
        if(exception instanceof GlobalException){
        	GlobalException biz = (GlobalException)exception;
        	return ResVo.fail(biz.getCodeMsg());
        }else if(exception instanceof BindException) {
			BindException ex = (BindException)exception;
			List<ObjectError> errors = ex.getAllErrors();
			ObjectError error = errors.get(0);
			String msg = error.getDefaultMessage();
			return ResVo.fail(CodeMsg.BIND_ERROR.fillArgs(msg));
		}else if(exception instanceof MethodArgumentNotValidException){
			MethodArgumentNotValidException ex = (MethodArgumentNotValidException)exception;
			ObjectError objectError = ex.getBindingResult().getAllErrors().get(0);
			String msg = objectError.getDefaultMessage();
			if(objectError instanceof FieldError) {
				msg = ((FieldError)objectError).getField() + msg;
			}
			return ResVo.fail(CodeMsg.BIND_ERROR.fillArgs(msg));
		}else if(exception instanceof HttpRequestMethodNotSupportedException) {
			HttpRequestMethodNotSupportedException ex = (HttpRequestMethodNotSupportedException)exception;
			String msg = ex.getMessage();
			return ResVo.fail(CodeMsg.REQUEST_METHOD_ERROR.fillArgs(msg));
		}else if(exception instanceof ConstraintViolationException){
			ConstraintViolationException ex = (ConstraintViolationException)exception;
			Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
			ConstraintViolation violation = violations.iterator().next();
			String property = violation.getPropertyPath().toString();
			String msg = property + violation.getMessage();
			return ResVo.fail(CodeMsg.REQUEST_METHOD_ERROR.fillArgs(msg));
		}else {
			log.error(exception.getMessage(), exception);
			return ResVo.fail(CodeMsg.SERVER_ERROR);
		}
    }
}
