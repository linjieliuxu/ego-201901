package com.ego.cms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/10
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Data
@ConfigurationProperties(prefix = "ego.sms")
public class SmsProperties {

    String accessKeyId;

    String accessKeySecret;

    String signName;

    String verifyCodeTemplate;
}
