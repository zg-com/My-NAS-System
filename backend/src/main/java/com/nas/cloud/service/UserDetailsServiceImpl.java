package com.nas.cloud.service;

import com.nas.cloud.entity.User;
import com.nas.cloud.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Spring Security不认识我们自己的User类，所以需要将我们的User类转换成Spring 的UserDetails
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private  UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        //查人
        User user = userRepository.findByUsername(username);
        //查不到抛异常
        if(user == null ){
            throw new UsernameNotFoundException("用户不存在" + username);
        }
        //处理角色权限，如果没设置角色权限，默认先给一个ROLRE_USER,普通用户
        String role = (user.getRole() == null || user.getRole().isEmpty()) ? "ROLE_USER" : user.getRole();
        //将字符串角色转换成认识的Authoriry对象
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(role)
        );
        //开始翻译（数据转换）
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities //处理好的权限
        );

    }

}
