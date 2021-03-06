package com.ego.auth;

import com.ego.auth.entity.UserInfo;
import com.ego.auth.utils.JwtUtils;
import com.ego.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * 〈\〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/12
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
public class JwtTest {

    private static final String pubKeyPath = "C:\\tmp\\rsa\\rsa.pub";

    private static final String priKeyPath = "C:\\tmp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;
    /**
     * 注释@Before代码运行
     * @throws Exception
     */
    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "123123123sdfascvxhrghr3412");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU2MDczNzgyNH0.QwVd80EpVoa-DDdmuBBlmex1XKVCiUPLKwiA1za4NLvwfo2aT821Sm3_ZpAlMS7vvqYZOaozzoPSPGpd_MY13g4rMyv5joxmU87U2pw_bTqZzwXPwl_tl9N6L9TXPlAnw7-4aLFP8Gt9-7KkhzKqrkKWg5Hq4M7l-F7JyeIncl4";

        // 解析token
        UserInfo user = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}
