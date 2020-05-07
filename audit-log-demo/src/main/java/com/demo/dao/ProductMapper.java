/** 
 * copyright(c) 2019-2029 mamcharge.com
 */
 
package com.demo.dao;

import com.demo.domain.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    /**根据id查询*/
    Product selectById(@Param("id")Long id);

    /**查询所有*/
    List<Product> listAll();


    /**根据ids查询*/
    List<Product> listByIds(List<Long> ids);

    /**
     * 更新商品信息
     * */
    public int update(Product product);

    /**
     * 插入商品信息
     * */
    public int insert(Product product);

    /**
     * 删除商品信息
     * */
    public int delete(Product product);

    /**
     * 批量插入商品信息
     * */
    public int batchInsert(List<Product> products);

    /**
     * 批量插入商品信息
     * */
    public int batchInsert2(@Param("list") List<Product> products);

    /**
     * 批量删除商品
     * */
    int deleteBatch(List<Long> ids);

    /**
     * 批量删除商品
     * */
    int deleteBatch2(@Param("list") List<Product> ps);

    /**
     * 批量更新商品
     * */
    int updateBatch(List<Product> ps);

    /**
     * 批量更新商品
     * */
    int updateBatch2(@Param("list")List<Product> ps);

}