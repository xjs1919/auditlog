package com.github.xjs.auditlog.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Http接口帮助类
 *
 * @date 2019/12/6 9:54
 **/
@Slf4j
public class HttpUtil {

    /**
     * 调用post接口。<br/>
     * 把请求参数用json格式放在http消息体中传递给服务端，设置请求头为application/json
     * @param url 接口地址
     * @param reqParamBean 请求参数bean，可以是普通的javabean或者是JsonObbject
     * @param resDataClazz 响应内容的data部分对应的class
     * @return 响应内容的data部分
     *  */
    public static <T> T postJson(RestTemplate restTemplate, String url, Object reqParamBean, Class<T> resDataClazz){
        String params = "";
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/json;charset=UTF-8");
            if(reqParamBean instanceof  String){
                params = (String)reqParamBean;
            }else if(reqParamBean instanceof JSONObject){
                params = ((JSONObject) reqParamBean).toJSONString();
            }else{
                params = JSON.toJSONString(reqParamBean);
            }
            HttpEntity<String> entity = new HttpEntity<String>(params, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, entity, String.class);
            JSONObject result = JSON.parseObject(responseEntity.getBody());
            int errcode = result.getIntValue("errcode");
            String errmsg = result.getString("errmsg");
            if(errcode != 0){
                log.error("调用接口{}失败:{}", url, errmsg);
                return null;
            }else{
                if(resDataClazz == String.class){
                    return (T)result.getString("data");
                }if(resDataClazz == Boolean.class || resDataClazz == boolean.class){
                    return (T)result.getBoolean("data");
                }else{
                    JSONObject dataObj = result.getJSONObject("data");
                    return JSON.toJavaObject(dataObj, resDataClazz);
                }
            }
        }catch(Exception e){
            log.error("调用接口：{}异常，参数:{}，异常信息：{}", url, params, e);
            return null;
        }
    }
}
