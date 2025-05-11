package com.codecatalysts.payright.service;

import com.codecatalysts.payright.Dto.UserRegistrationDTO;
import com.codecatalysts.payright.exception.UserAlreadyExistsException;
import com.codecatalysts.payright.model.User;
import com.codecatalysts.payright.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Transactional // Ensure this is transactional
    public User registerNewUser(UserRegistrationDTO registrationDTO) {
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new UserAlreadyExistsException("User already exists with username: " + registrationDTO.getUsername());
        }
        User newUser = new User();
        newUser.setUsername(registrationDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        newUser.setFullName(registrationDTO.getFullName());
        User savedUser = userRepository.save(newUser);

        // Create a demo wallet for the new user with an initial balance (e.g., $100 demo cash)
        walletService.createWalletForUser(savedUser.getId(), new BigDecimal("100.00")); // User gets $100 demo cash

        return savedUser; // Return the user, walletId will be set by createWalletForUser
    }


    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}

