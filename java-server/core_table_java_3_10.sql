-- ============================================
-- 核心表结构定义 SQL
-- 团队编号: java_3_10
-- 数据库: java_3_10
-- ============================================

-- 1. Person 人员表
CREATE TABLE IF NOT EXISTS `person` (
  `person_id` INT NOT NULL AUTO_INCREMENT COMMENT '人员ID，主键',
  `num` VARCHAR(20) NOT NULL COMMENT '人员编号（学号/工号）',
  `name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
  `type` VARCHAR(2) DEFAULT NULL COMMENT '人员类型：0-管理员，1-学生，2-教师',
  `dept` VARCHAR(50) DEFAULT NULL COMMENT '学院/部门',
  `card` VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
  `gender` VARCHAR(2) DEFAULT NULL COMMENT '性别：1-男，2-女',
  `birthday` VARCHAR(10) DEFAULT NULL COMMENT '出生日期',
  `email` VARCHAR(60) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话',
  `address` VARCHAR(20) DEFAULT NULL COMMENT '地址',
  `introduce` VARCHAR(1000) DEFAULT NULL COMMENT '个人简介',
  ` photo` LONGBLOB DEFAULT NULL COMMENT '照片（注意：JPA实体类中列名带空格）',
  PRIMARY KEY (`person_id`),
  UNIQUE KEY `uk_person_num` (`num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员表';

-- 2. Student 学生表
CREATE TABLE IF NOT EXISTS `student` (
  `person_id` INT NOT NULL COMMENT '学生ID，与person表主键相同',
  `major` VARCHAR(20) DEFAULT NULL COMMENT '专业',
  `class_name` VARCHAR(50) DEFAULT NULL COMMENT '班级',
  PRIMARY KEY (`person_id`),
  CONSTRAINT `fk_student_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 3. User 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `person_id` INT NOT NULL COMMENT '用户ID，与person表主键相同',
  `user_type_id` INT DEFAULT NULL COMMENT '用户类型ID',
  `user_name` VARCHAR(20) NOT NULL COMMENT '登录账号',
  `password` VARCHAR(60) NOT NULL COMMENT '密码（BCrypt加密）',
  `login_count` INT DEFAULT 0 COMMENT '登录次数',
  `last_login_time` VARCHAR(20) DEFAULT NULL COMMENT '最后登录时间',
  `create_time` VARCHAR(20) DEFAULT NULL COMMENT '创建时间',
  `creator_id` INT DEFAULT NULL COMMENT '创建者ID',
  PRIMARY KEY (`person_id`),
  UNIQUE KEY `uk_user_username` (`user_name`),
  CONSTRAINT `fk_user_person` FOREIGN KEY (`person_id`) REFERENCES `person` (`person_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 4. Menu 菜单表
CREATE TABLE IF NOT EXISTS `menu` (
  `id` INT NOT NULL COMMENT '菜单ID，主键',
  `pid` INT DEFAULT NULL COMMENT '父菜单ID',
  `user_type_ids` VARCHAR(255) DEFAULT NULL COMMENT '可见用户类型ID列表',
  `name` VARCHAR(40) DEFAULT NULL COMMENT '菜单名（FXML文件名）',
  `title` VARCHAR(40) DEFAULT NULL COMMENT '菜单标题',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_menu_parent` FOREIGN KEY (`pid`) REFERENCES `menu` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 5. Dictionary 数据字典表
CREATE TABLE IF NOT EXISTS `dictionary` (
  `id` INT NOT NULL AUTO_INCREMENT COMMENT '字典ID，主键',
  `pid` INT DEFAULT NULL COMMENT '父字典ID',
  `value` VARCHAR(40) DEFAULT NULL COMMENT '字典值',
  `label` VARCHAR(40) DEFAULT NULL COMMENT '字典标签',
  PRIMARY KEY (`id`),
  CONSTRAINT `fk_dictionary_parent` FOREIGN KEY (`pid`) REFERENCES `dictionary` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

-- ============================================
-- 重要说明：
-- 1. 当前项目使用 JPA ddl-auto: update 模式，表结构由JPA自动生成
-- 2. 此SQL文件仅作为文档参考，如需手动导入请先DROP TABLE
-- 3. Person表的 photo 字段在JPA中定义为 " photo"（带空格），已同步
-- 4. 所有外键约束已由JPA自动创建，无需手动添加
-- ============================================

-- 如需重新创建表，请先执行：
-- DROP TABLE IF EXISTS dictionary;
-- DROP TABLE IF EXISTS menu;
-- DROP TABLE IF EXISTS user;
-- DROP TABLE IF EXISTS student;
-- DROP TABLE IF EXISTS person;
-- 然后再执行下面的CREATE TABLE语句
-- ============================================
DESC person;
DESC student;
DESC `user`;
DESC menu;
DESC dictionary;
