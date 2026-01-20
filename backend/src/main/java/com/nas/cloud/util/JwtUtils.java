package com.nas.cloud.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoder;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

//JWT工具类，负责生成token，验证token以及从token中提取信息
@Component
public class JwtUtils {

    //1、设置密钥（secretKey）
    public static final String SECRET = "2005046B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    //2、发卡，此处留意个简单接口只传一个用户名
    public String generateToken(String username){
        Map<String, Object> claims  = new HashMap<>();
        //claims主要是实现你要往卡里存的额外信息，比如昵称，权限啥的。结构就是 键值对：值
        return createToken(claims,username);
    }

    //2、真正的造卡逻辑
    private String createToken(Map<String,Object> claims,String subject){
        //获取当前时间的毫秒数
        long now = System.currentTimeMillis();
        //设置过期时间，单位是毫秒
        long expirationTime = now + 1000*60*60*24*7;//7天

        return Jwts.builder() //拿出造卡机
                .setClaims(claims) //放入自定义信息
                .setSubject(subject)//放入主题，这里目前指的是用户名
                .setIssuedAt(new Date(now)) // 打印发卡日期
                .setExpiration(new Date(expirationTime))//打印过期日期
                .signWith(getSignKey(),SignatureAlgorithm.HS256)//盖章！，用密钥和HS256算法加密签名
                .compact(); //压制成字符串
    }
    //2、把字符串的密钥转换成加密算法能识别的Key对象
    private Key getSignKey(){
        //因为SECRET是字符串，要先解码成字节数组
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        //使用HMAC-SHA 算法生成密钥对象
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //3、验卡，Token是否有效，token是前端传来的令牌，username是数据库查出来的真实用户名
    public boolean validateToken(String token, String username) {
        //从token中读出来的用户名
        final String usernameInToken = extractUsername(token);
        //比对 卡里的名字和真实名字是不是一样的，卡有没有过期
        return (usernameInToken.equals(username) && ! isTokenExpired(token));
    }
    //3、检查Token是不是过期了
    private  Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date()); //获取过期的时间，判断一下是不是在当前时间之前
    }
    //3、提取过期时间
    private Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    //3、提取token中的信息
    //3、提取用户名
    public String extractUsername(String token){
           return extractClaim(token,Claims::getSubject); //后面的claims。。。是一个方法引用，表示我熬获取subject字段
    }

    //3、泛型方法<T> 是一个通用的取值器
    //传过来token再告诉我想要什么字段，就能给解析出来
    public <T> T extractClaim(String token,Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);//先全部读出来
        return claimsResolver.apply(claims);//然后应用你想要的操作
    }

    //3、把加密字符串还原成Claims对象
    private Claims extractAllClaims(String token){
        return Jwts.parserBuilder() //拿出验卡机
                .setSigningKey(getSignKey())//放入我们自己的密钥
                .build()
                .parseClaimsJws(token)//解析前端传入的token看看是不是被篡改了，篡改了就是解析出来的密钥和我们传入的密钥不对，就会直接报错
                .getBody();//拿到卡片主体的内容
    }

}
