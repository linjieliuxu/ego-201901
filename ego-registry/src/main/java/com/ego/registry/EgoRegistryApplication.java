package com.ego.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

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
@EnableEurekaServer
public class EgoRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(EgoRegistryApplication.class, args);
    }
}
