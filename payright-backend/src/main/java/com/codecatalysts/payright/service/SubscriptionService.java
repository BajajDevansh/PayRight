package com.codecatalysts.payright.service;

import com.codecatalysts.payright.Dto.CancellationLinkDTO;
import com.codecatalysts.payright.exception.InsufficientFundsException;
import com.codecatalysts.payright.exception.ResourceNotFoundException;
import com.codecatalysts.payright.model.Subscription;
import com.codecatalysts.payright.model.WalletTransactionType;
import com.codecatalysts.payright.repositories.SubscriptionRepository;

import com.codecatalysts.payright.repositories.WalletTransactionRepository;
import com.codecatalysts.payright.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SubscriptionService {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    // subService self-injection should have been removed

    private static final Map<String, String> KNOWN_CANCELLATION_URLS = new HashMap<>();
    static {
        KNOWN_CANCELLATION_URLS.put("netflix", "https://www.netflix.com/cancelplan");
        KNOWN_CANCELLATION_URLS.put("spotify", "https://www.spotify.com/account/subscription/cancel/");
        KNOWN_CANCELLATION_URLS.put("amazon prime video", "https://www.amazon.com/gp/video/settings/channels");
        KNOWN_CANCELLATION_URLS.put("hulu", "https://help.hulu.com/s/article/manage-subscription");
        KNOWN_CANCELLATION_URLS.put("disney+", "https://help.disneyplus.com/csp");
        KNOWN_CANCELLATION_URLS.put("dropbox", "https://www.dropbox.com/account/plan");
        KNOWN_CANCELLATION_URLS.put("icloud", "https://support.apple.com/en-us/HT202039");
    }


    @Transactional
    public void processSubscriptionPaymentsDue() {
        LocalDate today = LocalDate.now();
        List<Subscription> dueSubscriptions = subscriptionRepository
                .findByNextBillingDateBeforeAndActiveTrueAndAutoRenewsTrueAndPayFromWalletTrue(today.plusDays(1));

        logger.info("Found {} subscriptions due for wallet payment processing.", dueSubscriptions.size());

        for (Subscription sub : dueSubscriptions) {
            boolean alreadyPaidThisCycle = walletTransactionRepository
                    .findByUserIdAndRelatedSubscriptionIdAndTypeOrderByTransactionDateDesc(sub.getUserId(), sub.getId(), WalletTransactionType.DEBIT)
                    .stream()
                    .anyMatch(wt -> !wt.getTransactionDate().toLocalDate().isBefore(sub.getNextBillingDate().minusMonths(1)));

            if ((sub.getNextBillingDate().isEqual(today) || sub.getNextBillingDate().isBefore(today)) && !alreadyPaidThisCycle) {
                try {
                    logger.info("Attempting wallet payment for subscription: {} (User: {})", sub.getName(), sub.getUserId());
                    walletService.processDebit(
                            sub.getUserId(),
                            sub.getAmount(),
                            "Payment for " + sub.getName() + " subscription",
                            sub.getId(),
                            null
                    );

                    LocalDate newNextBillingDate = DateUtil.calculateNextBillingDate(sub.getNextBillingDate(), sub.getFrequency());
                    sub.setNextBillingDate(newNextBillingDate);
                    subscriptionRepository.save(sub);

                    notificationService.createNotification(sub.getUserId(),
                            String.format("Successfully paid for %s from your demo wallet.", sub.getName()),
                            "WALLET_PAYMENT_SUCCESS", sub.getId());

                } catch (InsufficientFundsException e) {
                    logger.warn("Insufficient funds for subscription {} (User: {}). Payment skipped.", sub.getName(), sub.getUserId());
                } catch (Exception e) {
                    logger.error("Error processing wallet payment for subscription {} (User: {}): {}", sub.getName(), sub.getUserId(), e.getMessage(), e);
                }
            } else if (alreadyPaidThisCycle) {
                logger.info("Subscription {} (User: {}) already paid this cycle. Skipping wallet payment.", sub.getName(), sub.getUserId());
            } else {
                logger.info("Subscription {} (User: {}) not due yet (Next: {}). Skipping wallet payment.", sub.getName(), sub.getUserId(), sub.getNextBillingDate());
            }
        }
    }

    @Transactional
    public Subscription togglePayFromWallet(String userId, String subscriptionId, boolean enable) {
        Subscription subscription = getSubscriptionById(userId, subscriptionId);
        if (!subscription.isActive() && enable) {
            throw new IllegalStateException("Cannot enable wallet payment for an inactive subscription.");
        }
        subscription.setPayFromWallet(enable);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        logger.info("User {} set 'payFromWallet' to {} for subscription {}", userId, enable, subscriptionId);
        String status = enable ? "enabled" : "disabled";
        notificationService.createNotification(userId,
                "Payment from demo wallet has been " + status + " for " + subscription.getName() + ".",
                "SUBSCRIPTION_WALLET_PREF_CHANGED", subscriptionId);
        return updatedSubscription;
    }


    public List<Subscription> getActiveSubscriptionsForUser(String userId) {
        return subscriptionRepository.findByUserIdAndActiveTrueOrderByNextBillingDateAsc(userId);
    }

    public Subscription getSubscriptionById(String userId, String subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .filter(sub -> sub.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id " + subscriptionId + " for user " + userId));
    }

    @Transactional
    public Subscription updateSubscription(String userId, String subscriptionId, Subscription subscriptionDetails) {
        Subscription subscription = getSubscriptionById(userId, subscriptionId);
        subscription.setName(subscriptionDetails.getName());
        subscription.setAmount(subscriptionDetails.getAmount());
        subscription.setCurrency(subscriptionDetails.getCurrency());
        subscription.setFrequency(subscriptionDetails.getFrequency());
        subscription.setNextBillingDate(subscriptionDetails.getNextBillingDate());
        subscription.setCategory(subscriptionDetails.getCategory());
        subscription.setNotes(subscriptionDetails.getNotes());
        subscription.setActive(subscriptionDetails.isActive());
        subscription.setAutoRenews(subscriptionDetails.isAutoRenews());
        if (subscriptionDetails.getCancellationUrl() != null && !subscriptionDetails.getCancellationUrl().isBlank()) {
            subscription.setCancellationUrl(subscriptionDetails.getCancellationUrl());
        }
        if (subscriptionDetails.isPayFromWallet() != subscription.isPayFromWallet()) {
            subscription.setPayFromWallet(subscriptionDetails.isPayFromWallet());
        }
        Subscription updatedSubscription = subscriptionRepository.save(subscription);
        logger.info("Updated subscription {} for user {}", updatedSubscription.getId(), userId);
        return updatedSubscription;
    }

    @Transactional
    public void deleteSubscription(String userId, String subscriptionId) {
        Subscription subscription = getSubscriptionById(userId, subscriptionId);
        subscriptionRepository.delete(subscription);
        logger.info("Deleted subscription {} for user {}", subscriptionId, userId);
    }

    public void checkUpcomingRenewals() {
        LocalDate today = LocalDate.now();
        LocalDate oneWeekFromNow = today.plusDays(7);
        List<Subscription> upcomingRenewals = subscriptionRepository
                .findByNextBillingDateBeforeAndActiveTrueAndAutoRenewsTrue(oneWeekFromNow);
        logger.info("Found {} subscriptions with upcoming renewals.", upcomingRenewals.size());
        for (Subscription sub : upcomingRenewals) {

            if (sub.isActive() && sub.isAutoRenews() && !sub.getNextBillingDate().isBefore(today)) {

                boolean notificationRecentlySent = notificationService.getNotificationsForSubscriptionByType(sub.getId(), "RENEWAL_ALERT")
                        .stream()
                        .anyMatch(n -> n.getCreatedAt().toLocalDate().isAfter(today.minusDays(3))); // e.g., within last 3 days

                if (!notificationRecentlySent) {
                    String message = String.format(
                            "Upcoming renewal for %s on %s for %s %s.",
                            sub.getName(), sub.getNextBillingDate(), sub.getAmount(), sub.getCurrency()
                    );
                    notificationService.createNotification(sub.getUserId(), message, "RENEWAL_ALERT", sub.getId());
                }
            }
        }
    }



    public CancellationLinkDTO getCancellationLink(String userId, String subscriptionId) {
        Subscription subscription = getSubscriptionById(userId, subscriptionId);
        String url = subscription.getCancellationUrl();
        String message;

        if (url == null || url.isBlank()) {
            String subNameLower = subscription.getName().toLowerCase();
            for (Map.Entry<String, String> entry : KNOWN_CANCELLATION_URLS.entrySet()) {
                if (subNameLower.contains(entry.getKey())) {
                    url = entry.getValue();
                    break;
                }
            }
        }

        if (url == null || url.isBlank()) {
            message = "Cancellation URL not found for this subscription. Please search for '" + subscription.getName() + " cancellation' on the web.";
            logger.warn("Cancellation URL not found for subscription id: {}, name: {}", subscription.getId(), subscription.getName());
        } else {
            message = "You will be redirected to the cancellation page. After cancelling, please mark this subscription as 'cancelled' in PayRight.";
        }
        return new CancellationLinkDTO(subscription.getId(), subscription.getName(), url, message);
    }

    @Transactional
    public Subscription markAsCancelled(String userId, String subscriptionId) {
        Subscription subscription = getSubscriptionById(userId, subscriptionId);
        if (!subscription.isActive()) {
            logger.warn("Subscription {} for user {} is already inactive.", subscriptionId, userId);
            return subscription;
        }
        subscription.setActive(false);
        subscription.setCancellationDate(LocalDate.now());

        subscription.setAutoRenews(false);
        Subscription savedSubscription = subscriptionRepository.save(subscription);
        logger.info("Subscription {} for user {} marked as cancelled.", savedSubscription.getId(), userId);
        notificationService.createNotification(userId,
                "Subscription '" + savedSubscription.getName() + "' has been marked as cancelled.",
                "SUBSCRIPTION_CANCELLED",
                savedSubscription.getId());
        return savedSubscription;
    }

    public void findAndSetCancellationUrl(Subscription subscription) {
        if (subscription == null || subscription.getName() == null || (subscription.getCancellationUrl() != null && !subscription.getCancellationUrl().isBlank())) {
            return;
        }
        String subNameLower = subscription.getName().toLowerCase();
        for (Map.Entry<String, String> entry : KNOWN_CANCELLATION_URLS.entrySet()) {
            if (subNameLower.contains(entry.getKey())) {
                subscription.setCancellationUrl(entry.getValue());
                logger.debug("Auto-set cancellation URL for {} to: {}", subscription.getName(), entry.getValue());
                return;
            }
        }
        logger.debug("Could not auto-find cancellation URL for: {}", subscription.getName());
    }
}