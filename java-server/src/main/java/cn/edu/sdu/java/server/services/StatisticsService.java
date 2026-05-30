package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.StatisticsDay;
import cn.edu.sdu.java.server.payload.request.DataRequest;
import cn.edu.sdu.java.server.payload.response.DataResponse;
import cn.edu.sdu.java.server.repositorys.ModifyLogRepository;
import cn.edu.sdu.java.server.repositorys.RequestLogRepository;
import cn.edu.sdu.java.server.repositorys.StatisticsDayRepository;
import cn.edu.sdu.java.server.repositorys.UserRepository;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class StatisticsService {
    private final UserRepository userRepository;
    private final RequestLogRepository requestLogRepository;
    private final ModifyLogRepository modifyLogRepository;
    
    public StatisticsService(UserRepository userRepository, 
                            RequestLogRepository requestLogRepository,
                            ModifyLogRepository modifyLogRepository) {
        this.userRepository = userRepository;
        this.requestLogRepository = requestLogRepository;
        this.modifyLogRepository = modifyLogRepository;
    }

    public DataResponse getMainPageData(DataRequest dataRequest) {
        Date today = new Date();
        int i;
        Integer id;
        Object[] a;
        Long l;
        String name;
        
        // 1. 实时统计用户总数
        long total = userRepository.count();
        
        // 2. 实时统计本月登录数
        Date monthStart = DateTimeTool.formatDateTime(DateTimeTool.parseDateTime(today, "yyyy-MM-01") + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
        Integer monthCount = userRepository.countLastLoginTime(DateTimeTool.parseDateTime(monthStart, "yyyy-MM-dd HH:mm:ss"));
        
        // 3. 实时统计今日登录数
        Integer dayCount = userRepository.countLastLoginTime(DateTimeTool.parseDateTime(today, "yyyy-MM-dd") + " 00:00:00");
        
        Map<String,Object> data = new HashMap<>();
        Map<String,Object> m = new HashMap<>();
        m.put("total", (int) total);
        m.put("monthCount", monthCount);
        m.put("dayCount", dayCount);
        data.put("onlineUser", m);
        
        // 4. 实时统计各角色用户数
        List<?> nList = userRepository.getCountList();
        List<Map<String,Object>> userTypeList = new ArrayList<>();
        for(i = 0; i < nList.size(); i++) {
            m = new HashMap<>();
            a = (Object[])nList.get(i);
            id = (Integer)a[0];
            l = (Long)a[1];
            if(id == 1)
                name = "管理员";
            else if(id == 2)
                name = "学生";
            else if(id == 3)
                name = "教师";
            else
                name = "";
            m.put("name", name);
            m.put("value", l.intValue());
            userTypeList.add(m);
        }
        data.put("userTypeList", userTypeList);
        
        // 5. 【关键修改】动态生成最近10天的统计数据，实时查询
        List<String> dayList = new ArrayList<>();
        List<Integer> lList = new ArrayList<>();  // 登录数
        List<Integer> rList = new ArrayList<>();  // 请求数
        List<Integer> cList = new ArrayList<>();  // 创建数
        List<Integer> mList = new ArrayList<>();  // 修改数
        
        // 遍历最近10天（从今天往前推9天）
        for (int d = 9; d >= 0; d--) {
            Date currentDate = DateTimeTool.prevDay(today, d);
            String dateStr = DateTimeTool.parseDateTime(currentDate, "yyyyMMdd");
            String dateTimeStr = DateTimeTool.parseDateTime(currentDate, "yyyy-MM-dd");
            
            dayList.add(dateStr);
            
            // 实时查询当天的登录次数
            Integer loginCount = userRepository.countLoginByDate(
                dateTimeStr + " 00:00:00", 
                dateTimeStr + " 23:59:59"
            );
            lList.add(loginCount != null ? loginCount : 0);
            
            // 实时查询当天的请求次数
            Integer requestCount = requestLogRepository.countByDate(
                dateTimeStr + " 00:00:00", 
                dateTimeStr + " 23:59:59"
            );
            rList.add(requestCount != null ? requestCount : 0);
            
            // 实时查询当天的创建数（暂时用0，可根据需求扩展）
            cList.add(0);
            
            // 实时查询当天的修改数
            Integer modifyCount = modifyLogRepository.countByDate(
                dateTimeStr + " 00:00:00", 
                dateTimeStr + " 23:59:59"
            );
            mList.add(modifyCount != null ? modifyCount : 0);
        }
        
        m = new HashMap<>();
        m.put("value", dayList);
        m.put("label1", lList);
        m.put("label2", rList);
        data.put("requestData", m);
        
        m = new HashMap<>();
        m.put("value", dayList);
        m.put("label1", cList);
        m.put("label2", mList);
        data.put("operateData", m);

        return CommonMethod.getReturnData(data);
    }

}
