package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * StatService 统计模块业务逻辑层
 * 实现 4 类核心统计：班级学生数、课程平均分、考勤率、请假通过率
 */
@Slf4j
@Service
public class StatService {

    private final StudentRepository studentRepository;
    private final ScoreRepository scoreRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentLeaveRepository studentLeaveRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public StatService(StudentRepository studentRepository,
                       ScoreRepository scoreRepository,
                       AttendanceRepository attendanceRepository,
                       StudentLeaveRepository studentLeaveRepository,
                       CourseRepository courseRepository,
                       UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.scoreRepository = scoreRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentLeaveRepository = studentLeaveRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    /**
     * 统计各班级学生数
     * @return 班级学生数统计列表
     */
    public DataResponse getClassStudentCount() {
        try {
            List<Object[]> resultList = studentRepository.countStudentsByClass();
            List<Map<String, Object>> dataList = new ArrayList<>();
            
            for (Object[] row : resultList) {
                Map<String, Object> m = new HashMap<>();
                m.put("className", row[0]);
                m.put("studentCount", row[1]);
                dataList.add(m);
            }
            
            log.info("查询班级学生数统计成功，共 {} 个班级", dataList.size());
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            log.error("查询班级学生数统计失败", e);
            throw new RuntimeException("统计失败：" + e.getMessage());
        }
    }

    /**
     * 统计各课程平均分
     * @return 课程平均分统计列表
     */
    public DataResponse getCourseScoreAvg() {
        try {
            List<Object[]> resultList = scoreRepository.getAverageScoreByCourse();
            List<Map<String, Object>> dataList = new ArrayList<>();
            
            for (Object[] row : resultList) {
                Map<String, Object> m = new HashMap<>();
                Integer courseId = ((Number) row[0]).intValue();
                Double avgScore = (Double) row[1];
                
                // 保留两位小数
                avgScore = Math.round(avgScore * 100.0) / 100.0;
                
                m.put("courseId", courseId);
                m.put("avgScore", avgScore);
                dataList.add(m);
            }
            
            log.info("查询课程平均分统计成功，共 {} 门课程", dataList.size());
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            log.error("查询课程平均分统计失败", e);
            throw new RuntimeException("统计失败：" + e.getMessage());
        }
    }

    /**
     * 统计各课程考勤率
     * @return 课程考勤率统计列表
     */
    public DataResponse getAttendanceRate() {
        try {
            List<Object[]> resultList = attendanceRepository.getAttendanceRateByCourse();
            List<Map<String, Object>> dataList = new ArrayList<>();
            
            for (Object[] row : resultList) {
                Map<String, Object> m = new HashMap<>();
                Integer courseId = ((Number) row[0]).intValue();
                Double attendanceRate = (Double) row[1];
                
                // 保留两位小数
                attendanceRate = Math.round(attendanceRate * 100.0) / 100.0;
                
                m.put("courseId", courseId);
                m.put("attendanceRate", attendanceRate);
                dataList.add(m);
            }
            
            log.info("查询课程考勤率统计成功，共 {} 门课程", dataList.size());
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            log.error("查询课程考勤率统计失败", e);
            throw new RuntimeException("统计失败：" + e.getMessage());
        }
    }

    /**
     * 统计请假通过率
     * @return 请假通过率（百分比）
     */
    public DataResponse getLeavePassRate() {
        try {
            Double passRate = studentLeaveRepository.getLeavePassRate();
            
            Map<String, Object> data = new HashMap<>();
            if (passRate != null) {
                // 保留两位小数
                passRate = Math.round(passRate * 100.0) / 100.0;
                data.put("passRate", passRate);
            } else {
                data.put("passRate", 0.0);
            }
            
            log.info("查询请假通过率统计成功，通过率：{}%", data.get("passRate"));
            return CommonMethod.getReturnData(data);
        } catch (Exception e) {
            log.error("查询请假通过率统计失败", e);
            throw new RuntimeException("统计失败：" + e.getMessage());
        }
    }

    /**
     * 获取仪表盘汇总数据
     * @return 包含总学生数、总课程数、待审批请假数的 Map
     */
    public DataResponse getDashboardData() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // 1. 总学生数
            dashboard.put("totalStudents", studentRepository.count());
            
            // 2. 总课程数
            dashboard.put("totalCourses", courseRepository.count());
            
            // 3. 待审批请假数 (state=0)
            long pendingLeaves = studentLeaveRepository.findAll().stream()
                    .filter(leave -> leave.getState() != null && leave.getState() == 0)
                    .count();
            dashboard.put("pendingLeaves", pendingLeaves);
            
            // 4. 平均出勤率 (简化计算，取所有课程的平均值)
            List<Object[]> attendanceRates = attendanceRepository.getAttendanceRateByCourse();
            double avgRate = 0.0;
            if (!attendanceRates.isEmpty()) {
                double sum = 0.0;
                for (Object[] row : attendanceRates) {
                    sum += (Double) row[1];
                }
                avgRate = Math.round((sum / attendanceRates.size()) * 100.0) / 100.0;
            }
            dashboard.put("avgAttendanceRate", avgRate);
            
            log.info("查询仪表盘汇总数据成功");
            return CommonMethod.getReturnData(dashboard);
        } catch (Exception e) {
            log.error("查询仪表盘汇总数据失败", e);
            throw new RuntimeException("统计失败：" + e.getMessage());
        }
    }
}
