package com.codecatalysts.payright.controller;

import com.codecatalysts.payright.Dto.AlternativeSuggestionDTO;
import com.codecatalysts.payright.service.SuggestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    @Autowired
    private SuggestionService suggestionService;

    @GetMapping("/alternatives")
    public ResponseEntity<List<AlternativeSuggestionDTO>> getAlternativeSuggestions(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @RequestParam String appName,
            @RequestParam(required = false) String category) {
        List<AlternativeSuggestionDTO> suggestions = suggestionService.getAlternativesForApp(appName, category);
        if (suggestions.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(suggestions);
    }
}
