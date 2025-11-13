package com.hamza.nlp;

import com.hamza.TfIdfProcessor;
import com.hamza.config.SearchEngineConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import safar.basic.morphology.stemmer.factory.StemmerFactory;
import safar.basic.morphology.stemmer.interfaces.IStemmer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

// Classe utilitaire pour la recherche de documents avec TF-IDF et SAFAR stemmer

public class DocumentSearchUtils {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchEngine {
        private DocumentSearcher searcher;
        private Map<String, Map<String, Double>> tfidfMatrix;

        // Recherche des documents pertinents pour une requête.
        public List<DocumentSearcher.DocumentSearchResult> search(String query) {
            return searcher.searchDocuments(query, 0.01, 5);
        }

        public List<DocumentSearcher.DocumentSearchResult> search(String query, double threshold, int maxResults) {
            return searcher.searchDocuments(query, threshold, maxResults);
        }


        public SearchStatistics getStatistics() {
            int totalDocuments = tfidfMatrix.size();
            int totalUniqueTerms = tfidfMatrix.values().stream()
                    .flatMap(doc -> doc.keySet().stream())
                    .collect(Collectors.toSet()).size();

            return new SearchStatistics(totalDocuments, totalUniqueTerms);
        }
    }

    @AllArgsConstructor
    @Data
    public static class SearchStatistics {
        private final int totalDocuments;
        private final int totalUniqueTerms;

        @Override
        public String toString() {
            return String.format("Documents: %d, Termes uniques: %d", totalDocuments, totalUniqueTerms);
        }
    }
    // Crée un moteur de recherche à partir d'une configuration.

    public static SearchEngine createSearchEngine(SearchEngineConfig.SearchConfig config) throws IOException {
        System.out.println("🏗️  Initialisation du moteur de recherche...");
        
        // 1. Chargement des données
        Set<String> stopWords = loadStopWords(config.getStopWordsPath());
        Map<String, String> documents = loadDocuments(config.getDocumentPaths());
        IStemmer stemmer = StemmerFactory.getLight10Implementation();
        
        // 2. Génération TF-IDF
        System.out.println("📊 Génération de la matrice TF-IDF...");
        Map<String, Map<String, Long>> occurrenceMap = TfIdfProcessor.buildOccurrenceMapStatic(documents, stemmer, stopWords);
        
        Map<String, Map<String, Double>> tfMap = new LinkedHashMap<>();
        for (var entry : occurrenceMap.entrySet()) {
            tfMap.put(entry.getKey(), TfIdfUtils.computeTF(entry.getValue()));
        }
        
        Map<String, Double> idfMap = TfIdfUtils.computeIDF(occurrenceMap);
        Map<String, Map<String, Double>> tfidfMatrix = TfIdfUtils.computeTfIdf(tfMap, idfMap);
        
        // 3. Création du chercheur
        DocumentSearcher searcher = new DocumentSearcher(tfidfMatrix, idfMap, stopWords, stemmer);;
        
        System.out.println("✅ Moteur de recherche initialisé avec succès!");
        return new SearchEngine(searcher, tfidfMatrix);
    }




    private static Set<String> loadStopWords(String path) throws IOException {
        try (var lines = Files.lines(Paths.get(path))) {
            return lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toSet());
        }
    }
    
    private static Map<String, String> loadDocuments(List<String> filePaths) throws IOException {
        Map<String, String> documents = new LinkedHashMap<>();
        for (int i = 0; i < filePaths.size(); i++) {
            String docName = "doc" + (i + 1);
            String path = filePaths.get(i);
            documents.put(docName, Files.readString(Paths.get(path), StandardCharsets.UTF_8));
        }
        return documents;
    }
}
