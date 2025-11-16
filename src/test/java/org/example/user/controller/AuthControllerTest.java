package org.example.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.user.dto.LoginDto;
import org.example.user.dto.UserRegistrationDto;
import org.example.user.entity.User;
import org.example.user.security.JwtTokenProvider;
import org.example.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRegistrationDto registrationDto;
    private LoginDto loginDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");

        loginDto = new LoginDto();
        loginDto.setUsernameOrEmail("testuser");
        loginDto.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(User.Role.USER);
    }

    @Test
    void registerUser_Success() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationDto.class))).thenReturn(user);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(userService).registerUser(any(UserRegistrationDto.class));
    }

    @Test
    void registerUser_ValidationError() throws Exception {
        // Given
        registrationDto.setUsername(""); // Invalid username

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerUser_UsernameAlreadyExists() throws Exception {
        // Given
        when(userService.registerUser(any(UserRegistrationDto.class)))
                .thenThrow(new RuntimeException("Username is already taken!"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    @Test
    void registerUserWithSocialMedia_Success() throws Exception {
        // Given
        registrationDto.setProvider("google");
        registrationDto.setProviderId("google123");
        registrationDto.setProfileImageUrl("https://example.com/image.jpg");
        
        when(userService.registerUserWithSocialMedia(any(UserRegistrationDto.class))).thenReturn(user);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/register/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully with google"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

        verify(userService).registerUserWithSocialMedia(any(UserRegistrationDto.class));
        verify(tokenProvider).generateToken(any(Authentication.class));
    }

    @Test
    void registerUserWithSocialMedia_MissingProvider() throws Exception {
        // Given
        registrationDto.setProviderId("google123"); // Missing provider

        // When & Then
        mockMvc.perform(post("/api/auth/register/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Provider and provider ID are required for social media registration"));
    }

    @Test
    void loginUser_Success() throws Exception {
        // Given
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User authenticated successfully"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(tokenProvider).generateToken(any(Authentication.class));
    }

    @Test
    void loginUser_InvalidCredentials() throws Exception {
        // Given
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username/email or password"));
    }

    @Test
    void loginWithSocialMedia_Success() throws Exception {
        // Given
        Map<String, String> socialLoginData = new HashMap<>();
        socialLoginData.put("provider", "google");
        socialLoginData.put("providerId", "google123");
        
        when(userService.findByProviderAndProviderId("google", "google123")).thenReturn(user);
        when(tokenProvider.generateToken(any(Authentication.class))).thenReturn("jwt-token");

        // When & Then
        mockMvc.perform(post("/api/auth/login/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socialLoginData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User authenticated successfully with google"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token"));

        verify(userService).findByProviderAndProviderId("google", "google123");
        verify(tokenProvider).generateToken(any(Authentication.class));
    }

    @Test
    void loginWithSocialMedia_UserNotFound() throws Exception {
        // Given
        Map<String, String> socialLoginData = new HashMap<>();
        socialLoginData.put("provider", "google");
        socialLoginData.put("providerId", "google123");
        
        when(userService.findByProviderAndProviderId("google", "google123")).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/auth/login/social")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(socialLoginData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found. Please register first."));
    }

    @Test
    void checkUsernameAvailability_Available() throws Exception {
        // Given
        when(userService.existsByUsername("newuser")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/check-username")
                .param("username", "newuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Username is available"));
    }

    @Test
    void checkUsernameAvailability_NotAvailable() throws Exception {
        // Given
        when(userService.existsByUsername("testuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/auth/check-username")
                .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false))
                .andExpect(jsonPath("$.message").value("Username is already taken"));
    }

    @Test
    void checkEmailAvailability_Available() throws Exception {
        // Given
        when(userService.existsByEmail("new@example.com")).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/auth/check-email")
                .param("email", "new@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.message").value("Email is available"));
    }
}
