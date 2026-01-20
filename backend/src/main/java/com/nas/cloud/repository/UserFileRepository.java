package com.nas.cloud.repository;

import com.nas.cloud.entity.UserFile;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 获取时光轴聚合数据
     * 注意：
     * 1. 过滤掉 is_deleted = 1 (回收站的)
     * 2. 过滤掉 shoot_time 为 NULL 的 (没有拍摄时间的图，暂时不显示在时光轴，或者以后逻辑改为用 upload_time 兜底)
     * 3. 这里的返回值 List<TimelineSummary> 会自动把 SQL 结果映射进去
     */
    @Query(value = "SELECT " +
            "YEAR(shoot_time) AS year, " +
            "MONTH(shoot_time) AS month, " +
            "COUNT(*) AS count " +
            "FROM nas_file " +
            "WHERE user_id = :userId " +
            "AND is_deleted = 0 " +
            "AND shoot_time IS NOT NULL " +
            "GROUP BY YEAR(shoot_time), MONTH(shoot_time) " +
            "ORDER BY year DESC, month DESC",
            nativeQuery = true)
    List<TimelineSummary> findTimelineSummary(@Param("userId") Long userId);

    //支持按照月份和年份查找
    @Query(value = "SELECT * FROM nas_file WHERE user_id = :userId AND YEAR(shoot_time) = :year AND MONTH(shoot_time) = :month AND is_deleted = 0 ORDER BY shoot_time DESC", nativeQuery = true)
    List<UserFile> findByYearAndMonth(@Param("userId") Long userId, @Param("year") Integer year, @Param("month") Integer month);
}
