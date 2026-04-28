package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.models.StudentLeave;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.StudentLeaveRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 请假审批业务服务
 * 实现学生提交请假、管理员审批、状态流转等核心业务逻辑
 */
@Slf4j
@Service
public class StudentLeaveService {

    private final StudentLeaveRepository studentLeaveRepository;
    private final StudentRepository studentRepository;

    public StudentLeaveService(StudentLeaveRepository studentLeaveRepository,
                               StudentRepository studentRepository) {
        this.studentLeaveRepository = studentLeaveRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 查询请假列表
     * 管理员/教师：查询所有请假记录
     * 学生：只能查询自己的请假记录
     */
    public DataResponse getStudentLeaveList(DataRequest dataRequest) {
        try {
            Map<String, Object> form = dataRequest.getMap("form");
            Integer state = CommonMethod.getInteger(form, "state");
            String search = CommonMethod.getString(form, "search");
            String studentNum = CommonMethod.getString(form, "studentNum");
            String teacherNum = CommonMethod.getString(form, "teacherNum");

            List<StudentLeave> leaveList;

            if (RoleCheckUtil.isAdmin() || RoleCheckUtil.hasRole("TEACHER")) {
                // 管理员/教师可查所有，按条件筛选
                leaveList = studentLeaveRepository.getStudentLeaveList(state, search, studentNum, teacherNum);
            } else {
                // 学生只能查自己的请假记录
                Integer personId = CommonMethod.getPersonId();
                if (personId == null) {
                    return CommonMethod.getReturnMessageError("用户信息不存在！");
                }
                leaveList = studentLeaveRepository.getStudentLeaveList(-1, search, studentNum, teacherNum);
                leaveList.removeIf(sl -> !sl.getStudent().getPerson().getPersonId().equals(personId));
            }

            return CommonMethod.getReturnData(getStudentLeaveMapList(leaveList));
        } catch (Exception e) {
            log.error("查询请假列表失败", e);
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 学生提交请假申请
     * 状态默认为 0（待审批）
     */
    public DataResponse studentLeaveApply(DataRequest dataRequest) {
        try {
            // 【关键修改】必须先从 dataRequest 获取 form 对象
            Map<String, Object> form = dataRequest.getMap("form");
            
            // 然后从 form 对象中获取 personId
            Integer personId = CommonMethod.getInteger(form, "personId");
            String leaveDate = CommonMethod.getString(form, "leaveDate");
            String reason = CommonMethod.getString(form, "reason");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(reason, "请假原因");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            if (leaveDate == null || leaveDate.trim().isEmpty()) {
                return CommonMethod.getReturnMessageError("请假时间不能为空！");
            }

            // 防重提交：检查该学生在同一天是否已提交过待审批的请假
            List<StudentLeave> existingLeaves = studentLeaveRepository.getStudentLeaveList(-1, "", "", "");
            for (StudentLeave sl : existingLeaves) {
                if (sl.getStudent().getPerson().getPersonId().equals(personId) 
                    && sl.getLeaveDate().equals(leaveDate) 
                    && sl.getState() == 0) {
                    return CommonMethod.getReturnMessageError("该日期已有待审批的请假申请，请勿重复提交！");
                }
            }

            // 查找学生
            Optional<Student> sOp = studentRepository.findById(personId);
            if (sOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("学生不存在！");
            }

            StudentLeave studentLeave = new StudentLeave();
            studentLeave.setStudent(sOp.get());
            studentLeave.setLeaveDate(leaveDate);
            studentLeave.setReason(reason);
            studentLeave.setState(0); // 0 = 待审批
            studentLeave.setApplyTime(new Date());

            studentLeaveRepository.save(studentLeave);

            return CommonMethod.getReturnData(studentLeave.getStudentLeaveId());
        } catch (Exception e) {
            log.error("提交请假申请失败", e);
            return CommonMethod.getReturnMessageError("提交失败：" + e.getMessage());
        }
    }

    /**
     * 管理员/教师审批请假
     * state: 1 = 通过，2 = 驳回
     */
    public DataResponse studentLeaveApprove(DataRequest dataRequest) {
        try {
            // 权限校验：只有管理员和教师能审批
            if (!RoleCheckUtil.isAdmin() && !RoleCheckUtil.hasRole("TEACHER")) {
                return CommonMethod.getReturnMessageError("权限不足，只有教师和管理员可以审批请假！");
            }

            Integer studentLeaveId = dataRequest.getInteger("studentLeaveId");
            Map<String, Object> form = dataRequest.getMap("form");
            Integer state = CommonMethod.getInteger(form, "state");
            String comment = CommonMethod.getString(form, "comment");

            if (studentLeaveId == null || studentLeaveId <= 0) {
                return CommonMethod.getReturnMessageError("请假 ID 不能为空！");
            }

            if (state == null || (state != 1 && state != 2)) {
                return CommonMethod.getReturnMessageError("审批状态必须为 1（通过）或 2（驳回）！");
            }

            Optional<StudentLeave> slOp = studentLeaveRepository.findById(studentLeaveId);
            if (slOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("请假记录不存在！");
            }

            StudentLeave studentLeave = slOp.get();

            // 防止重复审批
            if (studentLeave.getState() != 0) {
                return CommonMethod.getReturnMessageError("该请假已审批，无法重复操作！");
            }

            // 更新审批状态
            studentLeave.setState(state);
            studentLeave.setTeacherComment(comment);
            studentLeave.setTeacherTime(new Date());

            studentLeaveRepository.save(studentLeave);

            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("审批请假失败", e);
            return CommonMethod.getReturnMessageError("审批失败：" + e.getMessage());
        }
    }

    /**
     * 将 StudentLeave 对象列表转换为 Map 列表（用于前端渲染）
     */
    private List<Map<String, Object>> getStudentLeaveMapList(List<StudentLeave> leaveList) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (leaveList == null || leaveList.isEmpty()) {
            return result;
        }

        for (StudentLeave sl : leaveList) {
            Map<String, Object> m = new HashMap<>();
            m.put("studentLeaveId", sl.getStudentLeaveId());
            m.put("personId", sl.getStudent().getPerson().getPersonId());
            m.put("studentNum", sl.getStudent().getPerson().getNum());
            m.put("studentName", sl.getStudent().getPerson().getName());
            m.put("leaveDate", sl.getLeaveDate());
            m.put("reason", sl.getReason());
            m.put("state", sl.getState());
            m.put("applyTime", sl.getApplyTime());
            m.put("teacherComment", sl.getTeacherComment());
            m.put("teacherTime", sl.getTeacherTime());
            m.put("adminComment", sl.getAdminComment());
            m.put("adminTime", sl.getAdminTime());

            if (sl.getTeacher() != null && sl.getTeacher().getPerson() != null) {
                m.put("teacherName", sl.getTeacher().getPerson().getName());
            } else {
                m.put("teacherName", "");
            }

            result.add(m);
        }
        return result;
    }
}
