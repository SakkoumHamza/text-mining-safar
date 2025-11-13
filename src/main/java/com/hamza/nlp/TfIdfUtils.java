package com.hamza.nlp;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor
public class TfIdfUtils {

    public static Map<String, Double> computeTF(Map<String, Long> freqMap) {
        double total = freqMap.values().stream().mapToLong(Long::longValue).sum();
        Map<String, Double> tf = new LinkedHashMap<>();
        if (total == 0) return tf;
        for (var e : freqMap.entrySet()) {
            tf.put(e.getKey(), e.getValue() / total);
        }
        return tf;
    }

    public static Map<String, Double> computeIDF(Map<String, Map<String, Long>> allDocs) {
        Map<String, Double> idf = new LinkedHashMap<>();
        double totalDocs = allDocs.size();
        Set<String> allTerms = allDocs.values().stream().flatMap(m -> m.keySet().stream()).collect(Collectors.toSet());

        for (String term : allTerms) {
            long docsWithTerm = allDocs.values().stream().filter(m -> m.containsKey(term)).count();
            // Formule IDF (avec lissage +1 pour éviter la division par zéro si un terme n'est nulle part)
            idf.put(term, Math.log(totalDocs / (1.0 + docsWithTerm)) + 1.0); // +1.0 Lissage Additif
        }
        return idf;
    }

    // Calcule le score TF-IDF final pour tous les documents et tous les termes.

    public static Map<String, Map<String, Double>> computeTfIdf(Map<String, Map<String, Double>> tfMap, Map<String, Double> idfMap) {
        Map<String, Map<String, Double>> tfidfMap = new LinkedHashMap<>();

        // Parcourt la carte TF (doc1, doc2, ...)
        for (var tfEntry : tfMap.entrySet()) {
            String docName = tfEntry.getKey();
            Map<String, Double> docTf = tfEntry.getValue();
            Map<String, Double> tfidf = new LinkedHashMap<>();

            // Pour chaque terme dans le document, multiplie son TF par son IDF global
            for (var termEntry : docTf.entrySet()) {
                String term = termEntry.getKey();
                Double tfScore = termEntry.getValue();
                Double idfScore = idfMap.getOrDefault(term, 0.0); // Récupère le score IDF

                tfidf.put(term, tfScore * idfScore);
            }
            tfidfMap.put(docName, tfidf);
        }
        return tfidfMap;
    }

}
