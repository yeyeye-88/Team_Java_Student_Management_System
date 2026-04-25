package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.SystemInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
 * SystemInfo 数据操作接口，主要实现SystemInfo数据的查询操作
 */
@Repository
public interface SystemInfoRepository extends JpaRepository<SystemInfo,Integer> {
}
