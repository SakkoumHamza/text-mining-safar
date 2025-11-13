package com.hamza.config;

import com.hamza.nlp.DocumentSearchUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;


@Configuration
public class SearchEngineConfig {

    @Bean
    public DocumentSearchUtils.SearchEngine searchEngine() throws Exception {
        System.out.println("🔧 Configuration du moteur de recherche Spring Bean...");
        SearchConfig config = SearchEngineConfig.getDefaultConfig();
        DocumentSearchUtils.SearchEngine engine = DocumentSearchUtils.createSearchEngine(config);
        System.out.println("✅ Moteur de recherche configuré comme Bean Spring");
        return engine;
    }


    @Data
    @AllArgsConstructor
    public static class SearchConfig {
        private final String stopWordsPath;
        private final List<String> documentPaths;
    }

    public static SearchConfig getDefaultConfig() {
        return new SearchConfig(
                "/Users/mac/Documents/Projects/TextMinig_v2/src/main/resources/stop_words_arabic.txt",
                Arrays.asList(
                        "/Users/mac/Documents/Projects/TextMinig_v2/src/main/resources/doc1.txt",
                        "/Users/mac/Documents/Projects/TextMinig_v2/src/main/resources/doc2.txt",
                        "/Users/mac/Documents/Projects/TextMinig_v2/src/main/resources/doc3.txt"
                )
        );
    }

}
