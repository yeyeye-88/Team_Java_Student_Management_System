package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.PunishmentService;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * PunishmentController 处分管理接口层
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/punishment")
public class PunishmentController {

    private final PunishmentService punishmentService;

    public PunishmentController(PunishmentService punishmentService) {
        this.punishmentService = punishmentService;
    }

    /**
     * 记录违纪行为
     * 权限：仅管理员
     */
    @PostMapping("/addPunishment")
    public DataResponse addPunishment(@Valid @RequestBody DataRequest dataRequest) {
        return punishmentService.addPunishment(dataRequest);
    }

    /**
     * 添加处理结果
     * 权限：仅管理员
     */
    @PostMapping("/addResult")
    public DataResponse addPunishmentResult(@Valid @RequestBody DataRequest dataRequest) {
        return punishmentService.addPunishmentResult(dataRequest);
    }

    /**
     * 查询处分列表
     * 权限：管理员可查全部，学生只能查自己
     */
    @PostMapping("/list")
    public DataResponse getPunishmentList(@RequestBody(required = false) DataRequest dataRequest) {
        if (dataRequest == null) return punishmentService.getPunishmentList(null, null);
        Integer personId = dataRequest.getInteger("personId");
        Integer status = dataRequest.getInteger("status");
        return punishmentService.getPunishmentList(personId, status);
    }

    /**
     * 撤销处分（软删除）
     * 权限：仅管理员
     */
    @PostMapping("/revoke")
    public DataResponse revokePunishment(@Valid @RequestBody DataRequest dataRequest) {
        return punishmentService.revokePunishment(dataRequest);
    }

    /**
     * 查询处理结果列表
     * 权限：关联处分记录可见
     */
    @PostMapping("/resultList")
    public DataResponse getResultList(@RequestBody(required = false) DataRequest dataRequest) {
        if (dataRequest == null) return punishmentService.getResultList(null);
        Integer punishmentId = dataRequest.getInteger("punishmentId");
        return punishmentService.getResultList(punishmentId);
    }
}
