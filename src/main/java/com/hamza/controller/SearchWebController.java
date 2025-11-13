package com.hamza.controller;

import com.hamza.dto.SearchRequestDto;
import com.hamza.dto.SearchResponseDto;
import com.hamza.nlp.DocumentSearchUtils;
import com.hamza.service.DocumentSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


// Contrôleur Web pour l'interface utilisateur Thymeleaf

@Controller
public class SearchWebController {
    
    private final DocumentSearchService searchService;
    
    @Autowired
    public SearchWebController(DocumentSearchService searchService) {
        this.searchService = searchService;
    }

    @ModelAttribute("searchRequest")
    public SearchRequestDto getSearchRequest() {
        return new SearchRequestDto();
    }

    // Les statistiques
    @ModelAttribute("statistics")
    public DocumentSearchUtils.SearchStatistics getStatistics() {
        return searchService.getSearchStatistics();
    }

    // Page d'accueil
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Recherche
    @PostMapping("/search")
    public String search(@Valid @ModelAttribute SearchRequestDto searchRequest,
                        BindingResult bindingResult, 
                        Model model) {
        
        if (bindingResult.hasErrors()) {
            return "index";
        }
        
        // Effectuer la recherche
        SearchResponseDto response = searchService.searchDocuments(searchRequest);
        model.addAttribute("searchResponse", response);
        
        return "index";
    }

    // Resultas de recherhce
    @GetMapping("/results")
    public String results(@RequestParam String query, 
                         @RequestParam(defaultValue = "0.01") double threshold,
                         @RequestParam(defaultValue = "5") int maxResults,
                         Model model) {
        
        SearchRequestDto request = new SearchRequestDto(query, threshold, maxResults);
        SearchResponseDto response = searchService.searchDocuments(request);
        
        model.addAttribute("searchRequest", request);
        model.addAttribute("searchResponse", response);

        return "index";
    }

    
    // Les suggestions
    @GetMapping("/api/suggestions")
    @ResponseBody
    public String[] getSuggestions(@RequestParam String term) {
        return new String[]{
                "بغداد",
                "الإمبراطورية",
                "العلوم",
                "دمشق",
                "الأندلس"
        };
    }
}
