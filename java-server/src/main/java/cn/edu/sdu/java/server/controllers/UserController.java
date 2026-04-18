package cn.edu.sdu.java.server.controllers;

import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 查询所有用户（管理员、教师可看，学生不可看）
     */
    @PostMapping("/findAll")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public DataResponse findAll(@Valid @RequestBody DataRequest dataRequest) {
        return userService.findAllUsers();
    }

    /**
     * 添加/修改用户（仅管理员可操作）
     */
    @PostMapping("/userSave")
    @PreAuthorize("hasRole('ADMIN')")
    public DataResponse userSave(@Valid @RequestBody DataRequest dataRequest) {
        return userService.userSave(dataRequest);
    }
}
