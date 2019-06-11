package com.ego.user.api;

import com.ego.user.pojo.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;

/**
 * 〈〉
 *
 * @author coach tam
 * @email 327395128@qq.com
 * @create 2019/6/11
 * @since 1.0.0
 * 〈坚持灵活 灵活坚持〉
 */
public interface UserApi {

    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> check(@PathVariable("data") String data, @PathVariable("type")Integer type);

    @PostMapping("/send")
    public ResponseEntity<Boolean> send(@RequestParam("phone") String phone);

    @PostMapping("/register")
    public ResponseEntity<Boolean> register(@Valid User user, @RequestParam("code") String code);

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseEntity<User> queryUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password
    );
}
