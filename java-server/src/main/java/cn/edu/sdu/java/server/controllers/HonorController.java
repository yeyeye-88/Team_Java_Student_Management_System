package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.HonorService;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * HonorController 荣誉管理接口层
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/honor")
public class HonorController {

    private final HonorService honorService;

    public HonorController(HonorService honorService) {
        this.honorService = honorService;
    }

    /**
     * 录入学生奖项
     * 权限：管理员/教师
     */
    @PostMapping("/addHonor")
    public DataResponse addHonor(@Valid @RequestBody DataRequest dataRequest) {
        return honorService.addHonor(dataRequest);
    }

    /**
     * 学生提交荣誉申请
     * 权限：学生
     */
    @PostMapping("/submitHonor")
    public DataResponse submitHonor(@Valid @RequestBody DataRequest dataRequest) {
        return honorService.submitHonor(dataRequest);
    }

    /**
     * 审核荣誉（通过/驳回）
     * 权限：管理员/教师
     */
    @PostMapping("/auditHonor")
    public DataResponse auditHonor(@Valid @RequestBody DataRequest dataRequest) {
        return honorService.auditHonor(dataRequest);
    }

    /**
     * 查询荣誉列表
     * 权限：管理员可查全部，学生只能查自己
     */
    @PostMapping("/list")
    public DataResponse getHonorList(@RequestBody(required = false) DataRequest dataRequest) {
        if (dataRequest == null) return honorService.getHonorList(null, null);
        Integer personId = dataRequest.getInteger("personId");
        String level = dataRequest.getString("level");
        return honorService.getHonorList(personId, level);
    }
}
