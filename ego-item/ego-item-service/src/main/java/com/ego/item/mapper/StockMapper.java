package com.ego.item.mapper;

import com.ego.item.pojo.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface StockMapper extends tk.mybatis.mapper.common.Mapper<Stock> {
    @Update("update tb_stock set stock = stock - #{num} where sku_id = #{skuId} and stock >= #{num}")
    int decreaseStock(@Param("skuId") Long skuId, @Param("num") Integer num);


    @Update("update tb_stock set seckill_stock = seckill_stock - #{num} where sku_id = #{skuId} and seckill_stock >= #{num}")
    int decreaseSeckillStock(@Param("skuId") Long skuId, @Param("num") Integer num);
}
