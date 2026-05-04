package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.InnovationAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InnovationAchievementRepository extends JpaRepository<InnovationAchievement, Integer> {

    // 查询某项目的所有成果
    @Query("FROM InnovationAchievement WHERE project.projectId = ?1 ORDER BY submitTime DESC")
    List<InnovationAchievement> findByProjectId(Integer projectId);

    // 查询某人的所有成果
    @Query("FROM InnovationAchievement WHERE personId = ?1 ORDER BY submitTime DESC")
    List<InnovationAchievement> findByPersonId(Integer personId);
}
