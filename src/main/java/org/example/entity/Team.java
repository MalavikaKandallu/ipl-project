package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "team")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "team")
    private Set<MatchTeam> matchTeams = new HashSet<>();

    @OneToMany(mappedBy = "team")
    private Set<Inning> innings = new HashSet<>();
}
