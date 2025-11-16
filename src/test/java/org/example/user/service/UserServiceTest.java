package org.example.user.service;

import org.example.user.dto.UserRegistrationDto;
import org.example.user.entity.User;
import org.example.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto registrationDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setPassword("password123");
        registrationDto.setFirstName("Test");
        registrationDto.setLastName("User");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(User.Role.USER);
    }

    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.registerUser(registrationDto);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("user-events"), eq("user-registered"), any());
    }

    @Test
    void registerUser_UsernameAlreadyExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.registerUser(registrationDto));
        assertEquals("Username is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_EmailAlreadyExists() {
        // Given
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.registerUser(registrationDto));
        assertEquals("Email is already in use!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserWithSocialMedia_Success() {
        // Given
        registrationDto.setProvider("google");
        registrationDto.setProviderId("google123");
        registrationDto.setProfileImageUrl("https://example.com/image.jpg");
        
        when(userRepository.findByProviderAndProviderId(anyString(), anyString())).thenReturn(null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        User result = userService.registerUserWithSocialMedia(registrationDto);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
        verify(kafkaTemplate).send(eq("user-events"), eq("user-registered-social"), any());
    }

    @Test
    void registerUserWithSocialMedia_AlreadyExists() {
        // Given
        registrationDto.setProvider("google");
        registrationDto.setProviderId("google123");
        
        when(userRepository.findByProviderAndProviderId(anyString(), anyString())).thenReturn(user);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.registerUserWithSocialMedia(registrationDto));
        assertTrue(exception.getMessage().contains("already registered"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByProviderAndProviderId_Success() {
        // Given
        when(userRepository.findByProviderAndProviderId("google", "google123")).thenReturn(user);

        // When
        User result = userService.findByProviderAndProviderId("google", "google123");

        // Then
        assertNotNull(result);
        assertEquals(user, result);
        verify(userRepository).findByProviderAndProviderId("google", "google123");
    }

    @Test
    void findByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void findByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void existsByUsername_True() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertTrue(result);
        verify(userRepository).existsByUsername("testuser");
    }

    @Test
    void existsByEmail_True() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
        verify(userRepository).existsByEmail("test@example.com");
    }

    @Test
    void generateUsernameFromSocialMedia_UniqueUsername() {
        // Given
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setProvider("google");
        
        when(userRepository.existsByUsername("johndoe")).thenReturn(false);

        // When
        String result = userService.generateUsernameFromSocialMedia(registrationDto);

        // Then
        assertEquals("johndoe", result);
    }

    @Test
    void generateUsernameFromSocialMedia_ConflictResolution() {
        // Given
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setProvider("google");
        
        when(userRepository.existsByUsername("johndoe")).thenReturn(true);
        when(userRepository.existsByUsername("johndoegoogle1")).thenReturn(false);

        // When
        String result = userService.generateUsernameFromSocialMedia(registrationDto);

        // Then
        assertEquals("johndoegoogle1", result);
    }
}
