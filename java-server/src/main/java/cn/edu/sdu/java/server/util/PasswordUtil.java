package cn.edu.sdu.java.server.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * 用于生成 BCrypt 加密后的密码，方便初始化测试数据
 */
public class PasswordUtil {
    
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return BCrypt 加密后的密码
     */
    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
    
    /**
     * 主方法 - 生成常用密码的 BCrypt 加密值
     */
    public static void main(String[] args) {
        System.out.println("=== BCrypt 密码加密工具 ===\n");
        
        String[] passwords = {"123456", "admin", "admin123", "password"};
        
        for (String pwd : passwords) {
            String encoded = encode(pwd);
            System.out.println("原始密码: " + pwd);
            System.out.println("加密后:   " + encoded);
            System.out.println("验证结果: " + matches(pwd, encoded));
            System.out.println("---");
        }
        
        System.out.println("\n=== SQL 更新语句示例 ===\n");
        System.out.println("-- 更新 admin 用户密码为 123456");
        System.out.println("UPDATE user SET password = '" + encode("123456") + "' WHERE user_name = 'admin';");
    }
}
