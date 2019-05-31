package com.ego.item.mapper;

import com.ego.item.pojo.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;

import java.util.List;

@Mapper
public interface CategoryMapper extends tk.mybatis.mapper.common.Mapper<Category>, SelectByIdListMapper<Category,Long> {
    @Select("select c.id,c.name,c.parent_id,c.is_parent,c.sort from tb_category c join tb_category_brand cb on c.id = cb.category_id where cb.brand_id = #{bid}")
    List<Category> queryCategegoryListByBid(@Param("bid") Long bid);
}
