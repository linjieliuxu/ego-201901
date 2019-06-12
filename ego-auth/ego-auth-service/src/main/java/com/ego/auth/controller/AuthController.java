package com.ego.auth.controller;

import com.ego.auth.client.UserClient;
import com.ego.auth.config.JwtProperties;
import com.ego.auth.entity.UserInfo;
import com.ego.auth.utils.JwtUtils;
import com.ego.auth.utils.RsaUtils;
import com.ego.common.utils.CookieUtils;
import com.ego.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/12
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private UserClient userClient;

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestParam("username")String username,
            @RequestParam("password")String password,
            HttpServletResponse response, HttpServletRequest request
            )
    {
        //判断用户是否存在
        User user = userClient.queryUser(username, password).getBody();
        if(user==null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            //生成令牌
            String token = JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()), jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            //将令牌写入Cookie
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getCookieMaxAge(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }


    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(HttpServletRequest request,HttpServletResponse response)
    {
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        try {
            UserInfo userinfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());

            //刷新token
            //生成令牌
            token = JwtUtils.generateToken(userinfo, jwtProperties.getPrivateKey(), jwtProperties.getExpire());
            //将令牌写入Cookie
            CookieUtils.setCookie(request, response, jwtProperties.getCookieName(), token, jwtProperties.getCookieMaxAge(), true);

            return ResponseEntity.ok(userinfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
