package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.CourseSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseScheduleRepository extends JpaRepository<CourseSchedule, Integer> {

    // 按班级和学期查询课表
    @Query("FROM CourseSchedule WHERE className = ?1 AND semester = ?2 AND status = 1 ORDER BY dayOfWeek, startPeriod")
    List<CourseSchedule> findByClassNameAndSemester(String className, String semester);

    // 按教师查询课表
    @Query("FROM CourseSchedule WHERE teacher.personId = ?1 AND semester = ?2 AND status = 1 ORDER BY dayOfWeek, startPeriod")
    List<CourseSchedule> findByTeacherIdAndSemester(Integer teacherId, String semester);

    // 按教师查询所有学期的课表（用于权限校验）
    @Query("FROM CourseSchedule WHERE teacher.personId = ?1 AND status = 1 ORDER BY semester, dayOfWeek, startPeriod")
    List<CourseSchedule> findByTeacherIdAllSemesters(Integer teacherId);

    // 按课程查询课表
    @Query("FROM CourseSchedule WHERE course.courseId = ?1 AND semester = ?2 AND status = 1")
    List<CourseSchedule> findByCourseIdAndSemester(Integer courseId, String semester);
    
    // 按课程ID查询所有排课记录（包括停用状态）
    @Query("FROM CourseSchedule WHERE course.courseId = ?1")
    List<CourseSchedule> findByCourse_CourseId(Integer courseId);

    // 查询已停用的排课记录（用于重新启用）
    @Query("FROM CourseSchedule WHERE course.courseId = ?1 AND className = ?2 AND semester = ?3 AND status = ?4")
    List<CourseSchedule> findByCourse_CourseIdAndClassNameAndSemesterAndStatus(
        Integer courseId, String className, String semester, Integer status);
}
