package com.hamza;

// --- Imports de votre JAR ---

import safar.util.tokenization.impl.SAFARTokenizer;
import safar.util.tokenization.interfaces.ITokenizer;
import java.io.IOException;
import java.nio.charset.StandardCharsets; // Important pour l'arabe
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class main {

    public static void main(String[] args) throws IOException {

        // === 1. Load Stop Words from file ===
        Set<String> stopWords = Files.lines(Paths.get("/Users/mac/Documents/Projects/TextMinig/target/classes/stop_words_arabic.txt"))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toSet());

        // === 2. Lire le texte depuis un fichier (CECI EST VOTRE MODIFICATION) ===
        String textFilePath = "/Users/mac/Documents/Projects/TextMinig/src/main/resources/arabic_text.txt";
        String text = Files.readString(Paths.get(textFilePath), StandardCharsets.UTF_8);

        // === 3. Tokenization (using SAFAR) ===
        ITokenizer tokenizer = new SAFARTokenizer();

        // SAFAR returns String[], so convert it to a List<String>
        String[] tokenArray = tokenizer.tokenize(text);
        List<String> tokens = Arrays.asList(tokenArray);

        // === 4. Filter stop words and count word frequency ===
        Map<String, Long> wordMap = tokens.stream()
                .map(String::toLowerCase)
                .map(token -> token.replaceAll("[^\\u0621-\\u064A]", "")) // clean punctuation
                .filter(token -> !token.isBlank())
                .filter(token -> !stopWords.contains(token))
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));


        // === 5. Trier par fréquence (inchangé) ===
        LinkedHashMap<String, Long> sortedMap = wordMap.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // === 6. Afficher la carte des racines (stems) ===
        System.out.println("===== Carte des Racines du Corpus (Arabe) =====");
        sortedMap.forEach((word, count) -> System.out.println(word + " : " + count));

        // === 7. Sauvegarder les résultats ===
        Files.write(Paths.get("corpus_map_arabic.txt"),
                () -> sortedMap.entrySet().stream()
                        .<CharSequence>map(e -> e.getKey() + " : ".formatted(e.getValue()))
                        .iterator());
        List<String> corpus = new ArrayList<>();
        corpus.add(Files.readString(Paths.get("src/main/resources/oumawiya.txt")));
        corpus.add(Files.readString(Paths.get("src/main/resources/otoman.txt")));
        corpus.add(Files.readString(Paths.get("src/main/resources/abassia.txt")));
    }
}