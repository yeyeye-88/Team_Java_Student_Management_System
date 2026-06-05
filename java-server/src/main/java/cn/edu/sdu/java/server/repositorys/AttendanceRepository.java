package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Attendance 数据操作接口
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    
    // 根据学生 personId 查询考勤列表
    List<Attendance> findByStudentPersonId(Integer personId);
    
    // 按课程 ID 查询
    List<Attendance> findByCourseCourseId(Integer courseId);
    
    // 按状态查询
    List<Attendance> findByState(Integer state);
    
    // 按考勤时间查询（支持日期范围）
    @Query(value = "from Attendance where attendanceTime >= ?1 and attendanceTime <= ?2")
    List<Attendance> findByAttendanceTimeRange(Date startTime, Date endTime);

    // 第 7 周任务：统计功能 - 按课程统计考勤率（state=3 表示正常出勤）
    @Query("SELECT a.course.courseId, " +
           "SUM(CASE WHEN a.state = 3 THEN 1 ELSE 0 END) * 100.0 / COUNT(a.attendanceId) " +
           "FROM Attendance a GROUP BY a.course.courseId")
    List<Object[]> getAttendanceRateByCourse();

    // 按课程和状态统计考勤分布
    @Query("SELECT a.course.courseId, a.state, COUNT(a.attendanceId) " +
           "FROM Attendance a GROUP BY a.course.courseId, a.state")
    List<Object[]> getAttendanceStatsByCourseAndState();
    
    // 支持多条件组合查询
    @Query("SELECT a FROM Attendance a WHERE " +
           "(:personId IS NULL OR a.student.person.personId = :personId) AND " +
           "(:courseId IS NULL OR a.course.courseId = :courseId) AND " +
           "(:state IS NULL OR a.state = :state)")
    List<Attendance> findByConditions(@Param("personId") Integer personId,
                                     @Param("courseId") Integer courseId,
                                     @Param("state") Integer state);
}
