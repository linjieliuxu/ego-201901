package com.ego.item.mapper;

import com.ego.item.pojo.Sku;
import com.ego.item.pojo.Spu;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SkuMapper extends tk.mybatis.mapper.common.Mapper<Sku> {
    @Select("select sk.*,st.* from tb_sku sk left join tb_stock st on sk.id = st.sku_id where sk.spu_id = #{spuId}")
    @Results({
            //处理特殊字段映射
            @Result(property = "ownSpec",column ="own_spec"),
            //关联查询字段
            @Result(property = "stock.skuId",column ="sku_id"),
            @Result(property = "stock.stock",column ="stock")
    })
    List<Sku> selectBySpuId(@Param("spuId") Long spuId);
}
