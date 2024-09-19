package com.indium;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.MatchController;
import org.example.entity.Match;
import org.example.entity.Player;
import org.example.repository.MatchRepository;
import org.example.service.MatchService;
import org.example.service.QueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(MatchController.class)
@ContextConfiguration(classes = {MatchControllerTest.TestConfig.class})  // Specify configuration
public class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;
@MockBean
private MatchRepository matchRepository;
    @MockBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @MockBean
    private MatchService matchService;

    @MockBean
    private QueryService queryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Configuration
    @ComponentScan(basePackages = {"org.example.controller", "org.example.service"})  // Adjust package as necessary
    static class TestConfig {
        // You can define beans here if needed
    }

    @Test
    public void testUploadJsonData_Success() throws Exception {
        String jsonContent = "{\"match\": \"data\"}";

        // Mock the behavior of matchService and queryService
        doNothing().when(matchService).saveMatchData(jsonContent);
        doNothing().when(queryService).clearAllCaches();

        mockMvc.perform(post("/api/matches/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isOk())
                .andExpect(content().string("JSON data uploaded and processed successfully"));

        verify(matchService, times(1)).saveMatchData(jsonContent);
        verify(queryService, times(1)).clearAllCaches();
    }


    @Test
    public void testUploadJsonData_IOException() throws Exception {
        String jsonContent = "{\"match\": \"data\"}";

        // Mock the behavior to throw IOException
        doThrow(new IOException("Test IO exception")).when(matchService).saveMatchData(jsonContent);

        mockMvc.perform(post("/api/matches/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to process the JSON data: Test IO exception"));

        verify(matchService, times(1)).saveMatchData(jsonContent);
        verify(queryService, never()).clearAllCaches();
    }

    @Test
    public void testUploadJsonData_UnexpectedException() throws Exception {
        String jsonContent = "{\"match\": \"data\"}";

        // Mock the behavior to throw a generic exception
        doThrow(new RuntimeException("Test unexpected exception")).when(matchService).saveMatchData(jsonContent);

        mockMvc.perform(post("/api/matches/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("An unexpected error occurred: Test unexpected exception"));

        verify(matchService, times(1)).saveMatchData(jsonContent);
        verify(queryService, never()).clearAllCaches();
    }

    @Test
    public void testGetMatchesByPlayerName() throws Exception {
        String playerName = "Virat Kohli";
        List<Match> matches = Arrays.asList(new Match(), new Match());

        when(queryService.getMatchesByPlayerName(playerName)).thenReturn(matches);

        mockMvc.perform(get("/api/matches/player")
                        .param("playerName", playerName)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(matches)));

        verify(kafkaTemplate, times(1)).send(eq("Ipl-topic"), anyString());
    }

    @Test
    public void testGetCumulativeScoreByPlayerName() throws Exception {
        String playerName = "Virat Kohli";
        Integer cumulativeScore = 1000;

        when(queryService.getCumulativeScoreByPlayerName(playerName)).thenReturn(cumulativeScore);

        mockMvc.perform(get("/api/matches/cumulativeScore/{playerName}", playerName)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(cumulativeScore.toString()));

        verify(kafkaTemplate, times(1)).send(eq("Ipl-topic"), anyString());
    }

    @Test
    public void testGetMatchesByDate() throws Exception {
        LocalDate date = LocalDate.of(2024, 9, 17);
        List<Match> matches = Arrays.asList(new Match(), new Match());

        when(queryService.getMatchesByDate(date)).thenReturn(matches);

        mockMvc.perform(get("/api/matches/date/{date}", date)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(matches)));

        verify(kafkaTemplate, times(1)).send(eq("Ipl-topic"), anyString());
    }

    @Test
    public void testGetTopBatsmen() throws Exception {
        int page = 0;
        int size = 10;
        List<Player> players = Arrays.asList(new Player(), new Player());

        // Mock the service to return a page with the players
        when(queryService.getTopBatsmen(PageRequest.of(page, size)))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(players, PageRequest.of(page, size), players.size()));

        // Perform the request and verify the response
        mockMvc.perform(get("/api/matches/batsmen/top")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(players.get(0).getId()))
                .andExpect(jsonPath("$.content[0].name").value(players.get(0).getName()))
                .andExpect(jsonPath("$.content[0].totalScore").value(players.get(0).getTotalScore()))
                .andExpect(jsonPath("$.content[1].id").value(players.get(1).getId()))
                .andExpect(jsonPath("$.content[1].name").value(players.get(1).getName()))
                .andExpect(jsonPath("$.content[1].totalScore").value(players.get(1).getTotalScore()))
                .andExpect(jsonPath("$.totalElements").value(players.size()))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(size))
                .andExpect(jsonPath("$.number").value(page));

        verify(kafkaTemplate, times(1)).send(eq("Ipl-topic"), anyString());
    }

    @Test
    public void testGetMatchesByPlayerName_NoMatchesFound() throws Exception {
        String playerName = "Nonexistent Player";

        when(queryService.getMatchesByPlayerName(playerName)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/matches/player")
                        .param("playerName", playerName)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));

        verify(kafkaTemplate, times(1)).send(eq("Ipl-topic"), anyString());
    }
}
