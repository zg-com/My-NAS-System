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
    private String username;
    //对应数据库中password字段
    private String password;

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
