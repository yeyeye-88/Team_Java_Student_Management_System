package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.StatService;
import org.springframework.web.bind.annotation.*;

/**
 * StatController 统计模块接口层
 * 提供 4 类统计接口：班级学生数、课程平均分、考勤率、请假通过率
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/stat")
public class StatController {

    private final StatService statService;

    public StatController(StatService statService) {
        this.statService = statService;
    }

    /**
     * 统计各班级学生数
     * @return 班级学生数列表
     */
    @PostMapping("/classStudentCount")
    public DataResponse getClassStudentCount() {
        return statService.getClassStudentCount();
    }

    /**
     * 统计各课程平均分
     * @return 课程平均分列表
     */
    @PostMapping("/courseScoreAvg")
    public DataResponse getCourseScoreAvg() {
        return statService.getCourseScoreAvg();
    }

    /**
     * 统计各课程考勤率
     * @return 课程考勤率列表
     */
    @PostMapping("/attendanceRate")
    public DataResponse getAttendanceRate() {
        return statService.getAttendanceRate();
    }

    /**
     * 统计请假通过率
     * @return 请假通过率（百分比）
     */
    @PostMapping("/leavePassRate")
    public DataResponse getLeavePassRate() {
        return statService.getLeavePassRate();
    }

    /**
     * 获取仪表盘汇总数据
     * @return 核心统计数据汇总
     */
    @PostMapping("/dashboard")
    public DataResponse getDashboard() {
        return statService.getDashboardData();
    }
}
