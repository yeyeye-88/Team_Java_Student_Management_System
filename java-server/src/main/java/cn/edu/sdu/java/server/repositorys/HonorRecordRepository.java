package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.HonorRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * HonorRecordRepository 荣誉记录数据访问层
 */
public interface HonorRecordRepository extends JpaRepository<HonorRecord, Integer> {

    // 根据学生 ID 查询荣誉列表
    @Query("FROM HonorRecord WHERE personId = ?1 ORDER BY createTime DESC")
    List<HonorRecord> findByPersonId(Integer personId);

    // 查询所有荣誉（按创建时间降序）
    @Query("FROM HonorRecord ORDER BY createTime DESC")
    List<HonorRecord> findAllOrderByCreateTime();

    // 根据级别查询荣誉
    @Query("FROM HonorRecord WHERE level = ?1 ORDER BY createTime DESC")
    List<HonorRecord> findByLevel(String level);
}
