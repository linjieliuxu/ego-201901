package com.ego.auth.client;

import com.ego.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/11
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@FeignClient("user-service")
public interface UserClient extends UserApi {
}
