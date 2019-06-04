package com.ego.search;

import com.ego.common.pojo.PageResult;
import com.ego.item.bo.SpuBO;
import com.ego.search.client.GoodsClient;
import com.ego.search.dao.GoodsRespository;
import com.ego.search.pojo.Goods;
import com.ego.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/3
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ImportDataTest {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRespository goodsRespository;

    @Autowired
    private SearchService searchService;
    @Test
    public void createIndex(){
        // 创建索引
        this.elasticsearchTemplate.createIndex(Goods.class);
        // 配置映射
        this.elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void testImportData(){
        //将mysql中的在售spu数据导入到es中

        int size = 0;
        int page = 1;
        do {
            //分批次导入数据
            PageResult<SpuBO> result = goodsClient.page("",true,page++,10).getBody();
            List<SpuBO> items = result.getItems();
            size = items.size();

            List<Goods> goodsList = new ArrayList<>();
            //将result中的list数据 保存到 es中
            items.forEach(spuBO -> {
                //spuBo --> goods
                Goods goods = searchService.buildGoods(spuBO);

                goodsList.add(goods);
            });

            goodsRespository.saveAll(goodsList);

        }while (size == 10);

    }
}
