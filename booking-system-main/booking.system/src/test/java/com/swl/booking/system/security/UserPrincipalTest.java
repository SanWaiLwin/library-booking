package com.swl.booking.system.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.swl.booking.system.entity.User;

/**
 * Comprehensive test suite for UserPrincipal
 * Tests user principal functionality for Spring Security integration
 */
@ExtendWith(MockitoExtension.class)
class UserPrincipalTest {

    @Mock
    private User mockUser;

    private UserPrincipal userPrincipal;
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "John Doe";
    private static final String TEST_ADDRESS = "123 Test Street";

    @BeforeEach
    void setUp() {
        // Setup common mock behavior with lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(mockUser.getId()).thenReturn(TEST_USER_ID);
        lenient().when(mockUser.getEmail()).thenReturn(TEST_EMAIL);
        lenient().when(mockUser.getPassword()).thenReturn(TEST_PASSWORD);
        lenient().when(mockUser.getName()).thenReturn(TEST_NAME);
        lenient().when(mockUser.getAddress()).thenReturn(TEST_ADDRESS);
        lenient().when(mockUser.isVerified()).thenReturn(true);
    }

    @Test
    void create_ShouldCreateUserPrincipal_WithValidUser() {
        // Act
        userPrincipal = UserPrincipal.create(mockUser);

        // Assert
        assertNotNull(userPrincipal);
        assertEquals(TEST_USER_ID, userPrincipal.getId());
        assertEquals(TEST_EMAIL, userPrincipal.getUsername());
        assertEquals(TEST_PASSWORD, userPrincipal.getPassword());
        assertTrue(userPrincipal.isSuperAdmin());
    }

