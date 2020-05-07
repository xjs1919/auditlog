package com.github.xjs.auditlog.log;

import com.alibaba.fastjson.JSON;
import com.github.xjs.auditlog.config.ActionAuditProperties;
import com.github.xjs.auditlog.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.io.File;

@Slf4j
public class HttpAuditLogService implements IAuditLogService {

    private ActionAuditProperties properties;
    private RestTemplate restTemplate;

    public HttpAuditLogService(ActionAuditProperties properties, RestTemplate restTemplate){
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public void log(final AuditLog auditLog) {
        if(auditLog == null){
            log.error("auditLog为空，无法上传");
            return;
        }
        String requestData = JSON.toJSONString(auditLog);
        LogUtil.debugOnlyMsg(log, ()->"客户端上传审计日志："+requestData);
        try{
            //请求服务端
            String postResult = HttpUtil.postJson(restTemplate, properties.getUploadUrl(), requestData, String.class);
            LogUtil.debugOnlyMsg(log, ()->"上传结果："+postResult);
            if(postResult == null){
                saveRequestToLocal(requestData);
            }
        }catch(Exception e){
            log.error(e.getMessage(), e);
            saveRequestToLocal(requestData);
        }
    }

    //写到本地文件
    private void saveRequestToLocal(String requestData){
        String fallBackFile = properties.getUploadFallBackFile();
        if(!StringUtil.isEmpty(fallBackFile)){
            File outFile = new File(fallBackFile);
            File dir = outFile.getParentFile();
            if(!dir.exists()){
                dir.mkdirs();
            }
            IOUtil.appendLineToFile(requestData, outFile);
        }
    }

}
