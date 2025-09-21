package com.swl.booking.system.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;

/**
 * Comprehensive test suite for JwtTokenProvider
 * Tests token generation, validation, and extraction functionality
 */
@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET = "testSecretKey123456789";
    private static final long TEST_EXPIRATION = 24; // 24 hours
    private static final String TEST_USERNAME = "testuser@example.com";

    @BeforeEach
    void setUp() {
        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationIns", TEST_EXPIRATION);
    }

    @Test
    void generateToken_ShouldReturnValidToken_WhenUsernameProvided() {
        // Act
        String token = jwtTokenProvider.generateToken(TEST_USERNAME);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        
        // Verify token structure (JWT has 3 parts separated by dots)
        String[] tokenParts = token.split("\\.");
        assertEquals(3, tokenParts.length);
    }

    @Test
    void generateToken_ShouldContainCorrectClaims_WhenUsernameProvided() {
        // Act
        String token = jwtTokenProvider.generateToken(TEST_USERNAME);

        // Assert - Parse token to verify claims
        String secretKey = Base64.getEncoder().encodeToString(TEST_SECRET.getBytes());
        Claims claims = Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();

        assertEquals(TEST_USERNAME, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        
        // Verify expiration is set correctly (within reasonable time range)
        long expectedExpiration = System.currentTimeMillis() + (TEST_EXPIRATION * 3600000);
        long actualExpiration = claims.getExpiration().getTime();
        assertTrue(Math.abs(expectedExpiration - actualExpiration) < 5000); // 5 second tolerance
    }

    @Test
    void validateToken_ShouldReturnTrue_WhenTokenIsValid() {
        // Arrange
        String validToken = jwtTokenProvider.generateToken(TEST_USERNAME);

        // Act
        boolean isValid = jwtTokenProvider.validateToken(validToken);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsNull() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsEmpty() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsMalformed() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("invalid.token.format");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenHasWrongSignature() {
        // Arrange - Create token with different secret
        String wrongSecret = "wrongSecret123";
        String tokenWithWrongSignature = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SignatureAlgorithm.HS512, Base64.getEncoder().encodeToString(wrongSecret.getBytes()))
                .compact();

        // Act
        boolean isValid = jwtTokenProvider.validateToken(tokenWithWrongSignature);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_ShouldReturnFalse_WhenTokenIsExpired() {
        // Arrange - Create expired token
        String secretKey = Base64.getEncoder().encodeToString(TEST_SECRET.getBytes());
        String expiredToken = Jwts.builder()
                .setSubject(TEST_USERNAME)
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago (expired)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        // Act
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void getUsernameFromToken_ShouldReturnCorrectUsername_WhenTokenIsValid() {
        // Arrange
        String token = jwtTokenProvider.generateToken(TEST_USERNAME);

        // Act
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertEquals(TEST_USERNAME, extractedUsername);
    }

    @Test
    void getUsernameFromToken_ShouldThrowException_WhenTokenIsInvalid() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken("invalid.token.format");
        });
    }

    @Test
    void getUsernameFromToken_ShouldThrowException_WhenTokenIsNull() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUsernameFromToken(null);
        });
    }

    @Test
    void getUserIdFromJWT_ShouldReturnCorrectUserId_WhenTokenContainsNumericSubject() {
        // Arrange - Create token with numeric subject
        String userId = "12345";
        String secretKey = Base64.getEncoder().encodeToString(TEST_SECRET.getBytes());
        String tokenWithUserId = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();

        // Act
        Long extractedUserId = jwtTokenProvider.getUserIdFromJWT(tokenWithUserId);

        // Assert
        assertEquals(Long.valueOf(userId), extractedUserId);
    }

    @Test
    void getUserIdFromJWT_ShouldThrowException_WhenSubjectIsNotNumeric() {
        // Arrange
        String token = jwtTokenProvider.generateToken(TEST_USERNAME); // Non-numeric subject

        // Act & Assert
        assertThrows(NumberFormatException.class, () -> {
            jwtTokenProvider.getUserIdFromJWT(token);
        });
    }

    @Test
    void getUserIdFromJWT_ShouldThrowException_WhenTokenIsInvalid() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtTokenProvider.getUserIdFromJWT("invalid.token.format");
        });
    }

    @Test
    void generateToken_ShouldGenerateDifferentTokens_ForSameUserAtDifferentTimes() throws InterruptedException {
        // Arrange
        String firstToken = jwtTokenProvider.generateToken(TEST_USERNAME);
        
        // Wait a small amount to ensure different issued time
        Thread.sleep(1000);
        
        // Act
        String secondToken = jwtTokenProvider.generateToken(TEST_USERNAME);

        // Assert
        assertNotEquals(firstToken, secondToken);
        
        // Both tokens should be valid
        assertTrue(jwtTokenProvider.validateToken(firstToken));
        assertTrue(jwtTokenProvider.validateToken(secondToken));
        
        // Both should have same username
        assertEquals(TEST_USERNAME, jwtTokenProvider.getUsernameFromToken(firstToken));
        assertEquals(TEST_USERNAME, jwtTokenProvider.getUsernameFromToken(secondToken));
    }

    @Test
    void tokenLifecycle_ShouldWorkCorrectly_FromGenerationToValidation() {
        // Arrange & Act - Full lifecycle test
        String token = jwtTokenProvider.generateToken(TEST_USERNAME);
        boolean isValid = jwtTokenProvider.validateToken(token);
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(token);
        assertTrue(isValid);
        assertEquals(TEST_USERNAME, extractedUsername);
    }
}