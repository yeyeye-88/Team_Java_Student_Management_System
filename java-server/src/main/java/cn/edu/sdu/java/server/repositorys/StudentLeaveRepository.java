package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.StudentLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentLeaveRepository extends JpaRepository<StudentLeave,Integer> {
    @Query(value = "SELECT DISTINCT sl FROM StudentLeave sl " +
           "LEFT JOIN FETCH sl.student s " +
           "LEFT JOIN FETCH s.person sp " +
           "LEFT JOIN FETCH sl.teacher t " +
           "LEFT JOIN FETCH t.person tp " +
           "WHERE (?1 < 0 OR sl.state = ?1) " +
           "AND (?2 = '' OR sp.name LIKE %?2% OR tp.name LIKE %?2% OR sl.reason LIKE %?2%) " +
           "AND (?3 = '' OR sp.num = ?3) " +
           "AND (?4 = '' OR tp.num = ?4)")
    List<StudentLeave> getStudentLeaveList(Integer state, String search, String studentNum, String teacherNum);

    @Query(value="select s.student.personId, count(s.studentLeaveId) from StudentLeave s where s.student.personId in ?1 group by s.student.personId" )
    List<?> getStudentStatisticsList(List<Integer> personId);
}
