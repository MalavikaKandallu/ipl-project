package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "inning")
public class Inning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "total_runs")
    private Integer totalRuns;

    @Column(name = "total_overs")
    private Integer totalOvers;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    // Additional methods if needed
}

