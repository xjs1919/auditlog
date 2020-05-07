package com.github.xjs.auditlog.log;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class AuditLog {
    /**appId*/
    private String appKey;
    /**用户ID*/
    private String userId;
    /**用户名*/
    private String userName;
    /**用户昵称*/
    private String userNick;
    /**客户端ip*/
    private String clientIp;
    /**controller描述*/
    private String modelDesc;
    /**接口描述*/
    private String apiDesc;
    /**请求uri*/
    private String uri;
    /**请求方法*/
    private String method;
    /**请求时间*/
    private Long createAt;
    /**请求参数，可以为空*/
    private Map<String, Object> params;
    /**请求是否成功，不代表业务是否成功，可以为空*/
    private Boolean success;
    /**请求耗时，毫秒*/
    private Integer costMills;
    /**响应结果，可以为空*/
    private Object response;
    /**扩展信息列表，可以为空*/
    private List<Diff> extList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Diff{
        private String key;
        private Object oldValue;
        private Object newValue;

        public static Diff ofNew(String key, Object newValue){
            return new Diff(key, null, newValue );
        }
        public static Diff ofOld(String key, Object oldValue){
            return new Diff(key, oldValue, null );
        }
        public static Diff of(String key, Object oldValue, Object newValue){
            return new Diff(key, oldValue, newValue );
        }
    }

}
