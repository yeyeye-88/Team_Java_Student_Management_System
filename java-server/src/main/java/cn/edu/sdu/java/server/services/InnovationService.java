package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.InnovationAchievement;
import cn.edu.sdu.java.server.models.InnovationProject;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.InnovationAchievementRepository;
import cn.edu.sdu.java.server.repositorys.InnovationProjectRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * InnovationService 实践创新管理业务逻辑层
 */
@Slf4j
@Service
public class InnovationService {

    private final InnovationProjectRepository projectRepository;
    private final InnovationAchievementRepository achievementRepository;

    public InnovationService(InnovationProjectRepository projectRepository, InnovationAchievementRepository achievementRepository) {
        this.projectRepository = projectRepository;
        this.achievementRepository = achievementRepository;
    }

    /**
     * 创建创新项目（管理员或教师）
     */
    public DataResponse createProject(DataRequest dataRequest) {
        try {
            // 权限校验：管理员或教师可以创建项目
            if (!RoleCheckUtil.isAdminOrTeacher()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员和教师可以创建项目！");
            }

            Map<String, Object> form = dataRequest.getMap("form");
            String name = CommonMethod.getString(form, "name");
            String category = CommonMethod.getString(form, "category");
            Integer leaderId = CommonMethod.getInteger(form, "leaderId");
            String teamMembers = CommonMethod.getString(form, "teamMembers");
            String description = CommonMethod.getString(form, "description");
            Integer status = CommonMethod.getInteger0(form, "status");
            Date startDate = CommonMethod.getDate(form, "startDate");
            Date endDate = CommonMethod.getDate(form, "endDate");

            String errorMsg = ParamCheckUtil.checkRequired(name, "项目名称");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 检查项目名称是否已存在
            InnovationProject existingProject = projectRepository.findByName(name);
            if (existingProject != null) {
                return CommonMethod.getReturnMessageError("项目名称已存在，请勿重复创建！");
            }

            InnovationProject project = new InnovationProject();
            project.setName(name);
            project.setCategory(category);
            project.setLeaderId(leaderId);
            project.setTeamMembers(teamMembers);
            project.setDescription(description);
            project.setStatus(status);
            project.setStartDate(startDate);
            project.setEndDate(endDate);
            project.setCreateTime(new Date());

            projectRepository.save(project);
            log.info("创建创新项目成功，项目名称: {}, 操作人: {}", name, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(project.getProjectId());
        } catch (Exception e) {
            log.error("创建创新项目失败", e);
            throw new RuntimeException("创建失败：" + e.getMessage());
        }
    }

    /**
     * 查询项目列表
     */
    public DataResponse getProjectList(Integer status) {
        try {
            List<InnovationProject> projects;
            if (status != null) {
                projects = projectRepository.findByStatus(status);
            } else {
                projects = projectRepository.findAllOrderByCreateTime();
            }

            List<Map<String, Object>> list = new ArrayList<>();
            for (InnovationProject project : projects) {
                Map<String, Object> m = new HashMap<>();
                m.put("projectId", project.getProjectId());
                m.put("name", project.getName());
                m.put("category", project.getCategory());
                m.put("leaderId", project.getLeaderId());
                m.put("teamMembers", project.getTeamMembers());
                m.put("description", project.getDescription());
                m.put("status", project.getStatus());
                m.put("startDate", project.getStartDate());
                m.put("endDate", project.getEndDate());
                m.put("createTime", project.getCreateTime());
                list.add(m);
            }

            return CommonMethod.getReturnData(list);
        } catch (Exception e) {
            log.error("查询项目列表失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 提交实践成果（学生或管理员）
     */
    public DataResponse submitAchievement(DataRequest dataRequest) {
        try {
            Map<String, Object> form = dataRequest.getMap("form");
            Integer projectId = CommonMethod.getInteger(form, "projectId");
            Integer personId = CommonMethod.getInteger(form, "personId");
            String title = CommonMethod.getString(form, "title");
            String content = CommonMethod.getString(form, "content");
            String achievementType = CommonMethod.getString(form, "achievementType");

            String errorMsg = ParamCheckUtil.checkRequired(projectId != null ? projectId.toString() : "", "项目 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(title, "成果标题");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 权限校验：学生只能提交自己的
            if (RoleCheckUtil.hasRole("STUDENT")) {
                Integer currentPersonId = CommonMethod.getPersonId();
                if (personId != null && !currentPersonId.equals(personId)) {
                    return CommonMethod.getReturnMessageError("权限不足，只能提交自己的成果！");
                }
                personId = currentPersonId;
            }

            // 检查项目是否存在
            Optional<InnovationProject> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("项目不存在！");
            }

            InnovationAchievement achievement = new InnovationAchievement();
            achievement.setProject(projectOpt.get());
            achievement.setPersonId(personId);
            achievement.setTitle(title);
            achievement.setContent(content);
            achievement.setAchievementType(achievementType);
            achievement.setSubmitTime(new Date());

            achievementRepository.save(achievement);
            log.info("提交实践成果成功，projectId: {}, title: {}, 操作人: {}", projectId, title, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(achievement.getAchievementId());
        } catch (Exception e) {
            log.error("提交实践成果失败", e);
            throw new RuntimeException("提交失败：" + e.getMessage());
        }
    }

    /**
     * 查询成果列表
     */
    public DataResponse getAchievementList(Integer projectId, Integer personId) {
        try {
            List<InnovationAchievement> achievements;

            if (projectId != null) {
                achievements = achievementRepository.findByProjectId(projectId);
            } else if (personId != null) {
                // 权限校验：学生只能查自己的
                if (RoleCheckUtil.hasRole("STUDENT")) {
                    Integer currentPersonId = CommonMethod.getPersonId();
                    if (!currentPersonId.equals(personId)) {
                        return CommonMethod.getReturnMessageError("权限不足，只能查询自己的成果！");
                    }
                }
                achievements = achievementRepository.findByPersonId(personId);
            } else {
                achievements = achievementRepository.findAll();
            }

            List<Map<String, Object>> list = new ArrayList<>();
            for (InnovationAchievement a : achievements) {
                Map<String, Object> m = new HashMap<>();
                m.put("achievementId", a.getAchievementId());
                m.put("projectId", a.getProject().getProjectId());
                m.put("projectName", a.getProject().getName());
                m.put("personId", a.getPersonId());
                m.put("title", a.getTitle());
                m.put("content", a.getContent());
                m.put("achievementType", a.getAchievementType());
                m.put("submitTime", a.getSubmitTime());
                list.add(m);
            }

            return CommonMethod.getReturnData(list);
        } catch (Exception e) {
            log.error("查询成果列表失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 终止项目（管理员或教师）
     */
    public DataResponse terminateProject(DataRequest dataRequest) {
        try {
            // 权限校验：管理员或教师
            if (!RoleCheckUtil.isAdminOrTeacher()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员和教师可以终止项目！");
            }

            Map<String, Object> form = dataRequest.getMap("form");
            Integer projectId = CommonMethod.getInteger(form, "projectId");

            String errorMsg = ParamCheckUtil.checkRequired(projectId != null ? projectId.toString() : "", "项目 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            Optional<InnovationProject> projectOpt = projectRepository.findById(projectId);
            if (projectOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("项目不存在！");
            }

            InnovationProject project = projectOpt.get();
            project.setStatus(2); // 2=已终止
            projectRepository.save(project);

            log.info("终止项目成功，projectId: {}, 操作人: {}", projectId, CommonMethod.getPersonId());
            return CommonMethod.getReturnMessageOK("项目已终止");
        } catch (Exception e) {
            log.error("终止项目失败", e);
            throw new RuntimeException("终止失败：" + e.getMessage());
        }
    }
}
