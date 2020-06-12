/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.controller;

import com.demo.vo.CodeMsg;
import com.demo.vo.ResVo;
import com.github.xjs.auditlog.anno.AuditApi;
import com.github.xjs.auditlog.anno.AuditModel;
import com.github.xjs.auditlog.aop.UserNameExtractor;
import com.github.xjs.auditlog.user.AuditUserInfo;
import com.github.xjs.auditlog.util.UUIDUtil;
import com.github.xjs.auditlog.util.WebUtil;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;

@AuditModel(desc="登录", enable = false)
@RestController
@RequestMapping("/login")
public class LoginController {

    public static final String TOKEN_NAME = "token";
    public static ConcurrentHashMap<String, AuditUserInfo> users = new ConcurrentHashMap<String, AuditUserInfo>();

    public static class LoginUserNameExtractor implements UserNameExtractor {
        @Override
        public String extractUserName(Object[] args){
            LoginVo loginVo = (LoginVo)args[0];
            return loginVo.getUsername();
        }
    }

    @AuditApi(desc="用户登录", isLogin = true, userNameSpel = "#vo.username", userNameExtractor = LoginUserNameExtractor.class, isLogResponse = true)
    @PostMapping("/login")
    public ResVo login(@RequestBody LoginVo vo, HttpServletResponse response){
        String usr = vo.getUsername();
        String pwd = vo.getPassword();
        if("xjs".equals(usr) && "123456".equals(pwd)) {
            String token = UUIDUtil.uuid();
            users.put(token, new AuditUserInfo("1", "xjs", "张三"));
            WebUtil.addCookie(response, TOKEN_NAME, token, Integer.MAX_VALUE);
            return ResVo.ok(token);
        }else{
            return ResVo.fail(CodeMsg.LOGIN_ERROR);
        }
    }

    @Data
    public static class LoginVo{
        private String username;
        private String password;
    }
}
