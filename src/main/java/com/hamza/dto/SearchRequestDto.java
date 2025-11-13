package com.hamza.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SearchRequestDto {
    
    @NotBlank(message = "يرجى إدخال نص البحث")
    private String query;
    
    @DecimalMin(value = "0.001", message = "حد التشابه يجب أن يكون أكبر من 0.001")
    private double threshold = 0.01;

    @Min(value = 1, message = "عدد النتائج يجب أن يكون أكبر من 0")
    private int maxResults = 5;
    
    // Constructeurs
    public SearchRequestDto() {}
    
    public SearchRequestDto(String query) {
        this.query = query;
    }
    
    public SearchRequestDto(String query, double threshold, int maxResults) {
        this.query = query;
        this.threshold = threshold;
        this.maxResults = maxResults;
    }

    
    @Override
    public String toString() {
        return String.format("SearchRequest{query='%s', threshold=%.3f, maxResults=%d}", 
                query, threshold, maxResults);
    }
}
