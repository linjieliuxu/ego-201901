package com.ego.user.controller;

import com.ego.user.pojo.User;
import com.ego.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
@RestController
public class UserController {
    @Autowired
    private UserService userService;
    //http://api.ego.com/api/user/check/18180692791/2
    @GetMapping("/check/{data}/{type}")
    public ResponseEntity<Boolean> check(@PathVariable("data") String data,@PathVariable("type")Integer type)
    {
        Boolean isOk = userService.check(data, type);
        if(isOk==null)
        {
            return  ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(!isOk);
    }

    @PostMapping("/send")
    public ResponseEntity<Boolean> send(@RequestParam("phone") String phone)
    {
        Boolean isOk = userService.sendMessage(phone);

        return ResponseEntity.ok(isOk);
    }

    @PostMapping("/register")
    public ResponseEntity<Boolean> register(@Valid User user, @RequestParam("code") String code) {
        Boolean isOk = userService.register(user, code);
        if(!isOk)
        {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

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
    ) {
        User user = this.userService.queryUser(username, password);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(user);
    }
}
