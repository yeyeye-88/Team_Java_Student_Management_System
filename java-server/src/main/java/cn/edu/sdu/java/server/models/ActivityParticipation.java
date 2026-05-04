package cn.edu.sdu.java.server.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "activity_participation")
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
