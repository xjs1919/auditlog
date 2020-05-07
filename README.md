# auditlog
用户操作记录审计

## demo演示
- 1.创建数据库导入数据
[数据库sql](https://github.com/xjs1919/auditlog/blob/master/audit-log-demo/demo.sql)
- 2.运行demo
- 3.运行admin
- 4.打开浏览器，输入http://localhost:8081,用户名：xjs，密码：123456

## 接入步骤
- 1.下载源码
本地执行：mvn clean install -DskipTests 安装依赖
- 2.添加依赖
```xml
<dependency>
    <groupId>com.github.xjs</groupId>
    <artifactId>audit-log-sdk</artifactId>
    <version>1.0.0</version>
</dependency>
```
- 3.在需要审计的controller上添加注解
比如：
```java
@AuditApi(desc="用户登录", isLogin = true, isLogResponse = true)
@PostMapping("/login")
public ResVo login(@RequestBody LoginVo vo, HttpServletResponse response){
}
```

## 相关注解
- 1.AuditModel
```java
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
```

- 2.AuditApi
```java
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
```

## 记录操作数据的前后变化
可以使用AuditContext.addDiff()
