package cn.edu.sdu.java.server.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 角色判断工具类
 */
public class RoleCheckUtil {

    /**
     * 检查当前登录用户是否拥有指定角色
     * @param roleName 角色名称 (如: "ADMIN", "STUDENT")
     * @return true 如果拥有该角色
     */
    public static boolean hasRole(String roleName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String targetRole = "ROLE_" + roleName.toUpperCase(); // Spring Security 默认前缀

        for (GrantedAuthority authority : authorities) {
            if (targetRole.equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查当前用户是否是管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 检查当前用户是否是学生
     */
    public static boolean isStudent() {
        return hasRole("STUDENT");
    }

    /**
     * 判断当前用户是否是管理员或教师
     */
    public static boolean isAdminOrTeacher() {
        return isAdmin() || hasRole("TEACHER");
    }
}
