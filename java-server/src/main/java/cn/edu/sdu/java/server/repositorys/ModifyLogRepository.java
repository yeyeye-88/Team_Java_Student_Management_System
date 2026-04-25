package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.ModifyLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * ModifyLog 数据操作接口，主要实现ModifyLog数据的查询操作
 * List<ModifyLog> findByTableName(String tableName); 根据表名查询修改日志
 * List<ModifyLog> findByOperatorId(Integer operatorId); 根据操作人ID查询修改日志
 */
@Repository
public interface ModifyLogRepository extends JpaRepository<ModifyLog,Integer>{
}
