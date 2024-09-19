package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "powerplay")
public class Powerplay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "inning_id", nullable = false)
    private Inning inning;

    @Column(name = "start_over")
    private Integer startOver;

    @Column(name = "end_over")
    private Integer endOver;

    // Additional methods if needed
}

