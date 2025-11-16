package org.example.user.repository;

import org.example.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByResetToken(String resetToken);
    
    List<User> findByResetTokenExpiryBefore(LocalDateTime dateTime);
    
    List<User> findByRole(User.Role role);
    
    List<User> findByEnabledTrue();
    
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Social media authentication methods
    User findByProviderAndProviderId(String provider, String providerId);
    
    List<User> findByProvider(String provider);
    
    boolean existsByProviderAndProviderId(String provider, String providerId);
}
