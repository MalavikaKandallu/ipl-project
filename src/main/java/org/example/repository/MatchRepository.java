package org.example.repository;

import org.example.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m " +
            "JOIN MatchPlayer mp ON m.id = mp.match.id " +
            "JOIN Player p ON mp.player.id = p.id " +
            "WHERE p.name = :playerName")
    List<Match> findMatchesByPlayerName(@Param("playerName") String playerName);


    // Query to get all matches by a specific date
    @Query("SELECT m FROM Match m WHERE m.date = :date")
    List<Match> findMatchesByDate(@Param("date") LocalDate date);

    // Query to get scores for innings in a match
    @Query("SELECT i.totalRuns FROM Inning i WHERE i.match.id = :matchId")
    List<Integer> findScoresByMatchId(@Param("matchId") Long matchId);



}
