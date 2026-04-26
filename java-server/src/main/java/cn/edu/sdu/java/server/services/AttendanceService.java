package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Attendance;
import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.AttendanceRepository;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AttendanceService 考勤管理业务逻辑层
 */
@Slf4j
@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepository, CourseRepository courseRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * 查询考勤列表
     * 权限控制：
     * - 管理员/教师：可以查询所有学生考勤
     * - 学生：只能查询自己的考勤
     */
    public DataResponse getAttendanceList(DataRequest dataRequest) {
        try {
            Integer currentUserId = CommonMethod.getPersonId();
            boolean isAdminOrTeacher = RoleCheckUtil.isAdmin() || RoleCheckUtil.hasRole("TEACHER");
            
            Integer personId = dataRequest.getInteger("personId");
            Integer courseId = dataRequest.getInteger("courseId");
            String attendanceTime = dataRequest.getString("attendanceTime");
            Integer state = dataRequest.getInteger("state");
            
            // 如果是学生，强制只能查自己的考勤
            if (!isAdminOrTeacher) {
                personId = currentUserId;
            }

            List<Attendance> attendanceList;
            if (courseId != null && courseId > 0) {
                // 按课程查询
                attendanceList = attendanceRepository.findByCourseCourseId(courseId);
            } else if (state != null) {
                // 按状态查询
                attendanceList = attendanceRepository.findByState(state);
            } else if (personId != null && personId > 0) {
                // 按学生 ID 查询
                attendanceList = attendanceRepository.findByStudentPersonId(personId);
            } else {
                // 管理员查询所有
                attendanceList = attendanceRepository.findAll();
            }

            return CommonMethod.getReturnData(getAttendanceMapList(attendanceList));
        } catch (Exception e) {
            log.error("查询考勤列表失败", e);
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存考勤（新增或修改）
     */
    public DataResponse attendanceSave(DataRequest dataRequest) {
        try {
            // 权限拦截：只有管理员和教师能录入/修改考勤
            if (!RoleCheckUtil.isAdmin() && !RoleCheckUtil.hasRole("TEACHER")) {
                return CommonMethod.getReturnMessageError("权限不足，只有教师和管理员可以录入考勤！");
            }
            
            Integer attendanceId = dataRequest.getInteger("attendanceId");
            Integer personId = dataRequest.getInteger("personId");
            Integer courseId = dataRequest.getInteger("courseId");
            String attendanceTime = dataRequest.getString("attendanceTime");
            Integer state = dataRequest.getInteger("state");
            String remark = dataRequest.getString("remark");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(courseId != null ? courseId.toString() : "", "课程 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            if (state == null || state < 0 || state > 4) {
                return CommonMethod.getReturnMessageError("考勤状态必须在 0-4 之间！");
            }

            Attendance attendance;
            if (attendanceId != null && attendanceId > 0) {
                Optional<Attendance> op = attendanceRepository.findById(attendanceId);
                if (op.isPresent()) {
                    attendance = op.get();
                } else {
                    return CommonMethod.getReturnMessageError("考勤记录不存在！");
                }
            } else {
                attendance = new Attendance();
            }

            // 关联学生和课程
            Optional<Student> sOp = studentRepository.findById(personId);
            if (sOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("学生不存在！");
            }
            attendance.setStudent(sOp.get());
            
            Optional<Course> cOp = courseRepository.findById(courseId);
            if (cOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("课程不存在！");
            }
            attendance.setCourse(cOp.get());
            
            attendance.setAttendanceTime(new Date());
            attendance.setState(state);
            attendance.setRemark(remark != null ? remark : "");

            attendanceRepository.save(attendance);

            return CommonMethod.getReturnData(attendance.getAttendanceId());
        } catch (Exception e) {
            log.error("保存考勤失败", e);
            return CommonMethod.getReturnMessageError("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除考勤记录
     */
    public DataResponse attendanceDelete(DataRequest dataRequest) {
        try {
            // 权限拦截：只有管理员和教师能删除考勤
            if (!RoleCheckUtil.isAdmin() && !RoleCheckUtil.hasRole("TEACHER")) {
                return CommonMethod.getReturnMessageError("权限不足，只有教师和管理员可以删除考勤！");
            }
            
            Integer attendanceId = dataRequest.getInteger("attendanceId");
            if (attendanceId == null || attendanceId <= 0) {
                return CommonMethod.getReturnMessageError("考勤 ID 不能为空！");
            }

            attendanceRepository.deleteById(attendanceId);
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("删除考勤失败", e);
            return CommonMethod.getReturnMessageError("删除失败：" + e.getMessage());
        }
    }

    /**
     * 将 Attendance 对象列表转换为 Map 列表
     */
    private List<Map<String, Object>> getAttendanceMapList(List<Attendance> attendanceList) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (attendanceList == null || attendanceList.isEmpty()) {
            return result;
        }

        for (Attendance a : attendanceList) {
            Map<String, Object> m = new HashMap<>();
            m.put("attendanceId", a.getAttendanceId());
            m.put("personId", a.getStudent().getPerson().getPersonId());
            m.put("studentNum", a.getStudent().getPerson().getNum());
            m.put("studentName", a.getStudent().getPerson().getName());
            m.put("courseId", a.getCourse().getCourseId());
            m.put("courseName", a.getCourse().getName());
            m.put("attendanceTime", a.getAttendanceTime());
            m.put("state", a.getState());
            m.put("remark", a.getRemark());
            result.add(m);
        }
        return result;
    }
}
