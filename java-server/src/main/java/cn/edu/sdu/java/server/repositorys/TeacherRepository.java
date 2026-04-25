package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Teacher 数据操作接口，主要实现Teacher数据的查询操作
 * Optional<Teacher> findByPersonPersonId(Integer personId); 根据关联的Person的personId查询获得Option<Teacher>对象
 * Optional<Teacher> findByPersonNum(String num); 根据关联的Person的num查询获得Option<Teacher>对象
 * List<Teacher> findTeacherListByNumName(String numName); 根据输入的参数查询教师列表
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher,Integer> {
}
