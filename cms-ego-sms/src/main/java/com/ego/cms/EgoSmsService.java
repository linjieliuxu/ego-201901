package com.ego.cms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/10
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class EgoSmsService {
    public static void main(String[] args) {
        SpringApplication.run(EgoSmsService.class, args);
    }
}
