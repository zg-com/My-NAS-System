package com.nas.cloud.controller;

import com.nas.cloud.entity.User;
import com.nas.cloud.repository.UserRepository;
import com.nas.cloud.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;//用来做登录校验

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;//加密器，用于给密码加密

    //登录接口
    @PostMapping("/login")
    public Map<String, Object> login (@RequestBody Map<String,String> params){
        String username = params.get("username");
        String password = params.get("password");

        //查库，对比一下密码，不对就会报错，程序中断
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username,password)
        );

        //走到这就说明登录成功了,此时 authentication中就已经包含了用户的详细信息了
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //成功了就发一张token
        String token = jwtUtils.generateToken(username);

        //查一下这个人的完整信息
        User user = userRepository.findByUsername(username);

        //封装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("code",200);
        result.put("msg", "登陆成功");
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username",user.getUsername());
        result.put("role",user.getRole());

        return result;
    }

    //注册接口
    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody User user){
        Map<String, Object> result = new HashMap<>();
        System.out.println("根据用户名查询结果为："+userRepository.findByUsername(user.getUsername()));
        //检查是否已被注册
        if(userRepository.findByUsername(user.getUsername()) != null) {
            System.out.println(userRepository.findByUsername(user.getUsername()));
            result.put("code",0);
            result.put("msg", "用户已存在");
            return result;
        }

        //密码加密
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        //设置默认角色
        if(user.getRole() == null || user.getRole().isEmpty()){
            user.setRole("ROLE_USER");
        }

        //保存
        userRepository.save(user);

        result.put("code",200);
        result.put("msg", "登陆成功");
        result.put("userId", user.getId());
        result.put("username",user.getUsername());
        return result;
    }
}
