package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * CourseSchedule 课表安排实体类
 */
@Getter
@Setter
@Entity
@Table(name = "course_schedule",
        indexes = {
                @Index(name = "idx_schedule_class", columnList = "class_name"),
                @Index(name = "idx_schedule_semester", columnList = "semester")
        })
public class CourseSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer scheduleId;

    // 关联课程
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // 任课教师
    @ManyToOne
    @JoinColumn(name = "teacher_id", referencedColumnName = "person_id", nullable = false)
    private Teacher teacher;

    // 上课班级名称
    @NotBlank
    @Column(name = "class_name")
    private String className;

    // 学期（如：2026 春季）
    @NotBlank
    private String semester;

    // 星期几（1-7，1 表示周一）
    @NotNull
    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    // 开始节次（如：1 表示第 1 节）
    @NotNull
    @Column(name = "start_period")
    private Integer startPeriod;

    // 结束节次（如：2 表示第 2 节）
    @NotNull
    @Column(name = "end_period")
    private Integer endPeriod;

    // 上课地点（如：教学楼 A-301）
    @NotBlank
    private String location;

    // 状态（1-正常/2-停用）
    private Integer status;
}
