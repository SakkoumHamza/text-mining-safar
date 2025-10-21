package com.hamza.nlp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
public class WordExtractor {

    public List<String> extractWords(String filePath) {
        // Une liste pour stocker les mots de CE fichier
        List<String> wordsList = new ArrayList<>();

        System.out.println("Thread [" + Thread.currentThread().getName() + "] démarre la lecture de : " + filePath);

        try (FileInputStream fis = new FileInputStream(new File(filePath));
             XWPFDocument document = new XWPFDocument(fis)) {

            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (XWPFParagraph para : paragraphs) {
                String texteNettoye = para.getText().replaceAll("[^\\p{L}\\p{N}]+", " ");
                String[] mots = texteNettoye.trim().split("\\s+");

                for (String mot : mots) {
                    if (!mot.isEmpty()) {
                        wordsList.add(mot);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur en lisant " + filePath + ": " + e.getMessage());
        }

        System.out.println("Thread [" + Thread.currentThread().getName() + "] a terminé. Mots trouvés : " + wordsList.size());
        return wordsList;
    }
}