package com.hamza.nlp;

import java.util.List;

public class utils {
    public double calculateTF(String word, List<String> document) {
        long count = 0;
        for (String w : document) {
            if (w.equals(word)) {
                count++;
            }
        }
        return (double) count / document.size();
    }

    // 3. Fonction pour IDF
    public double calculateIDF(String word, List<List<String>> allDocuments) {
        long docsContainingWord = 0;
        for (List<String> doc : allDocuments) {
            if (doc.contains(word)) {
                docsContainingWord++;
            }
        }
        // Ajouter +1 au dénominateur est une astuce (smoothing) pour éviter log(N/0)
        // s'il y a un mot qui n'apparaît nulle part.
        return Math.log((double) allDocuments.size() / (docsContainingWord + 1));
    }

//    Elle combine les deux.
    public double calculateTfIdf(String word, List<String> document, List<List<String>> allDocuments) {
        double tf = calculateTF(word, document);
        double idf = calculateIDF(word, allDocuments);
        return tf * idf;
    }
}
