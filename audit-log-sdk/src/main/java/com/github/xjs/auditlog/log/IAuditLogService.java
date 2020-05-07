/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.log;

/**
 * 日志服务
 *
 * @author xujs@mamcharge.com
 * @date 2019/12/9 14:10
 **/
public interface IAuditLogService {

    /**
     * 记录日志
     * @param auditLog 数据
     * */
    public void log(AuditLog auditLog);

}
