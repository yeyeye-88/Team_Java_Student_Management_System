package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * HonorRecord 荣誉记录实体类
 * 权限控制：管理员/教师可录入，所有人可查（学生仅能查自己）
 */
@Getter
@Setter
@Entity
@Table(name = "honor_record",
       indexes = {
           @Index(name = "idx_honor_person", columnList = "person_id"),
           @Index(name = "idx_honor_level", columnList = "level"),
           @Index(name = "idx_honor_status", columnList = "status"),
           @Index(name = "idx_honor_date", columnList = "award_date")
       })
public class HonorRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer honorId;

    // 获奖学生 ID（关联 person 表）
    private Integer personId;

    @Size(max = 200)
    private String title; // 奖项名称

    @Size(max = 100)
    private String level; // 级别（国家级/省级/校级）

    @Size(max = 100)
    private String category; // 类别（学术/竞赛/体育/文艺等）

    @Column(columnDefinition = "TEXT")
    private String description; // 详细描述

    private Date awardDate; // 获奖日期

    @Size(max = 200)
    private String issuer; // 颁发机构

    // 录入人 ID（关联 person 表，记录是谁录入的）
    private Integer recorderId;

    // 状态：0=待审核，1=已通过，2=已驳回
    private Integer status;

    @Column(name = "create_time")
    private Date createTime;

    // 证书扫描件路径（用于上传证书图片/PDF）
    @Size(max = 500)
    private String certificateUrl;
}
