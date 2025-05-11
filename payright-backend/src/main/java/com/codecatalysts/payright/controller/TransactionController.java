package com.codecatalysts.payright.controller;

import com.codecatalysts.payright.model.Transaction;
import com.codecatalysts.payright.model.User;
import com.codecatalysts.payright.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<Transaction>> getUserTransactions(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User currentUser = getCurrentPayRightUser(principal);
        List<Transaction> transactions = transactionService.getTransactionsForUser(currentUser.getId());
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/sync-mock")
    public ResponseEntity<List<Transaction>> syncMockTransactions(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User currentUser = getCurrentPayRightUser(principal);
        List<Transaction> transactions = transactionService.loadAndProcessMockTransactions(currentUser.getId());
        return ResponseEntity.ok(transactions);
    }
    @Autowired
    com.codecatalysts.payright.service.UserService appUserService;
    private User getCurrentPayRightUser(org.springframework.security.core.userdetails.User principal){
        if (principal == null) throw new SecurityException("User not authenticated");
        User user=appUserService.findByUsername(principal.getUsername());
        if (user == null) throw new SecurityException("Authenticated user not found in application records");
        return user;
    }
}
