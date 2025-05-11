package com.codecatalysts.payright.service;
import com.codecatalysts.payright.Dto.AIAnalysisResult;
import com.codecatalysts.payright.model.Subscription;
import com.codecatalysts.payright.model.Transaction;
import com.codecatalysts.payright.model.WalletTransaction;
import com.codecatalysts.payright.model.WalletTransactionType;
import com.codecatalysts.payright.repositories.SubscriptionRepository;
import com.codecatalysts.payright.repositories.TransactionRepository;
import com.codecatalysts.payright.repositories.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;



@Service
public class TransactionService {
    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private AIService aiService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SubscriptionService subService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    public List<Transaction> getTransactionsForUser(String userId) {
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    @Transactional
    public List<Transaction> loadAndProcessMockTransactions(String userId) {
        logger.info("Loading mock transactions for user: {}", userId);
        try {
            ClassPathResource resource = new ClassPathResource("mock-data/transactions.json");
            InputStream inputStream = resource.getInputStream();
            byte[] bdata = FileCopyUtils.copyToByteArray(inputStream);
            String data = new String(bdata, StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            List<Transaction> mockTransactions = objectMapper.readValue(data, new TypeReference<List<Transaction>>() {});

            List<Transaction> newTransactionsToSave = mockTransactions.stream()
                    .peek(t -> t.setUserId(userId))
                    .filter(t -> transactionRepository.findByUserIdOrderByDateDesc(userId).stream()
                            .noneMatch(existing -> existing.getDescription().equals(t.getDescription()) &&
                                    existing.getDate().equals(t.getDate()) &&
                                    existing.getAmount().compareTo(t.getAmount()) == 0))
                    .collect(Collectors.toList());

            if (!newTransactionsToSave.isEmpty()) {
                transactionRepository.saveAll(newTransactionsToSave);
                logger.info("Saved {} new mock transactions for user {}", newTransactionsToSave.size(), userId);

                List<Transaction> allUserTransactions = transactionRepository.findByUserIdOrderByDateDesc(userId);
                if (!allUserTransactions.isEmpty()) {
                    AIAnalysisResult analysisResult = aiService.analyzeTransactionsForSubscriptions(allUserTransactions);
                    processAIAnalysisResult(analysisResult, userId);
                }
            } else {
                logger.info("No new mock transactions to load for user {}", userId);
            }

        } catch (IOException e) {
            logger.error("Error loading mock transactions for user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to load mock transactions", e);
        }
        return transactionRepository.findByUserIdOrderByDateDesc(userId);
    }

    @Transactional
    public void processAIAnalysisResult(AIAnalysisResult result, String userId) {
        if (result == null || result.getIdentifiedSubscriptions() == null) {
            logger.warn("Received null or empty AI analysis result for user {}", userId);
            return;
        }
        logger.info("Processing AI analysis result for user {}: {} subscriptions identified.", userId, result.getIdentifiedSubscriptions().size());

        for (AIAnalysisResult.IdentifiedSubscriptionAI aiSub : result.getIdentifiedSubscriptions()) {
            Optional<Subscription> optDbSubscription = subscriptionRepository
                    .findByUserIdAndActiveTrueOrderByNextBillingDateAsc(userId).stream()
                    .filter(s -> s.getName() != null && s.getName().equalsIgnoreCase(aiSub.getName()) &&
                            s.getAmount().compareTo(aiSub.getEstimatedAmount()) == 0)
                    .findFirst();

            if (optDbSubscription.isPresent()) {
                Subscription dbSubscription = optDbSubscription.get();

                if (dbSubscription.isPayFromWallet()) {
                    List<WalletTransaction> recentPayments = walletTransactionRepository
                            .findByUserIdAndRelatedSubscriptionIdAndTypeOrderByTransactionDateDesc(
                                    userId, dbSubscription.getId(), WalletTransactionType.DEBIT);

                    for (String originalTxId : aiSub.getMatchedTransactionIds()) {
                        Transaction originalTx = transactionRepository.findById(originalTxId).orElse(null);
                        if (originalTx != null && !originalTx.isCoveredByWalletPayment()) {
                            boolean wasCovered = recentPayments.stream().anyMatch(wp ->
                                    wp.getTransactionDate().toLocalDate().getMonth() == originalTx.getDate().getMonth() &&
                                            wp.getTransactionDate().toLocalDate().getYear() == originalTx.getDate().getYear() // Simplistic check
                            );
                            if (wasCovered) {
                                originalTx.setCoveredByWalletPayment(true);
                                transactionRepository.save(originalTx);
                                logger.info("Marked original transaction {} as covered by wallet payment for subscription {}",
                                        originalTx.getId(), dbSubscription.getName());
                            }
                        }
                    }
                }
            }
        }

        if (result.getAlerts() != null && !result.getAlerts().isEmpty()) {
            result.getAlerts().forEach(alertMsg ->
                    notificationService.createNotification(userId, alertMsg, "AI_ALERT", null)
            );
        }

    }

    private LocalDate calculateNextBillingDate(LocalDate fromDate, String frequency) {
        if (frequency == null) return fromDate.plusMonths(1);
        switch (frequency.toLowerCase()) {
            case "monthly": return YearMonth.from(fromDate).plusMonths(1).atDay(fromDate.getDayOfMonth());
            case "yearly": return fromDate.plusYears(1);
            case "weekly": return fromDate.plusWeeks(1);
            default: return fromDate.plusMonths(1);
        }
    }
}





