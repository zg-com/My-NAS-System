package com.nas.cloud.config;

import com.nas.cloud.service.UserDetailsServiceImpl;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.awt.image.CropImageFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;//我们写好的过滤器

    @Autowired
    @Lazy //避免循环依赖报错，因为Service是依赖Config的，config又依赖Service
    private UserDetailsServiceImpl userDetailsService;//档案管理员

    //-----组件配置--------
    //配置密码加密器
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();//以后凡是涉及到密码比对，都用BCrypt算法
    }

    //配置认证提供者
    //连接“认证管理器”和“档案管理员”的桥梁
    //告诉管理器，查人用userDetailsServer，对比密码用passwordEncode
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    //配置日志认证管理器（authenticationManager）
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    //-----核心规则配置（securityFilterChain）---------------------
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //禁用CSRF跨站请求伪造防护
                .csrf(csrf -> csrf.disable())

                //配置访问权限规则（白名单 vs 黑名单）
                .authorizeHttpRequests(req->req
                        //白名单
                        .requestMatchers("/login","/register","/error").permitAll()

                        //其他所有接口必须登录认证后才能访问
                        .anyRequest().authenticated()
                )
                //设置Session策略为无状态，因为我们不用我们用的是jwt
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //指定认证提供者
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


}
