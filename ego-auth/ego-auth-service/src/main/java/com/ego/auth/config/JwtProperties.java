package com.ego.auth.config;

import com.ego.auth.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/12
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@Data
@ConfigurationProperties(prefix = "ego.jwt")
public class JwtProperties {
    private String secret; // 密钥

    private String pubKeyPath;// 公钥

    private String priKeyPath;// 私钥

    private int expire;// token过期时间

    private PublicKey publicKey; // 公钥

    private PrivateKey privateKey; // 私钥

    private String cookieName; //cookie名字

    private Integer cookieMaxAge; //cookie有效期 <秒>

    public JwtProperties() {
        System.out.println();
    }

    @PostConstruct
    public void init() {
        //如果没有就初始化公钥和私钥
        File pubKeyFile = new File(pubKeyPath);
        File priKeyFile = new File(priKeyPath);

        if(!pubKeyFile.exists() || !priKeyFile.exists())
        {
            try {
                RsaUtils.generateKey(pubKeyPath, priKeyPath, secret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            publicKey=RsaUtils.getPublicKey(pubKeyPath);
            privateKey=RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
