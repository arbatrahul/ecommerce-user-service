package org.example.user.service;

import org.example.user.dto.*;
import org.example.user.entity.User;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail));
        
        return user;
    }

    public User registerUser(UserRegistrationDto registrationDto) {
        // Check if username already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);
        
        // Send user registration event to Kafka
        kafkaTemplate.send("user-events", "user-registered", 
            new UserEvent(savedUser.getId(), "USER_REGISTERED", savedUser.getEmail()));
        
        return savedUser;
    }

    public User registerUserWithSocialMedia(UserRegistrationDto registrationDto) {
        // Check if user already exists with this social media provider
        User existingUser = userRepository.findByProviderAndProviderId(
            registrationDto.getProvider(), registrationDto.getProviderId());
        
        if (existingUser != null) {
            throw new RuntimeException("User already registered with this " + registrationDto.getProvider() + " account!");
        }

        // Check if email already exists (for different provider)
        if (registrationDto.getEmail() != null && userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email is already in use with a different account!");
        }

        // Create new user
        User user = new User();
        user.setUsername(generateUsernameFromSocialMedia(registrationDto));
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password for social media users
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhoneNumber(registrationDto.getPhoneNumber());
        user.setProvider(registrationDto.getProvider());
        user.setProviderId(registrationDto.getProviderId());
        user.setProfileImageUrl(registrationDto.getProfileImageUrl());
        user.setRole(User.Role.USER);

        User savedUser = userRepository.save(user);
        
        // Send user registration event to Kafka
        kafkaTemplate.send("user-events", "user-registered-social", 
            new UserEvent(savedUser.getId(), "USER_REGISTERED_SOCIAL", savedUser.getEmail(), registrationDto.getProvider()));
        
        return savedUser;
    }

    public User findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    String generateUsernameFromSocialMedia(UserRegistrationDto registrationDto) {
        String baseUsername = registrationDto.getFirstName().toLowerCase() + 
                             registrationDto.getLastName().toLowerCase();
        
        // Remove spaces and special characters
        baseUsername = baseUsername.replaceAll("[^a-zA-Z0-9]", "");
        
        // Check if username exists, if so, append provider and number
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + registrationDto.getProvider().toLowerCase() + counter;
            counter++;
        }
        
        return username;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User updateUser(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Check if new username is already taken by another user
        if (!user.getUsername().equals(userDto.getUsername()) && 
            userRepository.existsByUsername(userDto.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        // Check if new email is already taken by another user
        if (!user.getEmail().equals(userDto.getEmail()) && 
            userRepository.existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setPhoneNumber(userDto.getPhoneNumber());

        User updatedUser = userRepository.save(user);
        
        // Send user updated event to Kafka
        kafkaTemplate.send("user-events", "user-updated", 
            new UserEvent(updatedUser.getId(), "USER_UPDATED", updatedUser.getEmail()));
        
        return updatedUser;
    }

    public void changePassword(Long userId, PasswordResetDto.ChangePasswordDto changePasswordDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Verify current password
        if (!passwordEncoder.matches(changePasswordDto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect!");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
        
        // Send password changed event to Kafka
        kafkaTemplate.send("user-events", "password-changed", 
            new UserEvent(user.getId(), "PASSWORD_CHANGED", user.getEmail()));
    }

    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(24)); // Token expires in 24 hours

        userRepository.save(user);
        
        // Send password reset event to Kafka (for email notification)
        kafkaTemplate.send("notification-events", "password-reset-requested", 
            new PasswordResetEvent(user.getId(), user.getEmail(), resetToken));
    }

    public void resetPassword(PasswordResetDto.PasswordResetConfirmDto resetDto) {
        User user = userRepository.findByResetToken(resetDto.getResetToken())
                .orElseThrow(() -> new RuntimeException("Invalid reset token!"));

        // Check if token is expired
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired!");
        }

        // Update password and clear reset token
        user.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
        
        // Send password reset completed event to Kafka
        kafkaTemplate.send("user-events", "password-reset-completed", 
            new UserEvent(user.getId(), "PASSWORD_RESET_COMPLETED", user.getEmail()));
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    public List<UserDto> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role).stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setEnabled(true);
        userRepository.save(user);
        
        // Send user enabled event to Kafka
        kafkaTemplate.send("user-events", "user-enabled", 
            new UserEvent(user.getId(), "USER_ENABLED", user.getEmail()));
    }

    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setEnabled(false);
        userRepository.save(user);
        
        // Send user disabled event to Kafka
        kafkaTemplate.send("user-events", "user-disabled", 
            new UserEvent(user.getId(), "USER_DISABLED", user.getEmail()));
    }

    // Clean up expired reset tokens (can be scheduled)
    public void cleanupExpiredResetTokens() {
        List<User> usersWithExpiredTokens = userRepository.findByResetTokenExpiryBefore(LocalDateTime.now());
        for (User user : usersWithExpiredTokens) {
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);
        }
    }

    // Inner classes for Kafka events
    public static class UserEvent {
        private Long userId;
        private String eventType;
        private String email;
        private String provider;
        private LocalDateTime timestamp;
        
        public UserEvent(Long userId, String eventType, String email) {
            this.userId = userId;
            this.eventType = eventType;
            this.email = email;
            this.timestamp = LocalDateTime.now();
        }
        
        public UserEvent(Long userId, String eventType, String email, String provider) {
            this.userId = userId;
            this.eventType = eventType;
            this.email = email;
            this.provider = provider;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }

    public static class PasswordResetEvent {
        private Long userId;
        private String email;
        private String resetToken;
        private LocalDateTime timestamp;
        
        public PasswordResetEvent(Long userId, String email, String resetToken) {
            this.userId = userId;
            this.email = email;
            this.resetToken = resetToken;
            this.timestamp = LocalDateTime.now();
        }
        
        // Getters and setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getResetToken() { return resetToken; }
        public void setResetToken(String resetToken) { this.resetToken = resetToken; }
        
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    }
}
