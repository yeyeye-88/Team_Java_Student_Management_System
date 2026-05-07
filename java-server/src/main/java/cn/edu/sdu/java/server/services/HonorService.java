package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.HonorRecord;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.HonorRecordRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * HonorService 荣誉记录管理业务逻辑层
 * 权限规则：
 * - 录入：管理员/教师
 * - 查询：管理员可查全部，学生只能查自己
 */
@Slf4j
@Service
public class HonorService {

    private final HonorRecordRepository honorRecordRepository;

    public HonorService(HonorRecordRepository honorRecordRepository) {
        this.honorRecordRepository = honorRecordRepository;
    }

    /**
     * 录入学生奖项（管理员或教师）
     */
    public DataResponse addHonor(DataRequest dataRequest) {
        try {
            // 权限校验
            if (!RoleCheckUtil.isAdminOrTeacher()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员和教师可以录入奖项！");
            }

            Map<String, Object> form = dataRequest.getMap("form");
            Integer personId = CommonMethod.getInteger(form, "personId");
            String title = CommonMethod.getString(form, "title");
            String level = CommonMethod.getString(form, "level");
            String category = CommonMethod.getString(form, "category");
            String description = CommonMethod.getString(form, "description");
            Date awardDate = CommonMethod.getDate(form, "awardDate");
            String issuer = CommonMethod.getString(form, "issuer");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(title, "奖项名称");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 创建荣誉记录
            HonorRecord record = new HonorRecord();
            record.setPersonId(personId);
            record.setTitle(title);
            record.setLevel(level);
            record.setCategory(category);
            record.setDescription(description);
            record.setAwardDate(awardDate);
            record.setIssuer(issuer);
            record.setRecorderId(CommonMethod.getPersonId()); // 记录录入人
            record.setStatus(1); // 默认已通过
            record.setCreateTime(new Date());

            honorRecordRepository.save(record);
            log.info("录入荣誉成功，personId: {}, title: {}, 操作人: {}", personId, title, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(record.getHonorId());
        } catch (Exception e) {
            log.error("录入荣誉失败", e);
            throw new RuntimeException("录入失败：" + e.getMessage());
        }
    }

    /**
     * 查询荣誉列表
     */
    public DataResponse getHonorList(Integer personId, String level) {
        try {
            List<HonorRecord> records;

            if (personId != null) {
                // 权限校验：学生只能查自己的
                if (RoleCheckUtil.hasRole("STUDENT")) {
                    Integer currentPersonId = CommonMethod.getPersonId();
                    if (!currentPersonId.equals(personId)) {
                        return CommonMethod.getReturnMessageError("权限不足，只能查询自己的荣誉记录！");
                    }
                }
                records = honorRecordRepository.findByPersonId(personId);
            } else if (level != null) {
                // 按级别查询
                records = honorRecordRepository.findByLevel(level);
            } else {
                // 管理员查询全部
                if (RoleCheckUtil.hasRole("STUDENT")) {
                    return CommonMethod.getReturnMessageError("权限不足，请指定查询条件！");
                }
                records = honorRecordRepository.findAllOrderByCreateTime();
            }

            // 转换为返回格式
            List<Map<String, Object>> list = new ArrayList<>();
            for (HonorRecord r : records) {
                Map<String, Object> m = new HashMap<>();
                m.put("honorId", r.getHonorId());
                m.put("personId", r.getPersonId());
                m.put("title", r.getTitle());
                m.put("level", r.getLevel());
                m.put("category", r.getCategory());
                m.put("description", r.getDescription());
                m.put("awardDate", r.getAwardDate());
                m.put("issuer", r.getIssuer());
                m.put("recorderId", r.getRecorderId());
                m.put("status", r.getStatus());
                m.put("statusName", getStatusName(r.getStatus()));
                m.put("createTime", r.getCreateTime());
                list.add(m);
            }

            return CommonMethod.getReturnData(list);
        } catch (Exception e) {
            log.error("查询荣誉列表失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 状态码转中文
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已驳回";
            default: return "未知";
        }
    }
}
