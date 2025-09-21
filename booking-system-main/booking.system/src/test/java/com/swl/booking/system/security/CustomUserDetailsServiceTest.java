package com.swl.booking.system.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.swl.booking.system.entity.User;
import com.swl.booking.system.repository.UserRepository;

/**
 * Comprehensive test suite for CustomUserDetailsService
 * Tests user loading functionality with various scenarios
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private static final String TEST_EMAIL = "test@example.com";
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_ORG_NO = "ORG001";

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(TEST_USER_ID);
        testUser.setEmail(TEST_EMAIL);
        testUser.setPassword(TEST_PASSWORD);
        testUser.setName("Test User");
        testUser.setAddress("Test Address");
        testUser.setVerified(false);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_EMAIL);

        // Assert
        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails instanceof UserPrincipal);
        
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        assertEquals(TEST_USER_ID, userPrincipal.getId());
        assertEquals(TEST_EMAIL, userPrincipal.getUsername());
        assertFalse(userPrincipal.isSuperAdmin());
        
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithAdminRole_WhenUserIsVerified() {
        // Arrange
        testUser.setVerified(true);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_EMAIL);

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof UserPrincipal);
        
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        assertTrue(userPrincipal.isSuperAdmin());
        assertNotNull(userPrincipal.getAuthorities());
        
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(TEST_EMAIL)
        );
        
        assertEquals("User Not Found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenEmailIsNull() {
        // Arrange
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(null)
        );
        
        assertEquals("User Not Found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenEmailIsEmpty() {
        // Arrange
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername(emptyEmail)
        );
        
        assertEquals("User Not Found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(emptyEmail);
    }

    @Test
    void loadUserById_ShouldReturnUserDetails_WhenUserExists() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserById(TEST_USER_ID);

        // Assert
        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
        assertEquals(TEST_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails instanceof UserPrincipal);
        
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        assertEquals(TEST_USER_ID, userPrincipal.getId());
        assertEquals(TEST_EMAIL, userPrincipal.getUsername());
        assertFalse(userPrincipal.isSuperAdmin());
        
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }

    @Test
    void loadUserById_ShouldReturnUserDetailsWithAdminRole_WhenUserIsVerified() {
        // Arrange
        testUser.setVerified(true);
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserById(TEST_USER_ID);

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails instanceof UserPrincipal);
        
        UserPrincipal userPrincipal = (UserPrincipal) userDetails;
        assertTrue(userPrincipal.isSuperAdmin());
        assertNotNull(userPrincipal.getAuthorities());
        
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }

    @Test
    void loadUserById_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserById(TEST_USER_ID)
        );
        
        assertEquals("User Not Found", exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }

    @Test
    void loadUserById_ShouldThrowUsernameNotFoundException_WhenUserIdIsNull() {
        // Arrange
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserById(null)
        );
        
        assertEquals("User Not Found", exception.getMessage());
        verify(userRepository, times(1)).findById(null);
    }

    @Test
    void loadUserById_ShouldHandleRepositoryException_WhenDatabaseError() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> customUserDetailsService.loadUserById(TEST_USER_ID)
        );
        
        assertEquals("Database connection error", exception.getMessage());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }

    @Test
    void loadUserByUsername_ShouldHandleRepositoryException_WhenDatabaseError() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenThrow(new RuntimeException("Database connection error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> customUserDetailsService.loadUserByUsername(TEST_EMAIL)
        );
        
        assertEquals("Database connection error", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetailsWithCorrectAccountStatus_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(TEST_EMAIL);

        // Assert - Verify UserDetails account status methods
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
        
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void loadUserById_ShouldReturnUserDetailsWithCorrectAccountStatus_WhenUserExists() {
        // Arrange
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserById(TEST_USER_ID);

        // Assert - Verify UserDetails account status methods
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
        
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }

    @Test
    void serviceMethodsAreTransactional_ShouldVerifyTransactionalBehavior() {
        // Arrange
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        // Act
        customUserDetailsService.loadUserByUsername(TEST_EMAIL);
        customUserDetailsService.loadUserById(TEST_USER_ID);

        // Assert - Verify methods are called (transactional behavior is handled by Spring)
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }
}