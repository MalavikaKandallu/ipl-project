package org.example.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Data
@Entity
@Table(name = "delivery")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "over_id", nullable = false)
    private Over over;

    @ManyToOne
    @JoinColumn(name = "inning_id", nullable = false)
    private Inning inning; // Add this field

    @ManyToOne
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "batter")
    private String batter;

    @Column(name = "bowler")
    private String bowler;

    @Column(name = "non_striker")
    private String nonStriker;

    @Column(name = "runs_batter")
    private Integer runsBatter;

    @Column(name = "runs_extras")
    private Integer runsExtras;

    @Column(name = "total_runs")
    private Integer totalRuns;

    @Column(columnDefinition = "JSON")
    private String extras;

    // Method to set extras safely, ensuring valid JSON
    public void setExtras(String extras) {
        // Validate and set JSON string
        if (isValidJson(extras)) {
            this.extras = extras;
        } else {
            this.extras = "{}"; // Set to empty JSON object if invalid or empty
        }
    }

    // Helper method to check if a string is valid JSON
    private boolean isValidJson(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.readTree(jsonString);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
