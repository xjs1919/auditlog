/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.service;

import com.alibaba.fastjson.JSON;
import com.github.xjs.auditlog.domain.AuditEvent;
import com.github.xjs.auditlog.vo.req.AuditEventVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * kafka consumer
 *
 * @date 2019/12/9 14:48
 **/
@Service
@Slf4j
public class KafkaConsumerService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @KafkaListener(topics = {"${spring.kafka.audit-topic}"})
    public void receive(String message){
        if(StringUtils.isEmpty(message)){
            return;
        }
        try{
            AuditEventVo auditLogDto = JSON.toJavaObject(JSON.parseObject(message), AuditEventVo.class);
            if(log.isDebugEnabled()){
                log.debug("消费者收到数据:{}", auditLogDto);
            }
            AuditEvent auditLog = convertToAuditEvent(auditLogDto);
            if(auditLog != null){
                mongoTemplate.save(auditLog);
            }
        }catch(Exception e){
            log.error("生产者数据格式错误:{}", message);
        }
    }

    private AuditEvent convertToAuditEvent(AuditEventVo auditEventVo){
        if(auditEventVo == null){
            return null;
        }
        AuditEvent auditEvent = new AuditEvent();
        BeanUtils.copyProperties(auditEventVo, auditEvent, "params", "response");
        Map<String, Object> params = auditEventVo.getParams();
        Map<String, Object> response = auditEventVo.getResponse();
        auditEvent.setParams(CollectionUtils.isEmpty(params)?null:JSON.toJSONString(params));
        auditEvent.setResponse(CollectionUtils.isEmpty(response)?null:JSON.toJSONString(response));
        return auditEvent;
    }

}
