-- ============================================
-- 数据合规性检查 SQL
-- 团队编号: java_3_10
-- ============================================

-- 1. 检查3角色账号是否存在
SELECT '角色账号检查' as check_type;
SELECT u.user_name, p.type, CASE p.type WHEN '0' THEN '管理员' WHEN '1' THEN '学生' WHEN '2' THEN '教师' END as role_name
FROM user u JOIN person p ON u.person_id = p.person_id
WHERE u.user_name IN ('admin', 'stu01', 'tea01');

-- 2. 检查学生数量是否达到10条
SELECT '学生数量检查' as check_type;
SELECT COUNT(*) as student_count FROM student;

-- 3. 检查菜单是否有基础菜单
SELECT '菜单数据检查' as check_type;
SELECT COUNT(*) as menu_count FROM menu;
SELECT id, pid, title FROM menu ORDER BY id LIMIT 10;

-- 4. 检查字典是否包含性别字典
SELECT '字典数据检查' as check_type;
SELECT d.id, d.pid, d.value, d.label 
FROM dictionary d 
WHERE d.pid IS NULL OR d.pid = (SELECT id FROM dictionary WHERE value = 'gender');

-- 5. 检查外键关联是否正常
SELECT '外键关联检查' as check_type;
SELECT s.person_id, p.num, s.major, s.class_name 
FROM student s 
JOIN person p ON s.person_id = p.person_id 
LIMIT 5;


-- ============================================
-- 重要说明：
-- 1. 执行前请确认表已通过JPA自动生成（ddl-auto: update）
-- 2. 如果表中已有数据，INSERT可能会报主键冲突
-- 3. 建议先备份数据库或清空表数据再导入
-- 4. 需要先确保 user_type 表中有对应的角色数据
-- ============================================

-- 如需清空现有数据，可执行：
-- DELETE FROM dictionary;
-- DELETE FROM menu;
-- DELETE FROM user;
-- DELETE FROM student;
-- DELETE FROM person;
-- ============================================

-- ============================================
-- 1. 添加3个角色测试账号
-- ============================================

-- 1.1 管理员账户 (admin/123456)
INSERT INTO `person` (`person_id`, `num`, `name`, `type`, `dept`) VALUES 
(1, 'admin', '系统管理员', '0', '信息中心');

INSERT INTO `user` (`person_id`, `user_name`, `password`, `login_count`) VALUES 
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0);

-- 1.2 学生账户 (stu01/123456)
INSERT INTO `person` (`person_id`, `num`, `name`, `type`, `dept`) VALUES 
(2, 'stu01', '张三', '1', '计算机学院');

