package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "target")
public class Target {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inning_id", nullable = false)
    private Inning inning;

    @Column(name = "runs")
    private Integer runs;

    @Column(name = "overs")
    private Integer overs;

    // Additional methods if needed
}
