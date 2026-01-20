package com.nas.cloud.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity //告诉JPA这是一个实体类，请帮我在数据库中生成对应的表
@Table(name = "nas_user") //指定表名
@Data //Lombok注解：自动生成getter，setter，toString等代码
public class User {
    @Id //声明主键
    @GeneratedValue(strategy = GenerationType.IDENTITY)//声明主键自增(1,2,3...)
    private long id;
    //对应数据库中的username字段
    @Column(unique = true,nullable = false) //不能重复，不能没有
    private String username;//用户名
    //对应数据库中password字段
    @Column(nullable = false) //不能为空
    private String password;//密码
    private String email;//邮箱
    private String phoneNumber;//手机号


    //新增角色判断
    //管理员存“ROLE_ADMIN”,普通用户存“ROLE_USER”
    //前面加上ROLE_是Spring Security的默认规范
    private String role;

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public long getId(){
        return this.id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public String getUsername(){
        return this.username;
    }
    public void setUsername(String name){
        this.username = name;
    }

    public String getPassword(){
        return this.password;
    }
    public void setPassword(String password){
        this.password = password;
    }
}
