package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Integer> {

    // 查询所有活动（按创建时间降序）
    @Query("FROM Activity ORDER BY createTime DESC")
    List<Activity> findAllOrderByCreateTime();

    // 根据发布者查询活动
    @Query("FROM Activity WHERE publisherId = ?1 ORDER BY createTime DESC")
    List<Activity> findByPublisherId(Integer publisherId);

    // 根据活动名称查询（用于防重复）
    @Query("FROM Activity WHERE name = ?1")
    Activity findByName(String name);
}
