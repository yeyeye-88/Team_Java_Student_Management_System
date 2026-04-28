package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.StudentLeaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 请假管理控制器
 * 提供请假申请、审批、查询等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/studentLeave")
public class StudentLeaveController {

    @Autowired
    private StudentLeaveService studentLeaveService;

    /**
     * 查询请假列表
     * 管理员/教师：查询所有请假记录
     * 学生：只能查询自己的请假记录
     */
    @PostMapping("/getStudentLeaveList")
    public DataResponse getStudentLeaveList(@RequestBody DataRequest dataRequest) {
        return studentLeaveService.getStudentLeaveList(dataRequest);
    }

    /**
     * 学生提交请假申请
     * 状态默认为 0（待审批）
     */
    @PostMapping("/studentLeaveApply")
    public DataResponse studentLeaveApply(@RequestBody DataRequest dataRequest) {
        return studentLeaveService.studentLeaveApply(dataRequest);
    }

    /**
     * 管理员/教师审批请假
     * state: 1 = 通过，2 = 驳回
     */
    @PostMapping("/studentLeaveApprove")
    public DataResponse studentLeaveApprove(@RequestBody DataRequest dataRequest) {
        return studentLeaveService.studentLeaveApprove(dataRequest);
    }
}
