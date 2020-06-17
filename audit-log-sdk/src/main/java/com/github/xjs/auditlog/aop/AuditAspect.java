package com.github.xjs.auditlog.aop;


import com.alibaba.fastjson.JSON;
import com.github.xjs.auditlog.anno.AuditApi;
import com.github.xjs.auditlog.anno.AuditModel;
import com.github.xjs.auditlog.config.ActionAuditProperties;
import com.github.xjs.auditlog.log.AuditLog;
import com.github.xjs.auditlog.log.IAuditLogService;
import com.github.xjs.auditlog.user.AuditUserInfo;
import com.github.xjs.auditlog.user.IAuditUserService;
import com.github.xjs.auditlog.util.StringUtil;
import com.github.xjs.auditlog.util.ThreadPoolUtil;
import com.github.xjs.auditlog.util.WebUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Aspect
public class AuditAspect implements ApplicationContextAware {

    /**构造函数初始化**/
    private final ActionAuditProperties properties;
    private final IAuditUserService auditUserService;
    private final IAuditLogService auditLogService;
    private final AuditModel defaultAuditMolde;
    private final AuditApi defaultAuditApi;
    public AuditAspect(ActionAuditProperties properties, IAuditUserService auditUserService, IAuditLogService auditLogService){
        this.properties = properties;
        this.auditUserService = auditUserService;
        this.auditLogService = auditLogService;
        this.defaultAuditMolde = AnnotatedElementUtils.getMergedAnnotation(DummyClass.class, AuditModel.class);
        this.defaultAuditApi = AnnotatedElementUtils.getMergedAnnotation(ClassUtils.getMethod(DummyClass.class, "dummyMethod", String.class), AuditApi.class);
    }

    /**ApplicationContextAware回调**/
    private ApplicationContext applicationContext;
    private List<String> basePackages;
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException{
        this.applicationContext = applicationContext;
        this.basePackages = AutoConfigurationPackages.get(this.applicationContext);
    }

    /**AOP，参考：
    //https://stackoverflow.com/questions/52992365/spring-creates-proxy-for-wrong-classes-when-using-aop-class-level-annotation/53452483
    //https://blog.csdn.net/qq_23167527/article/details/78623639
     */
    @Pointcut("execution(* (@com.github.xjs.auditlog.anno.AuditModel *).*(..)) ||" +
            "execution(@com.github.xjs.auditlog.anno.AuditApi * *(..))")
    public void requestMappingCut() {
    }

    @Around("requestMappingCut()")
    public Object aroundRequestMapping(ProceedingJoinPoint joinPoint) throws Throwable {
        Object args[] = joinPoint.getArgs();
        Object controller = joinPoint.getTarget();
        Method method = ((MethodSignature)(joinPoint.getSignature())).getMethod();
        //拿到类和方法上的注解
        AuditModel auditModel = controller.getClass().getAnnotationsByType(AuditModel.class)[0];
        AuditApi auditApi = null;
        for (Annotation annotation : method.getDeclaredAnnotations()) {
            if (annotation instanceof AuditApi) {
                auditApi = (AuditApi) annotation;
                break;
            }
        }
        //判断是否开启
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        boolean isGet = httpRequest.getMethod().toUpperCase().equals("GET");
        boolean enable = isAuditEnable(auditModel, auditApi, isGet);
        if(!enable){
            return joinPoint.proceed(args);
        }
        auditModel = (auditModel==null?this.defaultAuditMolde:auditModel);
        auditApi = (auditApi==null?this.defaultAuditApi :auditApi);
        String uri = httpRequest.getRequestURI();
        String clientIp = WebUtil.getRemoteIP(httpRequest);
        long createAt = System.currentTimeMillis();
        AuditUserInfo userInfo = null;
        //获取所有的参数名和参数的值, 参数值会copy一份，防止controller内部会对参数做修改
        List<ParamNameValue> paramNameValues = getAllParamNameValues(method, args, auditApi.ignoreParamClasses(), basePackages);
        //说明是登录接口
        if(auditApi.isLogin()){
            //只记录登录的用户名, 这里也可以通过用户名再调用接口去获取用户信息, 但是如果用户名是错误的就无法获取用户信息
            String userName = extractUserName(auditApi, args, paramNameValues);
            if(StringUtils.isEmpty(userName)){
                log.error("登录接口没有设置userNameExtractor，无法获取用户名");
            }else{
                userInfo = new AuditUserInfo(null, userName, null);
            }
        }else{
            // 获取登录的用户
            userInfo = auditUserService.getUserInfo(httpRequest);
            if(userInfo == null){
                log.error("无法获取用户信息");
                return joinPoint.proceed(args);
            }
        }
        Object responseResult = null;
        boolean success = true;
        int costMills = 0;
        try{
            //执行controller方法
            long start = System.currentTimeMillis();
            responseResult = joinPoint.proceed(args);
            long end = System.currentTimeMillis();
            costMills = (int)(end-start);
            success = true;
        }catch(Throwable e){
            success = false;
            throw e;
        }finally{
            final AuditLog auditLog = new AuditLog();
            auditLog.setAppKey(properties.getAppKey());
            auditLog.setUserId(userInfo==null?null:userInfo.getUserId());
            auditLog.setUserName(userInfo==null?null:userInfo.getUserName());
            auditLog.setUserNick(userInfo==null?null:userInfo.getUserNick());
            auditLog.setClientIp(clientIp);
            auditLog.setModelDesc(StringUtil.isEmpty(auditModel.desc()) ? controller.getClass().getName() : auditModel.desc());
            auditLog.setApiDesc(StringUtil.isEmpty(auditApi.desc())?uri:auditApi.desc());
            auditLog.setUri(uri);
            auditLog.setMethod(controller.getClass().getName()+"#"+method.getName());
            auditLog.setCreateAt(createAt);
            auditLog.setCostMills(costMills);
            auditLog.setSuccess(success);
            auditLog.setExtList(AuditContext.getDiffList());
            AuditContext.clear();
            if(auditApi.isLogRequestParams()){
                Map<String, Object> paramSignature = getAuditRequestParams(paramNameValues, auditApi.ignoreParamClasses());
                Map<String, Object> paramExtractor = getAuditRequestParams(paramNameValues, auditApi.requestParamExtractor());
                auditLog.setParams(CollectionUtils.isEmpty(paramExtractor)?paramSignature:paramExtractor);
            }
            if(auditApi.isLogResponse()){
                auditLog.setResponse(responseResult);
            }
            // 异步执行后续操作
            ThreadPoolUtil.execute(()->{
                auditLogService.log(auditLog);
            });
        }
        return responseResult;
    }

