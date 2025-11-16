package org.example.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetDto {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    // For reset password request
    public static class PasswordResetRequestDto {
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        private String email;
        
        public PasswordResetRequestDto() {}
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
    }
    
    // For reset password confirmation
    public static class PasswordResetConfirmDto {
        @NotBlank(message = "Reset token is required")
        private String resetToken;
        
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;
        
        public PasswordResetConfirmDto() {}
        
        public String getResetToken() {
            return resetToken;
        }
        
        public void setResetToken(String resetToken) {
            this.resetToken = resetToken;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
    
    // For change password (authenticated user)
    public static class ChangePasswordDto {
        @NotBlank(message = "Current password is required")
        private String currentPassword;
        
        @NotBlank(message = "New password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String newPassword;
        
        public ChangePasswordDto() {}
        
        public String getCurrentPassword() {
            return currentPassword;
        }
        
        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }
        
        public String getNewPassword() {
            return newPassword;
        }
        
        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
    
    // Constructors
    public PasswordResetDto() {}
    
    public PasswordResetDto(String email) {
        this.email = email;
    }
    
    // Getters and Setters
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
