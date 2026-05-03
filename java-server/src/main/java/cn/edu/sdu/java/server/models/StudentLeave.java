package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(	name = "student_leave",
        uniqueConstraints = {
        })
public class StudentLeave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentLeaveId;

    @ManyToOne
    @JoinColumn(name="student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name="teacher_id")
    private Teacher teacher;

    @Size(max=50)
    private String leaveDate;
    @Size(max=100)
    private String reason;
    private Integer state;
    private Date applyTime;
    @Size(max=100)
    private String teacherComment;
    private Date teacherTime;
    @Size(max=100)
    private String adminComment;
    private Date adminTime;

    // 新增字段：请假类型和请假时长
    @Size(max = 20)
    private String leaveType;      // 请假类型：病假/事假
    private Integer leaveDuration; // 请假时长 (小时)
}