    /**
     * 从请求参数中提取出登录的用户名
     *
     * */
    private String extractUserName(AuditApi auditApi, Object[] args, List<ParamNameValue> paramNameValues) {
        String userName = null;
        if(StringUtil.isEmpty(userName)){
            String userNameSpel = auditApi.userNameSpel();
            userName = extractUserNameBySpel(userNameSpel, paramNameValues);
        }
        if(StringUtil.isEmpty(userName)){
            Class<? extends UserNameExtractor>  userNameExtractorClass = auditApi.userNameExtractor();
            userName = extractUserNameByClass(userNameExtractorClass, args);
        }
        return userName;
    }

    /**
     * 使用Spel从请求参数中提取出登录的用户名
     *
     * */
    private String extractUserNameBySpel(String userNameSpel, List<ParamNameValue> paramNameValues) {
        if(StringUtil.isEmpty(userNameSpel) || CollectionUtils.isEmpty(paramNameValues)){
            return null;
        }
        try{
            EvaluationContext ctx = new StandardEvaluationContext();
            for(ParamNameValue paramNameValue : paramNameValues){
                ctx.setVariable(paramNameValue.getParamName(), paramNameValue.getParamValue());
            }
            ExpressionParser parser = new SpelExpressionParser();
            return parser.parseExpression(userNameSpel).getValue(ctx, String.class);
        }catch(Exception e){
            log.error("Spel表达式：{}错误", userNameSpel);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 使用回调从请求参数中提取出登录的用户名
     *
     * */
    private String extractUserNameByClass(Class<? extends UserNameExtractor> userNameExtractorClass, Object[] args) {
        if(userNameExtractorClass == null){
            return null;
        }
        if(userNameExtractorClass == UserNameExtractor.class){
            return null;
        }
        try{
            UserNameExtractor userNameExtractor =  userNameExtractorClass.newInstance();
            return userNameExtractor.extractUserName(args);
        }catch(Exception e){
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 判断是否启用audit，方法的优先级高与类的优先级
     *
     * */
    private boolean isAuditEnable(AuditModel auditModel, AuditApi auditApi, boolean isGet){
        if(auditModel == null && auditApi == null){
            return true;
        }else if(auditModel != null && auditApi == null){
            //默认不记录get
            return auditModel.enable() && !isGet;
        }else if(auditModel == null && auditApi != null){
            return auditApi.enable();
        }else if(auditModel != null && auditApi != null){
            return auditApi.enable();
        }else{
            throw new IllegalArgumentException("impossible");
        }
    }
    /**
     * 复制一份请求参数，防止controller方法内部会修改参数
     * */
    private Object copyArg(Object arg, Class[] ignoreClasses, List<String> basePackages) {
        if(arg == null){
            return arg;
        }
        Object argCopy = arg;
        Class argClass = arg.getClass();
        if(Collection.class.isAssignableFrom(argClass)){
            Collection copy = (Collection) BeanUtils.instantiateClass(argClass);
            Collection origin = (Collection)arg;
            copy.addAll(origin);
            argCopy = copy;
        }else if(Map.class.isAssignableFrom(argClass)){
            Map copy = (Map)BeanUtils.instantiateClass(argClass);
            Map origin = (Map)arg;
            copy.putAll(origin);
            argCopy = copy;
        }else if(isCandidateAuditParam(argClass, ignoreClasses) && isCandidatePackage(argClass.getPackage().getName(), basePackages)){
            String json = JSON.toJSONString(arg);
            Object copy = JSON.toJavaObject(JSON.parseObject(json), argClass);
            argCopy = copy;
        }
        return argCopy;
    }

    /**
     * 把请求参数按照名值对重新组织，value会copy一份
     * */
    private List<ParamNameValue> getAllParamNameValues(Method method, Object[] arguments, Class[] ignoreClasses, List<String> basePackages){
        DefaultParameterNameDiscoverer dpnd = new DefaultParameterNameDiscoverer();
        String[] parameterNames = dpnd.getParameterNames(method);
        List<ParamNameValue> pnvs = new ArrayList<>(arguments.length);
        for(int i=0; i<arguments.length; i++) {
            String parameterName = parameterNames[i];
            Object parameterValue = arguments[i];
            Class<?> parameterClass = parameterValue.getClass();
            pnvs.add(new ParamNameValue(parameterName, copyArg(parameterValue, ignoreClasses, basePackages), parameterClass.getName()));
        }
        return pnvs;
    }

    /**
     * 判断一个包是否在basePackage下面
     * */
    private boolean isCandidatePackage(String targetPackage, List<String> basePackages) {
        for(String basePackage : basePackages){
            if(targetPackage.startsWith(basePackage)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取要审计的请求参数，使用方法签名上的参数
     * */
    private Map<String, Object> getAuditRequestParams(List<ParamNameValue> pnvs, Class[] ignoreParamClasses){
        Map<String, Object> result = new HashMap<>(pnvs.size());
        for(int i=0; i<pnvs.size(); i++) {
            ParamNameValue pnv = pnvs.get(i);
            Class<?> parameterClass = pnv.paramValue.getClass();
            if(!isCandidateAuditParam(parameterClass, ignoreParamClasses)){
                continue;
            }
            result.put(pnv.getParamName(), pnv.getParamValue());
        }
        return result;
    }

    /**
     * 获取要审计的请求参数,使用RequestParamExtractor
     * */
    private Map<String, Object> getAuditRequestParams(List<ParamNameValue> pnvs, Class<? extends RequestParamExtractor> extractorClass){
        if(CollectionUtils.isEmpty(pnvs) || extractorClass == RequestParamExtractor.class){
            return null;
        }
        try{
            Object[] args = new Object[pnvs.size()];
            for(int i=0; i<pnvs.size(); i++){
                ParamNameValue pnv = pnvs.get(i);
                args[i] = pnv.getParamValue();
            }
            RequestParamExtractor extractor = extractorClass.newInstance();
            return extractor.extractRequestParams(args);
        }catch(Exception e){
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 判断是否是要审计的参数
     * */
    private boolean isCandidateAuditParam(Class<?> parameterClass, Class[] ignoreParamClasses) {
        if(HttpServletRequest.class.isAssignableFrom(parameterClass) ||
                HttpServletResponse.class.isAssignableFrom(parameterClass) ||
                MultipartFile.class.isAssignableFrom(parameterClass) ||
                Part.class.isAssignableFrom(parameterClass) ||
                Model.class.isAssignableFrom(parameterClass)){
            return false;
        }
        if(ignoreParamClasses == null || ignoreParamClasses.length <= 0){
            return true;
        }
        for(Class<?> ignoreClass : ignoreParamClasses){
            if(ignoreClass == null){
                continue;
            }
            if(ignoreClass.isAssignableFrom(parameterClass)){
                return false;
            }
        }
        return true;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class ParamNameValue{
        private String paramName;
        private Object paramValue;
        private String valueType;
    }

    @AuditModel
    public static class DummyClass{
        @AuditApi
        public void dummyMethod(String dummyParam){
        }
    }
}
