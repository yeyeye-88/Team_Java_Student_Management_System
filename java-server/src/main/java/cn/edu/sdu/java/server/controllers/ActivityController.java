package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.ActivityService;
import cn.edu.sdu.java.server.util.CommonMethod;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * ActivityController 活动管理接口层
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    /**
     * 发布校园活动
     */
    @PostMapping("/publish")
    public DataResponse publishActivity(@Valid @RequestBody DataRequest dataRequest) {
        return activityService.publishActivity(dataRequest);
    }

    /**
     * 查询活动列表
     */
    @PostMapping("/list")
    public DataResponse getActivityList(@RequestBody(required = false) DataRequest dataRequest) {
        return activityService.getActivityList();
    }

    /**
     * 参与活动
     */
    @PostMapping("/join")
    public DataResponse joinActivity(@Valid @RequestBody DataRequest dataRequest) {
        return activityService.joinActivity(dataRequest);
    }

    /**
     * 查询参与记录
     */
    @PostMapping("/participation")
    public DataResponse getParticipationList(@RequestBody(required = false) DataRequest dataRequest) {
        Integer activityId = dataRequest != null ? CommonMethod.getInteger(dataRequest.getMap("data"), "activityId") : null;
        Integer personId = dataRequest != null ? CommonMethod.getInteger(dataRequest.getMap("data"), "personId") : null;
        return activityService.getParticipationList(activityId, personId);
    }

    /**
     * 取消活动
     */
    @PostMapping("/cancel")
    public DataResponse cancelActivity(@Valid @RequestBody DataRequest dataRequest) {
        return activityService.cancelActivity(dataRequest);
    }
}
