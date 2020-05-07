package com.github.xjs.auditlog.anno;


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
     * 是否记录请求参数，默认记录
     * */
    boolean isLogRequestParams() default true;

    /**
     * 是否记录响应信息，默认不记录
     * */
    boolean isLogResponse() default false;

    /**
     * 忽略的请求参数，当isLogRequestParams开启时有效,默认已经忽略了HttpServletRequest、HttpServletResponse、Model、Multipart、Part
     * */
    Class[] ignoreParamClasses() default {};

}
