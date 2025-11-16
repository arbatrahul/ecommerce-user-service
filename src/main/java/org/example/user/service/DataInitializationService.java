package org.example.user.service;

import org.example.user.entity.User;
import org.example.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
    }

    private void initializeUsers() {
        // Create admin user if not exists
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@ecommerce.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setRole(User.Role.ADMIN);
            admin.setEnabled(true);
            
            userRepository.save(admin);
            System.out.println("Admin user created: admin/admin123");
        }

        // Create test user if not exists
        if (!userRepository.existsByUsername("testuser")) {
            User testUser = new User();
            testUser.setUsername("testuser");
            testUser.setEmail("testuser@example.com");
            testUser.setPassword(passwordEncoder.encode("password"));
            testUser.setFirstName("Test");
            testUser.setLastName("User");
            testUser.setPhoneNumber("555-1234");
            testUser.setRole(User.Role.USER);
            testUser.setEnabled(true);
            
            userRepository.save(testUser);
            System.out.println("Test user created: testuser/password");
        }

        // Create another test user
        if (!userRepository.existsByUsername("john.doe")) {
            User johnDoe = new User();
            johnDoe.setUsername("john.doe");
            johnDoe.setEmail("john.doe@example.com");
            johnDoe.setPassword(passwordEncoder.encode("password123"));
            johnDoe.setFirstName("John");
            johnDoe.setLastName("Doe");
            johnDoe.setPhoneNumber("555-5678");
            johnDoe.setRole(User.Role.USER);
            johnDoe.setEnabled(true);
            
            userRepository.save(johnDoe);
            System.out.println("User created: john.doe/password123");
        }
    }
}
