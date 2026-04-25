package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 学生信息 Repository 测试类
 * 第 2 天任务：配合成员 2 做学生信息联调
 */
@SpringBootTest
class StudentInfoRepositoryTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private StudentService studentService;

    /**
     * 生成唯一学号（避免重复）
     */
    private String generateUniqueNum(String prefix) {
        return prefix + System.currentTimeMillis() % 10000;
    }

    /**
     * 测试 1：学生信息增加
     */
    @Test
    void testStudentCreate() {
        System.out.println("=== 测试：学生信息增加 ===");
        
        // 构造请求数据（使用唯一学号）
        String uniqueNum = generateUniqueNum("2025");
        System.out.println("使用学号: " + uniqueNum);
        Map<String, Object> form = new HashMap<>();
        form.put("num", uniqueNum);
        form.put("name", "测试学生");
        form.put("major", "计算机科学与技术");
        form.put("className", "计算机2401");
        form.put("dept", "计算机学院");
        form.put("gender", "1");
        form.put("birthday", "2000-01-01");
        form.put("email", "test@example.com");
        form.put("phone", "13800138000");
        form.put("address", "山东省济南市");
        
        DataRequest dataRequest = new DataRequest();
        dataRequest.add("form", form);
        
        // 调用保存方法
        DataResponse response = studentService.studentEditSave(dataRequest);
        System.out.println("新增学生响应: code=" + response.getCode() + ", msg=" + response.getMsg() + ", data=" + response.getData());
        
        assertNotNull(response);
        if (response.getCode() != 0) {
            System.out.println("❌ 测试失败原因: " + response.getMsg());
        }
        assertEquals(0, response.getCode(), "应该返回成功，错误信息：" + response.getMsg());
        
        // 验证数据已保存
        Integer personId = (Integer) response.getData();
        Optional<Student> op = studentRepository.findById(personId);
        assertTrue(op.isPresent(), "学生记录应该存在");
        
        Student student = op.get();
        assertEquals(uniqueNum, student.getPerson().getNum());
        assertEquals("测试学生", student.getPerson().getName());
        assertEquals("计算机科学与技术", student.getMajor());
        System.out.println("✓ 学生信息增加成功，personId: " + personId);
    }

    /**
     * 测试 2：学生信息删除
     */
    @Test
    void testStudentDelete() {
        System.out.println("\n=== 测试：学生信息删除 ===");
        
        // 先创建一个测试学生（使用唯一学号）
        String uniqueNum = generateUniqueNum("2025");
        System.out.println("使用学号: " + uniqueNum);
        Map<String, Object> form = new HashMap<>();
        form.put("num", uniqueNum);
        form.put("name", "待删除学生");
        form.put("major", "软件工程");
        form.put("className", "软件2401");
        
        DataRequest createRequest = new DataRequest();
        createRequest.add("form", form);
        DataResponse createResponse = studentService.studentEditSave(createRequest);
        System.out.println("创建响应: code=" + createResponse.getCode() + ", msg=" + createResponse.getMsg());
        
        if (createResponse.getCode() != 0) {
            System.out.println("❌ 创建失败，无法继续删除测试");
            fail("创建学生失败: " + createResponse.getMsg());
        }
        
        Integer personId = (Integer) createResponse.getData();
        System.out.println("创建测试学生，personId: " + personId);
        
        // 执行删除
        DataRequest deleteRequest = new DataRequest();
        deleteRequest.add("personId", personId);
        DataResponse deleteResponse = studentService.studentDelete(deleteRequest);
        System.out.println("删除学生响应: " + deleteResponse);
        
        assertNotNull(deleteResponse);
        assertEquals(0, deleteResponse.getCode(), "删除应该成功，错误信息：" + deleteResponse.getMsg());
        
        // 验证数据已删除
        Optional<Student> op = studentRepository.findById(personId);
        assertFalse(op.isPresent(), "学生记录应该已被删除");
        System.out.println("✓ 学生信息删除成功");
    }

    /**
     * 测试 3：学生信息修改
     */
    @Test
    void testStudentUpdate() {
        System.out.println("\n=== 测试：学生信息修改 ===");
        
        // 先创建一个测试学生（使用唯一学号）
        String uniqueNum = generateUniqueNum("2025");
        System.out.println("使用学号: " + uniqueNum);
        Map<String, Object> form = new HashMap<>();
        form.put("num", uniqueNum);
        form.put("name", "修改前姓名");
        form.put("major", "信息安全");
        form.put("className", "安全2401");
        
        DataRequest createRequest = new DataRequest();
        createRequest.add("form", form);
        DataResponse createResponse = studentService.studentEditSave(createRequest);
        System.out.println("创建响应: code=" + createResponse.getCode() + ", msg=" + createResponse.getMsg());
        
        if (createResponse.getCode() != 0) {
            System.out.println("❌ 创建失败，无法继续修改测试");
            fail("创建学生失败: " + createResponse.getMsg());
        }
        
        Integer personId = (Integer) createResponse.getData();
        System.out.println("创建测试学生，personId: " + personId);
        
        // 修改学生信息
        Map<String, Object> updateForm = new HashMap<>();
        updateForm.put("num", uniqueNum); // 学号不变
        updateForm.put("name", "修改后姓名");
        updateForm.put("major", "网络工程");
        updateForm.put("className", "网络2401");
        updateForm.put("dept", "信息学院");
        updateForm.put("gender", "2");
        updateForm.put("birthday", "2001-05-15");
        updateForm.put("email", "updated@example.com");
        updateForm.put("phone", "13900139000");
        
        DataRequest updateRequest = new DataRequest();
        updateRequest.add("personId", personId);
        updateRequest.add("form", updateForm);
        
        DataResponse updateResponse = studentService.studentEditSave(updateRequest);
        System.out.println("修改学生响应: " + updateResponse);
        
        assertNotNull(updateResponse);
        assertEquals(0, updateResponse.getCode(), "修改应该成功，错误信息：" + updateResponse.getMsg());
        
        // 验证数据已更新
        Optional<Student> op = studentRepository.findById(personId);
        assertTrue(op.isPresent());
        Student student = op.get();
        assertEquals("修改后姓名", student.getPerson().getName());
        assertEquals("网络工程", student.getMajor());
        assertEquals("网络2401", student.getClassName());
        System.out.println("✓ 学生信息修改成功");
    }

    /**
     * 测试 4：学生信息查询（单个）
     */
    @Test
    void testStudentQuerySingle() {
        System.out.println("\n=== 测试：学生信息查询（单个） ===");
        
        // 先创建一个测试学生（使用时间戳确保学号唯一）
        String uniqueNum = generateUniqueNum("2025");
        System.out.println("使用学号: " + uniqueNum);
        Map<String, Object> form = new HashMap<>();
        form.put("num", uniqueNum);
        form.put("name", "查询测试学生");
        form.put("major", "人工智能");
        form.put("className", "AI2401");
        
        DataRequest createRequest = new DataRequest();
        createRequest.add("form", form);
        DataResponse createResponse = studentService.studentEditSave(createRequest);
        System.out.println("创建响应: code=" + createResponse.getCode() + ", msg=" + createResponse.getMsg());
        
        assertNotNull(createResponse);
        if (createResponse.getCode() != 0) {
            System.out.println("❌ 创建失败，无法继续查询测试");
            fail("创建学生失败: " + createResponse.getMsg());
        }
        
        Integer personId = (Integer) createResponse.getData();
        
        // 查询单个学生
        DataRequest queryRequest = new DataRequest();
        queryRequest.add("personId", personId);
        DataResponse queryResponse = studentService.getStudentInfo(queryRequest);
        System.out.println("查询学生响应: " + queryResponse);
        
        assertNotNull(queryResponse);
        assertEquals(0, queryResponse.getCode(), "查询应该成功，错误信息：" + queryResponse.getMsg());
        assertNotNull(queryResponse.getData());
        System.out.println("✓ 学生信息查询成功");
    }

    /**
     * 测试 5：学生信息查询（列表）
     */
    @Test
    void testStudentQueryList() {
        System.out.println("\n=== 测试：学生信息查询（列表） ===");
        
        // 查询所有学生
        DataRequest queryAllRequest = new DataRequest();
        queryAllRequest.add("numName", "");
        DataResponse queryAllResponse = studentService.getStudentList(queryAllRequest);
        System.out.println("查询所有学生响应: " + queryAllResponse);
        assertNotNull(queryAllResponse);
        
        // 模糊查询
        DataRequest queryLikeRequest = new DataRequest();
        queryLikeRequest.add("numName", "2024");
        DataResponse queryLikeResponse = studentService.getStudentList(queryLikeRequest);
        System.out.println("模糊查询学生响应: " + queryLikeResponse);
        assertNotNull(queryLikeResponse);
        System.out.println("✓ 学生列表查询成功");
    }

    /**
     * 测试 6：按班级查询学生
     */
    @Test
    void testStudentQueryByClassName() {
        System.out.println("\n=== 测试：按班级查询学生 ===");
        
        List<Student> students = studentRepository.findByClassName("计算机2401");
        System.out.println("计算机2401 班级学生数: " + students.size());
        
        for (Student s : students) {
            System.out.println("  - " + s.getPerson().getName() + " (" + s.getPerson().getNum() + ")");
        }
        System.out.println("✓ 按班级查询成功");
    }

    /**
     * 测试 7：学号或姓名模糊查询
     */
    @Test
    void testStudentQueryByNumOrName() {
        System.out.println("\n=== 测试：学号或姓名模糊查询 ===");
        
        List<Student> students = studentRepository.findByPersonNumLikeOrPersonNameLike("%2024%", "%测试%");
        System.out.println("模糊查询结果数: " + students.size());
        
        for (Student s : students) {
            System.out.println("  - " + s.getPerson().getName() + " (" + s.getPerson().getNum() + ")");
        }
        System.out.println("✓ 模糊查询成功");
    }

    /**
     * 测试 8：分页查询学生
     */
    @Test
    void testStudentQueryPage() {
        System.out.println("\n=== 测试：分页查询学生 ===");
        
        DataRequest pageRequest = new DataRequest();
        pageRequest.add("numName", "");
        pageRequest.add("currentPage", 1);
        
        DataResponse pageResponse = studentService.getStudentPageData(pageRequest);
        System.out.println("分页查询响应: " + pageResponse);
        assertNotNull(pageResponse);
        
        Map<String, Object> data = (Map<String, Object>) pageResponse.getData();
        assertNotNull(data);
        System.out.println("总记录数: " + data.get("dataTotal"));
        System.out.println("每页大小: " + data.get("pageSize"));
        System.out.println("✓ 分页查询成功");
    }

    /**
     * 测试 9：学号重复性检查
     */
    @Test
    void testStudentDuplicateNum() {
        System.out.println("\n=== 测试：学号重复性检查 ===");
        
        // 创建第一个学生（使用唯一学号）
        String uniqueNum = generateUniqueNum("2025");
        System.out.println("使用学号: " + uniqueNum);
        Map<String, Object> form1 = new HashMap<>();
        form1.put("num", uniqueNum);
        form1.put("name", "学生A");
        form1.put("major", "计算机");
        form1.put("className", "CS2401");
        
        DataRequest request1 = new DataRequest();
        request1.add("form", form1);
        DataResponse response1 = studentService.studentEditSave(request1);
        System.out.println("第一次创建响应: code=" + response1.getCode() + ", msg=" + response1.getMsg());
        
        // 尝试创建相同学号的学生
        Map<String, Object> form2 = new HashMap<>();
        form2.put("num", uniqueNum); // 相同学号
        form2.put("name", "学生B");
        form2.put("major", "软件");
        form2.put("className", "SE2401");
        
        DataRequest request2 = new DataRequest();
        request2.add("form", form2);
        DataResponse response2 = studentService.studentEditSave(request2);
        System.out.println("第二次创建响应（应失败）: " + response2);
        
        assertNotEquals(0, response2.getCode(), "学号重复应该返回错误");
        System.out.println("✓ 学号重复检查成功");
    }

    /**
     * 测试 10：Student 与 Person 关联查询
     */
    @Test
    void testStudentPersonAssociation() {
        System.out.println("\n=== 测试：Student 与 Person 关联查询 ===");
        
        // 查询所有学生并验证关联
        List<Student> students = studentRepository.findAll();
        System.out.println("总学生数: " + students.size());
        
        int validCount = 0;
        for (Student s : students) {
            Person p = s.getPerson();
            if (p != null) {
                validCount++;
                System.out.println("  学生: " + p.getName() + 
                                 ", 学号: " + p.getNum() + 
                                 ", 专业: " + s.getMajor() + 
                                 ", 班级: " + s.getClassName());
            }
        }
        System.out.println("有效关联记录: " + validCount + "/" + students.size());
        System.out.println("✓ Student-Person 关联查询成功");
    }
}
