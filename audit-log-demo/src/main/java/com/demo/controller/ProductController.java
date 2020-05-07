/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.controller;

import com.demo.vo.ResVo;
import com.github.xjs.auditlog.anno.AuditApi;
import com.github.xjs.auditlog.anno.AuditModel;
import com.github.xjs.auditlog.log.IAuditLogService;
import com.demo.domain.Product;
import com.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@AuditModel(desc="商品", enable = false)
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    ProductService prodService;

    @Autowired
    private IAuditLogService logService;

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

    @AuditApi(desc="添加商品", isLogResponse=true)
    @PostMapping("/add")
    public ResVo add(@RequestBody Product prod){
        prodService.insert(prod);
        return ResVo.ok(prod.getId());
    }

    @AuditApi(desc="更新商品", isLogResponse=true)
    @PostMapping("/update")
    public ResVo update(@RequestBody Product prod){
        int ret = prodService.update(prod);
        return ResVo.ok(ret > 0);
    }

    @AuditApi(desc="删除商品", isLogResponse=true)
    @PostMapping("/delete")
    public ResVo delete(@RequestBody Product prod){
        int ret = prodService.delete(prod);
        return ResVo.ok(ret > 0);
    }

}
