package com.codecatalysts.payright.service;

import com.codecatalysts.payright.Dto.AddFundsRequestDTO;
import com.codecatalysts.payright.exception.InsufficientFundsException;
import com.codecatalysts.payright.exception.ResourceNotFoundException;
import com.codecatalysts.payright.model.User;
import com.codecatalysts.payright.model.Wallet;
import com.codecatalysts.payright.model.WalletTransaction;
import com.codecatalysts.payright.model.WalletTransactionType;
import com.codecatalysts.payright.repositories.UserRepository;
import com.codecatalysts.payright.repositories.WalletRepository;
import com.codecatalysts.payright.repositories.WalletTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {
    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);
    public static final String DEMO_CURRENCY = "USD_DEMO"; // Define a constant for demo currency

    @Autowired
    private WalletRepository walletRepository;
    @Autowired
    private WalletTransactionRepository walletTransactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Wallet createWalletForUser(String userId, BigDecimal initialBalance) {
        if (walletRepository.findByUserId(userId).isPresent()) {
            logger.warn("Wallet already exists for user {}", userId);
            return walletRepository.findByUserId(userId).get();
        }
        Wallet newWallet = new Wallet(userId, initialBalance, DEMO_CURRENCY);
        Wallet savedWallet = walletRepository.save(newWallet);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for wallet creation: " + userId));
        user.setWalletId(savedWallet.getId());
        userRepository.save(user);

        logger.info("Created wallet {} with balance {} {} for user {}", savedWallet.getId(), initialBalance, DEMO_CURRENCY, userId);
        return savedWallet;
    }

    public Wallet getWalletByUserId(String userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user: " + userId));
    }

    @Transactional
    public Wallet addFunds(String userId, AddFundsRequestDTO addFundsRequest) {
        Wallet wallet = getWalletByUserId(userId);
        if (!wallet.getCurrency().equals(addFundsRequest.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch. Wallet currency: " + wallet.getCurrency() + ", Requested: " + addFundsRequest.getCurrency());
        }

        wallet.setBalance(wallet.getBalance().add(addFundsRequest.getAmount()));
        Wallet updatedWallet = walletRepository.save(wallet);

        WalletTransaction creditTx = new WalletTransaction(
                wallet.getId(), userId, WalletTransactionType.CREDIT,
                addFundsRequest.getAmount(), wallet.getCurrency(), "User added funds to demo wallet."
        );
        walletTransactionRepository.save(creditTx);

        logger.info("Added {} {} to wallet {} for user {}. New balance: {}",
                addFundsRequest.getAmount(), wallet.getCurrency(), wallet.getId(), userId, updatedWallet.getBalance());
        notificationService.createNotification(userId,
                String.format("Successfully added %s %s to your demo wallet.", addFundsRequest.getAmount(), wallet.getCurrency()),
                "WALLET_FUNDS_ADDED", null);
        return updatedWallet;
    }

    @Transactional
    public WalletTransaction processDebit(String userId, BigDecimal amount, String description,
                                          String relatedSubscriptionId, String relatedBankTransactionId) throws InsufficientFundsException {
        Wallet wallet = getWalletByUserId(userId);
        if (wallet.getBalance().compareTo(amount) < 0) {
            logger.warn("Insufficient funds in wallet {} for user {} to debit {}. Balance: {}",
                    wallet.getId(), userId, amount, wallet.getBalance());
            notificationService.createNotification(userId,
                    String.format("Failed to pay for '%s'. Insufficient demo wallet balance.", description),
                    "WALLET_PAYMENT_FAILED", relatedSubscriptionId);
            throw new InsufficientFundsException("Insufficient funds in wallet to cover: " + description);
        }

        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        WalletTransaction debitTx = new WalletTransaction(
                wallet.getId(), userId, WalletTransactionType.DEBIT,
                amount, wallet.getCurrency(), description
        );
        debitTx.setRelatedSubscriptionId(relatedSubscriptionId);
        debitTx.setRelatedBankTransactionId(relatedBankTransactionId); // Link to original transaction
        WalletTransaction savedDebitTx = walletTransactionRepository.save(debitTx);

        logger.info("Debited {} {} from wallet {} for user {} for '{}'. New balance: {}",
                amount, wallet.getCurrency(), wallet.getId(), userId, description, wallet.getBalance());
        return savedDebitTx;
    }

    public Page<WalletTransaction> getWalletTransactionsForUser(String userId, Pageable pageable) {
        return walletTransactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
    }
}

