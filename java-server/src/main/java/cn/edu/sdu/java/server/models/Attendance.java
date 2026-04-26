package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Attendance 考勤表实体类
 * 保存学生考勤的基本信息
 */
@Getter
@Setter
@Entity
@Table(name = "attendance")
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attendanceId;

    @ManyToOne
    @JoinColumn(name = "person_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    // 考勤时间
    private Date attendanceTime;

    // 状态：0-未到，1-迟到，2-早退，3-正常，4-请假
    private Integer state;

    // 备注
    private String remark;
}
