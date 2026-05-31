package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.RequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/*
 * RequestLog 数据操作接口，主要实现RequestLog数据的查询操作
 */
@Repository
public interface RequestLogRepository extends JpaRepository<RequestLog, Integer> {
    
    /**
     * 统计指定日期范围内的请求数
     * @param startTime 开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间 yyyy-MM-dd HH:mm:ss
     * @return 请求数
     */
    @Query(value = "SELECT COUNT(*) FROM RequestLog WHERE startTime >= ?1 AND startTime <= ?2")
    Integer countByDate(String startTime, String endTime);
}
