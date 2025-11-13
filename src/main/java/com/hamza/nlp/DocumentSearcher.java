package com.hamza.nlp;

import lombok.AllArgsConstructor;
import lombok.Data;
import safar.basic.morphology.stemmer.interfaces.IStemmer;
import safar.basic.morphology.stemmer.model.StemmerAnalysis;
import safar.basic.morphology.stemmer.model.WordStemmerAnalysis;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

// Classe pour rechercher et comparer les documents basés sur des requêtes utilisateur en utilisant la similarité cosinus avec les matrices TF-IDF.

@AllArgsConstructor
public class DocumentSearcher {

    private final Map<String, Map<String, Double>> tfidfMatrix;
    private final Map<String, Double> idfMap;
    private final Set<String> stopWords;
    private final IStemmer stemmer;

    public List<DocumentSearchResult> searchDocuments(String query, 
                                                    double similarityThreshold, 
                                                    int maxResults) {
        System.out.println("🔍 Recherche pour la requête: \"" + query + "\"");
        
        // 1. Traiter la requête utilisateur
        Map<String, Double> queryTfIdf = processQuery(query);
        
        if (queryTfIdf.isEmpty()) {
            System.out.println("⚠️  Aucun terme valide trouvé dans la requête.");
            return Collections.emptyList();
        }
        
        // 2. Calculer la similarité avec chaque document
        List<DocumentSearchResult> results = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, Double>> docEntry : tfidfMatrix.entrySet()) {
            String docName = docEntry.getKey();
            Map<String, Double> docTfIdf = docEntry.getValue();
            
            double similarity = computeCosineSimilarity(queryTfIdf, docTfIdf);
            
            if (similarity >= similarityThreshold) {
                results.add(new DocumentSearchResult(docName, similarity, 
                    findMatchingTerms(queryTfIdf, docTfIdf)));
            }
        }
        
        // 3. Trier par score de similarité décroissant
        results.sort((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()));
        
        // 4. Limiter les résultats
        if (results.size() > maxResults) {
            results = results.subList(0, maxResults);
        }
        
        System.out.println("✅ " + results.size() + " document(s) trouvé(s).");
        return results;
    }
    
    // Traite une requête utilisateur pour créer son vecteur TF-IDF.

    private Map<String, Double> processQuery(String query) {
        System.out.println("  -> Traitement de la requête...");
        
        // 1. Stemming de la requête
        List<WordStemmerAnalysis> analyses = stemmer.stem(query);
        List<String> queryStems = new ArrayList<>();
        
        for (WordStemmerAnalysis wordAnalysis : analyses) {
            var analysisList = wordAnalysis.getListStemmerAnalysis();
            if (analysisList != null && !analysisList.isEmpty()) {
                StemmerAnalysis firstAnalysis = analysisList.get(0);
                String stem = firstAnalysis.getMorpheme();
                
                if (isValidStem(stem)) {
                    queryStems.add(stem);
                }
            }
        }
        
        System.out.println("  -> Termes extraits de la requête: " + queryStems);
        
        if (queryStems.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // 2. Calculer TF pour la requête
        Map<String, Long> queryFreq = queryStems.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        
        Map<String, Double> queryTf = TfIdfUtils.computeTF(queryFreq);
        
        // 3. Calculer TF-IDF pour la requête en utilisant l'IDF global
        Map<String, Double> queryTfIdf = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : queryTf.entrySet()) {
            String term = entry.getKey();
            Double tfScore = entry.getValue();
            Double idfScore = idfMap.getOrDefault(term, 0.0);
            
            // Si le terme n'existe pas dans le corpus, lui donner un IDF par défaut
            if (idfScore == 0.0) {
                // IDF pour un terme nouveau = log(N) où N est le nombre total de documents
                idfScore = Math.log(tfidfMatrix.size()) + 1.0;
            }
            
            queryTfIdf.put(term, tfScore * idfScore);
        }
        
        return queryTfIdf;
    }
    
    // Calcule la similarité cosinus entre deux vecteurs TF-IDF.
    private double computeCosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        // Obtenir tous les termes communs
        Set<String> commonTerms = new HashSet<>(vector1.keySet());
        commonTerms.retainAll(vector2.keySet());
        
        if (commonTerms.isEmpty()) {
            return 0.0;
        }
        
        // Calculer le produit scalaire
        double dotProduct = 0.0;
        for (String term : commonTerms) {
            dotProduct += vector1.get(term) * vector2.get(term);
        }
        
        // Calculer les normes
        double norm1 = Math.sqrt(vector1.values().stream().mapToDouble(v -> v * v).sum());
        double norm2 = Math.sqrt(vector2.values().stream().mapToDouble(v -> v * v).sum());
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (norm1 * norm2);
    }
    
    // Trouve les termes correspondants entre la requête et un document.

    private List<String> findMatchingTerms(Map<String, Double> queryTfIdf, Map<String, Double> docTfIdf) {
        Set<String> commonTerms = new HashSet<>(queryTfIdf.keySet());
        commonTerms.retainAll(docTfIdf.keySet());
        return new ArrayList<>(commonTerms);
    }
    
    // Valide si une racine (stem) est acceptable.
    private boolean isValidStem(String stem) {
        return stem != null &&
                !stem.isBlank() &&
                !stopWords.contains(stem) &&
                !stem.matches("\\d+") &&
                stem.length() > 1;
    }
    
    // Classe pour représenter un résultat de recherche.

    @AllArgsConstructor
    @Data
    public static class DocumentSearchResult {
        private final String documentName;
        private final double similarityScore;
        private final List<String> matchingTerms;

        @Override
        public String toString() {
            return String.format("Document: %s | Score: %.4f | Termes: %s", 
                    documentName, similarityScore, matchingTerms);
        }
    }
}
