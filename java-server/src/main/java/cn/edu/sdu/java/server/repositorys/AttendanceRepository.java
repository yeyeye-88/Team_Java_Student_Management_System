package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
}
