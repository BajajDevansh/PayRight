package com.codecatalysts.payright.service;

import com.codecatalysts.payright.Dto.AlternativeSuggestionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuggestionService {
    private static final Logger logger = LoggerFactory.getLogger(SuggestionService.class);

    @Autowired
    private AIService aiService;

    public List<AlternativeSuggestionDTO> getAlternativesForApp(String appName, String category) {
        if (appName == null || appName.trim().isEmpty()) {
            throw new IllegalArgumentException("App name cannot be empty for suggestion lookup.");
        }
        logger.info("Fetching alternative suggestions for app: {}, category: {}", appName, category);

        return aiService.getAlternativeSuggestions(appName, category);
    }
}
