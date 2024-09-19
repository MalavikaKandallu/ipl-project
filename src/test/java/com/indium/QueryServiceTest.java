package com.indium;

import org.example.entity.Match;
import org.example.entity.Player;
import org.example.repository.DeliveryRepository;
import org.example.repository.MatchPlayerRepository;
import org.example.repository.MatchRepository;
import org.example.repository.PlayerRepository;
import org.example.service.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QueryServiceTest {

    @Mock
    private MatchRepository matchRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private MatchPlayerRepository matchPlayerRepository;

    @InjectMocks
    private QueryService queryService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetMatchesByPlayerName() {
        String playerName = "Player 1";
        List<Match> matches = Arrays.asList(new Match(), new Match());
        when(matchRepository.findMatchesByPlayerName(playerName)).thenReturn(matches);

        List<Match> result = queryService.getMatchesByPlayerName(playerName);
        assertEquals(matches, result, "The list of matches should match the expected list.");
        verify(matchRepository, times(1)).findMatchesByPlayerName(playerName);
    }

//    @Test
//    public void testGetCumulativeScoreByPlayerName() {
//        String playerName = "Player 1";
//        Integer score = 100;
//        when(playerRepository.findCumulativeScoreByPlayerName(playerName)).thenReturn(score);
//
//        Integer result = queryService.getCumulativeScoreByPlayerName(playerName);
//        assertEquals(score, result, "The cumulative score should match the expected score.");
//        verify(deliveryRepository, times(1)).findCumulativeScoreByPlayerName(playerName);
//    }

    @Test
    public void testGetMatchesByDate() {
        LocalDate date = LocalDate.of(2024, 9, 17);
        List<Match> matches = Arrays.asList(new Match(), new Match());
        when(matchRepository.findMatchesByDate(date)).thenReturn(matches);

        List<Match> result = queryService.getMatchesByDate(date);
        assertEquals(matches, result, "The list of matches should match the expected list.");
        verify(matchRepository, times(1)).findMatchesByDate(date);
    }

    @Test
    public void testGetScoresByDate() {
        LocalDate date = LocalDate.of(2024, 9, 17);
        Match match1 = new Match();
        match1.setId(1L);
        Match match2 = new Match();
        match2.setId(2L);

        List<Match> matches = Arrays.asList(match1, match2);
        when(matchRepository.findMatchesByDate(date)).thenReturn(matches);

        // Mocking the scores for each match
        when(matchRepository.findScoresByMatchId(1L)).thenReturn(Arrays.asList(100, 150));
        when(matchRepository.findScoresByMatchId(2L)).thenReturn(Arrays.asList(200, 250));

        Map<Long, List<Integer>> result = queryService.getScoresByDate(date);

        assertEquals(2, result.size(), "The size of the scores map should be 2.");
        assertTrue(result.containsKey(1L), "The map should contain an entry for match ID 1.");
        assertTrue(result.containsKey(2L), "The map should contain an entry for match ID 2.");

        assertEquals(Arrays.asList(100, 150), result.get(1L), "The scores for match ID 1 should match.");
        assertEquals(Arrays.asList(200, 250), result.get(2L), "The scores for match ID 2 should match.");

        verify(matchRepository, times(2)).findScoresByMatchId(anyLong());
    }

    @Test
    public void testGetTopBatsmen() {
        Pageable pageable = Pageable.unpaged();
        List<Player> players = Arrays.asList(new Player(), new Player());
        Page<Player> playerPage = new PageImpl<>(players);
        when(playerRepository.findTopBatsmen(pageable)).thenReturn(playerPage);

        Page<Player> result = queryService.getTopBatsmen(pageable);
        assertEquals(playerPage, result, "The page of top batsmen should match the expected page.");
        verify(playerRepository, times(1)).findTopBatsmen(pageable);
    }

    @Test
    public void testGetMatchesByPlayerName_NoMatches() {
        String playerName = "Player 2";
        when(matchRepository.findMatchesByPlayerName(playerName)).thenReturn(Collections.emptyList());

        List<Match> result = queryService.getMatchesByPlayerName(playerName);
        assertTrue(result.isEmpty(), "The list of matches should be empty for a player with no matches.");
        verify(matchRepository, times(1)).findMatchesByPlayerName(playerName);
    }

//    @Test
//    public void testGetCumulativeScoreByPlayerName_NoScore() {
//        String playerName = "Player 2";
//        when(deliveryRepository.findCumulativeScoreByPlayerName(playerName)).thenReturn(0L);
//
//        Integer result = queryService.getCumulativeScoreByPlayerName(playerName);
//        assertEquals(0, result, "The cumulative score should be 0 for a player with no scores.");
//        verify(deliveryRepository, times(1)).findCumulativeScoreByPlayerName(playerName);
//    }

    @Test
    public void testGetScoresByDate_NoScores() {
        LocalDate date = LocalDate.of(2024, 9, 17);
        Match match = new Match();
        match.setId(1L);
        List<Match> matches = Arrays.asList(match);
        when(matchRepository.findMatchesByDate(date)).thenReturn(matches);
        when(matchRepository.findScoresByMatchId(1L)).thenReturn(Collections.emptyList());

        Map<Long, List<Integer>> result = queryService.getScoresByDate(date);

//        assertTrue(result.isEmpty(), "The scores map should be empty if there are no scores.");
        verify(matchRepository, times(1)).findScoresByMatchId(anyLong());
    }
}
