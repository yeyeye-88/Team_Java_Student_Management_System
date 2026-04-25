package cn.edu.sdu.java.server.services;

import cn.edu.sdu.java.server.models.*;
import cn.edu.sdu.java.server.payload.response.OptionItem;
import cn.edu.sdu.java.server.repositorys.*;
import cn.edu.sdu.java.server.util.ComDataUtil;
import cn.edu.sdu.java.server.util.CommonMethod;
import cn.edu.sdu.java.server.util.DateTimeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * SystemService 系统服务行数
 */
@Service
public class SystemService {
    private static final Logger log = LoggerFactory.getLogger(SystemService.class);
    private final DictionaryInfoRepository dictionaryInfoRepository; //数据数据操作自动注入
    private final SystemInfoRepository systemInfoRepository; //数据数据操作自动注入

    private final ModifyLogRepository modifyLogRepository; //数据数据操作自动注入
    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public SystemService(DictionaryInfoRepository dictionaryInfoRepository, SystemInfoRepository systemInfoRepository, ModifyLogRepository modifyLogRepository, UserRepository userRepository, PersonRepository personRepository, UserTypeRepository userTypeRepository, PasswordEncoder passwordEncoder) {
        this.dictionaryInfoRepository = dictionaryInfoRepository;
        this.systemInfoRepository = systemInfoRepository;
        this.modifyLogRepository = modifyLogRepository;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.userTypeRepository = userTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }
    /**
     *  initDictionary 初始数据字典 在系统初始时将数据字典加载内存，业务处理是直接从内从中获取数数据字典列表和数据字典名称
     */
    public void initDictionary() {
        List<OptionItem> itemList;
        OptionItem item;
        Map<String, String> sMap;
        String value;
        Map<String, List<OptionItem>> dictListMap = ComDataUtil.getInstance().getDictListMap();
        Map<String, Map<String, String>> dictMapMap = ComDataUtil.getInstance().getDictMapMap();
        List<DictionaryInfo>  dList =dictionaryInfoRepository.findRootList();
        List<DictionaryInfo> sList;
        for(DictionaryInfo df : dList) {
            value = df.getValue();
            sMap = new HashMap<>();
            dictMapMap.put(value, sMap);
            itemList = new ArrayList<>();
            dictListMap.put(value, itemList);
            sList = dictionaryInfoRepository.findByPid(df.getId());
            for (DictionaryInfo d : sList) {
                sMap.put(d.getValue(), d.getLabel());
                item = new OptionItem(d.getId(), d.getValue(), d.getLabel());
                itemList.add(item);
            }
        }
        ComDataUtil pi = ComDataUtil.getInstance();
        pi.setDictListMap(dictListMap);
        pi.setDictMapMap(dictMapMap);
    }
    public void initSystem() {
        List<SystemInfo> sList = systemInfoRepository.findAll();
        Map<String,String> map = new HashMap<>();
        for(SystemInfo s:sList) {
            map.put(s.getName(),s.getValue());
        }
        ComDataUtil pi = ComDataUtil.getInstance();
        pi.setSystemMap(map);
        
        // 初始化默认管理员账户
        initDefaultAdmin();
    }
    
    /**
     * 初始化默认管理员账户（如果不存在）
     */
    private void initDefaultAdmin() {
        String adminUsername = "admin";
        Optional<User> existingUser = userRepository.findByUserName(adminUsername);
        
        if (existingUser.isEmpty()) {
            log.info("创建默认管理员账户: {}", adminUsername);
            
            // 创建 Person
            Person person = new Person();
            person.setNum(adminUsername);
            person.setName("系统管理员");
            person.setType("0"); // 管理员类型
            personRepository.saveAndFlush(person);
            
            // 获取管理员用户类型
            UserType adminType = userTypeRepository.findByName(EUserType.ROLE_ADMIN.name());
            if (adminType == null) {
                log.warn("未找到管理员用户类型，请先初始化 user_type 表");
                return;
            }
            
            // 创建 User
            User user = new User();
            user.setPersonId(person.getPersonId());
            user.setPerson(person);
            user.setUserType(adminType);
            user.setUserName(adminUsername);
            user.setPassword(passwordEncoder.encode("123456")); // BCrypt 加密
            user.setLoginCount(0);
            user.setCreateTime(DateTimeTool.parseDateTime(new Date()));
            user.setCreatorId(person.getPersonId());
            userRepository.saveAndFlush(user);
            
            log.info("默认管理员账户创建成功！用户名: {}, 密码: 123456", adminUsername);
        } else {
            log.debug("管理员账户已存在，跳过初始化");
        }
    }
    public void modifyLog(Object o, boolean isCreate) {
        String info = CommonMethod.ObjectToJSon(o);
        if(info == null)
            return;
        String tableName = o.getClass().getName();
        int index = tableName.lastIndexOf('.');
        if(index > 0) {
            tableName = tableName.substring(index+1);
        }
        ModifyLog l = new ModifyLog();
        l.setTableName(tableName);
        if(isCreate)
            l.setType("0");
        else
            l.setType("1");
        l.setInfo(info);
        l.setOperateTime(DateTimeTool.parseDateTime(new Date()));
        l.setOperatorId(CommonMethod.getPersonId());
        modifyLogRepository.save(l);
    }
}

