package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Fee;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.FeeRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * FeeService 消费管理业务逻辑层
 * 实现学费缴纳、校园卡消费记录、月度报表生成、异常消费预警
 * 
 * 权限控制规则：
 * - 管理员：可操作和查询所有学生数据
 * - 学生：只能操作和查询自己的数据
 */
@Slf4j
@Service
public class FeeService {

    private final FeeRepository feeRepository;
    private final StudentRepository studentRepository;

    public FeeService(FeeRepository feeRepository, StudentRepository studentRepository) {
        this.feeRepository = feeRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 记录学费缴纳（type=1）
     * 权限：仅管理员可操作
     */
    public DataResponse recordTuitionFee(DataRequest dataRequest) {
        try {
            // 权限校验：只有管理员可以记录学费
            if (!RoleCheckUtil.isAdmin()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员可以记录学费！");
            }

            Map<String, Object> form = dataRequest.getMap("form");
            Integer personId = CommonMethod.getInteger(form, "personId");
            String day = CommonMethod.getString(form, "day");
            Double money = CommonMethod.getDouble(form, "money");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(day, "日期");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            if (money == null || money <= 0) {
                return CommonMethod.getReturnMessageError("金额必须大于 0！");
            }

            // 查找学生
            Optional<Student> sOp = studentRepository.findById(personId);
            if (sOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("学生不存在！");
            }

            // 检查是否已存在该日期的学费记录
            Optional<Fee> existingFee = feeRepository.findByStudentPersonIdAndDay(personId, day + "_tuition");
            if (existingFee.isPresent()) {
                return CommonMethod.getReturnMessageError("该日期已有学费记录，请勿重复添加！");
            }

            Fee fee = new Fee();
            fee.setStudent(sOp.get());
            fee.setDay(day + "_tuition"); // 添加后缀区分类型
            fee.setMoney(money);
            fee.setType(1); // 1=学费缴纳

            feeRepository.save(fee);
            log.info("学费缴纳记录成功，personId: {}, 金额: {}, 操作人: {}", personId, money, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(fee.getFeeId());
        } catch (Exception e) {
            log.error("记录学费缴纳失败", e);
            throw new RuntimeException("记录失败：" + e.getMessage());
        }
    }

    /**
     * 记录校园卡消费（type=2）
     * 权限：管理员或学生本人
     */
    public DataResponse recordCampusCardFee(DataRequest dataRequest) {
        try {
            Map<String, Object> form = dataRequest.getMap("form");
            Integer personId = CommonMethod.getInteger(form, "personId");
            String day = CommonMethod.getString(form, "day");
            Double money = CommonMethod.getDouble(form, "money");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(day, "日期");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            if (money == null || money <= 0) {
                return CommonMethod.getReturnMessageError("金额必须大于 0！");
            }

            // 权限校验：学生只能记录自己的消费
            if (RoleCheckUtil.hasRole("STUDENT")) {
                Integer currentPersonId = CommonMethod.getPersonId();
                if (!currentPersonId.equals(personId)) {
                    return CommonMethod.getReturnMessageError("权限不足，只能记录自己的消费！");
                }
            }

            // 查找学生
            Optional<Student> sOp = studentRepository.findById(personId);
            if (sOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("学生不存在！");
            }

            Fee fee = new Fee();
            fee.setStudent(sOp.get());
            fee.setDay(day);
            fee.setMoney(money);
            fee.setType(2); // 2=校园卡消费

            feeRepository.save(fee);
            log.info("校园卡消费记录成功，personId: {}, 金额: {}, 操作人: {}", personId, money, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(fee.getFeeId());
        } catch (Exception e) {
            log.error("记录校园卡消费失败", e);
            throw new RuntimeException("记录失败：" + e.getMessage());
        }
    }

    /**
     * 生成月度报表
     * 权限：管理员可查询任何学生，学生只能查询自己
     */
    public DataResponse generateMonthlyReport(DataRequest dataRequest) {
        try {
            Map<String, Object> form = dataRequest.getMap("form");
            Integer personId = CommonMethod.getInteger(form, "personId");
            String month = CommonMethod.getString(form, "month"); // 格式：2026-04

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(month, "月份");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 权限校验：学生只能查自己的报表
            if (RoleCheckUtil.hasRole("STUDENT")) {
                Integer currentPersonId = CommonMethod.getPersonId();
                if (!currentPersonId.equals(personId)) {
                    return CommonMethod.getReturnMessageError("权限不足，只能查询自己的消费报表！");
                }
            }

            // 查询月度总消费
            Double monthlyTotal = feeRepository.getMonthlyTotalByPersonId(personId, month);
            if (monthlyTotal == null) monthlyTotal = 0.0;

            // 查询该月所有消费记录
            List<Fee> feeList = feeRepository.findListByStudent(personId);
            List<Map<String, Object>> records = new ArrayList<>();
            double tuitionTotal = 0.0;
            double campusTotal = 0.0;

            for (Fee fee : feeList) {
                if (fee.getDay().startsWith(month)) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("feeId", fee.getFeeId());
                    m.put("day", fee.getDay());
                    m.put("money", fee.getMoney());
                    m.put("type", fee.getType());
                    m.put("typeName", fee.getType() == 1 ? "学费缴纳" : "校园卡消费");
                    records.add(m);

                    if (fee.getType() == 1) {
                        tuitionTotal += fee.getMoney();
                    } else {
                        campusTotal += fee.getMoney();
                    }
                }
            }

            Map<String, Object> report = new HashMap<>();
            report.put("personId", personId);
            report.put("month", month);
            report.put("monthlyTotal", Math.round(monthlyTotal * 100.0) / 100.0);
            report.put("tuitionTotal", Math.round(tuitionTotal * 100.0) / 100.0);
            report.put("campusTotal", Math.round(campusTotal * 100.0) / 100.0);
            report.put("records", records);

            log.info("生成月度报表成功，personId: {}, 月份: {}, 操作人: {}", personId, month, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(report);
        } catch (Exception e) {
            log.error("生成月度报表失败", e);
            throw new RuntimeException("生成失败：" + e.getMessage());
        }
    }

    /**
     * 预警异常消费（单日消费超过阈值）
     * 权限：管理员可检测任何学生，学生只能检测自己
     */
    public DataResponse detectAbnormalConsumption(DataRequest dataRequest) {
        try {
            Map<String, Object> form = dataRequest.getMap("form");
            Integer personId = CommonMethod.getInteger(form, "personId");
            String day = CommonMethod.getString(form, "day");
            Double threshold = CommonMethod.getDouble(form, "threshold"); // 默认阈值 500 元

            if (threshold == null || threshold <= 0) {
                threshold = 1000.0; // 默认异常消费阈值改为 1000 元
            }

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(day, "日期");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 权限校验：学生只能检测自己的消费
            if (RoleCheckUtil.hasRole("STUDENT")) {
                Integer currentPersonId = CommonMethod.getPersonId();
                if (!currentPersonId.equals(personId)) {
                    return CommonMethod.getReturnMessageError("权限不足，只能检测自己的消费！");
                }
            }

            // 查询该学生该天的总消费
            Double dailyTotal = feeRepository.getDailyTotalByPersonId(personId, day);
            if (dailyTotal == null) dailyTotal = 0.0;

            Map<String, Object> result = new HashMap<>();
            result.put("personId", personId);
            result.put("day", day);
            result.put("dailyTotal", Math.round(dailyTotal * 100.0) / 100.0);
            result.put("threshold", threshold);

            if (dailyTotal > threshold) {
                result.put("isAbnormal", true);
                result.put("warning", "⚠️ 异常消费预警：当日消费 " + dailyTotal + " 元，超过阈值 " + threshold + " 元！");
                log.warn("异常消费预警，personId: {}, 日期: {}, 消费: {}, 阈值: {}, 操作人: {}", 
                        personId, day, dailyTotal, threshold, CommonMethod.getPersonId());
            } else {
                result.put("isAbnormal", false);
                result.put("warning", "✅ 消费正常");
            }

            return CommonMethod.getReturnData(result);
        } catch (Exception e) {
            log.error("异常消费检测失败", e);
            throw new RuntimeException("检测失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询消费账单列表
     * 权限：管理员可查询所有，学生只能查询自己
     */
    public DataResponse getFeeList(Integer personId, Integer type, String month, Integer page, Integer size) {
        try {
            // 权限校验：学生只能查自己的账单
            if (RoleCheckUtil.hasRole("STUDENT")) {
                Integer currentPersonId = CommonMethod.getPersonId();
                if (personId != null && !currentPersonId.equals(personId)) {
                    return CommonMethod.getReturnMessageError("权限不足，只能查询自己的账单！");
                }
                personId = currentPersonId; // 强制只能查自己
            }

            // 构建分页参数
            Pageable pageable = PageRequest.of(page, size, Sort.by("day").descending());

            // 查询所有记录（后续可根据参数过滤）
            Page<Fee> feePage = feeRepository.findAll(pageable);

            // 转换为返回格式
            List<Map<String, Object>> content = new ArrayList<>();
            for (Fee fee : feePage.getContent()) {
                // 根据条件过滤
                if (personId != null && !fee.getStudent().getPersonId().equals(personId)) {
                    continue;
                }
                if (type != null && !fee.getType().equals(type)) {
                    continue;
                }
                if (month != null && !fee.getDay().startsWith(month)) {
                    continue;
                }

                Map<String, Object> m = new HashMap<>();
                m.put("feeId", fee.getFeeId());
                m.put("personId", fee.getStudent().getPersonId());
                m.put("studentName", fee.getStudent().getPerson().getName());
                m.put("day", fee.getDay());
                m.put("money", fee.getMoney());
                m.put("type", fee.getType());
                m.put("typeName", fee.getType() == 1 ? "学费缴纳" : "校园卡消费");
                content.add(m);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("content", content);
            result.put("totalElements", feePage.getTotalElements());
            result.put("totalPages", feePage.getTotalPages());
            result.put("page", page);
            result.put("size", size);

            log.info("查询消费账单列表成功，操作人: {}", CommonMethod.getPersonId());
            return CommonMethod.getReturnData(result);
        } catch (Exception e) {
            log.error("查询消费账单列表失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }
}
