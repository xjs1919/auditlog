package com.github.xjs.auditlog.aop;


import com.github.xjs.auditlog.config.ActionAuditProperties;
import com.github.xjs.auditlog.log.IAuditLogService;
import com.github.xjs.auditlog.anno.AuditApi;
import com.github.xjs.auditlog.anno.AuditModel;
import com.github.xjs.auditlog.log.AuditLog;
import com.github.xjs.auditlog.user.AuditUserInfo;
import com.github.xjs.auditlog.user.IAuditUserService;
import com.github.xjs.auditlog.util.LogUtil;
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
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
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

    /**AOP*/
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) ||" +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public void requestMappingCut() {
    }

    @Around("requestMappingCut()")
    public Object aroundRequestMapping(ProceedingJoinPoint joinPoint) throws Throwable {
        Object args[] = joinPoint.getArgs();
        Object controller = joinPoint.getTarget();
        MethodSignature methodSignature = (MethodSignature)(joinPoint.getSignature());
        Method method = methodSignature.getMethod();
        //判断是否需要拦截的controller
        boolean isCandidateController = isCandidateClass(controller.getClass(), basePackages);
        if(!isCandidateController){
            return joinPoint.proceed(args);
        }
        //拿到类和方法上的注解
        AuditModel auditModel = AnnotatedElementUtils.getMergedAnnotation(controller.getClass(), AuditModel.class);
        AuditApi auditApi = AnnotatedElementUtils.getMergedAnnotation(method, AuditApi.class);
        //判断是否开启
        boolean enable = isAuditEnable(auditModel, auditApi);
        if(!enable){
            return joinPoint.proceed(args);
        }
        HttpServletRequest httpRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        auditModel = (auditModel==null?this.defaultAuditMolde:auditModel);
        auditApi = (auditApi==null?this.defaultAuditApi :auditApi);
        String uri = httpRequest.getRequestURI();
        String clientIp = WebUtil.getRemoteIP(httpRequest);
        long createAt = System.currentTimeMillis();
        AuditUserInfo userInfo = null;
        //说明是登录接口
        if(auditApi.isLogin()){
            //用户信息在方法执行完成以后填入
        }else{
            // 获取登录的用户
            userInfo = auditUserService.getUserInfo(httpRequest);
            if(userInfo == null){
                log.error("无法获取用户信息");
                return joinPoint.proceed(args);
            }
        }
        // 执行Controller方法
        Object argsCopy[] = copyArgs(args, basePackages);
        List<ParamNameValue> paramNameValues = getAllParamNameValues(method, argsCopy);
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
            if(auditApi.isLogin() && success){
                //回填用户信息
                userInfo = auditUserService.getUserInfo(httpRequest);
            }
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
                auditLog.setParams(getAuditRequestParams(paramNameValues, auditApi.ignoreParamClasses()));
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
     * 判断是否启用audit，方法的优先级高与类的优先级
     *
     * */
    private boolean isAuditEnable(AuditModel auditModel, AuditApi auditApi){
        if(auditModel == null && auditApi == null){
            return true;
        }else if(auditModel != null && auditApi == null){
            return auditModel.enable();
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
    private Object[] copyArgs(Object[] args, List<String> basePackages) {
        if(args == null || args.length <= 0){
            return args;
        }
        Object[] result = new Object[args.length];
        for(int i=0; i<args.length; i++){
            Object arg = args[i];
            Object argCopy = arg;
            Class argClass = arg.getClass();
            if(Collection.class.isAssignableFrom(argClass)){
                Collection copy = (Collection)BeanUtils.instantiateClass(argClass);
                Collection origin = (Collection)arg;
                copy.addAll(origin);
                argCopy = copy;
            }else if(Map.class.isAssignableFrom(argClass)){
                Map copy = (Map)BeanUtils.instantiateClass(argClass);
                Map origin = (Map)arg;
                copy.putAll(origin);
                argCopy = copy;
            }else if(isCandidatePackage(argClass.getPackage().getName(),basePackages)){
                //说明是项目中自定义的bean
                Object copy = BeanUtils.instantiateClass(argClass);
                BeanUtils.copyProperties(arg, copy);
                argCopy = copy;
            }
            result[i] = argCopy;
        }
        return result;
    }


    private List<ParamNameValue> getAllParamNameValues(Method method, Object[] arguments){
        DefaultParameterNameDiscoverer dpnd = new DefaultParameterNameDiscoverer();
        String[] parameterNames = dpnd.getParameterNames(method);
        List<ParamNameValue> pnvs = new ArrayList<>(arguments.length);
        for(int i=0; i<arguments.length; i++) {
            String parameterName = parameterNames[i];
            Object parameterValue = arguments[i];
            Class<?> parameterClass = parameterValue.getClass();
            pnvs.add(new ParamNameValue(parameterName, parameterValue, parameterClass.getName()));
        }
        return pnvs;
    }

    /**
     * 判断一个类是否在basePackage下面
     * */
    private boolean isCandidateClass(Class clazz, List<String> basePackages) {
        String packageName = clazz.getPackage().getName();
        return isCandidatePackage(packageName, basePackages);
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
     * 获取要审计的请求参数
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