    @Test
    void create_ShouldThrowException_WhenUserIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            UserPrincipal.create(null);
        });
    }

    @Test
    void getAuthorities_ShouldReturnUserName_WhenUserIsNotVerified() {
        // Arrange
        when(mockUser.isVerified()).thenReturn(false);
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority(TEST_NAME)));
    }

    @Test
    void getAuthorities_ShouldReturnEmptyList_WhenUserIsVerified() {
        // Arrange
        when(mockUser.isVerified()).thenReturn(true);
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();

        // Assert
        assertNotNull(authorities);
        assertEquals(0, authorities.size());
    }

    @Test
    void isSuperAdmin_ShouldReturnVerificationStatus() {
        // Arrange
        when(mockUser.isVerified()).thenReturn(true);
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isSuperAdmin = userPrincipal.isSuperAdmin();

        // Assert
        assertTrue(isSuperAdmin);
    }

    @Test
    void isSuperAdmin_ShouldReturnFalse_WhenUserIsNotVerified() {
        // Arrange
        when(mockUser.isVerified()).thenReturn(false);
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isSuperAdmin = userPrincipal.isSuperAdmin();

        // Assert
        assertFalse(isSuperAdmin);
    }

    @Test
    void getPassword_ShouldReturnUserPassword() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        String password = userPrincipal.getPassword();

        // Assert
        assertEquals(TEST_PASSWORD, password);
    }

    @Test
    void isAccountNonExpired_ShouldReturnTrue_Always() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isAccountNonExpired = userPrincipal.isAccountNonExpired();

        // Assert
        assertTrue(isAccountNonExpired);
    }

    @Test
    void isAccountNonLocked_ShouldReturnTrue_Always() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isAccountNonLocked = userPrincipal.isAccountNonLocked();

        // Assert
        assertTrue(isAccountNonLocked);
    }

    @Test
    void isCredentialsNonExpired_ShouldReturnTrue_Always() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isCredentialsNonExpired = userPrincipal.isCredentialsNonExpired();

        // Assert
        assertTrue(isCredentialsNonExpired);
    }

    @Test
    void isEnabled_ShouldAlwaysReturnTrue() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isEnabled = userPrincipal.isEnabled();

        // Assert
        assertTrue(isEnabled);
    }

    @Test
    void isAccountNonExpired_ShouldAlwaysReturnTrue() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isAccountNonExpired = userPrincipal.isAccountNonExpired();

        // Assert
        assertTrue(isAccountNonExpired);
    }

    @Test
    void equals_ShouldReturnTrue_WhenComparingWithSameUserPrincipal() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);
        UserPrincipal otherUserPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isEqual = userPrincipal.equals(otherUserPrincipal);

        // Assert
        assertTrue(isEqual);
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingWithDifferentUserPrincipal() {
        // Arrange
        User otherUser = mock(User.class);
        lenient().when(otherUser.getId()).thenReturn(2L);
        lenient().when(otherUser.getEmail()).thenReturn("other@example.com");
        lenient().when(otherUser.getPassword()).thenReturn("otherpassword");
        lenient().when(otherUser.getName()).thenReturn("Jane Smith");
        lenient().when(otherUser.getAddress()).thenReturn("456 Other Street");
        lenient().when(otherUser.isVerified()).thenReturn(true);

        userPrincipal = UserPrincipal.create(mockUser);
        UserPrincipal otherUserPrincipal = UserPrincipal.create(otherUser);

        // Act
        boolean isEqual = userPrincipal.equals(otherUserPrincipal);

        // Assert
        assertFalse(isEqual);
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingWithNull() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isEqual = userPrincipal.equals(null);

        // Assert
        assertFalse(isEqual);
    }

    @Test
    void equals_ShouldReturnFalse_WhenComparingWithDifferentClass() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);
        String otherObject = "not a UserPrincipal";

        // Act
        boolean isEqual = userPrincipal.equals(otherObject);

        // Assert
        assertFalse(isEqual);
    }

    @Test
    void equals_ShouldReturnTrue_WhenComparingWithSelf() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        boolean isEqual = userPrincipal.equals(userPrincipal);

        // Assert
        assertTrue(isEqual);
    }

    @Test
    void hashCode_ShouldBeConsistent_ForSameUserPrincipal() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);
        UserPrincipal otherUserPrincipal = UserPrincipal.create(mockUser);

        // Act
        int hashCode1 = userPrincipal.hashCode();
        int hashCode2 = otherUserPrincipal.hashCode();

        // Assert
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void hashCode_ShouldBeDifferent_ForDifferentUserPrincipals() {
        // Arrange
        User otherUser = mock(User.class);
        lenient().when(otherUser.getId()).thenReturn(2L);
        lenient().when(otherUser.getEmail()).thenReturn("other@example.com");
        lenient().when(otherUser.getPassword()).thenReturn("otherpassword");
        lenient().when(otherUser.getName()).thenReturn("Jane Smith");
        lenient().when(otherUser.getAddress()).thenReturn("456 Other Street");
        lenient().when(otherUser.isVerified()).thenReturn(true);

        userPrincipal = UserPrincipal.create(mockUser);
        UserPrincipal otherUserPrincipal = UserPrincipal.create(otherUser);

        // Act
        int hashCode1 = userPrincipal.hashCode();
        int hashCode2 = otherUserPrincipal.hashCode();

        // Assert
        assertNotEquals(hashCode1, hashCode2);
    }

    @Test
    void getUsername_ShouldReturnEmail() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act
        String username = userPrincipal.getUsername();

        // Assert
        assertEquals(TEST_EMAIL, username);
    }

    @Test
    void create_ShouldHandleNullFields_Gracefully() {
        // Arrange
        when(mockUser.getId()).thenReturn(null);
        when(mockUser.getEmail()).thenReturn(null);
        when(mockUser.getPassword()).thenReturn(null);
        when(mockUser.getName()).thenReturn(null);
        when(mockUser.getAddress()).thenReturn(null);
        when(mockUser.isVerified()).thenReturn(false);

        // Act
        userPrincipal = UserPrincipal.create(mockUser);

        // Assert
        assertNotNull(userPrincipal);
        assertNull(userPrincipal.getId());
        assertEquals(userPrincipal.getUsername(), null);
        assertNull(userPrincipal.getPassword());
        assertFalse(userPrincipal.isSuperAdmin());
        assertTrue(userPrincipal.isEnabled());
        assertEquals(1, userPrincipal.getAuthorities().size());
    }

    @Test
    void userPrincipal_ShouldImplementUserDetailsInterface_Correctly() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act & Assert
        assertNotNull(userPrincipal.getUsername());
        assertNotNull(userPrincipal.getPassword());
        assertNotNull(userPrincipal.getAuthorities());
        assertTrue(userPrincipal.isAccountNonExpired());
        assertTrue(userPrincipal.isAccountNonLocked());
        assertTrue(userPrincipal.isCredentialsNonExpired());
        assertTrue(userPrincipal.isEnabled());
    }

    @Test
    void userPrincipal_ShouldBeSerializable() {
        // Arrange
        userPrincipal = UserPrincipal.create(mockUser);

        // Act & Assert
        // This test verifies that UserPrincipal can be serialized
        // The actual serialization test would require additional setup
        assertNotNull(userPrincipal);
        assertTrue(userPrincipal instanceof java.io.Serializable);
    }
}