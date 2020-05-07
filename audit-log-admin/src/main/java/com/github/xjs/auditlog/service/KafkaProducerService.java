/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * kafka producer
 *
 * @date 2019/12/9 14:48
 **/
@Service
@Slf4j
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.audit-topic}")
    private String topic;

    public void produce(String message){
        if(StringUtils.isEmpty(message)){
            return;
        }
        kafkaTemplate.send(topic, message);
    }

}
