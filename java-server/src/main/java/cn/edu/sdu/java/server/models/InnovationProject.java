package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "innovation_project")
public class InnovationProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer projectId;

    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String category;

    // 负责人（关联 person 表）
    private Integer leaderId;

    @Column(columnDefinition = "TEXT")
    private String teamMembers;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer status; // 0=进行中，1=已完成，2=已终止

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "create_time")
    private Date createTime;
}
