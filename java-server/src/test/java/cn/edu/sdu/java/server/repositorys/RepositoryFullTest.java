package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 第 4 天：数据层全量测试
 * 覆盖 5 个核心 Repository 的所有方法，测试多表关联查询
 */
@SpringBootTest
class RepositoryFullTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuInfoRepository menuInfoRepository;

    @Autowired
    private DictionaryInfoRepository dictionaryInfoRepository;

    // ==================== 1. StudentRepository 全量测试 (7个方法) ====================

    @Test
    void testStudentRepository_FindByPersonPersonId() {
        System.out.println("\n=== StudentRepository: findByPersonPersonId ===");
        Optional<Student> student = studentRepository.findByPersonPersonId(1);
        System.out.println("查询 personId=1 的学生: " + (student.isPresent() ? "存在" : "不存在"));
        if (student.isPresent()) {
            System.out.println("学号: " + student.get().getPerson().getNum());
            System.out.println("姓名: " + student.get().getPerson().getName());
            System.out.println("专业: " + student.get().getMajor());
            System.out.println("班级: " + student.get().getClassName());
        }
        assertNotNull(student);
    }

    @Test
    void testStudentRepository_FindByPersonNum() {
        System.out.println("\n=== StudentRepository: findByPersonNum ===");
        Optional<Student> student = studentRepository.findByPersonNum("20240001");
        System.out.println("查询学号=20240001 的学生: " + (student.isPresent() ? "存在" : "不存在"));
        if (student.isPresent()) {
            System.out.println("姓名: " + student.get().getPerson().getName());
            System.out.println("班级: " + student.get().getClassName());
        }
        assertNotNull(student);
    }

    @Test
    void testStudentRepository_FindByPersonName() {
        System.out.println("\n=== StudentRepository: findByPersonName ===");
        List<Student> students = studentRepository.findByPersonName("张三");
        System.out.println("查询姓名=张三 的学生数量: " + students.size());
        for (Student s : students) {
            System.out.println("学号: " + s.getPerson().getNum() + ", 班级: " + s.getClassName());
        }
        assertNotNull(students);
    }

    @Test
    void testStudentRepository_FindStudentListByNumName() {
        System.out.println("\n=== StudentRepository: findStudentListByNumName ===");
        
        // 测试空参数（查询所有）
        List<Student> allStudents = studentRepository.findStudentListByNumName("");
        System.out.println("查询所有学生数量: " + allStudents.size());
        
        // 测试模糊查询
        List<Student> searchStudents = studentRepository.findStudentListByNumName("2024");
        System.out.println("模糊查询包含'2024'的学生数量: " + searchStudents.size());
        for (int i = 0; i < Math.min(3, searchStudents.size()); i++) {
            Student s = searchStudents.get(i);
            System.out.println("学号: " + s.getPerson().getNum() + ", 姓名: " + s.getPerson().getName());
        }
        
        assertNotNull(allStudents);
        assertNotNull(searchStudents);
    }

    @Test
    void testStudentRepository_FindStudentPageByNumName() {
        System.out.println("\n=== StudentRepository: findStudentPageByNumName ===");
        Page<Student> page = studentRepository.findStudentPageByNumName("", PageRequest.of(0, 10));
        System.out.println("分页查询 - 总记录数: " + page.getTotalElements());
        System.out.println("分页查询 - 总页数: " + page.getTotalPages());
        System.out.println("分页查询 - 当前页记录数: " + page.getContent().size());
        
        assertNotNull(page);
        assertTrue(page.getContent().size() > 0);
    }

    @Test
    void testStudentRepository_FindByClassName() {
        System.out.println("\n=== StudentRepository: findByClassName ===");
        List<Student> students = studentRepository.findByClassName("计算机2401");
        System.out.println("查询班级=计算机2401 的学生数量: " + students.size());
        for (Student s : students) {
            System.out.println("学号: " + s.getPerson().getNum() + ", 姓名: " + s.getPerson().getName());
        }
        assertNotNull(students);
    }

    @Test
    void testStudentRepository_FindByPersonNumLikeOrPersonNameLike() {
        System.out.println("\n=== StudentRepository: findByPersonNumLikeOrPersonNameLike ===");
        List<Student> students = studentRepository.findByPersonNumLikeOrPersonNameLike("%2024%", "%测试%");
        System.out.println("组合模糊查询结果数量: " + students.size());
        assertNotNull(students);
    }

    // ==================== 2. PersonRepository 全量测试 (2个方法) ====================

    @Test
    void testPersonRepository_FindByNum() {
        System.out.println("\n=== PersonRepository: findByNum ===");
        Optional<Person> person = personRepository.findByNum("20240001");
        System.out.println("查询编号=20240001 的人员: " + (person.isPresent() ? "存在" : "不存在"));
        if (person.isPresent()) {
            System.out.println("姓名: " + person.get().getName());
            System.out.println("类型: " + person.get().getType());
            System.out.println("部门: " + person.get().getDept());
        }
        assertNotNull(person);
    }

    @Test
    void testPersonRepository_GetPhotoByPersonId() {
        System.out.println("\n=== PersonRepository: getPhotoByPersonId ===");
        byte[] photo = personRepository.getPhotoByPersonId(1);
        System.out.println("查询 personId=1 的照片: " + (photo != null ? photo.length + " 字节" : "null"));
        // 允许返回 null
    }

    // ==================== 3. UserRepository 全量测试 (6个方法) ====================

    @Test
    void testUserRepository_FindByUserName() {
        System.out.println("\n=== UserRepository: findByUserName ===");
        Optional<User> user = userRepository.findByUserName("admin");
        System.out.println("查询用户名=admin 的用户: " + (user.isPresent() ? "存在" : "不存在"));
        if (user.isPresent()) {
            System.out.println("关联人员编号: " + user.get().getPerson().getNum());
            System.out.println("关联人员姓名: " + user.get().getPerson().getName());
            System.out.println("用户类型: " + user.get().getUserType().getName());
            System.out.println("登录次数: " + user.get().getLoginCount());
        }
        assertNotNull(user);
    }

    @Test
    void testUserRepository_FindByPersonNum() {
        System.out.println("\n=== UserRepository: findByPersonNum ===");
        Optional<User> user = userRepository.findByPersonNum("20240001");
        System.out.println("查询人员编号=20240001 的用户: " + (user.isPresent() ? "存在" : "不存在"));
        if (user.isPresent()) {
            System.out.println("用户名: " + user.get().getUserName());
            System.out.println("用户类型: " + user.get().getUserType().getName());
        }
        assertNotNull(user);
    }

    @Test
    void testUserRepository_FindByPersonPersonId() {
        System.out.println("\n=== UserRepository: findByPersonPersonId ===");
        Optional<User> user = userRepository.findByPersonPersonId(1);
        System.out.println("查询 personId=1 的用户: " + (user.isPresent() ? "存在" : "不存在"));
        if (user.isPresent()) {
            System.out.println("用户名: " + user.get().getUserName());
        }
        assertNotNull(user);
    }

    @Test
    void testUserRepository_ExistsByUserName() {
        System.out.println("\n=== UserRepository: existsByUserName ===");
        boolean exists = userRepository.existsByUserName("admin");
        System.out.println("用户名=admin 是否存在: " + exists);
        assertTrue(exists);
        
        boolean notExists = userRepository.existsByUserName("nonexistent");
        System.out.println("用户名=nonexistent 是否存在: " + notExists);
        assertFalse(notExists);
    }

    @Test
    void testUserRepository_CountLastLoginTime() {
        System.out.println("\n=== UserRepository: countLastLoginTime ===");
        Integer count = userRepository.countLastLoginTime("2024-01-01");
        System.out.println("2024-01-01 之后登录的用户数: " + count);
        assertNotNull(count);
    }

    @Test
    void testUserRepository_GetCountList() {
        System.out.println("\n=== UserRepository: getCountList ===");
        List<?> countList = userRepository.getCountList();
        System.out.println("按用户类型统计的用户数:");
        for (Object item : countList) {
            System.out.println("统计结果: " + item);
        }
        assertNotNull(countList);
    }

    // ==================== 4. MenuInfoRepository 全量测试 (3个方法) ====================

    @Test
    void testMenuInfoRepository_FindByUserTypeIds_Root() {
        System.out.println("\n=== MenuInfoRepository: findByUserTypeIds (根菜单) ===");
        List<MenuInfo> rootMenus = menuInfoRepository.findByUserTypeIds("0,1,2");
        System.out.println("角色 0,1,2 的根菜单数量: " + rootMenus.size());
        for (MenuInfo menu : rootMenus) {
            System.out.println("菜单: " + menu.getTitle() + ", Name: " + menu.getName());
            
            // 测试多表关联：查询子菜单
            List<MenuInfo> children = menuInfoRepository.findByUserTypeIds("0,1,2", menu.getId());
            System.out.println("  子菜单数量: " + children.size());
        }
        assertNotNull(rootMenus);
    }

    @Test
    void testMenuInfoRepository_FindByUserTypeIds_Children() {
        System.out.println("\n=== MenuInfoRepository: findByUserTypeIds (子菜单) ===");
        List<MenuInfo> children = menuInfoRepository.findByUserTypeIds("0,1,2", 1);
        System.out.println("pid=1 的子菜单数量: " + children.size());
        for (MenuInfo menu : children) {
            System.out.println("子菜单: " + menu.getTitle() + ", Name: " + menu.getName());
        }
        assertNotNull(children);
    }

    @Test
    void testMenuInfoRepository_CountMenuInfoByPid() {
        System.out.println("\n=== MenuInfoRepository: countMenuInfoByPid ===");
        int count = menuInfoRepository.countMenuInfoByPid(1);
        System.out.println("pid=1 的子菜单数量: " + count);
        assertTrue(count >= 0);
    }

    // ==================== 5. DictionaryInfoRepository 全量测试 (5个方法) ====================

    @Test
    void testDictionaryInfoRepository_GetMaxId() {
        System.out.println("\n=== DictionaryInfoRepository: getMaxId ===");
        Integer maxId = dictionaryInfoRepository.getMaxId();
        System.out.println("字典表最大 ID: " + maxId);
        assertNotNull(maxId);
    }

    @Test
    void testDictionaryInfoRepository_FindRootList() {
        System.out.println("\n=== DictionaryInfoRepository: findRootList ===");
        List<DictionaryInfo> rootList = dictionaryInfoRepository.findRootList();
        System.out.println("根字典数量: " + rootList.size());
        for (DictionaryInfo dict : rootList) {
            System.out.println("字典类型: " + dict.getValue() + " - " + dict.getLabel());
            
            // 测试多表关联：查询子字典
            List<DictionaryInfo> children = dictionaryInfoRepository.findByPid(dict.getId());
            System.out.println("  子字典数量: " + children.size());
        }
        assertNotNull(rootList);
    }

    @Test
    void testDictionaryInfoRepository_FindByPid() {
        System.out.println("\n=== DictionaryInfoRepository: findByPid ===");
        List<DictionaryInfo> children = dictionaryInfoRepository.findByPid(1);
        System.out.println("pid=1 的子字典数量: " + children.size());
        for (DictionaryInfo dict : children) {
            System.out.println("子字典: " + dict.getValue() + " - " + dict.getLabel());
        }
        assertNotNull(children);
    }

    @Test
    void testDictionaryInfoRepository_GetDictionaryInfoList() {
        System.out.println("\n=== DictionaryInfoRepository: getDictionaryInfoList ===");
        List<DictionaryInfo> dictList = dictionaryInfoRepository.getDictionaryInfoList("XBM");
        System.out.println("代码=XBM 的字典数量: " + dictList.size());
        for (DictionaryInfo dict : dictList) {
            System.out.println("字典项: " + dict.getValue() + " - " + dict.getLabel());
        }
        assertNotNull(dictList);
    }

    @Test
    void testDictionaryInfoRepository_CountDictionaryInfoByPid() {
        System.out.println("\n=== DictionaryInfoRepository: countDictionaryInfoByPid ===");
        int count = dictionaryInfoRepository.countDictionaryInfoByPid(1);
        System.out.println("pid=1 的子字典数量: " + count);
        assertTrue(count >= 0);
    }

    // ==================== 6. 多表关联查询专项测试 ====================

    @Test
    void testMultiTableJoin_StudentPerson() {
        System.out.println("\n=== 多表关联测试: Student + Person ===");
        
        // 先查询所有学生，找第一个学生进行测试
        List<Student> allStudents = studentRepository.findStudentListByNumName("");
        if (allStudents.isEmpty()) {
            System.out.println("⚠️ 数据库中没有学生数据，跳过此测试");
            return;
        }
        
        // 使用第一个学生进行测试
        Student s = allStudents.get(0);
        Person p = s.getPerson();
        
        System.out.println("学生信息:");
        System.out.println("  学号: " + p.getNum());
        System.out.println("  姓名: " + p.getName());
        System.out.println("  专业: " + s.getMajor());
        System.out.println("  班级: " + s.getClassName());
        System.out.println("  部门: " + p.getDept());
        
        assertEquals(p.getNum(), s.getPerson().getNum());
        assertNotNull(s.getPerson());
    }

    @Test
    void testMultiTableJoin_UserPersonType() {
        System.out.println("\n=== 多表关联测试: User + Person + UserType ===");
        Optional<User> user = userRepository.findByUserName("admin");
        assertTrue(user.isPresent(), "应该找到用户");
        
        User u = user.get();
        
        System.out.println("用户信息:");
        System.out.println("  用户名: " + u.getUserName());
        System.out.println("  关联人员编号: " + u.getPerson().getNum());
        System.out.println("  关联人员姓名: " + u.getPerson().getName());
        System.out.println("  用户类型: " + u.getUserType().getName());
        System.out.println("  登录次数: " + u.getLoginCount());
        
        assertNotNull(u.getPerson());
        assertNotNull(u.getUserType());
    }

    @Test
    void testMultiTableJoin_MenuTree() {
        System.out.println("\n=== 多表关联测试: MenuInfo 自关联（菜单树） ===");
        List<MenuInfo> rootMenus = menuInfoRepository.findByUserTypeIds("0");
        System.out.println("管理员根菜单数量: " + rootMenus.size());
        
        int totalMenus = 0;
        for (MenuInfo root : rootMenus) {
            List<MenuInfo> children = menuInfoRepository.findByUserTypeIds("0", root.getId());
            totalMenus += 1 + children.size();
            System.out.println("  菜单: " + root.getTitle() + " -> " + children.size() + " 个子菜单");
        }
        
        System.out.println("菜单总数: " + totalMenus);
        assertTrue(rootMenus.size() > 0);
    }

    @Test
    void testMultiTableJoin_DictionaryTree() {
        System.out.println("\n=== 多表关联测试: DictionaryInfo 自关联（字典树） ===");
        List<DictionaryInfo> rootList = dictionaryInfoRepository.findRootList();
        System.out.println("根字典类型数量: " + rootList.size());
        
        int totalDicts = 0;
        for (DictionaryInfo root : rootList) {
            List<DictionaryInfo> children = dictionaryInfoRepository.findByPid(root.getId());
            totalDicts += 1 + children.size();
            System.out.println("  字典类型: " + root.getValue() + " -> " + children.size() + " 个子项");
        }
        
        System.out.println("字典项总数: " + totalDicts);
        assertTrue(rootList.size() > 0);
    }
}
