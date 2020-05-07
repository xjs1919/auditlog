package com.github.xjs.auditlog.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记Controller类
 * */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditModel {
    /**
     * 是否开启
     * */
    boolean enable() default true;
    /**
     * controller的描述，默认是controller的名字
     * */
    String desc() default "";
}
