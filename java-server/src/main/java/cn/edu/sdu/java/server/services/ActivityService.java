package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Activity;
import cn.edu.sdu.java.server.models.ActivityParticipation;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.ActivityParticipationRepository;
import cn.edu.sdu.java.server.repositorys.ActivityRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ActivityService 活动管理业务逻辑层
 */
@Slf4j
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityParticipationRepository participationRepository;

    public ActivityService(ActivityRepository activityRepository, ActivityParticipationRepository participationRepository) {
        this.activityRepository = activityRepository;
        this.participationRepository = participationRepository;
    }

    /**
     * 发布校园活动（仅管理员和教师）
     */
    public DataResponse publishActivity(DataRequest dataRequest) {
        try {
            // 权限校验：管理员或教师可以发布活动
            if (!RoleCheckUtil.isAdminOrTeacher()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员和教师可以发布活动！");
            }

            Map<String, Object> form = dataRequest.getMap("form");
            String name = CommonMethod.getString(form, "name");
            String location = CommonMethod.getString(form, "location");
            Date startTime = CommonMethod.getTime(form, "startTime");
            Date endTime = CommonMethod.getTime(form, "endTime");
            String description = CommonMethod.getString(form, "description");

            String errorMsg = ParamCheckUtil.checkRequired(name, "活动名称");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 检查活动名称是否已存在
            Activity existingActivity = activityRepository.findByName(name);
            if (existingActivity != null) {
                return CommonMethod.getReturnMessageError("活动名称已存在，请勿重复发布！");
            }

            Activity activity = new Activity();
            activity.setName(name);
            activity.setLocation(location);
            activity.setStartTime(startTime);
            activity.setEndTime(endTime);
            activity.setDescription(description);
            activity.setPublisherId(CommonMethod.getPersonId());
            activity.setCreateTime(new Date());

            activityRepository.save(activity);
            log.info("发布活动成功，活动名称: {}, 操作人: {}", name, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(activity.getActivityId());
        } catch (Exception e) {
            log.error("发布活动失败", e);
            throw new RuntimeException("发布失败：" + e.getMessage());
        }
    }

    /**
     * 查询活动列表
     */
    public DataResponse getActivityList() {
        try {
            List<Activity> activities = activityRepository.findAllOrderByCreateTime();
            List<Map<String, Object>> list = new ArrayList<>();

            for (Activity activity : activities) {
                Map<String, Object> m = new HashMap<>();
                m.put("activityId", activity.getActivityId());
                m.put("name", activity.getName());
                m.put("location", activity.getLocation());
                m.put("startTime", activity.getStartTime());
                m.put("endTime", activity.getEndTime());
                m.put("description", activity.getDescription());
                m.put("publisherId", activity.getPublisherId());
                m.put("createTime", activity.getCreateTime());
                list.add(m);
            }

            return CommonMethod.getReturnData(list);
        } catch (Exception e) {
            log.error("查询活动列表失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 参与活动（学生或管理员）
     */
    public DataResponse joinActivity(DataRequest dataRequest) {
        try {
            Map<String, Object> form = dataRequest.getMap("form");
            Integer activityId = CommonMethod.getInteger(form, "activityId");
            Integer personId = CommonMethod.getInteger(form, "personId");

            String errorMsg = ParamCheckUtil.checkRequired(activityId != null ? activityId.toString() : "", "活动 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 权限校验：只有学生可以参与活动
            if (!RoleCheckUtil.hasRole("STUDENT")) {
                return CommonMethod.getReturnMessageError("抱歉，活动参与功能仅对学生开放！教师和管理员可以发布和管理活动，但不能参与活动报名。");
            }
            
            Integer currentPersonId = CommonMethod.getPersonId();
            if (personId != null && !currentPersonId.equals(personId)) {
                return CommonMethod.getReturnMessageError("权限不足，只能为自己报名！");
            }
            personId = currentPersonId;

            // 检查是否已参与
            ActivityParticipation existing = participationRepository.findByActivityIdAndPersonId(activityId, personId);
            if (existing != null) {
                return CommonMethod.getReturnMessageError("您已参与该活动，请勿重复报名！");
            }

            // 检查活动是否存在
            Optional<Activity> activityOpt = activityRepository.findById(activityId);
            if (activityOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("活动不存在！");
            }

            ActivityParticipation participation = new ActivityParticipation();
            participation.setActivity(activityOpt.get());
            participation.setPersonId(personId);
            participation.setJoinTime(new Date());

            participationRepository.save(participation);
            log.info("参与活动成功，activityId: {}, personId: {}", activityId, personId);
            return CommonMethod.getReturnData(participation.getParticipationId());
        } catch (Exception e) {
            log.error("参与活动失败", e);
            throw new RuntimeException("参与失败：" + e.getMessage());
        }
    }

    /**
     * 查询活动参与记录
     */
    public DataResponse getParticipationList(Integer activityId, Integer personId) {
        try {
            List<ActivityParticipation> participations;

            if (activityId != null) {
                participations = participationRepository.findByActivityId(activityId);
            } else if (personId != null) {
                // 权限校验：学生只能查自己的
                if (RoleCheckUtil.hasRole("STUDENT")) {
                    Integer currentPersonId = CommonMethod.getPersonId();
                    if (!currentPersonId.equals(personId)) {
                        return CommonMethod.getReturnMessageError("权限不足，只能查询自己的参与记录！");
                    }
                }
                participations = participationRepository.findByPersonId(personId);
            } else {
                participations = participationRepository.findAll();
            }

            List<Map<String, Object>> list = new ArrayList<>();
            for (ActivityParticipation p : participations) {
                Map<String, Object> m = new HashMap<>();
                m.put("participationId", p.getParticipationId());
                m.put("activityId", p.getActivity().getActivityId());
                m.put("activityName", p.getActivity().getName());
                m.put("personId", p.getPersonId());
                m.put("joinTime", p.getJoinTime());
                m.put("remark", p.getRemark());
                list.add(m);
            }

            return CommonMethod.getReturnData(list);
        } catch (Exception e) {
            log.error("查询参与记录失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 取消活动（管理员或教师）
     */
    public DataResponse cancelActivity(DataRequest dataRequest) {
        try {
            // 权限校验：管理员或教师
            if (!RoleCheckUtil.isAdminOrTeacher()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员和教师可以取消活动！");
            }

            Map<String, Object> form = dataRequest.getMap("form");
            Integer activityId = CommonMethod.getInteger(form, "activityId");

            String errorMsg = ParamCheckUtil.checkRequired(activityId != null ? activityId.toString() : "", "活动 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            Optional<Activity> activityOpt = activityRepository.findById(activityId);
            if (activityOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("活动不存在！");
            }

            Activity activity = activityOpt.get();
            activity.setStatus(3); // 3=已取消
            activityRepository.save(activity);

            log.info("取消活动成功，activityId: {}, 操作人: {}", activityId, CommonMethod.getPersonId());
            return CommonMethod.getReturnMessageOK("活动已取消");
        } catch (Exception e) {
            log.error("取消活动失败", e);
            throw new RuntimeException("取消失败：" + e.getMessage());
        }
    }
}
