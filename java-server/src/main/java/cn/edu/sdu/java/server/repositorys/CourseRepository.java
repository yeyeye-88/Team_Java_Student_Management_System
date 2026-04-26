package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Course 课程数据操作接口
 * 主要实现 Course 数据的查询操作
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    
    /**
     * 根据课程编号或名称模糊查询（课程列表查询）
     * @param numName 课程编号或名称关键字
     * @return List<Course> 课程列表
     */
    @Query(value = "from Course where ?1='' or num like %?1% or name like %?1% ")
    List<Course> findCourseListByNumName(String numName);
    
    /**
     * 根据课程编号查询课程
     * @param num 课程编号
     * @return Optional<Course> 课程对象
     */
    Optional<Course> findByNum(String num);
    
    /**
     * 根据课程名称模糊查询
     * @param name 课程名称关键字
     * @return List<Course> 课程列表
     */
    List<Course> findByNameContaining(String name);
    
    /**
     * 根据学分查询课程列表
     * @param credit 学分
     * @return List<Course> 课程列表
     */
    List<Course> findByCredit(Integer credit);
    
    /**
     * 查询没有前序课程的课程（基础课）
     * @return List<Course> 基础课程列表
     */
    @Query("SELECT c FROM Course c WHERE c.preCourse IS NULL")
    List<Course> findBaseCourses();
    
    /**
     * 查询某门课程的所有后续课程
     * @param preCourseId 前序课程ID
     * @return List<Course> 后续课程列表
     */
    @Query("SELECT c FROM Course c WHERE c.preCourse.courseId = ?1")
    List<Course> findByPreCourseId(Integer preCourseId);
}
