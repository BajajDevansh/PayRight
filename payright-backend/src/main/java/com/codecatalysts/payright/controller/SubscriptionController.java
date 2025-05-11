package com.codecatalysts.payright.controller;

import com.codecatalysts.payright.Dto.CancellationLinkDTO;
import com.codecatalysts.payright.Dto.SubscriptionResponseDTO;
import com.codecatalysts.payright.Dto.ToggleWalletPaymentDTO;
import com.codecatalysts.payright.model.Subscription;
import com.codecatalysts.payright.model.User;
import com.codecatalysts.payright.service.SubscriptionService;
import com.codecatalysts.payright.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService appUserService;
    private User getCurrentPayRightUser(org.springframework.security.core.userdetails.User principal) {
        if (principal == null) throw new SecurityException("User not authenticated");
        User user = appUserService.findByUsername(principal.getUsername()); // Use the injected appUserService
        if (user == null) throw new SecurityException("Authenticated user not found in application records");
        return user;
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDTO>> getActiveSubscriptions(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User currentUser = getCurrentPayRightUser(principal);
        List<Subscription> subscriptions = subscriptionService.getActiveSubscriptionsForUser(currentUser.getId());
        List<SubscriptionResponseDTO> responseDTOs = subscriptions.stream()
                .map(SubscriptionResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PathVariable String id) {
        User currentUser = getCurrentPayRightUser(principal);
        Subscription subscription = subscriptionService.getSubscriptionById(currentUser.getId(), id);
        return ResponseEntity.ok(SubscriptionResponseDTO.fromEntity(subscription));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDTO> updateSubscription(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PathVariable String id,
            @Valid @RequestBody Subscription subscriptionDetails) {
        User currentUser = getCurrentPayRightUser(principal);
        Subscription updatedSubscription = subscriptionService.updateSubscription(currentUser.getId(), id, subscriptionDetails);
        return ResponseEntity.ok(SubscriptionResponseDTO.fromEntity(updatedSubscription));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PathVariable String id) {
        User currentUser = getCurrentPayRightUser(principal);
        subscriptionService.deleteSubscription(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-renewals")
    public ResponseEntity<String> checkRenewals() {
        subscriptionService.checkUpcomingRenewals();
        return ResponseEntity.ok("Renewal check process initiated.");
    }

    @GetMapping("/{id}/cancellation-link")
    public ResponseEntity<CancellationLinkDTO> getSubscriptionCancellationLink(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PathVariable String id) {
        User currentUser = getCurrentPayRightUser(principal);
        CancellationLinkDTO linkDTO = subscriptionService.getCancellationLink(currentUser.getId(), id);
        return ResponseEntity.ok(linkDTO);
    }

    @PostMapping("/{id}/mark-cancelled")
    public ResponseEntity<SubscriptionResponseDTO> markSubscriptionAsCancelled(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PathVariable String id) {
        User currentUser = getCurrentPayRightUser(principal);
        Subscription cancelledSubscription = subscriptionService.markAsCancelled(currentUser.getId(), id);


        return ResponseEntity.ok(SubscriptionResponseDTO.fromEntity(cancelledSubscription));
    }
    @PostMapping("/{id}/toggle-wallet-payment")
    public ResponseEntity<SubscriptionResponseDTO> toggleSubscriptionWalletPayment(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PathVariable String id,
            @Valid @RequestBody ToggleWalletPaymentDTO toggleRequest) {
        User currentUser = getCurrentPayRightUser(principal);
        Subscription updatedSubscription = subscriptionService.togglePayFromWallet(currentUser.getId(), id, toggleRequest.getEnable());
        return ResponseEntity.ok(SubscriptionResponseDTO.fromEntity(updatedSubscription));
    }
    @PostMapping("/process-due-payments")
    public ResponseEntity<String> processDueWalletPayments() {
        subscriptionService.processSubscriptionPaymentsDue();
        return ResponseEntity.ok("Wallet payment processing for due subscriptions initiated.");
    }
}



