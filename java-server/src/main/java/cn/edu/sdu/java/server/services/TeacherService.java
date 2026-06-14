package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.Teacher;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.TeacherRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.ParamCheckUtil;
import cn.edu.sdu.java.server.util.RoleCheckUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * TeacherService 教师信息服务层
 */
@Service
public class TeacherService {
    private static final Logger log = LoggerFactory.getLogger(TeacherService.class);
    
    private final PersonRepository personRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final SystemService systemService;

    public TeacherService(PersonRepository personRepository, 
                         TeacherRepository teacherRepository,
                         UserRepository userRepository,
                         SystemService systemService) {
        this.personRepository = personRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.systemService = systemService;
    }

    /**
     * 将 Teacher 对象转换为 Map
     */
    public Map<String, Object> getMapFromTeacher(Teacher t) {
        Map<String, Object> m = new HashMap<>();
        if (t == null) {
            return m;
        }
        
        Person p = t.getPerson();
        if (p != null) {
            m.put("personId", t.getPersonId());
            m.put("num", p.getNum());
            m.put("name", p.getName());
            m.put("type", p.getType());
            m.put("dept", p.getDept());
            m.put("card", p.getCard());
            m.put("gender", p.getGender());
            
            // 将 LocalDate 转换为字符串
            if (p.getBirthday() != null) {
                m.put("birthday", p.getBirthday().toString());
            } else {
                m.put("birthday", null);
            }
            
            m.put("email", p.getEmail());
            m.put("phone", p.getPhone());
            m.put("address", p.getAddress());
            m.put("introduce", p.getIntroduce());
        }
        
        m.put("title", t.getTitle());
        m.put("degree", t.getDegree());
        
        return m;
    }

    /**
     * 获取教师详细信息
     * @param dataRequest 请求参数，包含 personId
     * @return 教师详细信息
     */
    public DataResponse getTeacherInfo(DataRequest dataRequest) {
        try {
            Integer personId = dataRequest.getInteger("personId");
            if (personId == null || personId <= 0) {
                return CommonMethod.getReturnMessageError("教师 ID 不能为空！");
            }

            // 权限校验：老师只能查看自己的信息
            if (!RoleCheckUtil.isAdmin()) {
                Integer currentPersonId = CommonMethod.getPersonId();
                if (currentPersonId == null) {
                    return CommonMethod.getReturnMessageError("用户未登录！");
                }
                
                // 如果是教师角色，只能查看自己的信息
                if (RoleCheckUtil.hasRole("TEACHER")) {
                    if (!currentPersonId.equals(personId)) {
                        log.warn("越权访问拦截：老师 {} 尝试查看老师 {} 的信息", currentPersonId, personId);
                        return CommonMethod.getReturnMessageError("权限不足，只能查看自己的信息！");
                    }
                }
                // 其他角色（如学生）不允许查看教师信息
                else {
                    log.warn("非管理员/教师尝试查看教师信息，personId: {}, 当前用户: {}", personId, currentPersonId);
                    return CommonMethod.getReturnMessageError("权限不足，无法查看教师信息！");
                }
            }

            Optional<Teacher> op = teacherRepository.findById(personId);
            if (op.isEmpty()) {
                return CommonMethod.getReturnMessageError("教师不存在！");
            }

            Teacher t = op.get();
            return CommonMethod.getReturnData(getMapFromTeacher(t));
        } catch (Exception e) {
            log.error("查询教师信息失败，personId: {}", dataRequest.getInteger("personId"), e);
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 保存教师信息（仅支持修改）
     * @param dataRequest 请求参数，包含 personId 和 form
     * @return 修改结果
     */
    @Transactional
    public DataResponse teacherEditSave(DataRequest dataRequest) {
        try {
            Integer personId = dataRequest.getInteger("personId");
            Map<String, Object> form = dataRequest.getMap("form");
            
            String name = CommonMethod.getString(form, "name");
            String title = CommonMethod.getString(form, "title");
            String degree = CommonMethod.getString(form, "degree");

            // 参数校验
            String errorMsg = ParamCheckUtil.checkRequired(name, "姓名");
            if (errorMsg != null) return CommonMethod.getReturnMessageError(errorMsg);

            if (ParamCheckUtil.isOverLength(name, 50)) {
                return CommonMethod.getReturnMessageError("姓名长度不能超过 50 个字符！");
            }

            // 权限校验：老师只能修改自己的信息
            if (!RoleCheckUtil.isAdmin()) {
                Integer currentPersonId = CommonMethod.getPersonId();
                if (currentPersonId == null) {
                    return CommonMethod.getReturnMessageError("用户未登录！");
                }
                
                // 如果是教师角色，只能修改自己的信息
                if (RoleCheckUtil.hasRole("TEACHER")) {
                    if (personId == null || !currentPersonId.equals(personId)) {
                        log.warn("越权修改拦截：老师 {} 尝试修改老师 {} 的信息", currentPersonId, personId);
                        return CommonMethod.getReturnMessageError("权限不足，只能修改自己的信息！");
                    }
                }
                // 其他角色（如学生）不允许修改教师信息
                else {
                    log.warn("非管理员/教师尝试修改教师信息，personId: {}, 当前用户: {}", personId, currentPersonId);
                    return CommonMethod.getReturnMessageError("权限不足，无法修改教师信息！");
                }
            }

            // 查询教师记录
            Optional<Teacher> op = teacherRepository.findById(personId);
            if (op.isEmpty()) {
                return CommonMethod.getReturnMessageError("教师不存在！");
            }

            Teacher t = op.get();
            Person p = t.getPerson();

            // 更新 Person 表
            p.setName(name);
            p.setDept(CommonMethod.getString(form, "dept"));
            p.setCard(CommonMethod.getString(form, "card"));
            p.setGender(CommonMethod.getString(form, "gender"));
            
            String birthdayStr = CommonMethod.getString(form, "birthday");
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                p.setBirthday(LocalDate.parse(birthdayStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                p.setBirthday(null);
            }
            
            p.setEmail(CommonMethod.getString(form, "email"));
            p.setPhone(CommonMethod.getString(form, "phone"));
            p.setAddress(CommonMethod.getString(form, "address"));
            p.setIntroduce(CommonMethod.getString(form, "introduce"));
            personRepository.save(p);

            // 更新 Teacher 表
            t.setTitle(title);
            t.setDegree(degree);
            teacherRepository.save(t);

            log.info("教师信息修改成功，personId: {}, 操作人: {}", personId, CommonMethod.getPersonId());
            return CommonMethod.getReturnData(personId);
        } catch (Exception e) {
            log.error("保存教师信息失败", e);
            throw new RuntimeException("保存失败：" + e.getMessage());
        }
    }
}
