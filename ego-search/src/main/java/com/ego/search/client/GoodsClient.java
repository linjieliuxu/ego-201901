package com.ego.search.client;

import com.ego.common.pojo.PageResult;
import com.ego.item.api.GoodsApi;
import com.ego.item.bo.SpuBO;
import com.ego.item.pojo.Sku;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient("item-service")
public interface GoodsClient extends GoodsApi {

}
