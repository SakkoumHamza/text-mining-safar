package com.hamza.service;

import com.hamza.dto.SearchRequestDto;
import com.hamza.dto.SearchResponseDto;
import com.hamza.nlp.DocumentSearchUtils;
import com.hamza.nlp.DocumentSearcher;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// Service pour gérer les opérations de recherche de documents.

@Service
@AllArgsConstructor
public class DocumentSearchService {

    private final DocumentSearchUtils.SearchEngine searchEngine;

    // Effectue une recherche de documents basée sur la requête.
    public SearchResponseDto searchDocuments(SearchRequestDto request) {
        try {
            long startTime = System.currentTimeMillis();
            
            // Effectuer la recherche
            List<DocumentSearcher.DocumentSearchResult> searchResults = searchEngine.search(
                    request.getQuery(), 
                    request.getThreshold(), 
                    request.getMaxResults()
            );
            
            // Convertir les résultats en DTOs
            List<SearchResponseDto.DocumentResult> documentResults = searchResults.stream()
                    .map(result -> new SearchResponseDto.DocumentResult(
                            result.getDocumentName(),
                            result.getSimilarityScore(),
                            result.getMatchingTerms()
                    ))
                    .collect(Collectors.toList());
            
            long endTime = System.currentTimeMillis();
            double executionTime = (endTime - startTime) / 1000.0;
            
            return new SearchResponseDto(request.getQuery(), documentResults, executionTime);
            
        } catch (Exception e) {
            return SearchResponseDto.error(request.getQuery(),
                    "Erreur lors de la recherche: " + e.getMessage());
        }
    }

    

    public DocumentSearchUtils.SearchStatistics getSearchStatistics() {
        return searchEngine.getStatistics();
    }

}
