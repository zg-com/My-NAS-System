package com.nas.cloud.config;

import com.nas.cloud.service.UserDetailsServiceImpl;
import com.nas.cloud.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.firewall.HttpStatusRequestRejectedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
//过滤器用于检验前端传回的token是否是正常的
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils; //验卡

    @Autowired
    private UserDetailsServiceImpl userDetailsService;//档案管理员

    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException, IOException {

        //从请求头中找Authorization字段
        //前端发送请求的时候必须带上：Authorization：Bearer<token字符串>
        String authHeader = request.getHeader("Authorization");
        //新增对url自带token的支持
        if (authHeader == null && request.getParameter("token") != null) {
            authHeader = "Bearer " + request.getParameter("token");
        }

        String token = null;
        String username  = null;

        //检查一下有没有带Token，格式对不对，必须以“Bearer”来头，这是国际标准规范
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            //截取掉“Bearer ”一共7个字符，剩下的就是Token
            token = authHeader.substring(7);

            try{
                //提取用户名，如果过期了，或者是伪造的也会报错
                username = jwtUtils.extractUsername(token);

            }catch (Exception e){
                System.out.println("Token 解析警告：" + e.getMessage());
            }

            //核对身份,并且检查一下安全上下文中有没有检验过身份，没有检验的话就说明还没登陆
            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
                //取数据库中查询完整的档案，意义在于如果这个人在token过期之前被封号了或者改密码了也不能让进
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                //比对token
                if(jwtUtils.validateToken(token, userDetails.getUsername())){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,null,userDetails.getAuthorities()
                    );

                    //把请求的Ip地址等细节记录到通行证上（方便以后查日志）
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    //把通行证交给Security上下文
                    //这样就算放行了，后面Controller就可以通过SecurityContextHolder拿到用户信息
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

        }
        //放行
        filterChain.doFilter(request,response);
    }
}