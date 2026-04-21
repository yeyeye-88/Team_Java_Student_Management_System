package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Course;
import cn.edu.sdu.java.server.models.Score;
import cn.edu.sdu.java.server.models.Student;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.OptionItemList;
import cn.edu.sdu.java.server.repositorys.CourseRepository;
import cn.edu.sdu.java.server.repositorys.ScoreRepository;
import cn.edu.sdu.java.server.repositorys.StudentRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * ScoreService 成绩管理业务逻辑层
 * 实现成绩查询、录入、修改、删除等功能
 */
@Slf4j
@Service
public class ScoreService {
    private final ScoreRepository scoreRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public ScoreService(ScoreRepository scoreRepository, StudentRepository studentRepository, CourseRepository courseRepository) {
        this.scoreRepository = scoreRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    /**
     * 查询成绩列表（支持按学生 ID、课程 ID、课程名称筛选）
     * 权限控制：
     * - 管理员/教师：可以查询所有学生成绩
     * - 学生：只能查询自己的成绩
     * @param dataRequest 请求参数，包含 personId、courseId、courseName
     * @return 成绩列表（Map 格式）
     */
    public DataResponse getScoreList(DataRequest dataRequest) {
        try {
            // 获取当前登录用户的 personId
            Integer currentUserId = CommonMethod.getPersonId();
            
            // 判断角色：管理员或教师可以看全部，学生只能看自己
            boolean isAdminOrTeacher = RoleCheckUtil.isAdmin() || RoleCheckUtil.hasRole("TEACHER");
            
            Integer personId = dataRequest.getInteger("personId");
            Integer courseId = dataRequest.getInteger("courseId");
            String courseName = dataRequest.getString("courseName");
            
            // 如果是学生，强制只能查自己的成绩
            if (!isAdminOrTeacher) {
                personId = currentUserId;
            }

            List<Score> scoreList;
            if (courseId != null && courseId > 0) {
                scoreList = scoreRepository.findByStudentCourse(personId, courseId);
            } else if (courseName != null && !courseName.isEmpty()) {
                scoreList = scoreRepository.findByStudentCourse(personId, courseName);
            } else {
                scoreList = scoreRepository.findByStudentPersonId(personId);
            }

            return CommonMethod.getReturnData(getScoreMapList(scoreList));
        } catch (Exception e) {
            log.error("查询成绩列表失败", e);
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存成绩（新增或修改）
     * @param dataRequest 请求参数，包含 scoreId（修改时必填）和 form（表单数据）
     * @return 操作结果
     */
    public DataResponse scoreSave(DataRequest dataRequest) {
        try {
            Integer scoreId = dataRequest.getInteger("scoreId");
            Map<String, Object> form = dataRequest.getMap("form");
            Integer personId = CommonMethod.getInteger(form, "personId");
            Integer courseId = CommonMethod.getInteger(form, "courseId");
            Integer mark = CommonMethod.getInteger(form, "mark");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(personId != null ? personId.toString() : "", "学生 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            errorMsg = ParamCheckUtil.checkRequired(courseId != null ? courseId.toString() : "", "课程 ID");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            if (mark == null || mark < 0 || mark > 100) {
                return CommonMethod.getReturnMessageError("成绩必须在 0-100 之间！");
            }

            Score score;
            boolean isNew = false;
            if (scoreId != null && scoreId > 0) {
                Optional<Score> op = scoreRepository.findById(scoreId);
                if (op.isPresent()) {
                    score = op.get();
                } else {
                    return CommonMethod.getReturnMessageError("成绩记录不存在！");
                }
            } else {
                score = new Score();
                isNew = true;
            }

            // 关联学生和课程
            Optional<Student> sOp = studentRepository.findById(personId);
            if (sOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("学生不存在！");
            }
            score.setStudent(sOp.get());

            Optional<Course> cOp = courseRepository.findById(courseId);
            if (cOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("课程不存在！");
            }
            score.setCourse(cOp.get());

            score.setMark(mark);
            scoreRepository.save(score);

            return CommonMethod.getReturnData(score.getScoreId());
        } catch (Exception e) {
            log.error("保存成绩失败", e);
            return CommonMethod.getReturnMessageError("保存失败：" + e.getMessage());
        }
    }

    /**
     * 删除成绩记录
     * @param dataRequest 请求参数，包含 scoreId
     * @return 操作结果
     */
    public DataResponse scoreDelete(DataRequest dataRequest) {
        try {
            Integer scoreId = dataRequest.getInteger("scoreId");
            if (scoreId == null || scoreId <= 0) {
                return CommonMethod.getReturnMessageError("成绩 ID 不能为空！");
            }

            scoreRepository.deleteById(scoreId);
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("删除成绩失败，scoreId: {}", dataRequest.getInteger("scoreId"), e);
            return CommonMethod.getReturnMessageError("删除失败：" + e.getMessage());
        }
    }

    /**
     * 获取学生下拉选项列表（用于前端选择学生）
     */
    public OptionItemList getStudentItemOptionList(DataRequest dataRequest) {
        try {
            List<Student> studentList = studentRepository.findAll();
            List<cn.edu.sdu.java.server.payload.response.OptionItem> items = studentList.stream().map(s ->
                new cn.edu.sdu.java.server.payload.response.OptionItem(s.getPerson().getPersonId(), s.getPerson().getName(), s.getPerson().getName())
            ).toList();
            return new OptionItemList(0, items);
        } catch (Exception e) {
            log.error("获取学生下拉列表失败", e);
            return new OptionItemList(0, new ArrayList<>());
        }
    }

    /**
     * 获取课程下拉选项列表（用于前端选择课程）
     */
    public OptionItemList getCourseItemOptionList(DataRequest dataRequest) {
        try {
            List<Course> courseList = courseRepository.findAll();
            List<cn.edu.sdu.java.server.payload.response.OptionItem> items = courseList.stream().map(c ->
                new cn.edu.sdu.java.server.payload.response.OptionItem(c.getCourseId(), c.getName(), c.getName())
            ).toList();
            return new OptionItemList(0, items);
        } catch (Exception e) {
            log.error("获取课程下拉列表失败", e);
            return new OptionItemList(0, new ArrayList<>());
        }
    }

    /**
     * 将 Score 对象列表转换为 Map 列表（用于前端渲染）
     */
    private List<Map<String, Object>> getScoreMapList(List<Score> scoreList) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (scoreList == null || scoreList.isEmpty()) {
            return result;
        }

        for (Score s : scoreList) {
            Map<String, Object> m = new HashMap<>();
            m.put("scoreId", s.getScoreId());
            m.put("personId", s.getStudent().getPerson().getPersonId());
            m.put("studentNum", s.getStudent().getPerson().getNum());
            m.put("studentName", s.getStudent().getPerson().getName());
            m.put("courseId", s.getCourse().getCourseId());
            m.put("courseNum", s.getCourse().getNum());
            m.put("courseName", s.getCourse().getName());
            m.put("credit", s.getCourse().getCredit());
            m.put("mark", s.getMark());
            m.put("ranking", s.getRanking());
            result.add(m);
        }
        return result;
    }
}
