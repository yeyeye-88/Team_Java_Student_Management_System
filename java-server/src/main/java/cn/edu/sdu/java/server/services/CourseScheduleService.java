package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.CourseSchedule;
import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.CourseScheduleRepository;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * CourseScheduleService 课表安排管理业务逻辑层
 */
@Slf4j
@Service
public class CourseScheduleService {

    private final CourseScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public CourseScheduleService(CourseScheduleRepository scheduleRepository,
                                 CourseRepository courseRepository,
                                 TeacherRepository teacherRepository,
                                 StudentRepository studentRepository) {
        this.scheduleRepository = scheduleRepository;
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * 添加课表安排（管理员/教师）
     */
    public DataResponse addSchedule(DataRequest dataRequest) {
        try {
            if (!RoleCheckUtil.isAdminOrTeacher()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员和教师可以添加课表！");
            }

            // 兼容多种请求体格式
            Map<String, Object> form = dataRequest.getMap("form");
            if (form.isEmpty()) form = dataRequest.getMap("data");
            if (form.isEmpty()) {
                // 如果 dataRequest 内部结构特殊，尝试直接获取
                Integer courseId = dataRequest.getInteger("courseId");
                if (courseId == null) courseId = CommonMethod.getInteger(dataRequest.getMap(""), "courseId");
            }
            
            Integer courseId = CommonMethod.getInteger(form, "courseId");
            Integer teacherId = CommonMethod.getInteger(form, "teacherId");
            String className = CommonMethod.getString(form, "className");
            String semester = CommonMethod.getString(form, "semester");
            Integer dayOfWeek = CommonMethod.getInteger(form, "dayOfWeek");
            Integer startPeriod = CommonMethod.getInteger(form, "startPeriod");
            Integer endPeriod = CommonMethod.getInteger(form, "endPeriod");
            String location = CommonMethod.getString(form, "location");
            Integer status = CommonMethod.getInteger(form, "status");

            String errorMsg = ParamCheckUtil.checkRequired(courseId != null ? courseId.toString() : "", "课程 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(teacherId != null ? teacherId.toString() : "", "教师 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(semester, "学期");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            // 检查是否存在已停用的排课记录（status=2）
            List<CourseSchedule> allSchedules = scheduleRepository.findByClassNameAndSemester(className, semester);
            CourseSchedule deletedSchedule = null;
            for (CourseSchedule schedule : allSchedules) {
                if (schedule.getStatus() == 2 && schedule.getCourse().getCourseId().equals(courseId)) {
                    deletedSchedule = schedule;
                    break;
                }
            }
            
            CourseSchedule schedule;
            if (deletedSchedule != null) {
                // 复用已停用的记录
                schedule = deletedSchedule;
                log.info("发现已停用的排课记录，scheduleId: {}，将重新启用", schedule.getScheduleId());
            } else {
                // 检查时间冲突（只检查正常状态的排课）
                for (CourseSchedule existingSchedule : allSchedules) {
                    if (existingSchedule.getStatus() == 1 && // 只检查正常状态
                        existingSchedule.getDayOfWeek().equals(dayOfWeek) &&
                        existingSchedule.getEndPeriod() >= startPeriod &&
                        existingSchedule.getStartPeriod() <= endPeriod) {
                        return CommonMethod.getReturnMessageError("该班级在指定时间已有课程安排，请调整时间！");
                    }
                }
                schedule = new CourseSchedule();
            }

            Optional<Course> courseOpt = courseRepository.findById(courseId);
            if (courseOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("课程不存在！");
            }

            Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);
            if (teacherOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("教师不存在！");
            }

            schedule.setCourse(courseOpt.get());
            schedule.setTeacher(teacherOpt.get());
            schedule.setClassName(className);
            schedule.setSemester(semester);
            schedule.setDayOfWeek(dayOfWeek);
            schedule.setStartPeriod(startPeriod);
            schedule.setEndPeriod(endPeriod);
            schedule.setLocation(location);
            schedule.setStatus(status != null ? status : 1);

            scheduleRepository.save(schedule);
            log.info("添加课表成功，课程 ID: {}, 班级: {}, 学期: {}", courseId, className, semester);
            return CommonMethod.getReturnData(schedule.getScheduleId());
        } catch (Exception e) {
            log.error("添加课表失败", e);
            throw new RuntimeException("添加失败：" + e.getMessage());
        }
    }

    /**
     * 查询课表列表
     * 权限：管理员可查全部，学生/教师只能查自己的
     */
    public DataResponse getScheduleList(DataRequest dataRequest) {
        try {
            Map<String, Object> data = dataRequest.getMap("data");
            Integer teacherId = CommonMethod.getInteger(data, "teacherId");
            String className = CommonMethod.getString(data, "className");
            String semester = CommonMethod.getString(data, "semester");

            // 权限隔离
            if (RoleCheckUtil.hasRole("STUDENT")) {
                Integer currentPersonId = CommonMethod.getPersonId();
                Optional<Student> studentOpt = studentRepository.findByPersonPersonId(currentPersonId);
                if (studentOpt.isPresent()) {
                    className = studentOpt.get().getClassName();
                } else {
                    return CommonMethod.getReturnMessageError("未找到学生信息！");
                }
            } else if (RoleCheckUtil.hasRole("TEACHER")) {
                Integer currentPersonId = CommonMethod.getPersonId();
                Optional<Teacher> teacherOpt = teacherRepository.findById(currentPersonId);
                if (teacherOpt.isPresent()) {
                    teacherId = teacherOpt.get().getPersonId();
                } else {
                    return CommonMethod.getReturnMessageError("未找到教师信息！");
                }
            }

            List<CourseSchedule> schedules;
            // 修复：处理空字符串，确保只有在参数有效时才进行条件查询
            boolean validClassQuery = className != null && !className.trim().isEmpty() 
                                   && semester != null && !semester.trim().isEmpty();
            boolean validTeacherQuery = teacherId != null 
                                     && semester != null && !semester.trim().isEmpty();

            if (validClassQuery) {
                schedules = scheduleRepository.findByClassNameAndSemester(className, semester);
            } else if (validTeacherQuery) {
                schedules = scheduleRepository.findByTeacherIdAndSemester(teacherId, semester);
            } else {
                schedules = scheduleRepository.findAll();
            }

            List<Map<String, Object>> list = new ArrayList<>();
            for (CourseSchedule schedule : schedules) {
                Map<String, Object> m = new HashMap<>();
                m.put("scheduleId", schedule.getScheduleId());
                m.put("courseId", schedule.getCourse().getCourseId());
                m.put("courseName", schedule.getCourse().getName());
                m.put("courseNum", schedule.getCourse().getNum());
                m.put("teacherId", schedule.getTeacher().getPersonId());
                m.put("teacherName", schedule.getTeacher().getPerson().getName());
                m.put("className", schedule.getClassName());
                m.put("semester", schedule.getSemester());
                m.put("dayOfWeek", schedule.getDayOfWeek());
                m.put("startPeriod", schedule.getStartPeriod());
                m.put("endPeriod", schedule.getEndPeriod());
                m.put("location", schedule.getLocation());
                m.put("status", schedule.getStatus());
                list.add(m);
            }

            return CommonMethod.getReturnData(list);
        } catch (Exception e) {
            log.error("查询课表列表失败", e);
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 修改课表安排
     */
    public DataResponse updateSchedule(DataRequest dataRequest) {
        try {
            if (!RoleCheckUtil.isAdminOrTeacher()) {
                return CommonMethod.getReturnMessageError("权限不足！");
            }

            // 兼容多种请求体格式
            Map<String, Object> form = dataRequest.getMap("form");
            if (form.isEmpty()) form = dataRequest.getMap("data");
            
            Integer scheduleId = CommonMethod.getInteger(form, "scheduleId");

            String errorMsg = ParamCheckUtil.checkRequired(scheduleId != null ? scheduleId.toString() : "", "课表 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            Optional<CourseSchedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("课表记录不存在！");
            }

            CourseSchedule schedule = scheduleOpt.get();
            Integer courseId = CommonMethod.getInteger(form, "courseId");
            Integer teacherId = CommonMethod.getInteger(form, "teacherId");
            Integer dayOfWeek = CommonMethod.getInteger(form, "dayOfWeek");
            Integer startPeriod = CommonMethod.getInteger(form, "startPeriod");
            Integer endPeriod = CommonMethod.getInteger(form, "endPeriod");
            String location = CommonMethod.getString(form, "location");
            Integer status = CommonMethod.getInteger(form, "status");

            if (courseId != null) {
                Optional<Course> courseOpt = courseRepository.findById(courseId);
                if (courseOpt.isEmpty()) {
                    return CommonMethod.getReturnMessageError("课程不存在！");
                }
                schedule.setCourse(courseOpt.get());
            }
            if (teacherId != null) {
                Optional<Teacher> teacherOpt = teacherRepository.findById(teacherId);
                if (teacherOpt.isEmpty()) {
                    return CommonMethod.getReturnMessageError("教师不存在！");
                }
                schedule.setTeacher(teacherOpt.get());
            }
            if (dayOfWeek != null) schedule.setDayOfWeek(dayOfWeek);
            if (startPeriod != null) schedule.setStartPeriod(startPeriod);
            if (endPeriod != null) schedule.setEndPeriod(endPeriod);
            if (location != null) schedule.setLocation(location);
            if (status != null) schedule.setStatus(status);

            scheduleRepository.save(schedule);
            log.info("修改课表成功，scheduleId: {}", scheduleId);
            return CommonMethod.getReturnMessageOK("修改成功");
        } catch (Exception e) {
            log.error("修改课表失败", e);
            throw new RuntimeException("修改失败：" + e.getMessage());
        }
    }

    /**
     * 删除课表安排（软删除）
     */
    public DataResponse deleteSchedule(DataRequest dataRequest) {
        try {
            if (!RoleCheckUtil.isAdmin()) {
                return CommonMethod.getReturnMessageError("权限不足，只有管理员可以删除课表！");
            }

            // 兼容多种请求体格式
            Map<String, Object> form = dataRequest.getMap("form");
            if (form.isEmpty()) form = dataRequest.getMap("data");
            
            Integer scheduleId = CommonMethod.getInteger(form, "scheduleId");

            String errorMsg = ParamCheckUtil.checkRequired(scheduleId != null ? scheduleId.toString() : "", "课表 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            Optional<CourseSchedule> scheduleOpt = scheduleRepository.findById(scheduleId);
            if (scheduleOpt.isEmpty()) {
                return CommonMethod.getReturnMessageError("课表记录不存在！");
            }

            CourseSchedule schedule = scheduleOpt.get();
            schedule.setStatus(2); // 2=停用
            scheduleRepository.save(schedule);

            log.info("删除课表成功，scheduleId: {}", scheduleId);
            return CommonMethod.getReturnMessageOK("删除成功");
        } catch (Exception e) {
            log.error("删除课表失败", e);
            throw new RuntimeException("删除失败：" + e.getMessage());
        }
    }
}
