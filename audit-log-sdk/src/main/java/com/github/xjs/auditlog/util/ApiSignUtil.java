/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.util;

import org.springframework.util.StringUtils;

/**
 * api签名帮助类
 *
 * @date 2019/12/5 15:32
 **/
public class ApiSignUtil {

    public static boolean isSignValid(String appSecret, long timestamp, String rnd, String sign) {
        if(StringUtils.isEmpty(appSecret) || StringUtils.isEmpty(sign)){
            return false;
        }
        String signCalc = MD5Util.encode(appSecret+timestamp+rnd);
        if(!signCalc.equals(sign)) {
            return false;
        }
        long now = System.currentTimeMillis();
        if(now - timestamp > 5L * 60 * 1000) {
            return false;
        }
        return true;
    }

    public static String createSign(String appSecret, long timestamp, String rnd){
        if(StringUtils.isEmpty(appSecret) ||StringUtils.isEmpty(rnd) ){
            return null;
        }
        return MD5Util.encode(appSecret+timestamp+rnd);
    }

}
