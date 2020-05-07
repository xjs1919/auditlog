/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * user info
 *
 * @date 2019/12/4 16:51
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuditUserInfo {
    private String userId;
    private String userName;
    private String userNick;
}
