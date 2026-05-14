package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.CourseScheduleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * CourseScheduleController 课表安排管理接口层
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/courseSchedule")
public class CourseScheduleController {

    private final CourseScheduleService courseScheduleService;

    public CourseScheduleController(CourseScheduleService courseScheduleService) {
        this.courseScheduleService = courseScheduleService;
    }

    /**
     * 添加课表安排
     * 权限：管理员/教师
     */
    @PostMapping("/addSchedule")
    public DataResponse addSchedule(@Valid @RequestBody DataRequest dataRequest) {
        return courseScheduleService.addSchedule(dataRequest);
    }

    /**
     * 查询课表列表
     * 权限：管理员可查全部，学生/教师只能查自己的
     */
    @PostMapping("/list")
    public DataResponse getScheduleList(@RequestBody(required = false) DataRequest dataRequest) {
        if (dataRequest == null) dataRequest = new DataRequest();
        return courseScheduleService.getScheduleList(dataRequest);
    }

    /**
     * 修改课表安排
     * 权限：管理员/教师
     */
    @PostMapping("/updateSchedule")
    public DataResponse updateSchedule(@Valid @RequestBody DataRequest dataRequest) {
        return courseScheduleService.updateSchedule(dataRequest);
    }

    /**
     * 删除课表安排（软删除）
     * 权限：管理员
     */
    @PostMapping("/deleteSchedule")
    public DataResponse deleteSchedule(@Valid @RequestBody DataRequest dataRequest) {
        return courseScheduleService.deleteSchedule(dataRequest);
    }
}
