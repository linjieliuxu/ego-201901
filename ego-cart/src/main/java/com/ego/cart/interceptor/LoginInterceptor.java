package com.ego.cart.interceptor;

import com.ego.auth.entity.UserInfo;
import com.ego.auth.utils.JwtUtils;
import com.ego.cart.config.JwtProperties;
import com.ego.common.utils.CookieUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

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
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private JwtProperties jwtProperties;
    private final static ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //将用户信息存储到当前线程Threadloca中、
        String token = CookieUtils.getCookieValue(request, jwtProperties.getCookieName());
        if (StringUtils.isBlank(token)) {
            // 未登录,返回401
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        try
        {
                UserInfo userInfo = JwtUtils.getInfoFromToken(token, jwtProperties.getPublicKey());
                threadLocal.set(userInfo);
        }
        catch (Exception e)
        {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        return true;
    }


    public static UserInfo getLoginUser() {
        return threadLocal.get();
    }

}
