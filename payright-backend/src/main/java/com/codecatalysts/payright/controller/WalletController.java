package com.codecatalysts.payright.controller;

import com.codecatalysts.payright.Dto.AddFundsRequestDTO;
import com.codecatalysts.payright.Dto.WalletDTO;
import com.codecatalysts.payright.Dto.WalletTransactionDTO;
import com.codecatalysts.payright.model.User;
import com.codecatalysts.payright.model.Wallet;
import com.codecatalysts.payright.model.WalletTransaction;
import com.codecatalysts.payright.service.UserService;
import com.codecatalysts.payright.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    @Autowired
    private WalletService walletService;
    @Autowired
    private UserService appUserService;

    private User getCurrentPayRightUser(org.springframework.security.core.userdetails.User principal) {
        if (principal == null) throw new SecurityException("User not authenticated");
        User user = appUserService.findByUsername(principal.getUsername());
        if (user == null) throw new SecurityException("Authenticated user not found");
        return user;
    }

    @GetMapping
    public ResponseEntity<WalletDTO> getMyWallet(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User currentUser = getCurrentPayRightUser(principal);
        Wallet wallet = walletService.getWalletByUserId(currentUser.getId());
        return ResponseEntity.ok(WalletDTO.fromEntity(wallet));
    }

    @PostMapping("/add-funds")
    public ResponseEntity<WalletDTO> addFundsToWallet(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @Valid @RequestBody AddFundsRequestDTO addFundsRequest) {
        User currentUser = getCurrentPayRightUser(principal);
        Wallet updatedWallet = walletService.addFunds(currentUser.getId(), addFundsRequest);
        return ResponseEntity.ok(WalletDTO.fromEntity(updatedWallet));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<WalletTransactionDTO>> getMyWalletTransactions(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        User currentUser = getCurrentPayRightUser(principal);
        Page<WalletTransaction> transactionsPage = walletService.getWalletTransactionsForUser(currentUser.getId(), pageable);
        return ResponseEntity.ok(transactionsPage.map(WalletTransactionDTO::fromEntity));
    }
}
