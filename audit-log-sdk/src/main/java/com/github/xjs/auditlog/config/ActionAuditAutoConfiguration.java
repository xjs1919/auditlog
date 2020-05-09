/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.config;

import com.github.xjs.auditlog.aop.AuditAspect;
import com.github.xjs.auditlog.cache.DefaultAuditCacheService;
import com.github.xjs.auditlog.cache.IAuditCacheService;
import com.github.xjs.auditlog.log.DefaultAuditLogService;
import com.github.xjs.auditlog.log.HttpAuditLogService;
import com.github.xjs.auditlog.log.IAuditLogService;
import com.github.xjs.auditlog.user.IAuditUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 *  配置类
 *
 * @date 2019/12/4 16:19
 **/
@Slf4j
@Configuration
@ConditionalOnProperty(value="audit.enable", havingValue="true")
@EnableConfigurationProperties(ActionAuditProperties.class)
public class ActionAuditAutoConfiguration{

    /**配置*/
    private ActionAuditProperties properties;
    /**用户服务*/
    private IAuditUserService auditUserService;

    public ActionAuditAutoConfiguration(ActionAuditProperties properties, IAuditUserService auditUserService){
        this.properties = properties;
        this.auditUserService = auditUserService;
    }

    /**
     * 缓存服务
     * */
    @Bean
    @ConditionalOnMissingBean(IAuditCacheService.class)
    public IAuditCacheService auditCacheService(){
        return new DefaultAuditCacheService();
    }

    /**
     * 日志服务
     * */
    @Bean
    @ConditionalOnMissingBean(IAuditLogService.class)
    @ConditionalOnProperty(value="audit.uploadUrl")
    @Order(Ordered.LOWEST_PRECEDENCE-2)
    public IAuditLogService auditLogService(RestTemplate restTemplate){
        return new HttpAuditLogService(properties, restTemplate);
    }

    /**
     * 默认的日志服务
     * */
    @Bean
    @ConditionalOnMissingBean(IAuditLogService.class)
    @Order(Ordered.LOWEST_PRECEDENCE-1)
    public IAuditLogService defaultAuditLogService(){
        return new DefaultAuditLogService();
    }

    /**
     * aop配置
     * */
    @Bean
    public AuditAspect auditAspect(IAuditUserService auditUserService, IAuditLogService auditLogService){
        return new AuditAspect(properties, auditUserService, auditLogService);
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(){
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectionRequestTimeout(10000);
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(10000);
        //连接池设置
        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
        // 连接池最大连接数
        poolingConnectionManager.setMaxTotal(100);
        // 每个主机的并发
        poolingConnectionManager.setDefaultMaxPerRoute(100);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setConnectionManager(poolingConnectionManager);
        CloseableHttpClient httpClient = httpClientBuilder.build();
        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }



}
