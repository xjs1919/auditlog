/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 行为审计相关配置属性
 *
 * @date 2019/12/4 16:20
 **/
@ConfigurationProperties(prefix = "audit")
@Data
public class ActionAuditProperties {

    /**分配的app key*/
    private String appKey;

    /**分配的app secfret*/
    private String appSecret;

    /**是否启用审计*/
    private Boolean enable;

    /**从权限中心获取用户信息*/
    private String authUrl;

    /**上传audit日志*/
    private String uploadUrl;

    /**上传audit日志*/
    private String uploadFallBackFile;
}
