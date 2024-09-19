package org.example.repository;

import org.example.entity.MatchTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchTeamRepository extends JpaRepository<MatchTeam, Long> {
    // Custom queries can be added here if needed
}
