package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * PunishmentRecord 处分记录实体类
 * 权限控制：仅管理员可录入，所有人可查（学生仅能查自己）
 */
@Getter
@Setter
@Entity
@Table(name = "punishment_record",
       indexes = {
           @Index(name = "idx_punishment_person", columnList = "person_id"),
           @Index(name = "idx_punishment_status", columnList = "status"),
           @Index(name = "idx_punishment_level", columnList = "level"),
           @Index(name = "idx_punishment_date", columnList = "behavior_date")
       })
public class PunishmentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer punishmentId;

    // 违纪学生 ID（关联 person 表）
    private Integer personId;

    @Size(max = 200)
    private String behavior; // 违纪行为描述

    @Size(max = 100)
    private String level; // 处分级别（警告/严重警告/记过/留校察看/开除学籍）

    @Column(columnDefinition = "TEXT")
    private String description; // 违纪详情

    private Date behaviorDate; // 违纪日期

    @Size(max = 200)
    private String location; // 违纪地点

    // 记录人 ID（关联 person 表，记录是谁录入的）
    private Integer recorderId;

    // 状态：0=待处理，1=已处理
    private Integer status;

    @Column(name = "create_time")
    private Date createTime;
}
