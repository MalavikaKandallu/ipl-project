package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.entity.Match;
import org.example.entity.Player;
import org.example.service.QueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Match Operations", description = "Endpoints for retrieving match details and player statistics")
@RestController
@RequestMapping("/api/matches")
public class QueryController {

    private static final Logger logger = LoggerFactory.getLogger(QueryController.class);

    @Autowired
    private QueryService queryService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Operation(summary = "Get all matches played by a given player",
            description = "Retrieve a list of all matches played by the given player name",
            parameters = @Parameter(name = "playerName", description = "Name of the player", example = "Virat Kohli"))
    @ApiResponse(description = "List of matches played by the given player", responseCode = "200")
    @GetMapping("/player")
        public List<Match> getMatchesByPlayerName(@RequestParam String playerName) {
        System.out.println("player" + playerName);
        List<Match> matches = queryService.getMatchesByPlayerName(playerName);
        logAndSendToKafka("Matches for player " + playerName, matches);
        return matches;
    }

    @Operation(summary = "Get cumulative score of a player",
            description = "Retrieve the cumulative score of a player across all matches",
            parameters = @Parameter(name = "playerName", description = "Name of the player", example = "Virat Kohli"))
    @ApiResponse(description = "Cumulative score of the player", responseCode = "200")
    @GetMapping("/cumulativeScore/{playerName}")
    public String getCumulativeScoreByPlayerName(@PathVariable String playerName) {

        Integer cumulativeScore = queryService.getCumulativeScoreByPlayerName(playerName);
        logAndSendToKafka("Cumulative score for player " + playerName, cumulativeScore);
        return cumulativeScore.toString();
    }

    @Operation(summary = "Get all matches by date",
            description = "Retrieve a list of all matches played on the given date",
            parameters = @Parameter(name = "date", description = "Date of the matches", example = "2024-09-17"))
    @ApiResponse(description = "List of matches played on the given date", responseCode = "200")
    @GetMapping("/date/{date}")
    public ResponseEntity<List<Match>> getMatchesByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Match> matches = queryService.getMatchesByDate(date);
        logAndSendToKafka("Matches on date " + date, matches);
        return ResponseEntity.ok(matches);
    }

    @Operation(summary = "Get top batsmen",
            description = "Retrieve paginated list of top batsmen sorted by their total score",
            parameters = {
                    @Parameter(name = "page", description = "Page number", example = "0"),
                    @Parameter(name = "size", description = "Page size", example = "10")
            })
    @ApiResponse(description = "Paginated list of top batsmen", responseCode = "200")
    @GetMapping("/batsmen/top")
    public ResponseEntity<Page<Player>> getTopBatsmen(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Player> topBatsmen = queryService.getTopBatsmen(pageable);
        logAndSendToKafka("Top batsmen for page " + page + " and size " + size, topBatsmen.getContent());
        return ResponseEntity.ok(topBatsmen);
    }

    // Helper method to log and send data to Kafka
    private void logAndSendToKafka(String logPrefix, Object data) {
        try {
            String logMessage = objectMapper.writeValueAsString(data);
            logger.info("{}: {}", logPrefix, logMessage);
            kafkaTemplate.send("Ipl-topic", logMessage);
        } catch (JsonProcessingException e) {
            String errorMessage = "Error serializing data for " + logPrefix + ": " + e.getMessage();
            logger.error(errorMessage);
            kafkaTemplate.send("Ipl-topic", errorMessage);
        }
    }

}
