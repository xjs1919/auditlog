/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.controller;

import com.alibaba.fastjson.JSON;
import com.demo.vo.ResVo;
import com.github.xjs.auditlog.anno.AuditApi;
import com.github.xjs.auditlog.anno.AuditModel;
import com.github.xjs.auditlog.aop.RequestParamExtractor;
import com.github.xjs.auditlog.log.IAuditLogService;
import com.demo.domain.Product;
import com.demo.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AuditModel(desc="商品", enable = false)
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    ProductService prodService;

    @GetMapping("/info")
    public ResVo info(@Valid Long id){
        Product product = prodService.getById(id);
        return ResVo.ok(product);
    }

    @GetMapping("/list")
    public ResVo list(HttpServletRequest request){
        List<Product> products = prodService.listAll();
        return ResVo.ok(products);
    }

    @AuditApi(desc="添加商品", requestParamExtractor = AddRequestParamExtractor.class)
    @PostMapping("/add")
    public ResVo add(HttpServletRequest request){
        Product prod = product(request);
        request.setAttribute("prod", prod);
        prodService.insert(prod);
        return ResVo.ok(prod.getId());
    }

    @AuditApi(desc="更新商品")
    @PostMapping("/update")
    public ResVo update(@RequestBody Product prod){
        int ret = prodService.update(prod);
        return ResVo.ok(ret > 0);
    }

    @AuditApi(desc="删除商品")
    @PostMapping("/delete")
    public ResVo delete(@RequestBody Product prod){
        int ret = prodService.delete(prod);
        return ResVo.ok(ret > 0);
    }


    private static Product product(HttpServletRequest request){
        try{
            InputStream in = request.getInputStream();
            int len = 0;
            byte[] buff = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while((len = in.read(buff)) >= 0){
                out.write(buff, 0 , len);
            }
            out.close();
            in.close();
            return JSON.toJavaObject(JSON.parseObject(new String(out.toByteArray())), Product.class);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static class AddRequestParamExtractor implements RequestParamExtractor{
        @Override
        public Map<String, Object> extractRequestParams(Object[] args) {
            HttpServletRequest request = (HttpServletRequest)args[0];
            Product p = (Product)request.getAttribute("prod");
            if(p == null){
                return null;
            }
            Map<String, Object> map = new HashMap<>();
            map.put("prodname", p.getProdName());
            map.put("prodTitle", p.getProdTitle());
            map.put("prodPrice", p.getProdPrice());
            map.put("prodDetail", p.getProdDetail());
            return map;
        }
    }
}
