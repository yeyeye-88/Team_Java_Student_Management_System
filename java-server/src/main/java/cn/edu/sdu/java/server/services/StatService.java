package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.StudentLeave;
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
                
                // 获取课程名称
                String courseName = "";
                Optional<Course> courseOpt = courseRepository.findById(courseId);
                if (courseOpt.isPresent()) {
                    courseName = courseOpt.get().getName();
                }
                
                m.put("courseId", courseId);
                m.put("courseName", courseName);
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
            // 获取按课程和状态分类的统计数据
            List<Object[]> resultList = attendanceRepository.getAttendanceStatsByCourseAndState();
            
            // 按课程分组
            Map<Integer, Map<String, Object>> courseMap = new LinkedHashMap<>();
            
            // 状态码映射：0-未到，1-迟到，2-早退，3-正常，4-请假
            Map<Integer, String> statusMap = new HashMap<>();
            statusMap.put(0, "未到");
            statusMap.put(1, "迟到");
            statusMap.put(2, "早退");
            statusMap.put(3, "正常");
            statusMap.put(4, "请假");
            
            for (Object[] row : resultList) {
                Integer courseId = ((Number) row[0]).intValue();
                Integer state = ((Number) row[1]).intValue();
                Long count = (Long) row[2];
                
                // 如果该课程还没有在map中，初始化
                if (!courseMap.containsKey(courseId)) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("courseId", courseId);
                    
                    // 获取课程名称
                    String courseName = "";
                    Optional<Course> courseOpt = courseRepository.findById(courseId);
                    if (courseOpt.isPresent()) {
                        courseName = courseOpt.get().getName();
                    }
                    m.put("courseName", courseName);
                    m.put("totalCount", 0L);
                    
                    // 初始化各状态的计数
                    for (int i = 0; i <= 4; i++) {
                        m.put(statusMap.get(i), 0L);
                    }
                    courseMap.put(courseId, m);
                }
                
                // 更新该课程的统计数据
                Map<String, Object> courseData = courseMap.get(courseId);
                courseData.put("totalCount", (Long) courseData.get("totalCount") + count);
                courseData.put(statusMap.get(state), count);
            }
            
            // 计算考勤率（正常出勤的比例）
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (Map<String, Object> courseData : courseMap.values()) {
                Long totalCount = (Long) courseData.get("totalCount");
                Long normalCount = (Long) courseData.get("正常");
                
                Double attendanceRate = 0.0;
                if (totalCount > 0) {
                    attendanceRate = Math.round((normalCount * 100.0 / totalCount) * 100.0) / 100.0;
                }
                
                courseData.put("attendanceRate", attendanceRate);
                courseData.put("statusName", "正常"); // 主要统计的是正常出勤率
                dataList.add(courseData);
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
