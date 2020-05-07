package com.github.xjs.auditlog.vo.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AuditEventVo {
    /**appKey*/
    private String appKey;
    /**用户ID*/
    private String userId;
    /**用户名*/
    private String userName;
    /**用户昵称*/
    private String userNick;
    /**客户端ip*/
    private String clientIp;
    /**类描述*/
    private String modelDesc;
    /**接口描述，默认是uri*/
    private String apiDesc;
    /**请求uri*/
    private String uri;
    /**请求方法，controller+method*/
    private String method;
    /**请求时间*/
    private Long createAt;
    /**请求参数， JSON*/
    private Map<String, Object> params;
    /**请求是否成功*/
    private Boolean success;
    /**请求耗时，毫秒*/
    private Integer costMills;
    /**响应结果， JSON*/
    private Map<String, Object> response;
    /**扩展信息， JSON*/
    private List<Diff> extList;

    @Data
    public static class Diff{
        private String key;
        private Object oldValue;
        private Object newValue;
    }
}
