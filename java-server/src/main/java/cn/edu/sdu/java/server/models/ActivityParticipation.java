package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "activity_participation",
       indexes = {
           @Index(name = "idx_participation_activity", columnList = "activity_id"),
           @Index(name = "idx_participation_person", columnList = "person_id")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_participation_activity_person", columnNames = {"activity_id", "person_id"})
       })
public class ActivityParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer participationId;

    @ManyToOne
    @JoinColumn(name = "activity_id")
    private Activity activity;

    private Integer personId;

    @Column(name = "join_time")
    private Date joinTime;

    @Size(max = 200)
    private String remark;
}
