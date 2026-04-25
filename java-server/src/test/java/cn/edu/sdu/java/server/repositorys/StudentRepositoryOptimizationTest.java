package cn.edu.sdu.java.server.repositorys;

import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.services.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 第 3 天任务：Repository 方法优化测试
 * 测试索引添加后的查询性能提升
 */
@SpringBootTest
class StudentRepositoryOptimizationTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentService studentService;

    /**
     * 测试：按班级查询性能测试（使用索引）
     */
    @Test
    void testFindByClassNamePerformance() {
        System.out.println("\n=== 测试：按班级查询性能（使用索引） ===");
        
        // 测试多次查询取平均值
        int iterations = 10;
        long totalTime = 0;
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            
            // 执行查询
            List<Student> students = studentRepository.findByClassName("计算机2401");
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime += duration;
            
            System.out.println("第 " + (i + 1) + " 次查询耗时: " + duration + "ms, 结果数: " + students.size());
        }
        
        long avgTime = totalTime / iterations;
        System.out.println("\n✓ 平均查询时间: " + avgTime + "ms");
        System.out.println("✓ 索引查询性能测试完成");
    }

    /**
     * 测试：模糊查询性能测试
     */
    @Test
    void testFuzzyQueryPerformance() {
        System.out.println("\n=== 测试：模糊查询性能 ===");
        
        int iterations = 10;
        long totalTime = 0;
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            
            // 执行模糊查询
            List<Student> students = studentRepository.findStudentListByNumName("2024");
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime += duration;
            
            System.out.println("第 " + (i + 1) + " 次查询耗时: " + duration + "ms, 结果数: " + students.size());
        }
        
        long avgTime = totalTime / iterations;
        System.out.println("\n✓ 平均查询时间: " + avgTime + "ms");
        System.out.println("✓ 模糊查询性能测试完成");
    }

    /**
     * 测试：分页查询性能测试
     */
    @Test
    void testPageQueryPerformance() {
        System.out.println("\n=== 测试：分页查询性能 ===");
        
        int iterations = 10;
        long totalTime = 0;
        
        for (int i = 0; i < iterations; i++) {
            long startTime = System.currentTimeMillis();
            
            // 执行分页查询
            var students = studentService.getStudentPageData(
                new cn.edu.sdu.java.server.payload.request.DataRequest()
            );
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            totalTime += duration;
            
            System.out.println("第 " + (i + 1) + " 次查询耗时: " + duration + "ms");
        }
        
        long avgTime = totalTime / iterations;
        System.out.println("\n✓ 平均查询时间: " + avgTime + "ms");
        System.out.println("✓ 分页查询性能测试完成");
    }
}
