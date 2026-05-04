package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "activity")
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer activityId;

    @Size(max = 100)
    private String name;

    @Size(max = 100)
    private String location;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "end_time")
    private Date endTime;

    @Column(columnDefinition = "TEXT")
    private String description;

    // 发布者（关联 person 表）
    private Integer publisherId;

    // 状态：0=未开始，1=进行中，2=已结束，3=已取消
    private Integer status;

    @Column(name = "create_time")
    private Date createTime;
}
