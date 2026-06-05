package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.StudentLeaveService;
import cn.edu.sdu.java.server.util.CommonMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 请假审批控制器（兼容前端旧路径）
 */
@Slf4j
@RestController
@RequestMapping("/api/leave")
public class LeaveController {

    @Autowired
    private StudentLeaveService studentLeaveService;

    /**
     * 学生提交请假申请 - 兼容前端路径 /api/leave/submit
     */
    @PostMapping("/submit")
    public DataResponse submitLeave(@RequestBody DataRequest dataRequest) {
        log.info("收到请假申请请求，路径: /api/leave/submit");
        return studentLeaveService.studentLeaveApply(dataRequest);
    }

    /**
     * 审批请假 - 兼容前端路径 /api/leave/approve
     */
    @PostMapping("/approve")
    public DataResponse approveLeave(@RequestBody DataRequest dataRequest) {
        log.info("收到审批请求，路径: /api/leave/approve");
        return studentLeaveService.studentLeaveApprove(dataRequest);
    }
}
