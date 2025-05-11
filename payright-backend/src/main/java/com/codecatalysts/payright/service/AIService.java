package com.codecatalysts.payright.service;
import com.codecatalysts.payright.Dto.AIAnalysisResult;
import com.codecatalysts.payright.Dto.AISubscriptionAnalysisRequest;
import com.codecatalysts.payright.Dto.AlternativeSuggestionDTO;
import com.codecatalysts.payright.model.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class AIService {
    private static final Logger logger = LoggerFactory.getLogger(AIService.class);
    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public AIService(RestTemplate restTemplate, @Value("${ai.service.url}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    public AIAnalysisResult analyzeTransactionsForSubscriptions(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new AIAnalysisResult(); // Return empty result
        }
        String analyzeEndpoint = aiServiceUrl + "/analyze-transactions";
        logger.debug("Sending {} transactions to AI service at {}", transactions.size(), analyzeEndpoint);

        List<AISubscriptionAnalysisRequest.TransactionAIAttributes> aiTransactions = transactions.stream()
                .map(t -> new AISubscriptionAnalysisRequest.TransactionAIAttributes(
                        t.getId(), t.getDescription(), t.getAmount(), t.getDate(), t.getCurrency()))
                .collect(Collectors.toList());
        AISubscriptionAnalysisRequest requestPayload = new AISubscriptionAnalysisRequest(aiTransactions);

        try {
            ResponseEntity<AIAnalysisResult> response = restTemplate.postForEntity(
                    analyzeEndpoint, requestPayload, AIAnalysisResult.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully received analysis from AI service for {} transactions.", transactions.size());
                return response.getBody();
            } else {
                logger.error("AI service returned non-successful status: {} for transaction analysis", response.getStatusCode());
                return new AIAnalysisResult();
            }
        } catch (RestClientException e) {
            logger.error("Error calling AI service for transaction analysis: {}", e.getMessage(), e);
            return new AIAnalysisResult();
        }
    }

    public List<AlternativeSuggestionDTO> getAlternativeSuggestions(String appName, String category) {
        String suggestionsEndpoint = aiServiceUrl + "/suggest-alternatives";
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(suggestionsEndpoint)
                .queryParam("app_name", appName);
        if (category != null && !category.isEmpty()) {
            builder.queryParam("category", category);
        }

        logger.debug("Requesting alternative suggestions for app: '{}', category: '{}' from AI service", appName, category);

        try {
            ResponseEntity<List<AlternativeSuggestionDTO>> response = restTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<AlternativeSuggestionDTO>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                logger.info("Successfully received {} alternative suggestions for app '{}'", response.getBody().size(), appName);
                return response.getBody();
            } else {
                logger.error("AI service returned non-successful status: {} for alternative suggestions", response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (RestClientException e) {
            logger.error("Error calling AI service for alternative suggestions: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}

