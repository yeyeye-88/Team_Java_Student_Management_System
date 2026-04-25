package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.SystemInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
 * SystemInfo 数据操作接口，主要实现SystemInfo数据的查询操作
 * Optional<SystemInfo> findByName(String name); 根据名称查询系统配置信息
 */
@Repository
public interface SystemInfoRepository extends JpaRepository<SystemInfo,Integer> {
}
