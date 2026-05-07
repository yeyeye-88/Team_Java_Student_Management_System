package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.PunishmentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * PunishmentRecordRepository 处分记录数据访问层
 */
public interface PunishmentRecordRepository extends JpaRepository<PunishmentRecord, Integer> {

    // 根据学生 ID 查询处分列表
    @Query("FROM PunishmentRecord WHERE personId = ?1 ORDER BY createTime DESC")
    List<PunishmentRecord> findByPersonId(Integer personId);

    // 查询所有处分（按创建时间降序）
    @Query("FROM PunishmentRecord ORDER BY createTime DESC")
    List<PunishmentRecord> findAllOrderByCreateTime();

    // 根据状态查询处分
    @Query("FROM PunishmentRecord WHERE status = ?1 ORDER BY createTime DESC")
    List<PunishmentRecord> findByStatus(Integer status);

    // 根据学生 ID 和状态查询处分
    @Query("FROM PunishmentRecord WHERE personId = ?1 AND status = ?2 ORDER BY createTime DESC")
    List<PunishmentRecord> findByPersonIdAndStatus(Integer personId, Integer status);
}
