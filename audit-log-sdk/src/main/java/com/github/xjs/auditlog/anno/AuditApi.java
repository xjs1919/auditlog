package com.github.xjs.auditlog.anno;


import com.github.xjs.auditlog.aop.RequestParamExtractor;
import com.github.xjs.auditlog.aop.UserNameExtractor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记controller的一个方法
 * */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditApi {

    /**
     * 是否开启，优先级高于{@link AuditModel}
     * */
    boolean enable() default true;

    /**
     * api接口描述，比如：新增商品，默认值是请求的uri
     * */
    String desc() default "";

    /**
     * 是否是登录请求，如果是，则在登录完成以后 去获取用户信息
     * */
    boolean isLogin() default false;

    /**
     * 如果是登录请求，可以用这个Spel表达式引用变量从参数中提取出来用户名，优先级高于userNameExtractor()<br/>
     * SDK把所有的请求参数都作为变量放到了StandardEvaluationContext中,变量名是参数名，变量值就是参数值<br/>
     * 参考：<a href="https://docs.spring.io/spring/docs/5.0.0.M5/spring-framework-reference/html/expressions.html#expressions-ref-variables">Spel官网</a><br/>
     * */
    String userNameSpel() default "";

    /**
     * 如果是登录请求，可以用这个方法提取出来登录的用户名<br/>
     * * */
    Class<? extends UserNameExtractor> userNameExtractor() default UserNameExtractor.class;

    /**
     * 是否记录请求参数，默认记录
     * */
    boolean isLogRequestParams() default true;

    /**
     * 是否记录响应信息，默认不记录
     * */
    boolean isLogResponse() default true;

    /**
     * 忽略的请求参数，当isLogRequestParams开启时有效,默认已经忽略了HttpServletRequest、HttpServletResponse、Model、Multipart、Part
     * */
    Class[] ignoreParamClasses() default {};

    /**
     * 如果方法签名上无法提取请求参数（比如：文件上传的时候），可以用这个来提取<br/>
     * * */
    Class<? extends RequestParamExtractor> requestParamExtractor() default RequestParamExtractor.class;

}
