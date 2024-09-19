package org.example.repository;

import org.example.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    // Query to calculate cumulative score by player name
    @Query("SELECT SUM(d.runsBatter) FROM Delivery d " +
            "JOIN d.over o " +
            "JOIN o.inning i " +
            "JOIN MatchPlayer mp ON mp.match.id = i.match.id " +
            "JOIN Player p ON mp.player.id = p.id " +
            "WHERE p.name = :playerName")
    Long findCumulativeScoreByPlayerName(@Param("playerName") String playerName);

}
