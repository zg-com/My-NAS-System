package com.nas.cloud.repository;

import com.nas.cloud.entity.UserFile;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//只需要继承JpaRepository,就能够实现多种SQL的功能
public interface UserFileRepository extends JpaRepository<UserFile,Long> {
    //现在需要自定义一个 带过滤条件的查询器
    //只要写上 findBy+字段名，这个Spring Data JPA就会自动生成SQL
    List<UserFile> findByUserId(Long userId, Sort sort);
}
