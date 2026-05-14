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

    // 按课程查询课表
    @Query("FROM CourseSchedule WHERE course.courseId = ?1 AND semester = ?2 AND status = 1")
    List<CourseSchedule> findByCourseIdAndSemester(Integer courseId, String semester);
}
