package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.request.LoginRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.payload.response.JwtResponse;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import cn.edu.sdu.java.server.util.LoginControlUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final PersonRepository personRepository;
    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final StudentRepository studentRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(PersonRepository personRepository, UserRepository userRepository, UserTypeRepository userTypeRepository, StudentRepository studentRepository,AuthenticationManager authenticationManager, JwtService jwtService, PasswordEncoder encoder) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.userTypeRepository = userTypeRepository;
        this.studentRepository = studentRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }
    public ResponseEntity<?> authenticateUser(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            Optional<User> op= userRepository.findByUserName(loginRequest.getUsername());
            if(op.isPresent()) {
                User user= op.get();
                user.setLastLoginTime(DateTimeTool.parseDateTime(new Date()));
                Integer count = user.getLoginCount();
                if (count == null)
                    count = 1;
                else count += 1;
                user.setLoginCount(count);
                userRepository.save(user);
            }
            String jwt = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getPerName(),
                    roles.getFirst()));
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return ResponseEntity.status(401).body(CommonMethod.getReturnMessageError("用户名或密码错误：" + e.getMessage()));
        }
    }
    public DataResponse getValidateCode() {
        return CommonMethod.getReturnData(LoginControlUtil.getInstance().getValidateCodeDataMap());
    }

    public DataResponse testValidateInfo( DataRequest dataRequest) {
        Integer validateCodeId = dataRequest.getInteger("validateCodeId");
        String validateCode = dataRequest.getString("validateCode");
        LoginControlUtil li =  LoginControlUtil.getInstance();
        if(validateCodeId == null || validateCode== null || validateCode.isEmpty()) {
            return CommonMethod.getReturnMessageError("验证码为空！");
        }
        String value = li.getValidateCode(validateCodeId);
        if(!validateCode.equals(value))
            return CommonMethod.getReturnMessageError("验证码错位！");
        return CommonMethod.getReturnMessageOK();
    }
    
    /**
     * 生成 BCrypt 加密密码（用于修复数据库密码）
     */
    public DataResponse generatePassword(String password) {
        String encoded = encoder.encode(password);
        Map<String, Object> result = new HashMap<>();
        result.put("original", password);
        result.put("encoded", encoded);
        result.put("sql", "UPDATE user SET password = '" + encoded + "' WHERE user_name = 'admin';");
        return CommonMethod.getReturnData(result);
    }
    
    /*
     *  注册用户示例，我们项目暂时不用， 所有用户通过管理员添加，这里注册，没有考虑关联人员信息的创建，使用时参加学生添加功能的实现
     */
    @PostMapping("/registerUser")
    public DataResponse registerUser(@Valid @RequestBody DataRequest dataRequest) {
        String username = dataRequest.getString("username");
        String password = dataRequest.getString("password");
        String perName = dataRequest.getString("perName");
        String email = dataRequest.getString("email");
        String role = dataRequest.getString("role");
        UserType ut = null;
        Optional<User> uOp = userRepository.findByUserName(username);
        if(uOp.isPresent()) {
            return CommonMethod.getReturnMessageError("用户已经存在，不能注册！");
        }
        Person p = new Person();
        p.setNum(username);
        p.setName(perName);
        p.setEmail(email);
        if("ADMIN".equals(role)) {
            p.setType("1");
            ut = userTypeRepository.findByName(EUserType.ROLE_ADMIN.name());
        }else if("STUDENT".equals(role)) {
            p.setType("2");
            ut = userTypeRepository.findByName(EUserType.ROLE_STUDENT.name());
        }else if("TEACHER".equals(role)) {
            p.setType("3");
            ut = userTypeRepository.findByName(EUserType.ROLE_TEACHER.name());
        }
        personRepository.saveAndFlush(p);
        User u = new User();
        u.setPerson(p);
        u.setUserType(ut);
        u.setUserName(username);
        u.setPassword(encoder.encode(password));
        u.setCreateTime(DateTimeTool.parseDateTime(new Date()));
        u.setCreatorId(p.getPersonId());
        u.setLoginCount(0);
        userRepository.saveAndFlush(u);
        if("STUDENT".equals(role)) {
            Student s = new Student();   // 创建实体对象
            s.setPerson(p);
            studentRepository.saveAndFlush(s);  //插入新的Student记录
        }
        return CommonMethod.getReturnData(LoginControlUtil.getInstance().getValidateCodeDataMap());
    }
    
    /**
     * 退出登录
     */
    public DataResponse logout() {
        try {
            // 清除SecurityContext中的认证信息
            SecurityContextHolder.clearContext();
            log.info("用户退出登录成功，操作人: {}", CommonMethod.getUsername());
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("退出登录失败", e);
            return CommonMethod.getReturnMessageError("退出失败：" + e.getMessage());
        }
    }
    
    /**
     * 修改密码
     */
    public DataResponse changePassword(DataRequest dataRequest) {
        try {
            String oldPassword = dataRequest.getString("oldPassword");
            String newPassword = dataRequest.getString("newPassword");
            String confirmPassword = dataRequest.getString("confirmPassword");
            
            // 参数校验
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                return CommonMethod.getReturnMessageError("旧密码不能为空！");
            }
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return CommonMethod.getReturnMessageError("新密码不能为空！");
            }
            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                return CommonMethod.getReturnMessageError("确认密码不能为空！");
            }
            if (!newPassword.equals(confirmPassword)) {
                return CommonMethod.getReturnMessageError("两次输入的新密码不一致！");
            }
            if (newPassword.length() < 6) {
                return CommonMethod.getReturnMessageError("新密码长度不能少于6位！");
            }
            
            // 获取当前用户
            String username = CommonMethod.getUsername();
            if (username == null || username.trim().isEmpty()) {
                return CommonMethod.getReturnMessageError("用户未登录！");
            }
            
            Optional<User> userOp = userRepository.findByUserName(username);
            if (userOp.isEmpty()) {
                return CommonMethod.getReturnMessageError("用户不存在！");
            }
            
            User user = userOp.get();
            
            // 验证旧密码
            if (!encoder.matches(oldPassword, user.getPassword())) {
                return CommonMethod.getReturnMessageError("旧密码错误！");
            }
            
            // 检查新密码是否与旧密码相同
            if (encoder.matches(newPassword, user.getPassword())) {
                return CommonMethod.getReturnMessageError("新密码不能与旧密码相同！");
            }
            
            // 更新密码
            user.setPassword(encoder.encode(newPassword));
            userRepository.save(user);
            
            log.info("用户修改密码成功，用户名: {}", username);
            return CommonMethod.getReturnMessageOK();
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return CommonMethod.getReturnMessageError("修改失败：" + e.getMessage());
        }
    }
}
