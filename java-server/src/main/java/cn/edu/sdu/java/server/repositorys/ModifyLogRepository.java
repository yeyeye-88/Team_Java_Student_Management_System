package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.ModifyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/*
 * ModifyLog 数据操作接口，主要实现ModifyLog数据的查询操作
 */
@Repository
public interface ModifyLogRepository extends JpaRepository<ModifyLog,Integer>{
    
    /**
     * 统计指定日期范围内的修改记录数
     * @param startTime 开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间 yyyy-MM-dd HH:mm:ss
     * @return 修改记录数
     */
    @Query(value = "SELECT COUNT(*) FROM ModifyLog WHERE operateTime >= ?1 AND operateTime <= ?2")
    Integer countByDate(String startTime, String endTime);
}
