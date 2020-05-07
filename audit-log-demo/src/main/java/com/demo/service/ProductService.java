/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.service;

import com.alibaba.fastjson.JSON;
import com.demo.dao.ProductMapper;
import com.demo.domain.Product;
import com.github.xjs.auditlog.aop.AuditContext;
import com.github.xjs.auditlog.log.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductMapper productMapper;

    public Product getById(Long id){
        return productMapper.selectById(id);
    }

    public List<Product> listAll() {
        return productMapper.listAll();
    }

    public Long insert(Product product){
        productMapper.insert(product);
        AuditContext.addDiff(AuditLog.Diff.ofNew("添加", JSON.toJSONString(product)));
        return product.getId();
    }

    public int update(Product product){
        Product old = getById(product.getId());
        AuditContext.addDiff(AuditLog.Diff.of("修改", JSON.toJSONString(old), JSON.toJSONString(product)));
        return productMapper.update(product);
    }

    public int delete(Product product){
        Product old = getById(product.getId());
        AuditContext.addDiff(AuditLog.Diff.ofOld("删除", JSON.toJSONString(old)));
        return productMapper.delete(product);
    }
}
