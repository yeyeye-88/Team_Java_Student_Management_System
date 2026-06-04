package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * CourseService 课程管理业务逻辑层
 * 实现课程查询、录入、修改、删除等功能
 */
@Slf4j
@Service
public class CourseService {
    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * 查询课程列表（支持按课程编号/名称模糊搜索）
     * @param dataRequest 请求参数，包含 numName（模糊查询关键字）
     * @return 课程列表（Map 格式）
     */
    public DataResponse getCourseList(DataRequest dataRequest) {
        try {
            String numName = dataRequest.getString("numName");
            if (numName == null) numName = "";
            
            List<Course> cList = courseRepository.findCourseListByNumName(numName);
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> m;
            Course pc;
            for (Course c : cList) {
                m = new HashMap<>();
                m.put("courseId", c.getCourseId() + "");
                m.put("num", c.getNum());
                m.put("name", c.getName());
                m.put("credit", c.getCredit() + "");
                m.put("coursePath", c.getCoursePath());
                pc = c.getPreCourse();
                if (pc != null) {
                    m.put("preCourse", pc.getName());
                    m.put("preCourseId", pc.getCourseId());
                }
                dataList.add(m);
            }
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            log.error("查询课程列表失败", e);
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存课程（新增或修改）
     * @param dataRequest 请求参数，包含 courseId（修改时必填）和 form（表单数据）
     * @return 操作结果
     */
    public DataResponse courseSave(DataRequest dataRequest) {
        try {
            // 权限拦截：只有管理员和教师能录入/修改课程
            if (!RoleCheckUtil.isAdmin() && !RoleCheckUtil.hasRole("TEACHER")) {
                return CommonMethod.getReturnMessageError("权限不足，只有教师和管理员可以管理课程！");
            }

            Integer courseId = dataRequest.getInteger("courseId");
            Map<String, Object> form = dataRequest.getMap("form");
            String num = CommonMethod.getString(form, "num");
            String name = CommonMethod.getString(form, "name");
            String coursePath = CommonMethod.getString(form, "coursePath");
            // 为课程路径设置默认值,避免空值
            if (coursePath == null || coursePath.trim().isEmpty()) {
                coursePath = "OTHER";
            }
            Integer credit = CommonMethod.getInteger(form, "credit");
            Integer preCourseId = CommonMethod.getInteger(form, "preCourseId");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(num, "课程编号");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(name, "课程名称");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            if (credit == null || credit < 0 || credit > 10) {
                return CommonMethod.getReturnMessageError("学分必须在 0-10 之间！");
            }

            Optional<Course> op;
            Course c = null;

            if (courseId != null) {
                op = courseRepository.findById(courseId);
                if (op.isPresent()) c = op.get();
            }
            if (c == null) c = new Course();

            // 检查课程编号是否重复（排除自身）
            Optional<Course> existOp = courseRepository.findByNum(num);
            if (existOp.isPresent() && (c.getCourseId() == null || !c.getCourseId().equals(existOp.get().getCourseId()))) {
                return CommonMethod.getReturnMessageError("课程编号已存在，不能重复添加！");
            }

            Course pc = null;
            if (preCourseId != null && preCourseId > 0) {
                op = courseRepository.findById(preCourseId);
                if (op.isPresent()) pc = op.get();
            }

            c.setNum(num);
            c.setName(name);
            c.setCredit(credit);
            c.setCoursePath(coursePath);
            c.setPreCourse(pc);
            courseRepository.save(c);
            log.info("课程保存成功，courseId: {}, 操作人: {}", c.getCourseId(), CommonMethod.getPersonId());
            return CommonMethod.getReturnData(c.getCourseId());
        } catch (Exception e) {
            log.error("保存课程失败", e);
            throw new RuntimeException("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除课程记录
     * @param dataRequest 请求参数，包含 courseId
     * @return 操作结果
     */
    public DataResponse courseDelete(DataRequest dataRequest) {
        try {
            // 权限拦截：只有管理员和教师能删除课程
            if (!RoleCheckUtil.isAdmin() && !RoleCheckUtil.hasRole("TEACHER")) {
                return CommonMethod.getReturnMessageError("权限不足，只有教师和管理员可以删除课程！");
            }

            Integer courseId = dataRequest.getInteger("courseId");
            if (courseId == null || courseId <= 0) {
                return CommonMethod.getReturnMessageError("课程 ID 不能为空！");
            }

            Optional<Course> op = courseRepository.findById(courseId);
            if (op.isPresent()) {
                courseRepository.delete(op.get());
                log.info("课程删除成功，courseId: {}, 操作人: {}", courseId, CommonMethod.getPersonId());
            } else {
                return CommonMethod.getReturnMessageError("课程不存在！");
            }
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("删除课程失败", e);
            throw new RuntimeException("删除失败：" + e.getMessage());
        }
    }
}
