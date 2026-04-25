package cn.edu.sdu.java.server.util;

import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.math.BigDecimal;
import java.util.*;

/**
 * CommonMethod 公共处理方法实例类
 */
public class CommonMethod {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static final MediaType exelType = new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    public static DataResponse getReturnData(Object obj, String msg){
        return new   DataResponse(0,obj,msg);
    }
    public static DataResponse getReturnMessage(Integer code, String msg){
        return new   DataResponse(code,null,msg);
    }
    public static  DataResponse getReturnData(Object obj){
        return getReturnData(obj,null);
    }
    public static DataResponse getReturnMessageOK(String msg){
        return getReturnMessage(0, msg);
    }
    public static DataResponse getReturnMessageOK(){
        return getReturnMessage(0, null);
    }
    public static DataResponse getReturnMessageError(String msg){
        return getReturnMessage(1, msg);
    }

    /**
     * Integer getPersonId() 获得用户的personId 该static 方法可以在任何的Controller和Service中使用，可以获得当前登录用户的用户ID，可以通过在Web请求服务中使用该方法获取当前请求客户的信息
     * @return 当前登录用户的Person ID，如果未登录或测试环境则返回 null
     */
    public static Integer getPersonId(){
        try {
            Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!(obj instanceof UserDetailsImpl userDetails))
                return null;
            return userDetails.getId();
        } catch (Exception e) {
            // 测试环境或未登录时返回 null
            return null;
        }
    }
    public static String getUsername(){
        try {
            Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!(obj instanceof UserDetailsImpl userDetails))
                return null;
            return userDetails.getUsername();
        } catch (Exception e) {
            // 测试环境或未登录时返回 null
            return null;
        }
    }
    public static String getRoleName(){
        try {
            Object obj = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(!(obj instanceof UserDetailsImpl userDetails))
                return null;
            return  userDetails.getAuthorities().iterator().next().toString();
        } catch (Exception e) {
            // 测试环境或未登录时返回 null
            return null;
        }
    }

    public static String getString(Map<String,Object> data,String key){
        Object obj = data.get(key);
        if(obj == null)
            return "";
        if(obj instanceof String)
            return (String)obj;
        return obj.toString();
    }

    public static Integer getInteger(Map<String,Object> data,String key) {
        Object obj = data.get(key);
        if(obj == null)
            return null;
        if(obj instanceof Integer)
            return (Integer)obj;
        String str = obj.toString();
        try {
            return (int)Double.parseDouble(str);
        }catch(Exception e) {
            return null;
        }
    }

    public static Double getDouble0(Map<String,Object> data,String key) {
        Double d0 = 0d;
        Object obj = data.get(key);
        if(obj == null)
            return d0;
        if(obj instanceof Double)
            return (Double)obj;
        String str = obj.toString();
        try {
            return Double.parseDouble(str);
        }catch(Exception e) {
            return d0;
        }
    }

    public static Double getDouble2(Double f) {
        if (f == null)
            return 0d;
        BigDecimal bg = new BigDecimal(f);
        return bg.setScale(2, java.math.RoundingMode.HALF_UP).doubleValue();
    }
    public static String ObjectToJSon(Object o){
        try {
            return mapper.writeValueAsString(o);
        }catch(Exception e){
            return "";
        }
    }
    public static XSSFCellStyle createCellStyle(XSSFWorkbook workbook, int fontSize) {
        XSSFFont font = workbook.createFont();
        //在对应的workbook中新建字体
        font.setFontName("微软雅黑");
        //字体微软雅黑
        font.setFontHeightInPoints((short) fontSize);
        //设置字体大小
        XSSFCellStyle style = workbook.createCellStyle();
        //新建Cell字体
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
