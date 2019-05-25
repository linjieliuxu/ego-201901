package com.ego.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/5/25
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableZuulProxy
public class EgoApiGateway {
    public static void main(String[] args) {
        SpringApplication.run(EgoApiGateway.class, args);
    }
}
