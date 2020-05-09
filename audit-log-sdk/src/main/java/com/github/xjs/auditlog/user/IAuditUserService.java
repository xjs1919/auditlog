/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.user;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务
 * @date 2019/12/4 16:50
 **/
public interface IAuditUserService {

    /**
     * 从http request中获取用户相关信息
     *
     * @param request
     * */
    AuditUserInfo getUserInfo(HttpServletRequest request);
}
