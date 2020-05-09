/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;


/**
 * 默认的日志服务
 *
 * @date 2019/12/9 14:19
 **/
@Slf4j
public class DefaultAuditLogService implements IAuditLogService{

    @Override
    public void log(AuditLog auditLog){
        if(auditLog == null){
            log.error("auditLog数据内容为空");
            return;
        }
        log.info(JSON.toJSONString(auditLog));
    }
}
