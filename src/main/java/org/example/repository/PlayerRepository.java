package org.example.repository;

import org.example.entity.Player;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    // Query to find top batsmen with pagination
    @Query("SELECT p FROM Player p ORDER BY p.totalScore DESC")
    Page<Player> findTopBatsmen(Pageable pageable);

    // Find player by name
    Player findByName(String playerName);

    // Correct query to find player by name
    @Query("SELECT p FROM Player p WHERE p.name = :name")
    Player findPlayerByName(@Param("name") String name);

}
