package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "overs")
public class Over {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "inning_id", nullable = false)
//    private Inning inning;

    @Column(name = "over_number", nullable = false)
    private Integer overNumber;
    @ManyToOne(cascade = CascadeType.PERSIST) // or CascadeType.ALL
    @JoinColumn(name = "inning_id")
    private Inning inning;

    // Additional fields or methods if needed
}

