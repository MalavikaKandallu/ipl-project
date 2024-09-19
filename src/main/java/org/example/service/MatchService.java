package org.example.service;

import jakarta.transaction.Transactional;
import org.example.entity.*;
import org.example.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class MatchService {

    private static final Logger logger = Logger.getLogger(MatchService.class.getName());

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchPlayerRepository matchPlayerRepository;

    @Autowired
    private MatchTeamRepository matchTeamRepository;

    @Autowired
    private InningRepository inningRepository;

    @Autowired
    private OverRepository overRepository;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private PowerplayRepository powerplayRepository;

    @Autowired
    private TargetRepository targetRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void saveMatchData(String jsonContent) throws IOException {
        // Validate JSON content
        JsonNode rootNode = objectMapper.readTree(jsonContent);
        JsonNode infoNode = rootNode.path("info");
        JsonNode inningsNode = rootNode.path("innings");

        // Save Match
        Match match = new Match();
        match.setEventName(infoNode.path("event").path("name").asText());
        match.setMatchType(infoNode.path("match_type").asText());
        match.setCity(infoNode.path("city").asText());

        // Handle LocalDate (for 'date' field)
        try {
            String dateString = infoNode.path("dates").get(0).asText();
            if (dateString != null && !dateString.isEmpty()) {
                match.setDate(LocalDate.parse(dateString));
            } else {
                match.setDate(null); // Set to null if the date is missing or empty
            }
        } catch (Exception e) {
            logger.warning("Date parsing error: " + e.getMessage());
            match.setDate(null); // Handle incorrect/missing date
        }

        match.setVenue(infoNode.path("venue").asText());

        // Handle LocalDateTime (for 'created' field)
        try {
            String createdString = infoNode.path("created").asText();
            if (createdString != null && !createdString.isEmpty()) {
                match.setCreated(LocalDateTime.parse(createdString));
            } else {
                match.setCreated(null); // Set to null if the 'created' field is missing or empty
            }
        } catch (DateTimeParseException e) {
            logger.warning("Created date parsing error: " + e.getMessage());
            match.setCreated(null); // Handle incorrect/missing created field
        }

        match.setBallsPerOver(infoNode.path("balls_per_over").asInt());
        match.setSeason(infoNode.path("season").asText());
        matchRepository.save(match);

        // Save Teams and Players
        JsonNode teamsNode = infoNode.path("teams");
        Set<Team> teams = new HashSet<>();
        for (JsonNode teamNode : teamsNode) {
            Team team = new Team();
            team.setName(teamNode.asText());
            teamRepository.save(team);
            teams.add(team);

            JsonNode playersNode = infoNode.path("players").path(teamNode.asText());
            for (JsonNode playerNameNode : playersNode) {
                Player player = new Player();
                player.setName(playerNameNode.asText());
                playerRepository.save(player);

                // Save match-player relationships
                MatchPlayer matchPlayer = new MatchPlayer();
                matchPlayer.setMatch(match);
                matchPlayer.setPlayer(player);
                matchPlayerRepository.save(matchPlayer);
            }
        }

        // Save MatchTeam relationships
        for (Team team : teams) {
            MatchTeam matchTeam = new MatchTeam();
            matchTeam.setMatch(match);
            matchTeam.setTeam(team);
            matchTeamRepository.save(matchTeam);
        }

        // Save Innings, Overs, Deliveries, Powerplays, and Targets
        for (JsonNode inningNode : inningsNode) {
            Inning inning = new Inning();
            inning.setMatch(match);

            // Handle team assignment by name
            String teamName = inningNode.path("team").asText();
            List<Team> teamList = teamRepository.findByName(teamName);

            if (teamList.size() == 1) {
                Team team = teamList.get(0);
                inning.setTeam(team);
            } else if (teamList.size() > 1) {
                // Handle multiple results (log a warning, or throw an exception)
                logger.warning("Warning: Multiple teams found with name: " + teamName);
                // Optionally, throw an exception if appropriate
                // throw new IllegalStateException("Multiple teams found with name: " + teamName);
            } else {
                logger.warning("No team found with name: " + teamName);
                // Handle the case where no team is found
                inning.setTeam(null); // Ensure this is acceptable in your schema
            }

            inning.setTotalRuns(inningNode.path("total_runs").asInt());
            inning.setTotalOvers(inningNode.path("total_overs").asInt());
            inning.setIsCompleted(inningNode.path("is_completed").asBoolean());

            // Save inning only if team is not null
            if (inning.getTeam() != null) {
                inningRepository.save(inning);
            } else {
                logger.warning("Inning not saved due to missing team.");
            }

            for (JsonNode overNode : inningNode.path("overs")) {
                Over over = new Over();
                over.setInning(inning); // Set the inning for the over
                over.setOverNumber(overNode.path("over").asInt());
                overRepository.save(over);

                for (JsonNode deliveryNode : overNode.path("deliveries")) {
                    Delivery delivery = new Delivery();
                    delivery.setOver(over); // Set the over for the delivery
                    delivery.setInning(inning); // Set the inning for the delivery
                    delivery.setMatch(match); // Set the match for the delivery

                    // Ensure match, inning, and over are not null
                    if (delivery.getMatch() == null) {
                        logger.severe("Delivery match is null.");
                        throw new IllegalStateException("Delivery match cannot be null.");
                    }
                    if (delivery.getInning() == null) {
                        logger.severe("Delivery inning is null.");
                        throw new IllegalStateException("Delivery inning cannot be null.");
                    }
                    if (delivery.getOver() == null) {
                        logger.severe("Delivery over is null.");
                        throw new IllegalStateException("Delivery over cannot be null.");
                    }

                    delivery.setBatter(deliveryNode.path("batter").asText());
                    delivery.setBowler(deliveryNode.path("bowler").asText());
                    delivery.setNonStriker(deliveryNode.path("non_striker").asText());

                    // Extract values from 'runs'
                    JsonNode runsNode = deliveryNode.path("runs");
                    delivery.setRunsBatter(runsNode.path("batter").asInt());
                    delivery.setRunsExtras(runsNode.path("extras").asInt());
                    delivery.setTotalRuns(runsNode.path("total").asInt());

                    // Extract and set the 'extras' JSON object as a String
                    JsonNode extrasNode = deliveryNode.path("extras");
                    StringBuilder extrasStringBuilder = new StringBuilder();
                    if (extrasNode.isMissingNode() || extrasNode.isEmpty()) {
                        // Handle empty or missing extras
                        extrasStringBuilder.append("{}");
                    } else {
                        // Process the extrasNode as usual
                        extrasNode.fieldNames().forEachRemaining(fieldName -> {
                            int value = extrasNode.path(fieldName).asInt();
                            extrasStringBuilder.append(fieldName).append(": ").append(value).append(", ");
                        });
                        // Remove trailing comma and space if present
                        if (extrasStringBuilder.length() > 0) {
                            extrasStringBuilder.setLength(extrasStringBuilder.length() - 2);
                        }
                    }
                    delivery.setExtras(extrasStringBuilder.toString());

                    // Save Delivery entity
                    deliveryRepository.save(delivery);
                }

                for (JsonNode powerplayNode : inningNode.path("powerplays")) {
                    Powerplay powerplay = new Powerplay();
                    powerplay.setInning(inning);
                    powerplay.setStartOver(powerplayNode.path("start_over").asInt());
                    powerplay.setEndOver(powerplayNode.path("end_over").asInt());
                    powerplayRepository.save(powerplay);
                }

                if (inningNode.has("target")) {
                    Target target = new Target();
                    target.setInning(inning);
                    target.setRuns(inningNode.path("target").path("runs").asInt());
                    target.setOvers(inningNode.path("target").path("overs").asInt());
                    targetRepository.save(target);
                }
            }
        }
    }
}
