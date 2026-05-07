package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.PunishmentRecord;
import cn.edu.sdu.java.server.models.PunishmentResult;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.PunishmentRecordRepository;
import cn.edu.sdu.java.server.repositorys.PunishmentResultRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PunishmentService 处分管理业务逻辑层
 * 权限规则：
 * - 录入：仅管理员
 * - 查询：管理员可查全部，学生只能查自己
 */
@Slf4j
@Service
public class PunishmentService {

    private final PunishmentRecordRepository punishmentRecordRepository;
    private final PunishmentResultRepository punishmentResultRepository;

    public PunishmentService(PunishmentRecordRepository punishmentRecordRepository, 
                            PunishmentResultRepository punishmentResultRepository) {
        this.punishmentRecordRepository = punishmentRecordRepository;
        this.punishmentResultRepository = punishmentResultRepository;
    }

    /**
     * 记录违纪行为（仅管理员）
     */
    public DataResponse addPunishment(DataRequest dataRequest) {
        try {
            // 权限校验
            if (!RoleCheckUtil.isAdmin()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员可以记录违纪行为！");
            }

            Map<String, Object> form = dataRequest.getMap("form");
            Integer personId = CommonMethod.getInteger(form, "personId");
            String behavior = CommonMethod.getString(form, "behavior");
            String level = CommonMethod.getString(form, "level");
            String description = CommonMethod.getString(form, "description");
            Date behaviorDate = CommonMethod.getDate(form, "behaviorDate");
            String location = CommonMethod.getString(form, "location");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(behavior, "违纪行为");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 创建处分记录
            PunishmentRecord record = new PunishmentRecord();
            record.setPersonId(personId);
            record.setBehavior(behavior);
            record.setLevel(level);
            record.setDescription(description);
            record.setBehaviorDate(behaviorDate);
            record.setLocation(location);
            record.setRecorderId(CommonMethod.getPersonId()); // 记录录入人
            record.setStatus(0); // 待处理
            record.setCreateTime(new Date());

            punishmentRecordRepository.save(record);
            log.info("记录违纪成功，personId: {}, behavior: {}, 操作人: {}", personId, behavior, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(record.getPunishmentId());
        } catch (Exception e) {
            log.error("记录违纪失败", e);
            throw new RuntimeException("记录失败：" + e.getMessage());
        }
    }

    /**
     * 添加处理结果（仅管理员）
     */
    public DataResponse addPunishmentResult(DataRequest dataRequest) {
        try {
            // 权限校验
            if (!RoleCheckUtil.isAdmin()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员可以添加处理结果！");
            }

            Map<String, Object> form = dataRequest.getMap("form");
            Integer punishmentId = CommonMethod.getInteger(form, "punishmentId");
            String resultType = CommonMethod.getString(form, "resultType");
            String description = CommonMethod.getString(form, "description");
            Date resultDate = CommonMethod.getDate(form, "resultDate");
            String handler = CommonMethod.getString(form, "handler");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(punishmentId != null ? punishmentId.toString() : "", "处分 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(resultType, "处理结果类型");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 检查处分记录是否存在
            Optional<PunishmentRecord> recordOpt = punishmentRecordRepository.findById(punishmentId);
            if (recordOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("处分记录不存在！");
            }

            // 更新处分记录状态为已处理
            PunishmentRecord record = recordOpt.get();
            record.setStatus(1); // 已处理
            punishmentRecordRepository.save(record);

            // 创建处理结果
            PunishmentResult result = new PunishmentResult();
            result.setPunishmentId(punishmentId);
            result.setResultType(resultType);
            result.setDescription(description);
            result.setResultDate(resultDate);
            result.setHandler(handler);
            result.setCreateTime(new Date());

            punishmentResultRepository.save(result);
            log.info("添加处理结果成功，punishmentId: {}, resultType: {}, 操作人: {}", punishmentId, resultType, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(result.getResultId());
        } catch (Exception e) {
            log.error("添加处理结果失败", e);
            throw new RuntimeException("添加失败：" + e.getMessage());
        }
    }

    /**
     * 查询处分列表
     */
    public DataResponse getPunishmentList(Integer personId, Integer status) {
        try {
            List<PunishmentRecord> records;

            if (personId != null) {
                // 权限校验：学生只能查自己的
                if (RoleCheckUtil.hasRole("STUDENT")) {
                    Integer currentPersonId = CommonMethod.getPersonId();
                    if (!currentPersonId.equals(personId)) {
                        return CommonMethod.getReturnMessageError("权限不足，只能查询自己的处分记录！");
                    }
                }
                // 过滤掉已撤销(2)的记录
                List<PunishmentRecord> allRecords = punishmentRecordRepository.findByPersonId(personId);
                records = allRecords.stream()
                        .filter(r -> r.getStatus() == null || r.getStatus() != 2)
                        .collect(Collectors.toList());
            } else if (status != null) {
                // 按状态查询
                records = punishmentRecordRepository.findByStatus(status);
            } else {
                // 管理员查询全部（过滤掉已撤销的记录）
                if (RoleCheckUtil.hasRole("STUDENT")) {
                    return CommonMethod.getReturnMessageError("权限不足，请指定查询条件！");
                }
                // 默认查询待处理(0)和已处理(1)的记录，不显示已撤销(2)的
                List<PunishmentRecord> activeRecords = new ArrayList<>();
                activeRecords.addAll(punishmentRecordRepository.findByStatus(0));
                activeRecords.addAll(punishmentRecordRepository.findByStatus(1));
                activeRecords.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
                records = activeRecords;
            }

            // 转换为返回格式
            List<Map<String, Object>> list = new ArrayList<>();
            for (PunishmentRecord r : records) {
                Map<String, Object> m = new HashMap<>();
                m.put("punishmentId", r.getPunishmentId());
                m.put("personId", r.getPersonId());
                m.put("behavior", r.getBehavior());
                m.put("level", r.getLevel());
                m.put("description", r.getDescription());
                m.put("behaviorDate", r.getBehaviorDate());
                m.put("location", r.getLocation());
                m.put("recorderId", r.getRecorderId());
                m.put("status", r.getStatus());
                m.put("statusName", r.getStatus() == 0 ? "待处理" : "已处理");
                m.put("createTime", r.getCreateTime());
                list.add(m);
            }

            return CommonMethod.getReturnData(list);
        } catch (Exception e) {
            log.error("查询处分列表失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 撤销处分（软删除，仅管理员）
     */
    public DataResponse revokePunishment(DataRequest dataRequest) {
        try {
            if (!RoleCheckUtil.isAdmin()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员可以撤销处分！");
            }

            Integer punishmentId = dataRequest.getInteger("punishmentId");
            if (punishmentId == null) {
                return CommonMethod.getReturnMessageError("处分 ID 不能为空！");
            }

            Optional<PunishmentRecord> recordOpt = punishmentRecordRepository.findById(punishmentId);
            if (recordOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("处分记录不存在！");
            }

            PunishmentRecord record = recordOpt.get();
            record.setStatus(2); // 2=已撤销
            punishmentRecordRepository.save(record);

            log.info("撤销处分成功，punishmentId: {}, 操作人: {}", punishmentId, CommonMethod.getPersonId());
            return CommonMethod.getReturnMessage(200, "撤销成功");
        } catch (Exception e) {
            log.error("撤销处分失败", e);
            throw new RuntimeException("撤销失败：" + e.getMessage());
        }
    }

    /**
     * 查询处理结果列表
     */
    public DataResponse getResultList(Integer punishmentId) {
        try {
            List<PunishmentResult> results;
            if (punishmentId != null) {
                results = punishmentResultRepository.findByPunishmentId(punishmentId);
            } else {
                results = punishmentResultRepository.findAllOrderByCreateTime();
            }

            // 转换为返回格式
            List<Map<String, Object>> list = new ArrayList<>();
            for (PunishmentResult r : results) {
                Map<String, Object> m = new HashMap<>();
                m.put("resultId", r.getResultId());
                m.put("punishmentId", r.getPunishmentId());
                m.put("resultType", r.getResultType());
                m.put("description", r.getDescription());
                m.put("resultDate", r.getResultDate());
                m.put("handler", r.getHandler());
                m.put("createTime", r.getCreateTime());
                list.add(m);
            }

            return CommonMethod.getReturnData(list);
        } catch (Exception e) {
            log.error("查询处理结果失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }
}
