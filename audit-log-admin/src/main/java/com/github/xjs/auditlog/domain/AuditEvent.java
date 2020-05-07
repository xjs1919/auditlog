/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.domain;

import com.github.xjs.auditlog.vo.req.AuditEventVo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("audit_log")
public class AuditEvent {
    @Id
    private String id;
    /**appKey*/
    private String appKey;
    /**用户ID*/
    private String userId;
    /**用户名*/
    private String userName;
    /**用户昵称*/
    private String userNick;
    /**客户端ip*/
    private String clientIp;
    /**类描述，默认是Controller名字*/
    private String modelDesc;
    /**接口描述，默认是uri*/
    private String apiDesc;
    /**请求uri*/
    private String uri;
    /**请求方法，controller+method*/
    private String method;
    /**请求时间*/
    private Long createAt;
    /**请求参数，JSON字符串*/
    private String params;
    /**请求是否成功*/
    private Boolean success;
    /**请求耗时，毫秒*/
    private Integer costMills;
    /**响应结果，JSON字符串*/
    private String response;
    /**扩展信息*/
    private List<AuditEventVo.Diff> extList;
}
