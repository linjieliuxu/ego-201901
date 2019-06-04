package com.ego.search.service;

import com.ego.common.pojo.PageResult;
import com.ego.item.bo.SpuBO;
import com.ego.item.pojo.Sku;
import com.ego.search.bo.SearchRequest;
import com.ego.search.client.BrandClient;
import com.ego.search.client.CategoryClient;
import com.ego.search.client.GoodsClient;
import com.ego.search.dao.GoodsRespository;
import com.ego.search.pojo.Goods;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/3
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Slf4j
@Service
public class SearchService {
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GoodsRespository goodsRespository;

    public Goods buildGoods(SpuBO spuBO) {
        Goods goods = new Goods();
        try
        {
            goods.setSubTitle(spuBO.getSubTitle());
            List<Sku> skuList = goodsClient.querySkuListBySpuId(spuBO.getId()).getBody();
            //skuList --> json str
            String skus = mapper.writeValueAsString(skuList);
            goods.setSkus(skus);
            List<Long> prices = new ArrayList<>();
            skuList.forEach(sku->{
                prices.add(sku.getPrice());
            });
            goods.setPrice(prices);
            goods.setCreateTime(spuBO.getCreateTime());
            goods.setCid1(spuBO.getCid1());
            goods.setCid2(spuBO.getCid2());
            goods.setCid3(spuBO.getCid3());
            goods.setBrandId(spuBO.getBrandId());
            //标题  类别  品牌
            String cnames = categoryClient.queryNamesByCids(Arrays.asList(spuBO.getCid1(), spuBO.getCid2(), spuBO.getCid3())).getBody();
            String bname = brandClient.queryBrandByBid(spuBO.getBrandId()).getBody().getName();
            goods.setAll(spuBO.getTitle()  + " " +cnames +" "+bname);

            //可以用来搜索的动态属性 specs<String,Object>
            Map<String, Object> specs = new HashMap<>();

            //获取specifications --> List<Map<String,Object>>  -->循环遍历每个params --> seachable:true-->存入specs中
            String specifications = goodsClient.querySpuDetailBySpuId(spuBO.getId()).getBody().getSpecifications();
            List<Map<String,Object>> specList = mapper.readValue(specifications,new TypeReference<List<Map<String,Object>>>(){});

            specList.forEach(spec->{
                    List<Map<String,Object>> params = (List<Map<String,Object>>)spec.get("params");
                    params.forEach(param->{
                        if((boolean)param.get("global"))
                        {
                            if((boolean)param.get("searchable"))
                            {
                                specs.put(param.get("k").toString(),param.get("v"));
                            }
                        }
                    });
            });

            goods.setSpecs(specs);

            goods.setId(spuBO.getId());

        }catch (Exception e)
        {
            log.error("spu转goods发生错误:{}",e.getMessage());
        }

        return goods;
    }

    public PageResult<Goods> search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        Integer page = searchRequest.getPage();
        if (StringUtils.isBlank(key)) {
            return  null;
        }
        NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder();

        //指定字段查询
        searchQuery.withSourceFilter(new FetchSourceFilter(
                new String[]{"id","skus","subTitle"}, null));
        //分词查询 all
        searchQuery.withQuery(QueryBuilders.matchQuery("all",key));

        //分页查询
        searchQuery.withPageable(PageRequest.of(page-1,searchRequest.getSize()));

        Page<Goods> pageInfo = goodsRespository.search(searchQuery.build());


        return new PageResult<>(pageInfo.getTotalElements(),Long.valueOf(pageInfo.getTotalPages()),pageInfo.getContent());
    }
}
