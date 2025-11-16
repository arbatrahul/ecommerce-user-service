package org.example.user.controller;

import org.example.user.dto.*;
import org.example.user.entity.User;
import org.example.user.security.JwtTokenProvider;
import org.example.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User user = userService.registerUser(registrationDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", new UserDto(user));
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register/social")
    public ResponseEntity<Map<String, Object>> registerUserWithSocialMedia(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            // Validate social media registration
            if (registrationDto.getProvider() == null || registrationDto.getProviderId() == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Provider and provider ID are required for social media registration");
                return ResponseEntity.badRequest().body(response);
            }
            
            User user = userService.registerUserWithSocialMedia(registrationDto);
            
            // Generate JWT token for immediate login
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            String jwt = tokenProvider.generateToken(authentication);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully with " + registrationDto.getProvider());
            response.put("user", new UserDto(user));
            response.put("accessToken", jwt);
            response.put("tokenType", "Bearer");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login/social")
    public ResponseEntity<Map<String, Object>> loginWithSocialMedia(@RequestBody Map<String, String> socialLoginData) {
        try {
            String provider = socialLoginData.get("provider");
            String providerId = socialLoginData.get("providerId");
            
            if (provider == null || providerId == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Provider and provider ID are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            User user = userService.findByProviderAndProviderId(provider, providerId);
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "User not found. Please register first.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Generate JWT token
            Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            String jwt = tokenProvider.generateToken(authentication);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User authenticated successfully with " + provider);
            response.put("accessToken", jwt);
            response.put("tokenType", "Bearer");
            response.put("user", new UserDto(user));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Social media authentication failed: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginDto.getUsernameOrEmail(),
                    loginDto.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);
            
            User user = (User) authentication.getPrincipal();
            UserDto userDto = new UserDto(user);

            JwtAuthenticationResponse jwtResponse = new JwtAuthenticationResponse(jwt, userDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User authenticated successfully");
            response.put("accessToken", jwt);
            response.put("tokenType", "Bearer");
            response.put("user", userDto);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid username/email or password");
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@Valid @RequestBody PasswordResetDto.PasswordResetRequestDto resetRequestDto) {
        try {
            userService.initiatePasswordReset(resetRequestDto.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset email sent successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody PasswordResetDto.PasswordResetConfirmDto resetDto) {
        try {
            userService.resetPassword(resetDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check-username")
    public ResponseEntity<Map<String, Object>> checkUsernameAvailability(@RequestParam String username) {
        boolean isAvailable = !userService.existsByUsername(username);
        
        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        response.put("message", isAvailable ? "Username is available" : "Username is already taken");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmailAvailability(@RequestParam String email) {
        boolean isAvailable = !userService.existsByEmail(email);
        
        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        response.put("message", isAvailable ? "Email is available" : "Email is already in use");
        
        return ResponseEntity.ok(response);
    }
}
