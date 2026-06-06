package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.InnovationService;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * InnovationController 实践创新管理接口层
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/innovation")
public class InnovationController {

    private final InnovationService innovationService;

    public InnovationController(InnovationService innovationService) {
        this.innovationService = innovationService;
    }

    /**
     * 创建创新项目
     */
    @PostMapping("/createProject")
    public DataResponse createProject(@Valid @RequestBody DataRequest dataRequest) {
        return innovationService.createProject(dataRequest);
    }

    /**
     * 查询项目列表
     */
    @PostMapping("/projectList")
    public DataResponse getProjectList(@RequestBody(required = false) DataRequest dataRequest) {
        Integer status = dataRequest != null ? CommonMethod.getInteger(dataRequest.getMap("data"), "status") : null;
        return innovationService.getProjectList(status);
    }

    /**
     * 提交实践成果
     */
    @PostMapping("/submitAchievement")
    public DataResponse submitAchievement(@Valid @RequestBody DataRequest dataRequest) {
        return innovationService.submitAchievement(dataRequest);
    }

    /**
     * 查询成果列表
     */
    @PostMapping("/achievementList")
    public DataResponse getAchievementList(@RequestBody(required = false) DataRequest dataRequest) {
        Integer projectId = dataRequest != null ? CommonMethod.getInteger(dataRequest.getMap("data"), "projectId") : null;
        Integer personId = dataRequest != null ? CommonMethod.getInteger(dataRequest.getMap("data"), "personId") : null;
        return innovationService.getAchievementList(projectId, personId);
    }

    /**
     * 终止项目
     */
    @PostMapping("/terminateProject")
    public DataResponse terminateProject(@Valid @RequestBody DataRequest dataRequest) {
        return innovationService.terminateProject(dataRequest);
    }
}