INSERT INTO `user` (`person_id`, `user_name`, `password`, `login_count`) VALUES 
(2, 'stu01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0);

INSERT INTO `student` (`person_id`, `major`, `class_name`) VALUES 
(2, '计算机科学与技术', '计算机2023-1班');

-- 1.3 教师账户 (tea01/123456)
INSERT INTO `person` (`person_id`, `num`, `name`, `type`, `dept`) VALUES 
(3, 'tea01', '李老师', '2', '计算机学院');

INSERT INTO `user` (`person_id`, `user_name`, `password`, `login_count`) VALUES 
(3, 'tea01', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 0);

-- ============================================
-- 2. 添加10条测试学生信息
-- ============================================

INSERT INTO `person` (`person_id`, `num`, `name`, `type`, `dept`, `gender`, `birthday`, `email`, `phone`) VALUES 
(10, '2023001', '张三', '1', '计算机学院', '1', '2005-01-15', 'zhangsan@example.com', '13800138001'),
(11, '2023002', '李四', '1', '计算机学院', '2', '2005-02-20', 'lisi@example.com', '13800138002'),
(12, '2023003', '王五', '1', '计算机学院', '1', '2005-03-10', 'wangwu@example.com', '13800138003'),
(13, '2023004', '赵六', '1', '计算机学院', '2', '2005-04-25', 'zhaoliu@example.com', '13800138004'),
(14, '2023005', '孙七', '1', '计算机学院', '1', '2005-05-30', 'sunqi@example.com', '13800138005'),
(15, '2023006', '周八', '1', '计算机学院', '2', '2005-06-18', 'zhouba@example.com', '13800138006'),
(16, '2023007', '吴九', '1', '计算机学院', '1', '2005-07-22', 'wujiu@example.com', '13800138007'),
(17, '2023008', '郑十', '1', '计算机学院', '2', '2005-08-14', 'zhengshi@example.com', '13800138008'),
(18, '2023009', '刘一', '1', '计算机学院', '1', '2005-09-05', 'liuyi@example.com', '13800138009'),
(19, '2023010', '陈二', '1', '计算机学院', '2', '2005-10-12', 'chener@example.com', '13800138010');

INSERT INTO `student` (`person_id`, `major`, `class_name`) VALUES 
(10, '计算机科学与技术', '计算机2023-1班'),
(11, '计算机科学与技术', '计算机2023-1班'),
(12, '软件工程', '软件工程2023-1班'),
(13, '软件工程', '软件工程2023-1班'),
(14, '数据科学与大数据技术', '大数据2023-1班'),
(15, '数据科学与大数据技术', '大数据2023-1班'),
(16, '人工智能', '人工智能2023-1班'),
(17, '人工智能', '人工智能2023-1班'),
(18, '信息安全', '信息安全2023-1班'),
(19, '信息安全', '信息安全2023-1班');

-- ============================================
-- 3. 添加基础菜单
-- ============================================

-- 一级菜单
INSERT INTO `menu` (`id`, `pid`, `name`, `title`, `user_type_ids`) VALUES 
(1, NULL, 'Dashboard', '系统首页', '0,1,2'),
(2, NULL, 'Student', '学生管理', '0,2'),
(3, NULL, 'Course', '课程管理', '0,1,2'),
(4, NULL, 'Score', '成绩管理', '0,1,2'),
(5, NULL, 'Statistics', '统计分析', '0'),
(6, NULL, 'System', '系统设置', '0');

-- 二级菜单（学生管理）
INSERT INTO `menu` (`id`, `pid`, `name`, `title`, `user_type_ids`) VALUES 
(21, 2, 'StudentInfo', '基本信息', '0,2'),
(22, 2, 'StudentLeave', '请假管理', '0,1,2');

-- 二级菜单（课程管理）
INSERT INTO `menu` (`id`, `pid`, `name`, `title`, `user_type_ids`) VALUES 
(31, 3, 'CourseList', '课程列表', '0,1,2'),
(32, 3, 'CourseSelect', '选课管理', '0,1,2');

-- 二级菜单（系统设置）
INSERT INTO `menu` (`id`, `pid`, `name`, `title`, `user_type_ids`) VALUES 
(61, 6, 'User', '用户管理', '0'),
(62, 6, 'Menu', '菜单管理', '0'),
(63, 6, 'Dictionary', '字典管理', '0');

-- ============================================
-- 4. 添加性别字典
-- ============================================

-- 性别字典类型
INSERT INTO `dictionary` (`id`, `pid`, `value`, `label`) VALUES 
(1, NULL, 'gender', '性别');

-- 性别字典项
INSERT INTO `dictionary` (`id`, `pid`, `value`, `label`) VALUES 
(10, 1, '1', '男'),
(11, 1, '2', '女');

-- 人员类型字典
INSERT INTO `dictionary` (`id`, `pid`, `value`, `label`) VALUES 
(2, NULL, 'person_type', '人员类型'),
(20, 2, '0', '管理员'),
(21, 2, '1', '学生'),
(22, 2, '2', '教师');

-- ============================================
-- 验证数据
-- ============================================

-- 查询用户表
SELECT u.user_name, p.name, p.type, u.login_count 
FROM `user` u 
JOIN person p ON u.person_id = p.person_id;

-- 查询学生表
SELECT p.num, p.name, s.major, s.class_name 
FROM student s 
JOIN person p ON s.person_id = p.person_id;

-- 查询菜单表
SELECT m.id, m.pid, m.title, m.user_type_ids 
FROM menu m 
ORDER BY m.id;

-- 查询字典表
SELECT d.id, d.pid, d.value, d.label 
FROM dictionary d 
ORDER BY d.id;
