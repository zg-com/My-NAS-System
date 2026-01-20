package com.nas.cloud.repository;

import com.nas.cloud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

//继承JpaRepository<实体类，主键！类型>
//只要继承这个就瞬间有了几十种数据库的方法，不用写一行SQL
public interface UserRepository extends JpaRepository<User,Long> {
    User findByUsername(String username);

}
