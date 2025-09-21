package com.swl.booking.system.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Comprehensive test suite for JwtAuthenticationFilter
 * Tests JWT authentication filter functionality with various scenarios
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private UserDetails userDetails;

    @Mock
    private JWTSingleton jwtSingleton;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String TEST_TOKEN = "eyJhbGciOiJIUzUxMiJ9.test.token";
    private static final String TEST_USERNAME = "test@example.com";
    private static final String BEARER_TOKEN = "Bearer " + TEST_TOKEN;
    private static final String AUTH_URI = "/auth/profile";
    private static final String NON_AUTH_URI = "/api/books";

    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        // Inject mocked dependencies using reflection
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "tokenProvider", tokenProvider);
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "customUserDetailsService", customUserDetailsService);
        
        // Setup response writer
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);
        
        // Setup security context
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void doFilterInternal_ShouldReturnBadRequest_WhenAuthEndpointAndNoToken() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("JWT token missing."));
    }

    @Test
    void doFilterInternal_ShouldReturnBadRequest_WhenAuthEndpointAndEmptyToken() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn("");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("JWT token missing."));
    }

    @Test
    void doFilterInternal_ShouldReturnBadRequest_WhenAuthEndpointAndInvalidTokenFormat() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn("InvalidFormat");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("JWT token missing."));
    }

    @Test
    void doFilterInternal_ShouldReturnBadRequest_WhenTokenIsInvalid() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenProvider.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(false);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("Invalid JWT."));
    }

    @Test
    void doFilterInternal_ShouldReturnBadRequest_WhenUsernameIsNull() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenProvider.getUsernameFromToken(TEST_TOKEN)).thenReturn(null);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("Invalid JWT."));
    }

    @Test
    void doFilterInternal_ShouldReturnBadRequest_WhenTokenIsExpiredInSingleton() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenProvider.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        
        try (MockedStatic<JWTSingleton> mockedSingleton = mockStatic(JWTSingleton.class)) {
            mockedSingleton.when(JWTSingleton::getInstance).thenReturn(jwtSingleton);
            when(jwtSingleton.checkJWTexist(TEST_USERNAME, TEST_TOKEN)).thenReturn(true);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
            verify(filterChain, never()).doFilter(request, response);
            assertTrue(stringWriter.toString().contains("Invalid JWT."));
        }
    }

    @Test
    void doFilterInternal_ShouldHandleException_WhenTokenProviderThrowsException() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenProvider.getUsernameFromToken(TEST_TOKEN)).thenThrow(new RuntimeException("Token parsing error"));

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("Invalid JWT."));
    }

    @Test
    void doFilterInternal_ShouldHandleException_WhenUserDetailsServiceThrowsException() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(tokenProvider.getUsernameFromToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(tokenProvider.validateToken(TEST_TOKEN)).thenReturn(true);
        when(customUserDetailsService.loadUserByUsername(TEST_USERNAME))
            .thenThrow(new RuntimeException("User service error"));
        
        try (MockedStatic<JWTSingleton> mockedSingleton = mockStatic(JWTSingleton.class)) {
            mockedSingleton.when(JWTSingleton::getInstance).thenReturn(jwtSingleton);
            when(jwtSingleton.checkJWTexist(TEST_USERNAME, TEST_TOKEN)).thenReturn(false);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
            verify(filterChain, never()).doFilter(request, response);
            assertTrue(stringWriter.toString().contains("Invalid JWT."));
        }
    }

    @Test
    void doFilterInternal_ShouldNotProcessToken_WhenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Arrange
        when(request.getRequestURI()).thenReturn(AUTH_URI);
        when(request.getHeader("Authorization")).thenReturn("Basic dGVzdDp0ZXN0"); // Basic auth header

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("JWT token missing."));
        verify(tokenProvider, never()).validateToken(any());
    }

    @Test
    void doFilterInternal_ShouldHandleMultipleAuthEndpoints_Correctly() throws ServletException, IOException {
        // Test different auth endpoints
        String[] authEndpoints = {"/auth/profile", "/auth/update", "/auth/logout", "/auth/admin"};
        
        for (String endpoint : authEndpoints) {
            // Reset mocks
            reset(request, response, filterChain);
            stringWriter = new StringWriter();
            printWriter = new PrintWriter(stringWriter);
            when(response.getWriter()).thenReturn(printWriter);
            
            // Arrange
            when(request.getRequestURI()).thenReturn(endpoint);
            when(request.getHeader("Authorization")).thenReturn(null);

            // Act
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

            // Assert
            verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
            verify(filterChain, never()).doFilter(request, response);
            assertTrue(stringWriter.toString().contains("JWT token missing."));
        }
    }
}