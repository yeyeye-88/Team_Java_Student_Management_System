package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Score;
import cn.edu.sdu.java.server.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Score 成绩数据操作接口
 * 主要实现 Score 数据的查询操作
 */
@Repository
public interface ScoreRepository extends JpaRepository<Score, Integer> {
    
    /**
     * 根据学生查询所有成绩
     * @param student 学生对象
     * @return List<Score> 成绩列表
     */
    List<Score> findByStudent(Student student);
    
    /**
     * 根据学生ID查询所有成绩
     * @param personId 学生ID
     * @return List<Score> 成绩列表
     */
    @Query("SELECT s FROM Score s WHERE s.student.personId = ?1")
    List<Score> findByStudentPersonId(Integer personId);
    
    /**
     * 根据学生ID和课程ID查询成绩
     * @param personId 学生ID
     * @param courseId 课程ID
     * @return List<Score> 成绩列表
     */
    @Query(value="from Score where (?1=0 or student.personId=?1) and (?2=0 or course.courseId=?2)" )
    List<Score> findByStudentCourse(Integer personId, Integer courseId);
    
    /**
     * 根据学生ID和课程名称查询成绩
     * @param personId 学生ID
     * @param courseName 课程名称
     * @return List<Score> 成绩列表
     */
    @Query(value="from Score where student.personId=?1 and (?2=0 or course.name like %?2%)" )
    List<Score> findByStudentCourse(Integer personId, String courseName);
    
    /**
     * 根据课程ID查询所有成绩
     * @param courseId 课程ID
     * @return List<Score> 成绩列表
     */
    List<Score> findByCourseCourseId(Integer courseId);
    
    /**
     * 查询某学生在某课程的成绩
     * @param personId 学生ID
     * @param courseId 课程ID
     * @return Optional<Score> 成绩对象
     */
    Optional<Score> findByStudentPersonIdAndCourseCourseId(Integer personId, Integer courseId);
    
    /**
     * 查询某课程成绩排名（按分数降序）
     * @param courseId 课程ID
     * @return List<Score> 成绩列表（已排序）
     */
    @Query("SELECT s FROM Score s WHERE s.course.courseId = ?1 ORDER BY s.mark DESC")
    List<Score> findByCourseOrderByMarkDesc(Integer courseId);
    
    /**
     * 查询某学生不及格的成绩（<60分）
     * @param personId 学生ID
     * @return List<Score> 不及格成绩列表
     */
    @Query("SELECT s FROM Score s WHERE s.student.personId = ?1 AND s.mark < 60")
    List<Score> findFailedScoresByStudent(Integer personId);
    
    /**
     * 查询某课程的平均分
     * @param courseId 课程ID
     * @return Double 平均分
     */
    @Query("SELECT AVG(s.mark) FROM Score s WHERE s.course.courseId = ?1")
    Double findAverageMarkByCourse(Integer courseId);
    
    /**
     * 获取学生统计列表（用于学生统计功能）
     * @param personId 学生ID列表
     * @return List<?> 统计数据
     */
    @Query(value="select s.student.personId, count(s.scoreId), sum(s.mark),sum(s.course.credit),sum(s.course.credit* s.mark) from Score s where s.student.personId in ?1 group by s.student.personId" )
    List<?> getStudentStatisticsList(List<Integer> personId);
}
