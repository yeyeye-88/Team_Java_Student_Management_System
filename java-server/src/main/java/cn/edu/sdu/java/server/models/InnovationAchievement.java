package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "innovation_achievement",
       indexes = {
           @Index(name = "idx_achievement_project", columnList = "project_id"),
           @Index(name = "idx_achievement_person", columnList = "person_id"),
           @Index(name = "idx_achievement_type", columnList = "achievement_type")
       })
public class InnovationAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer achievementId;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private InnovationProject project;

    private Integer personId;

    @Size(max = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Size(max = 50)
    private String achievementType;

    @Column(name = "submit_time")
    private Date submitTime;
}
