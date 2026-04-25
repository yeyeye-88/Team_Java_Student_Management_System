package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.MenuInfo;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.MenuInfoRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuInfoService {
    private final MenuInfoRepository menuInfoRepository;

    public MenuInfoService(MenuInfoRepository menuInfoRepository) {
        this.menuInfoRepository = menuInfoRepository;
    }

    public DataResponse findAll() {
        List<MenuInfo> menuList = menuInfoRepository.findAll();
        return CommonMethod.getReturnData(menuList);
    }

    /**
     * 根据角色 ID 查询菜单列表（按角色查菜单）
     */
    public DataResponse findByUserTypeIds(String userTypeIds) {
        try {
            List<MenuInfo> menuList = menuInfoRepository.findByUserTypeIds(userTypeIds);
            return CommonMethod.getReturnData(menuList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询菜单失败：" + e.getMessage());
        }
    }

    /**
     * 根据父节点 ID 查询子菜单
     */
    public DataResponse findByPid(String userTypeIds, Integer pid) {
        try {
            List<MenuInfo> menuList = menuInfoRepository.findByUserTypeIds(userTypeIds, pid);
            return CommonMethod.getReturnData(menuList);
        } catch (Exception e) {
            return CommonMethod.getReturnMessageError("查询子菜单失败：" + e.getMessage());
        }
    }
}
