package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * AttendanceController 考勤管理接口
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService attendanceService;
    
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    /**
     * 查询考勤列表
     */
    @PostMapping("/getAttendanceList")
    public DataResponse getAttendanceList(@Valid @RequestBody DataRequest dataRequest) {
        return attendanceService.getAttendanceList(dataRequest);
    }

    /**
     * 保存考勤（新增或修改）
     */
    @PostMapping("/attendanceSave")
    public DataResponse attendanceSave(@Valid @RequestBody DataRequest dataRequest) {
        return attendanceService.attendanceSave(dataRequest);
    }

    /**
     * 删除考勤记录
     */
    @PostMapping("/attendanceDelete")
    public DataResponse attendanceDelete(@Valid @RequestBody DataRequest dataRequest) {
        return attendanceService.attendanceDelete(dataRequest);
    }
}
