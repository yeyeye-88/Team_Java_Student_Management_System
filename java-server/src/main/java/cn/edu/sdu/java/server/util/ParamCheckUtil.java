package cn.edu.sdu.java.server.util;

/**
 * 参数校验工具类
 */
public class ParamCheckUtil {

    /**
     * 检查字符串是否为空或 null
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串长度是否超过限制
     */
    public static boolean isOverLength(String str, int maxLength) {
        return str != null && str.length() > maxLength;
    }

    /**
     * 校验必填项，如果为空则返回错误信息
     */
    public static String checkRequired(String value, String fieldName) {
        if (isBlank(value)) {
            return fieldName + "不能为空！";
        }
        return null; // 表示校验通过
    }
}
