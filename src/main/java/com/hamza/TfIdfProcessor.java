package com.hamza;

import com.hamza.config.SearchEngineConfig;
import com.hamza.nlp.DocumentSearchUtils;
import com.hamza.nlp.TfIdfUtils;
import safar.basic.morphology.stemmer.factory.StemmerFactory;
import safar.basic.morphology.stemmer.interfaces.IStemmer;
import safar.basic.morphology.stemmer.model.StemmerAnalysis;
import safar.basic.morphology.stemmer.model.WordStemmerAnalysis;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TfIdfProcessor {

    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Démarrage du pipeline TF-IDF modulaire...");

        System.out.println("[ÉTAPE 1/6] Chargement des données...");
        Set<String> stopWords = loadStopWords(SearchEngineConfig.getDefaultConfig().getStopWordsPath());
        Map<String, String> documents = loadDocuments(SearchEngineConfig.getDefaultConfig().getDocumentPaths());

        System.out.println("\n[ÉTAPE 2/6] Initialisation du Stemmer SAFAR...");
        IStemmer stemmer = initializeStemmer();

        System.out.println("\n[ÉTAPE 3/6] Traitement du corpus...");
        Map<String, Map<String, Long>> occurrenceMap = buildOccurrenceMap(documents, stemmer, stopWords);

        System.out.println("\n[ÉTAPE 4/6] Calcul des scores TF et IDF...");
        Map<String, Map<String, Double>> tfMap = computeTfMap(occurrenceMap);

        Map<String, Double> idfMap = TfIdfUtils.computeIDF(occurrenceMap);
        printTfIdfApercus(tfMap, idfMap);

        System.out.println("\n[ÉTAPE 5/6] Calcul des scores TF-IDF finaux...");

        Map<String, Map<String, Double>> tfIdfMap = TfIdfUtils.computeTfIdf(tfMap, idfMap);
        System.out.println("✅ Calcul TF-IDF terminé.");

        System.out.println("\n--- PIPELINE TERMINÉ ---");
    }


    private static Set<String> loadStopWords(String path) throws IOException {
        System.out.println("  -> Chargement des mots vides depuis: " + path);
        Set<String> stopWords = Files.lines(Paths.get(path))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toSet());
        System.out.println("  ✅ " + stopWords.size() + " mots vides chargés.");
        return stopWords;
    }

    private static Map<String, String> loadDocuments(List<String> filePaths) throws IOException {
        Map<String, String> documents = new LinkedHashMap<>();
        for (int i = 0; i < filePaths.size(); i++) {
            String docName = "doc" + (i + 1);
            String path = filePaths.get(i);
            documents.put(docName, Files.readString(Paths.get(path), StandardCharsets.UTF_8));
            System.out.println("  -> Document '" + docName + "' chargé (" + path + ")");
        }
        return documents;
    }

    private static IStemmer initializeStemmer() {
        IStemmer stemmer = StemmerFactory.getLight10Implementation();
        System.out.println("  ✅ Raciniseur (Light10) initialisé !");
        return stemmer;
    }

    private static Map<String, Map<String, Long>> buildOccurrenceMap(
            Map<String, String> documents,
            IStemmer stemmer,
            Set<String> stopWords) {

        Map<String, Map<String, Long>> occurrenceMap = new LinkedHashMap<>();

        for (var docEntry : documents.entrySet()) {
            String docName = docEntry.getKey();
            String docText = docEntry.getValue();
            System.out.println("\n  --- Traitement de '" + docName + "' ---");

            List<String> docStems = processText(docText, stemmer, stopWords);

            Set<String> uniqueStems = new HashSet<>(docStems);
            System.out.println("  3. Racines (stems) uniques trouvées: " + uniqueStems.size());
            System.out.print("    [ ");
            uniqueStems.forEach(s -> System.out.print(s + " "));
            System.out.println("]");

            Map<String, Long> freqMap = docStems.stream()
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            occurrenceMap.put(docName, freqMap);
            System.out.println("  ✅ '" + docName + "' traité.");
        }
        return occurrenceMap;
    }

    private static List<String> processText(String text, IStemmer stemmer, Set<String> stopWords) {
        System.out.println("  1. Racinisation du texte (stemming)...");
        List<WordStemmerAnalysis> analyses = stemmer.stem(text);
        List<String> docStems = new ArrayList<>();
        System.out.println("  2. Extraction des morphèmes et filtrage...");

        for (WordStemmerAnalysis wordAnalysis : analyses) {
            var analysisList = wordAnalysis.getListStemmerAnalysis();
            if (analysisList != null && !analysisList.isEmpty()) {
                StemmerAnalysis firstAnalysis = analysisList.get(0);
                String stem = firstAnalysis.getMorpheme();

                if (isValidStem(stem, stopWords)) {
                    docStems.add(stem);
                }
            }
        }
        return docStems;
    }

    private static boolean isValidStem(String stem, Set<String> stopWords) {
        return stem != null &&           // Ne doit pas être nul
                !stem.isBlank() &&        // Ne doit pas être vide
                !stopWords.contains(stem) && // Ne doit pas être un mot vide
                !stem.matches("\\d+") &&  // Ne doit pas être un nombre
                stem.length() > 1;        // Ne doit pas être une seule lettre
    }

    private static Map<String, Map<String, Double>> computeTfMap(Map<String, Map<String, Long>> occurrenceMap) {
        Map<String, Map<String, Double>> tfMap = new LinkedHashMap<>();
        for (var entry : occurrenceMap.entrySet()) {
            // APPEL À LA CLASSE UTILS
            tfMap.put(entry.getKey(), TfIdfUtils.computeTF(entry.getValue()));
        }
        System.out.println("  ✅ Scores TF calculés.");
        return tfMap;
    }

    private static void printTfIdfApercus(Map<String, Map<String, Double>> tfMap, Map<String, Double> idfMap) {
        System.out.println("\n--- APERÇU SCORES TF ---");
        tfMap.forEach((doc, map) -> {
            System.out.println("  Document: \"" + doc + "\"");
            map.entrySet().stream().limit(5).forEach((entry) -> {
                System.out.println(String.format("    \"%s\": %.8f", entry.getKey(), entry.getValue()));
            });
            System.out.println("    ...");
        });

        System.out.println("\n--- APERÇU SCORES IDF ---");
        idfMap.entrySet().stream().limit(10).forEach((entry) -> {
            System.out.println(String.format("  \"%s\": %.8f", entry.getKey(), entry.getValue()));
        });
        System.out.println("  ...");
    }

    public static Map<String, Map<String, Long>> buildOccurrenceMapStatic(
            Map<String, String> documents,
            IStemmer stemmer,
            Set<String> stopWords) {
        return buildOccurrenceMap(documents, stemmer, stopWords);
    }
}