package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.Person;
import cn.edu.sdu.java.server.models.User;
import cn.edu.sdu.java.server.models.UserType;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.PersonRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.repositorys.UserTypeRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder encoder;
    private final PersonRepository personRepository; // 注入 Person 操作类

    public UserService(UserRepository userRepository, UserTypeRepository userTypeRepository, PasswordEncoder encoder, PersonRepository personRepository) {
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.encoder = encoder;
        this.personRepository = personRepository;
    }

    /**
     * 查询所有用户列表
     */
    public DataResponse findAllUsers() {
        try {
            List<User> uList = userRepository.findAll();
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (User u : uList) {
                Map<String, Object> m = new HashMap<>();
                m.put("personId", u.getPersonId());
                m.put("userName", u.getUserName());
                // 安全处理：不返回密码
                m.put("userTypeName", u.getUserType() != null ? u.getUserType().getName() : "未知角色");
                m.put("loginCount", u.getLoginCount());
                m.put("lastLoginTime", u.getLastLoginTime());
                dataList.add(m);
            }
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 添加/修改用户
     */
    public DataResponse userSave(DataRequest dataRequest) {
        try {
            Map<String, Object> form = dataRequest.getMap("form");
            Integer personId = CommonMethod.getInteger(form, "personId"); // 修改时传入 ID
            String userName = CommonMethod.getString(form, "userName");
            String password = CommonMethod.getString(form, "password");
            String userTypeName = CommonMethod.getString(form, "userTypeName");

            if (userName == null || userName.isEmpty()) {
                return CommonMethod.getReturnMessageError("登录账号不能为空！");
            }

            User u;
            boolean isNew = false;

            // 1. 判断是新增还是修改
            if (personId != null) {
                Optional<User> op = userRepository.findById(personId);
                if (op.isPresent()) {
                    u = op.get();
                    // 修改时，同步更新关联的 Person 信息
                    Optional<Person> pOp = personRepository.findById(personId);
                    if (pOp.isPresent()) {
                        Person p = pOp.get();
                        p.setName(userName); // 同步更新 Person 的 name
                        p.setNum(userName);  // 同步更新 Person 的 num (学号/账号)
                        personRepository.saveAndFlush(p);
                    }
                } else {
                    return CommonMethod.getReturnMessageError("用户不存在！");
                }
            } else {
                // 新增逻辑
                if (userRepository.existsByUserName(userName)) {
                    return CommonMethod.getReturnMessageError("登录账号已存在！");
                }
                u = new User();
                u.setUserName(userName);
                u.setLoginCount(0);
                isNew = true;
            }

            // 2. 处理 personId (核心修复：确保 ID 有值)
            if (u.getPersonId() == null) {
                // 尝试查找是否已有对应的 Person 记录
                Optional<Person> pOp = personRepository.findByNum(userName);
                if (pOp.isPresent()) {
                    u.setPersonId(pOp.get().getPersonId());
                } else {
                    // 如果没有 Person，自动创建一个基础 Person 记录
                    Person p = new Person();
                    p.setNum(userName);
                    p.setName(userName); // 默认名字同账号
                    p.setType("0"); // 默认类型
                    personRepository.saveAndFlush(p); // 保存并立即获取 ID
                    u.setPersonId(p.getPersonId());
                }
            }

            // 3. 设置角色
            UserType ut = userTypeRepository.findByName("ROLE_" + userTypeName);
            if (ut == null) ut = userTypeRepository.findByName(userTypeName);
            u.setUserType(ut);

            // 4. 处理密码
            if (isNew || (password != null && !password.isEmpty())) {
                u.setPassword(encoder.encode(password != null ? password : "123456"));
            }

            userRepository.saveAndFlush(u);
            return CommonMethod.getReturnData(u.getPersonId());
        } catch (Exception e) {
            log.error("保存用户失败", e);
            return CommonMethod.getReturnMessageError("保存失败：" + e.getMessage());
        }
    }

    /**
     * 根据账号查询用户（按账号查用户）
     */
    public DataResponse findByUserName(String userName) {
        try {
            Optional<User> op = userRepository.findByUserName(userName);
            if (op.isPresent()) {
                User u = op.get();
                Map<String, Object> m = new HashMap<>();
                m.put("personId", u.getPersonId());
                m.put("userName", u.getUserName());
                m.put("userTypeName", u.getUserType() != null ? u.getUserType().getName() : "未知角色");
                m.put("loginCount", u.getLoginCount());
                m.put("lastLoginTime", u.getLastLoginTime());
                return CommonMethod.getReturnData(m);
            } else {
                return CommonMethod.getReturnMessageError("用户不存在！");
            }
        } catch (Exception e) {
            log.error("查询用户失败", e);
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据角色名称查询用户列表（按角色查用户）
     */
    public DataResponse findByRoleName(String roleName) {
        try {
            List<User> uList = userRepository.findAll();
            List<Map<String, Object>> dataList = new ArrayList<>();
            for (User u : uList) {
                if (u.getUserType() != null && u.getUserType().getName().equals(roleName)) {
                    Map<String, Object> m = new HashMap<>();
                    m.put("personId", u.getPersonId());
                    m.put("userName", u.getUserName());
                    m.put("userTypeName", u.getUserType().getName());
                    m.put("loginCount", u.getLoginCount());
                    dataList.add(m);
                }
            }
            return CommonMethod.getReturnData(dataList);
        } catch (Exception e) {
            log.error("按角色查询用户失败", e);
            return CommonMethod.getReturnMessageError("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户
     */
    public DataResponse deleteUser(Integer personId) {
        try {
            if (personId == null) {
                return CommonMethod.getReturnMessageError("用户 ID 不能为空！");
            }
            Optional<User> op = userRepository.findById(personId);
            if (op.isPresent()) {
                userRepository.deleteById(personId);
                return CommonMethod.getReturnMessageOK("删除成功！");
            } else {
                return CommonMethod.getReturnMessageError("用户不存在！");
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return CommonMethod.getReturnMessageError("删除失败：" + e.getMessage());
        }
    }
}



