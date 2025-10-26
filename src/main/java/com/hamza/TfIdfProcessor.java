// Assurez-vous que ce package correspond à votre projet
package com.hamza;

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

    // --- CHEMINS DE CONFIGURATION ---
    private static final String STOP_WORDS_PATH = "/Users/mac/Documents/Projects/TextMinig/src/main/resources/stop_words_arabic.txt";
    private static final String OUTPUT_JSON_PATH = "/Users/mac/Documents/Projects/TextMinig/target/tfidf_results.json";
    private static final List<String> DOCUMENT_PATHS = List.of(
            "/Users/mac/Documents/Projects/TextMinig/src/main/resources/doc1.txt",
            "/Users/mac/Documents/Projects/TextMinig/src/main/resources/doc2.txt",
            "/Users/mac/Documents/Projects/TextMinig/src/main/resources/doc3.txt"
    );

    public static void main(String[] args) throws IOException {
        System.out.println("🚀 Démarrage du pipeline TF-IDF modulaire...");

        // --- 1. Chargement ---
        System.out.println("[ÉTAPE 1/6] Chargement des données...");
        Set<String> stopWords = loadStopWords(STOP_WORDS_PATH);
        Map<String, String> documents = loadDocuments(DOCUMENT_PATHS);

        // --- 2. Initialisation ---
        System.out.println("\n[ÉTAPE 2/6] Initialisation du Stemmer SAFAR...");
        IStemmer stemmer = initializeStemmer();

        // --- 3. Traitement & Comptage ---
        System.out.println("\n[ÉTAPE 3/6] Traitement du corpus...");
        Map<String, Map<String, Long>> occurrenceMap = buildOccurrenceMap(documents, stemmer, stopWords);

        // --- 4. Calcul TF & IDF (avec aperçus) ---
        System.out.println("\n[ÉTAPE 4/6] Calcul des scores TF et IDF...");
        Map<String, Map<String, Double>> tfMap = computeTfMap(occurrenceMap);
        // APPEL À LA CLASSE UTILS
        Map<String, Double> idfMap = TfIdfUtils.computeIDF(occurrenceMap);
        printTfIdfApercus(tfMap, idfMap);

        // --- 5. Calcul TF-IDF Final ---
        System.out.println("\n[ÉTAPE 5/6] Calcul des scores TF-IDF finaux...");
        // APPEL À LA CLASSE UTILS
        Map<String, Map<String, Double>> tfIdfMap = TfIdfUtils.computeTfIdf(tfMap, idfMap);
        System.out.println("✅ Calcul TF-IDF terminé.");

        // --- 6. Sauvegarde ---
        System.out.println("\n[ÉTAPE 6/6] Sauvegarde des résultats...");
        // APPEL À LA CLASSE UTILS
        saveResultsAsJson(tfIdfMap, OUTPUT_JSON_PATH);

        System.out.println("\n--- PIPELINE TERMINÉ ---");
    }

    // =================================================================
    // --- MODULES DU PIPELINE (Logique d'orchestration) ---
    // =================================================================

    /**
     * ÉTAPE 1: Charge le fichier de mots vides (stop words).
     */
    private static Set<String> loadStopWords(String path) throws IOException {
        System.out.println("  -> Chargement des mots vides depuis: " + path);
        Set<String> stopWords = Files.lines(Paths.get(path))
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toSet());
        System.out.println("  ✅ " + stopWords.size() + " mots vides chargés.");
        return stopWords;
    }

    /**
     * ÉTAPE 1: Charge les documents texte depuis une liste de chemins.
     */
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

    /**
     * ÉTAPE 2: Initialise le stemmer SAFAR.
     */
    private static IStemmer initializeStemmer() {
        IStemmer stemmer = StemmerFactory.getLight10Implementation();
        System.out.println("  ✅ Raciniseur (Light10) initialisé !");
        return stemmer;
    }

    /**
     * ÉTAPE 3: Construit la carte de fréquence des termes (racines) pour tous les documents.
     */
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

    /**
     * ÉTAPE 3 (Sous-tâche): Traite un texte brut pour en extraire une liste de racines filtrées.
     */
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

    /**
     * ÉTAPE 3 (Sous-tâche): Définit les règles de filtrage pour une racine (stem).
     */
    private static boolean isValidStem(String stem, Set<String> stopWords) {
        return stem != null &&           // Ne doit pas être nul
                !stem.isBlank() &&        // Ne doit pas être vide
                !stopWords.contains(stem) && // Ne doit pas être un mot vide
                !stem.matches("\\d+") &&  // Ne doit pas être un nombre
                stem.length() > 1;        // Ne doit pas être une seule lettre
    }

    /**
     * ÉTAPE 4: Calcule la carte TF pour tous les documents.
     */
    private static Map<String, Map<String, Double>> computeTfMap(Map<String, Map<String, Long>> occurrenceMap) {
        Map<String, Map<String, Double>> tfMap = new LinkedHashMap<>();
        for (var entry : occurrenceMap.entrySet()) {
            // APPEL À LA CLASSE UTILS
            tfMap.put(entry.getKey(), TfIdfUtils.computeTF(entry.getValue()));
        }
        System.out.println("  ✅ Scores TF calculés.");
        return tfMap;
    }

    /**
     * ÉTAPE 4: Affiche les aperçus TF et IDF dans la console.
     */
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

    /**
     * ÉTAPE 6: Convertit la carte TF-IDF finale en JSON et la sauvegarde dans un fichier.
     */
    private static void saveResultsAsJson(Map<String, Map<String, Double>> tfIdfMap, String outputPath) throws IOException {
        System.out.println("  -> Génération de la chaîne JSON...");
        // APPEL À LA CLASSE UTILS
        String jsonOutput = TfIdfUtils.convertMapToJson(tfIdfMap);

        System.out.println("  -> Écriture du fichier vers: " + outputPath);
        Files.writeString(Paths.get(outputPath), jsonOutput, StandardCharsets.UTF_8);

        System.out.println("✅ Fichier JSON sauvegardé avec succès !");

        System.out.println("\n--- APERÇU DE LA MATRICE TF-IDF (JSON) ---");
        System.out.println(jsonOutput);
    }
}