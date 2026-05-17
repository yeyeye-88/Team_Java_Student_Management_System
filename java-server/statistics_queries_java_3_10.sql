-- ============================================
-- 统计查询 SQL
-- 团队编号: java_3_10
-- 说明：第七周任务 - 统计功能查询
-- ============================================

-- ============================================
-- 统计1：班级学生数统计
-- ============================================

-- 按班级统计学生数（GROUP BY + COUNT）
SELECT 
    s.class_name AS className,
    COUNT(s.person_id) AS studentCount
FROM student s
GROUP BY s.class_name
ORDER BY studentCount DESC;

-- ============================================
-- 统计2：课程平均分统计
-- ============================================

-- 按课程统计平均分（GROUP BY + AVG）
SELECT 
    c.course_id AS courseId,
    c.name AS courseName,
    AVG(sc.mark) AS avgScore
FROM score sc
JOIN course c ON sc.course_id = c.course_id
GROUP BY c.course_id, c.name
ORDER BY avgScore DESC;

-- ============================================
-- 统计3：课程考勤率统计
-- ============================================

-- 按课程统计考勤率（state=1 表示出勤）
SELECT 
    c.course_id AS courseId,
    c.name AS courseName,
    SUM(CASE WHEN a.state = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(a.attendance_id) AS attendanceRate
FROM attendance a
JOIN course c ON a.course_id = c.course_id
GROUP BY c.course_id, c.name
ORDER BY attendanceRate DESC;

-- ============================================
-- 统计4：请假通过率统计
-- ============================================

-- 统计请假通过率（state=1 表示通过）
SELECT 
    SUM(CASE WHEN sl.state = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(sl.student_leave_id) AS passRate
FROM student_leave sl;

-- 按学生统计请假次数
SELECT 
    p.person_id AS personId,
    p.name AS studentName,
    p.num AS studentNum,
    COUNT(sl.student_leave_id) AS leaveCount
FROM student_leave sl
JOIN student s ON sl.student_id = s.person_id
JOIN person p ON s.person_id = p.person_id
GROUP BY p.person_id, p.name, p.num
ORDER BY leaveCount DESC;

-- ============================================
-- 综合统计查询
-- ============================================

-- 仪表盘汇总统计
SELECT 
    (SELECT COUNT(*) FROM student) AS totalStudents,
    (SELECT COUNT(*) FROM course) AS totalCourses,
    (SELECT COUNT(*) FROM attendance) AS totalAttendance,
    (SELECT COUNT(*) FROM student_leave WHERE state = 0) AS pendingLeaves,
    (SELECT SUM(CASE WHEN state = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*) FROM student_leave) AS leavePassRate;

-- ============================================
-- 使用说明：
-- ============================================
-- 1. 这些 SQL 可用于数据验证和报表生成
-- 2. 与 JPA Repository 中的聚合查询方法对应
-- 3. 可用于手动验证统计数据的准确性
