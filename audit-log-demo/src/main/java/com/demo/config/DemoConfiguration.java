package com.demo.config;

import com.demo.controller.LoginController;
import com.github.xjs.auditlog.user.AuditUserInfo;
import com.github.xjs.auditlog.user.IAuditUserService;
import com.github.xjs.auditlog.util.WebUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class DemoConfiguration {

    @Bean
    public IAuditUserService auditUserService() {
        return new DemoAuditUserService();
    }

    public static class DemoAuditUserService implements IAuditUserService{
        @Override
        public AuditUserInfo getUserInfo(HttpServletRequest request) {
            String token = WebUtil.getCookieValue(request, LoginController.TOKEN_NAME);
            if(token == null){
                return null;
            }
            return LoginController.users.get(token);
        }
    }

}
