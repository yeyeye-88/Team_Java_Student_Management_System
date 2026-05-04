package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.InnovationProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InnovationProjectRepository extends JpaRepository<InnovationProject, Integer> {

    // 查询所有项目（按创建时间降序）
    @Query("FROM InnovationProject ORDER BY createTime DESC")
    List<InnovationProject> findAllOrderByCreateTime();

    // 根据负责人查询项目
    @Query("FROM InnovationProject WHERE leaderId = ?1 ORDER BY createTime DESC")
    List<InnovationProject> findByLeaderId(Integer leaderId);

    // 根据状态查询项目
    @Query("FROM InnovationProject WHERE status = ?1 ORDER BY createTime DESC")
    List<InnovationProject> findByStatus(Integer status);

    // 根据项目名称查询（用于防重复）
    @Query("FROM InnovationProject WHERE name = ?1")
    InnovationProject findByName(String name);
}
