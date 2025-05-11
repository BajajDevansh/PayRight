package com.codecatalysts.payright.controller;

import com.codecatalysts.payright.model.Notification;
import com.codecatalysts.payright.model.User;

import com.codecatalysts.payright.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    @Autowired com.codecatalysts.payright.service.UserService appUserService; // Your UserService

    private User getCurrentPayRightUser(org.springframework.security.core.userdetails.User principal) {
        if (principal == null) throw new SecurityException("User not authenticated");
        User user = appUserService.findByUsername(principal.getUsername());
        if (user == null) throw new SecurityException("Authenticated user not found");
        return user;
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User currentUser = getCurrentPayRightUser(principal);
        List<Notification> notifications = notificationService.getUnreadNotificationsForUser(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(@AuthenticationPrincipal org.springframework.security.core.userdetails.User principal) {
        User currentUser = getCurrentPayRightUser(principal);
        List<Notification> notifications = notificationService.getAllNotificationsForUser(currentUser.getId());
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Notification> markNotificationAsRead(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User principal,
            @PathVariable String id) {
        User currentUser = getCurrentPayRightUser(principal);
        Notification notification = notificationService.markAsRead(currentUser.getId(), id);
        return ResponseEntity.ok(notification);
    }
}