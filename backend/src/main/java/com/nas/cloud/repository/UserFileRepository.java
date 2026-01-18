package com.nas.cloud.repository;

import com.nas.cloud.entity.UserFile;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

//只需要继承JpaRepository,就能够实现多种SQL的功能
public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    //现在需要自定义一个 带过滤条件的查询器
    //只要写上 findBy+字段名，这个Spring Data JPA就会自动生成SQL
    List<UserFile> findByUserId(Long userId, Sort sort);

    //查询某个人是否已经有了某个MD5文件
    UserFile findByUserIdAndMd5(Long userId, String md5);

    //根据MD5查找文件
    UserFile findFirstByMd5(String md5);


    //新增插某个文件夹下的所有图片
    //---------给资源管理器用---------------------------
    List<UserFile> findByUserIdAndParentId(Long userId, Long parentId, Sort sort);
    //---------给图库用-----------------------------------
    List<UserFile> findByUserIdAndIsFolderFalse(Long userId , Sort sort);
}
