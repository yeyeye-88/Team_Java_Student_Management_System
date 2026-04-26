package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * Teacher 数据操作接口，主要实现Teacher数据的查询操作
 */
@Repository
public interface TeacherRepository extends JpaRepository<Teacher,Integer> {
}
