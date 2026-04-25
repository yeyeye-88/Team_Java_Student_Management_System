package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.DictionaryInfo;
import cn.edu.sdu.java.server.models.MenuInfo;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.models.UserType;
import cn.edu.sdu.java.server.services.DictionaryInfoService;
import cn.edu.sdu.java.server.services.MenuInfoService;
import cn.edu.sdu.java.server.services.UserService;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限管理相关 Repository 测试类
 * 第 1 天任务：配合成员 2 做权限管理联调
 */
@SpringBootTest
@Transactional
class PermissionRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuInfoRepository menuInfoRepository;

    @Autowired
    private DictionaryInfoRepository dictionaryInfoRepository;

    @Autowired
    private UserTypeRepository userTypeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private MenuInfoService menuInfoService;

    @Autowired
    private DictionaryInfoService dictionaryInfoService;

    /**
     * 测试 1：按账号查用户
     */
    @Test
    void testFindByUserName() {
        System.out.println("=== 测试：按账号查用户 ===");
        
        // 测试查找已存在的用户
        Optional<User> op = userRepository.findByUserName("admin");
        if (op.isPresent()) {
            User user = op.get();
            System.out.println("找到用户: " + user.getUserName());
            System.out.println("用户 ID: " + user.getPersonId());
            System.out.println("用户角色: " + (user.getUserType() != null ? user.getUserType().getName() : "无"));
            assertNotNull(user.getPersonId());
        } else {
            System.out.println("用户 admin 不存在，可能是测试数据未导入");
        }
        
        // 测试 Service 层方法
        DataResponse response = userService.findByUserName("admin");
        System.out.println("Service 返回: " + response);
        assertNotNull(response);
    }

    /**
     * 测试 2：按角色查用户
     */
    @Test
    void testFindByRoleName() {
        System.out.println("\n=== 测试：按角色查用户 ===");
        
        // 查询所有用户，检查角色分配
        List<User> allUsers = userRepository.findAll();
        System.out.println("总用户数: " + allUsers.size());
        
        for (User u : allUsers) {
            System.out.println("用户: " + u.getUserName() + 
                             ", 角色: " + (u.getUserType() != null ? u.getUserType().getName() : "无"));
        }
        
        // 测试 Service 层按角色查询
        DataResponse response = userService.findByRoleName("ROLE_ADMIN");
        System.out.println("ROLE_ADMIN 用户列表: " + response);
        assertNotNull(response);
    }

    /**
     * 测试 3：按角色查菜单
     */
    @Test
    void testFindMenuByUserTypeIds() {
        System.out.println("\n=== 测试：按角色查菜单 ===");
        
        // 查询所有菜单
        List<MenuInfo> allMenus = menuInfoRepository.findAll();
        System.out.println("总菜单数: " + allMenus.size());
        
        for (MenuInfo menu : allMenus) {
            System.out.println("菜单: " + menu.getTitle() + 
                             ", 角色 IDs: " + menu.getUserTypeIds() + 
                             ", PID: " + menu.getPid());
        }
        
        // 测试按角色查询菜单（假设角色 ID 为 1）
        List<MenuInfo> menus = menuInfoRepository.findByUserTypeIds("1");
        System.out.println("角色 1 可访问的菜单数: " + menus.size());
        
        // 测试 Service 层方法
        DataResponse response = menuInfoService.findByUserTypeIds("1");
        System.out.println("Service 返回: " + response);
        assertNotNull(response);
    }

    /**
     * 测试 4：字典查询
     */
    @Test
    void testDictionaryQuery() {
        System.out.println("\n=== 测试：字典查询 ===");
        
        // 查询所有字典
        List<DictionaryInfo> allDicts = dictionaryInfoRepository.findAll();
        System.out.println("总字典项数: " + allDicts.size());
        
        // 查询根字典
        List<DictionaryInfo> rootDicts = dictionaryInfoRepository.findRootList();
        System.out.println("根字典数: " + rootDicts.size());
        
        for (DictionaryInfo dict : rootDicts) {
            System.out.println("根字典: " + dict.getLabel() + " (ID: " + dict.getId() + ")");
            
            // 查询子字典
            List<DictionaryInfo> childDicts = dictionaryInfoRepository.findByPid(dict.getId());
            System.out.println("  子字典数: " + childDicts.size());
            for (DictionaryInfo child : childDicts) {
                System.out.println("    - " + child.getLabel() + " (Value: " + child.getValue() + ")");
            }
        }
        
        // 测试 Service 层方法
        DataResponse response = dictionaryInfoService.findRootList();
        System.out.println("Service 返回: " + response);
        assertNotNull(response);
    }

    /**
     * 测试 5：用户角色关联检查
     */
    @Test
    void testUserRoleAssociation() {
        System.out.println("\n=== 测试：用户角色关联检查 ===");
        
        // 检查所有用户类型
        List<UserType> userTypes = userTypeRepository.findAll();
        System.out.println("用户类型数: " + userTypes.size());
        for (UserType ut : userTypes) {
            System.out.println("角色 ID: " + ut.getId() + ", 名称: " + ut.getName());
        }
        
        // 检查用户与角色的关联
        List<User> users = userRepository.findAll();
        for (User u : users) {
            if (u.getUserType() == null) {
                System.out.println("警告：用户 " + u.getUserName() + " 没有分配角色！");
            } else {
                System.out.println("用户 " + u.getUserName() + " -> 角色 " + u.getUserType().getName() + " ✓");
            }
        }
    }

    /**
     * 测试 6：用户增删改查完整流程
     */
    @Test
    void testUserCRUD() {
        System.out.println("\n=== 测试：用户 CRUD 完整流程 ===");
        
        // 查询所有用户
        DataResponse findAllResponse = userService.findAllUsers();
        System.out.println("查询所有用户: " + findAllResponse);
        assertNotNull(findAllResponse);
        
        // 获取用户列表用于后续测试
        List<User> users = userRepository.findAll();
        
        // 删除用户测试（如果有测试数据）
        if (!users.isEmpty()) {
            Integer testUserId = users.get(0).getPersonId();
            System.out.println("\n删除用户测试 (ID: " + testUserId + ")...");
            DataResponse deleteResponse = userService.deleteUser(testUserId);
            System.out.println("删除结果: " + deleteResponse);
        }
    }
}
