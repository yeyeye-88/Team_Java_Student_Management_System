package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.PunishmentResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * PunishmentResultRepository 处理结果数据访问层
 */
public interface PunishmentResultRepository extends JpaRepository<PunishmentResult, Integer> {

    // 根据处分 ID 查询处理结果列表
    @Query("FROM PunishmentResult WHERE punishmentId = ?1 ORDER BY createTime DESC")
    List<PunishmentResult> findByPunishmentId(Integer punishmentId);

    // 查询所有处理结果
    @Query("FROM PunishmentResult ORDER BY createTime DESC")
    List<PunishmentResult> findAllOrderByCreateTime();
}
