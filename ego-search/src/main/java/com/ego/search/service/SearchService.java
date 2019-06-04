package com.ego.search.service;

import com.ego.common.pojo.PageResult;
import com.ego.common.utils.NumberUtils;
import com.ego.item.api.SpecApi;
import com.ego.item.bo.SpuBO;
import com.ego.item.pojo.Brand;
import com.ego.item.pojo.Category;
import com.ego.item.pojo.Sku;
import com.ego.search.bo.SearchRequest;
import com.ego.search.bo.SearchResult;
import com.ego.search.client.BrandClient;
import com.ego.search.client.CategoryClient;
import com.ego.search.client.GoodsClient;
import com.ego.search.client.SpecClient;
import com.ego.search.dao.GoodsRespository;
import com.ego.search.pojo.Goods;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.InternalHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregator;
import org.elasticsearch.search.aggregations.metrics.stats.InternalStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private SpecClient specClient;
    @Autowired
    private BrandClient brandClient;

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GoodsRespository goodsRespository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public Goods buildGoods(SpuBO spuBO) {
        Goods goods = new Goods();
        try
        {
            goods.setSubTitle(spuBO.getSubTitle());
            List<Sku> skuList = goodsClient.querySkuListBySpuId(spuBO.getId()).getBody();
            if(skuList==null||skuList.size()==0)
            {
                return null;
            }
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
            System.out.println("`````````````"+spuBO.getId());
            List<Map<String,Object>> specList = mapper.readValue(specifications,new TypeReference<List<Map<String,Object>>>(){});

            specList.forEach(spec->{
                    List<Map<String,Object>> params = (List<Map<String,Object>>)spec.get("params");
                    params.forEach(param->{
//                        if((boolean)param.get("global"))
//                        {
                            if((boolean)param.get("searchable"))
                            {
                                if(param.get("v")!=null)
                                {
                                    specs.put(param.get("k").toString(),param.get("v"));
                                }
                                else if(param.get("options")!=null)
                                {
                                    List<String> options = (List<String>)param.get("options");
                                    specs.put(param.get("k").toString(),options);
                                }
                            }

//                        }
                    });
            });

            goods.setSpecs(specs);

            goods.setId(spuBO.getId());

        }catch (Exception e)
        {
            e.printStackTrace();
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
        QueryBuilder basicQuery =  buildBasicQueryWithFilter(searchRequest);
        searchQuery.withQuery(basicQuery);

        //分页查询
        searchQuery.withPageable(PageRequest.of(page-1,searchRequest.getSize()));

        //添加聚合
        searchQuery.addAggregation(AggregationBuilders.terms("categories").field("cid3"));
        searchQuery.addAggregation(AggregationBuilders.terms("brands").field("brandId"));

        //执行查询
        AggregatedPage<Goods> pageInfo = (AggregatedPage)goodsRespository.search(searchQuery.build());

        //查询分类聚合结果
        List<Category> categories = getCategoryAggResult(pageInfo);

        //查询品牌聚合结果
        List<Brand> brands = getBrandAggResult(pageInfo);


        //聚合其他规格参数(这些都是动态参数，不能像category和brand一样 直接指定字段，因此需要先知道该类别有哪些规格参数)
        List<Map<String, Object>> specs = getSpecs(categories.get(0),basicQuery);
        return new SearchResult(pageInfo.getTotalElements(),Long.valueOf(pageInfo.getTotalPages()),pageInfo.getContent(),categories,brands,specs);
    }

    /**
     * 构建带过滤条件的基本查询
     * @param searchRequest
     * @return
     */
    private QueryBuilder buildBasicQueryWithFilter(SearchRequest searchRequest) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));
        //过滤条件构造器
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        //整理过滤条件
        Map<String,String> filter = searchRequest.getFilter();
        for (Map.Entry<String,String> entry : filter.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String regex = "^(\\d+\\.?\\d*)-(\\d+\\.?\\d*)$";
            if (!"key".equals(key)) {
                if ("price".equals(key)){
                    if (!value.contains("元以上")) {
                        String[] nums = StringUtils.substringBefore(value, "元").split("-");
                        filterQueryBuilder.must(QueryBuilders.rangeQuery(key).gte(Double.valueOf(nums[0]) * 100).lt(Double.valueOf(nums[1]) * 100));
                    }else {
                        String num = StringUtils.substringBefore(value,"元以上");
                        filterQueryBuilder.must(QueryBuilders.rangeQuery(key).gte(Double.valueOf(num)*100));
                    }
                }else {
                    if (value.matches(regex)) {
                        Double[] nums = NumberUtils.searchNumber(value, regex);
                        //数值类型进行范围查询   lt:小于  gte:大于等于
                        filterQueryBuilder.must(QueryBuilders.rangeQuery("specs." + key).gte(nums[0]).lt(nums[1]));
                    } else {
                        //商品分类和品牌要特殊处理
                        if (key.equals("分类"))
                        {
                            key = "cid3";
                        }
                        else if(key.equals("品牌"))
                        {
                            key = "brandId";
                        }
                        else{
                            key = "specs." + key + ".keyword";
                        }
                        //字符串类型，进行term查询
                        filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
                    }
                }
            } else {
                break;
            }
        }
        //添加过滤条件
        queryBuilder.filter(filterQueryBuilder);
        return queryBuilder;
    }

    /**
     * 默认取第一个类别规格参数
     * @param category
     * @param basicQuery
     * @return
     */
    private List<Map<String, Object>> getSpecs(Category category, QueryBuilder basicQuery) {
        List<Map<String, Object>> result = new ArrayList<>();
        try
        {
            //根据cid查询规格json
            String specJson = specClient.querySpecificationByCategoryId(category.getId()).getBody();
            //将json转list<Map<String,Object>> 方便解析
            List<Map<String, Object>> specs = mapper.readValue(specJson, new TypeReference<List<Map<String, Object>>>() {
            });
            //区分规格参数为字符串 和  数字
            Set<String> strSpecs = new HashSet<>();
            Map<String, String> numSpecs = new HashMap<>();
            specs.forEach(spec->{
                List<Map<String,Object>> params = (List<Map<String,Object>>)spec.get("params");
                params.forEach(param->{
                    String k = (String)param.get("k");
                    if((boolean)param.get("searchable"))
                    {
                        if(param.get("numerical")!=null&&(boolean)param.get("numerical"))
                        {
                            numSpecs.put(k,param.get("unit").toString());
                        }
                        else
                        {
                            //字符串直接保存(词条聚合)
                            strSpecs.add(k);
                        }
                    }

                });
            });


                //数字需要记录单位 以及计算 间隔 (阶梯聚合)
            Map<String, Double> numSepcInterval = getNumSpecInterval(category.getId(),numSpecs);

            //准备聚合查询
            NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
            nativeSearchQueryBuilder.withQuery(basicQuery);
                //字符串聚合条件
            strSpecs.forEach(spec->{
                nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms(spec).field("specs." + spec + ".keyword"));
            });
                //数字聚合条件
            numSpecs.keySet().forEach(spec->{
                nativeSearchQueryBuilder.addAggregation(AggregationBuilders.histogram(spec).field("specs."+spec).interval(numSepcInterval.get(spec)).minDocCount(1));
            });

            Map<String, Aggregation> aggResultMap = this.elasticsearchTemplate.query(nativeSearchQueryBuilder.build(), searchResponse -> searchResponse.getAggregations().asMap());
            //解析聚合结果
                //字符聚合结果
                strSpecs.forEach(spec->{
                    StringTerms agg  = (StringTerms)aggResultMap.get(spec);
                    Map<String, Object> map = new HashMap<>();

                    map.put("k", spec);
                    map.put("options", agg.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList()));
                    result.add(map);
                });
                //数字聚合结果
                numSpecs.keySet().forEach(spec->{
                    Map<String, Object> map = new HashMap<>();
                    map.put("k",spec);
                    map.put("unit", numSpecs.get(spec));

                    InternalHistogram agg = (InternalHistogram)aggResultMap.get(spec);
                    map.put("options", agg.getBuckets().stream().map(bucket -> {
                        Double begin = (Double) bucket.getKey();
                        Double end = begin + numSepcInterval.get(spec);
                        if (NumberUtils.isInt(begin)&&NumberUtils.isInt(end)) {
                            return begin + "-" + end;
                        } else {
                            return NumberUtils.scale(begin, 2) + "-" + NumberUtils.scale(end, 2);
                        }
                    }).collect(Collectors.toList()));

                    result.add(map);
                });

            //封装返回数据
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

    private Map<String, Double> getNumSpecInterval(Long cid3,Map<String, String> numSpecs) {
//        Map<String, Double> result = new HashMap<>();
//
//        //间隔计算 需要 min max sum
//        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
//
//        searchQueryBuilder.withQuery(QueryBuilders.matchQuery("cid3",cid3.toString())).withSourceFilter(new FetchSourceFilter(new String[]{""},null)).withPageable(PageRequest.of(0,1));
//
////        numSpecs.forEach((k,v)->{
////            searchQueryBuilder.addAggregation(AggregationBuilders.stats(k).field("specs."+k));
////        });
//        //添加stats类型的聚合,同时返回avg、max、min、sum、count等
//        for (String key : numSpecs.keySet()){
//            searchQueryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
//        }
//
//        Map<String, Aggregation> aggMap = elasticsearchTemplate.query(searchQueryBuilder.build(), searchResponse -> searchResponse.getAggregations().asMap());
//
//        numSpecs.forEach((k,v)->{
//            InternalStats stats = (InternalStats)aggMap.get(k);
//            double interval = NumberUtils.getInterval(stats.getMin(), stats.getMax(), stats.getSum());
//            result.put(k, interval);
//        });
//        return result;

        Map<String,Double> numbericalSpecs = new HashMap<>();
//准备查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
//不查询任何数据
//        queryBuilder.withQuery(QueryBuilders.termQuery("cid3",cid3.toString())).withSourceFilter(new
//                FetchSourceFilter(new String[]{""},null)).withPageable(PageRequest.of(0,1));
//添加stats类型的聚合,同时返回avg、max、min、sum、count等
        for (String key : numSpecs.keySet()){
            queryBuilder.addAggregation(AggregationBuilders.stats(key).field("specs." + key));
        }
        Map<String,Aggregation> aggregationMap =
                this.elasticsearchTemplate.query(queryBuilder.build(),
                        searchResponse -> searchResponse.getAggregations().asMap()
                );
        for (String key : numSpecs.keySet()){
            InternalStats stats = (InternalStats) aggregationMap.get(key);
            double interval =
                    NumberUtils.getInterval(stats.getMin(),stats.getMax(),stats.getSum());
            numbericalSpecs.put(key,interval);
        }
        return numbericalSpecs;

    }

    private List<Brand> getBrandAggResult(AggregatedPage<Goods> pageInfo) {
        LongTerms categoriesAgg = (LongTerms)pageInfo.getAggregation("brands");
        List<Long> brandIds = categoriesAgg.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<Brand> brands = brandClient.queryBrandListByIds(brandIds).getBody();
        return brands;
    }

    private List<Category> getCategoryAggResult(AggregatedPage<Goods> pageInfo) {

        LongTerms categoriesAgg = (LongTerms) pageInfo.getAggregation("categories");
        List<Long> categoryIds = categoriesAgg.getBuckets().stream().map(bucket -> bucket.getKeyAsNumber().longValue()).collect(Collectors.toList());
        List<String> categoryNames = Arrays.asList(categoryClient.queryNamesByCids(categoryIds).getBody().split(","));
        AtomicInteger i = new AtomicInteger();
        List<Category> categories = categoryNames.stream().map(name -> {
            Category category = new Category();
            category.setId(categoryIds.get(i.getAndIncrement()));
            category.setName(name);
            return category;
        }).collect(Collectors.toList());

        return categories;
    }
}
