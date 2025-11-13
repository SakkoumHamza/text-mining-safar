package com.hamza.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class SearchResponseDto {
    
    private String query;
    private List<DocumentResult> results;
    private int totalResults;
    private double executionTime;
    private boolean success;
    private String message;


    public SearchResponseDto(String query, List<DocumentResult> results, double executionTime) {
        this.query = query;
        this.results = results;
        this.totalResults = results.size();
        this.executionTime = executionTime;
        this.success = true;
    }
    
    public static SearchResponseDto error(String query, String message) {
        SearchResponseDto response = new SearchResponseDto();
        response.query = query;
        response.success = false;
        response.message = message;
        response.totalResults = 0;
        return response;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DocumentResult {
        private String documentName;
        private double similarityScore;
        private List<String> matchingTerms;
        
        public String getFormattedScore() {
            return String.format("%.4f", similarityScore);
        }
        
        public String getScorePercentage() {
            return String.format("%.1f%%", similarityScore * 100);
        }
    }

}
