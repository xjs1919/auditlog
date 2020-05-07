/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.rest;

import com.alibaba.fastjson.JSON;
import com.github.xjs.auditlog.service.KafkaProducerService;
import com.github.xjs.auditlog.vo.common.ResVo;
import com.github.xjs.auditlog.vo.req.AuditEventVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 接收sdk的上传请求
 *
 **/
@Slf4j
@RestController
@RequestMapping("/api")
public class AuditApiController {

    @Autowired
    private KafkaProducerService producerService;

    /**
     * 客户端上传审计日志
     * */
    @PostMapping("/upload_audit")
    public ResVo info(@Valid @RequestBody AuditEventVo vo){
        String data = JSON.toJSONString(vo);
        if(log.isDebugEnabled()){
            log.debug("服务端收到客户端上传的数据：{}", data);
        }
        //写入kafka
        producerService.produce(data);
        return ResVo.ok("success");
    }

}
