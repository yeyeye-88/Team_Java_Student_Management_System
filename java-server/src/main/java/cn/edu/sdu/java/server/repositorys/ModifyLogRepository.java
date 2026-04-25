package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.ModifyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * ModifyLog 数据操作接口，主要实现ModifyLog数据的查询操作
 */
@Repository
public interface ModifyLogRepository extends JpaRepository<ModifyLog,Integer>{
}
