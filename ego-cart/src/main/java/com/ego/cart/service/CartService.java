package com.ego.cart.service;

import com.ego.auth.entity.UserInfo;
import com.ego.cart.client.GoodsClient;
import com.ego.cart.interceptor.LoginInterceptor;
import com.ego.cart.pojo.Cart;
import com.ego.common.utils.JsonUtils;
import com.ego.item.pojo.Sku;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/12
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Service
public class CartService {

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    public void addCart(Cart cart) {
        //获取用户购物车
        UserInfo user = LoginInterceptor.getLoginUser();

        BoundHashOperations<String, Object, Object> carts = redisTemplate.boundHashOps("ego:cart:uid:" + user.getId());

        Long skuId = cart.getSkuId();
        Integer num = cart.getNum();
        if(carts.hasKey(cart.getSkuId().toString()))
        {
            String json = carts.get(skuId.toString()).toString();
            cart = JsonUtils.parse(json, Cart.class);
            cart.setNum(cart.getNum() + num);
        }
        else
        {
            // 不存在，新增购物车数据
            cart.setUserId(user.getId());
            // 其它商品信息， 需要查询商品服务
            Sku sku = this.goodsClient.querySkuById(skuId);
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(), ",")[0]);
            cart.setPrice(sku.getPrice());
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
        }
        // 将购物车数据写入redis
        carts.put(cart.getSkuId().toString(), JsonUtils.serialize(cart));
    }

    public List<Cart> queryCartList() {
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();

        // 判断是否存在购物车
        String key = "ego:cart:uid:" + user.getId();
        if(!this.redisTemplate.hasKey(key)){
            // 不存在，直接返回
            return null;
        }
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        List<Object> carts = hashOps.values();
        // 判断是否有数据
        if(CollectionUtils.isEmpty(carts)){
            return null;
        }
        // 查询购物车数据
        return carts.stream().map(o -> JsonUtils.parse(o.toString(), Cart.class)).collect(Collectors.toList());
    }

    public void updateNum(Long skuId, Integer num) {
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        String key = "ego:cart:uid:" + user.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        // 获取购物车
        String json = hashOps.get(skuId.toString()).toString();
        Cart cart = JsonUtils.parse(json, Cart.class);
        cart.setNum(num);
        // 写入购物车
        hashOps.put(skuId.toString(), JsonUtils.serialize(cart));
    }

    public void deleteCart(String skuId) {
        // 获取登录用户
        UserInfo user = LoginInterceptor.getLoginUser();
        String key = "ego:cart:uid:" + user.getId();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);
        hashOps.delete(skuId);
    }
}
