package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

/**
 * PunishmentResult 处理结果实体类
 * 权限控制：仅管理员可录入，关联处分记录可见
 */
@Getter
@Setter
@Entity
@Table(name = "punishment_result")
public class PunishmentResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resultId;

    // 关联处分记录 ID（外键关联 punishment_record）
    private Integer punishmentId;

    @Size(max = 100)
    private String resultType; // 处理类型（通报批评/记过处分/留校察看/开除学籍等）

    @Column(columnDefinition = "TEXT")
    private String description; // 处理详情/依据

    private Date resultDate; // 处理决定日期

    @Size(max = 200)
    private String handler; // 处理人/处理部门

    @Column(name = "create_time")
    private Date createTime;
}
