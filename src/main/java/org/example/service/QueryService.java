package org.example.service;

import org.example.entity.Match;
import org.example.entity.MatchPlayer;
import org.example.entity.Player;
import org.example.repository.DeliveryRepository;
import org.example.repository.MatchPlayerRepository;
import org.example.repository.MatchRepository;
import org.example.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryService {

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private MatchPlayerRepository matchPlayerRepository;

    // Method to get all matches by player name with caching
    @Cacheable(value = "matchesByPlayerName", key = "#playerName")
    public List<Match> getMatchesByPlayerName(String playerName) {
        return matchRepository.findMatchesByPlayerName(playerName);
    }

    // Method to get cumulative score by player name with caching
    @Cacheable(value = "cumulativeScoreByPlayerName", key = "#playerName")
    public Integer getCumulativeScoreByPlayerName(String playerName) {
        Player cumulativeScoreByPlayerName = playerRepository.findByName(playerName);
        System.out.println(cumulativeScoreByPlayerName);

        if (cumulativeScoreByPlayerName != null) {
            return cumulativeScoreByPlayerName.getTotalScore();
        } else {
            return 0; // Or any appropriate default value
        }
    }


    // Method to get all matches by date with caching
    @Cacheable(value = "matchesByDate", key = "#date")
    public List<Match> getMatchesByDate(LocalDate date) {
        return matchRepository.findMatchesByDate(date);
    }

    // Method to get scores for matches on a given date with caching
    @Cacheable(value = "scoresByDate", key = "#date")
    public Map<Long, List<Integer>> getScoresByDate(LocalDate date) {
        List<Match> matches = getMatchesByDate(date);
        Map<Long, List<Integer>> scoresMap = new HashMap<>();
        for (Match match : matches) {
            List<Integer> scores = matchRepository.findScoresByMatchId(match.getId());
            scoresMap.put(match.getId(), scores);
        }
        return scoresMap;
    }

    // Method to get paginated top batsmen with caching
    @Cacheable(value = "topBatsmen", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<Player> getTopBatsmen(Pageable pageable) {
        return playerRepository.findTopBatsmen(pageable);
    }

    // Method to clear all caches
//    @CacheEvict(value = {"matchesByPlayerName", "cumulativeScoreByPlayerName", "matchesByDate", "scoresByDate", "topBatsmen"}, allEntries = true)
//    public void clearCache() {
//        // Cache evicted
//    }

    @CacheEvict(value = "matchesByPlayerName", key = "#playerName")
    public void clearCache(String playerName) {
        // Cache entry for the specific player will be removed
    }

    @CacheEvict(value = {"matchesByPlayerName", "cumulativeScoreByPlayerName", "matchesByDate", "scoresByDate", "topBatsmen"}, allEntries = true)
    public void clearAllCaches() {
        // All cache entries for "matchesByPlayerName" will be removed
    }


}
