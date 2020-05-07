/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.github.xjs.auditlog.util;

import java.util.UUID;

/**
 * UUID帮助类
 *
 * @date 2019/12/23 18:25
 **/
public class UUIDUtil {
    public static String uuid(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
