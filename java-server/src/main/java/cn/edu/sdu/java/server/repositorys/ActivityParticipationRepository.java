package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.ActivityParticipation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ActivityParticipationRepository extends JpaRepository<ActivityParticipation, Integer> {

    // 查询某活动的所有参与者
    @Query("FROM ActivityParticipation WHERE activity.activityId = ?1 ORDER BY joinTime DESC")
    List<ActivityParticipation> findByActivityId(Integer activityId);

    // 查询某人的所有参与记录
    @Query("FROM ActivityParticipation WHERE personId = ?1 ORDER BY joinTime DESC")
    List<ActivityParticipation> findByPersonId(Integer personId);

    // 检查是否已参与某活动
    @Query("FROM ActivityParticipation WHERE activity.activityId = ?1 AND personId = ?2")
    ActivityParticipation findByActivityIdAndPersonId(Integer activityId, Integer personId);
}
