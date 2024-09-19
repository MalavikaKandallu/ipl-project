package com.indium;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.Main;
import org.example.controller.QueryController;
import org.example.entity.Match;
import org.example.entity.Player;
import org.example.service.QueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QueryController.class)
@ContextConfiguration(classes = {Main.class})
public class QueryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryService queryService;

    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;  // Ensure this is mocked

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    public void testGetMatchesByPlayerName() throws Exception {
        String playerName = "WP Saha";
        Match match = new Match();
        match.setId(1L);
        match.setEventName("Indian Premier League");
        match.setMatchType("T20");
        match.setCity("Bangalore");
        match.setDate(LocalDate.of(2008, 4, 18));
        match.setVenue("M Chinnaswamy Stadium");
        match.setBallsPerOver(6);
        match.setSeason("2007/08");

        List<Match> matches = Arrays.asList(match);

        when(queryService.getMatchesByPlayerName(playerName)).thenReturn(matches);

        mockMvc.perform(get("/api/matches/player")
                        .param("playerName", playerName))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(matches)));

        verify(queryService, times(1)).getMatchesByPlayerName(playerName);
    }

    @Test
    public void testGetCumulativeScoreByPlayerName() throws Exception {
        String playerName = "Virat Kohli";
        Integer cumulativeScore = 120;

        when(queryService.getCumulativeScoreByPlayerName(playerName)).thenReturn(cumulativeScore);

        mockMvc.perform(get("/api/matches/cumulativeScore/{playerName}", playerName))
                .andExpect(status().isOk())
                .andExpect(content().string(cumulativeScore.toString()));

        verify(queryService, times(1)).getCumulativeScoreByPlayerName(playerName);
    }

    @Test
    public void testGetMatchesByDate() throws Exception {
        LocalDate date = LocalDate.now();
        Match match = new Match();
        match.setId(1L);
        match.setDate(date);
        List<Match> matches = Arrays.asList(match);

        when(queryService.getMatchesByDate(date)).thenReturn(matches);


        mockMvc.perform(get("/api/matches/date/{date}", date.toString()))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(matches)));


        verify(queryService, times(1)).getMatchesByDate(date);
    }

    @Test
    public void testGetTopBatsmen() throws Exception {
        Player player = new Player();
        player.setName("Player 1");
        List<Player> players = Arrays.asList(player);

        Page<Player> playerPage = new PageImpl<>(players, PageRequest.of(0, 10), players.size());

        when(queryService.getTopBatsmen(any())).thenReturn(playerPage);

        mockMvc.perform(get("/api/matches/batsmen/top")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())  // Ensure content is an array
                .andExpect(jsonPath("$.content[0].name").value("Player 1"))  // Check individual player's name
                .andExpect(jsonPath("$.content[0].id").isEmpty());  // Check the ID is null
        verify(queryService, times(1)).getTopBatsmen(any());
    }

    @Test
    public void testGetMatchesByPlayerNameNotFound() throws Exception {
        String playerName = "Non-existent Player";

        when(queryService.getMatchesByPlayerName(playerName)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/matches/player")
                        .param("playerName", playerName))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(queryService, times(1)).getMatchesByPlayerName(playerName);
    }
}
