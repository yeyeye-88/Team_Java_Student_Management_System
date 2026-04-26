-- ============================================
-- 外键关联测试 SQL
-- 团队编号: java_3_10
-- ============================================

-- ============================================
-- 重要说明：
-- 1. 当前项目使用 JPA ddl-auto: update，外键已由JPA自动创建
-- 2. 此文件用于测试外键约束是否生效
-- 3. 预期结果：所有带注释“应该失败”的INSERT都会报错
-- 4. 如果INSERT成功，说明外键约束未生效，需要检查JPA配置
-- ============================================

-- ============================================
-- 测试1：Student表外键关联（应该失败）
-- ============================================

-- 尝试插入一个不存在的person_id
-- 预期结果：报错，违反外键约束
INSERT INTO `student` (`person_id`, `major`, `class_name`) VALUES 
(999, '测试专业', '测试班级');

-- ============================================
-- 测试2：Menu表外键关联（应该失败）
-- ============================================

-- 尝试插入一个不存在的pid
-- 预期结果：报错，违反外键约束
INSERT INTO `menu` (`id`, `pid`, `name`, `title`) VALUES 
(999, 888, 'TestMenu', '测试菜单');

-- ============================================
-- 测试3：Dictionary表外键关联（应该失败）
-- ============================================

-- 尝试插入一个不存在的pid
-- 预期结果：报错，违反外键约束
INSERT INTO `dictionary` (`id`, `pid`, `value`, `label`) VALUES 
(999, 888, 'test', '测试项');

-- ============================================
-- 测试4：User表外键关联（应该失败）
-- ============================================

-- 尝试插入一个不存在的person_id
-- 预期结果：报错，违反外键约束
INSERT INTO `user` (`person_id`, `user_name`, `password`) VALUES 
(999, 'testuser', 'testpassword');

-- ============================================
-- 测试5：级联删除测试
-- ============================================

-- 删除一个person记录，验证关联的student和user是否被级联删除
-- 先插入测试数据
INSERT INTO `person` (`person_id`, `num`, `name`, `type`) VALUES 
(200, 'test200', '测试删除', '1');

INSERT INTO `student` (`person_id`, `major`, `class_name`) VALUES 
(200, '测试专业', '测试班级');

INSERT INTO `user` (`person_id`, `user_name`, `password`) VALUES 
(200, 'test200', 'testpassword');

-- 执行删除（应该级联删除student和user记录）
DELETE FROM `person` WHERE person_id = 200;

-- 验证删除结果（应该查询不到）
SELECT * FROM `student` WHERE person_id = 200;
SELECT * FROM `user` WHERE person_id = 200;

-- ============================================
-- 测试6：字段约束测试
-- ============================================

-- 测试person.num唯一约束（应该失败）
INSERT INTO `person` (`num`, `name`, `type`) VALUES 
('admin', '重复测试', '1');

-- 测试user.user_name唯一约束（应该失败）
INSERT INTO `user` (`person_id`, `user_name`, `password`) VALUES 
(300, 'admin', 'testpassword');

-- ============================================
-- 修正后的最终版SQL说明
-- ============================================

-- 如果上述测试都符合预期（外键约束生效、级联删除正常、唯一约束生效），
-- 说明表结构设计正确，可以用于生产环境。

-- 最终确认5张表的外键关系：
-- 1. student.person_id -> person.person_id (级联删除)
-- 2. user.person_id -> person.person_id (级联删除)
-- 3. menu.pid -> menu.id (级联删除)
-- 4. dictionary.pid -> dictionary.id (级联删除)
