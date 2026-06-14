package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.TeacherService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * TeacherController 教师管理接口层
 * 提供教师信息查询、修改接口
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    /**
     * 获取教师列表（管理员查看所有，教师只能查看自己）
     * @param dataRequest 包含查询条件（可选）
     * @return 教师列表
     */
    @PostMapping("/getTeacherList")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getTeacherList(@Valid @RequestBody DataRequest dataRequest) {
        return teacherService.getTeacherList(dataRequest);
    }

    /**
     * 获取教师详细信息
     * @param dataRequest 包含 personId（教师主键）
     * @return 教师详细信息
     */
    @PostMapping("/getTeacherInfo")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse getTeacherInfo(@Valid @RequestBody DataRequest dataRequest) {
        return teacherService.getTeacherInfo(dataRequest);
    }

    /**
     * 保存教师信息（仅支持修改，不支持新增）
     * @param dataRequest 包含 personId 和 form（表单数据）
     * @return 修改结果
     */
    @PostMapping("/teacherEditSave")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse teacherEditSave(@Valid @RequestBody DataRequest dataRequest) {
        return teacherService.teacherEditSave(dataRequest);
    }
}
