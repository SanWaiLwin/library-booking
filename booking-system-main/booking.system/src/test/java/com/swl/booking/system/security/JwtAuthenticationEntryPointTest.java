package com.swl.booking.system.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Comprehensive test suite for JwtAuthenticationEntryPoint
 * Tests authentication entry point functionality for unauthorized access
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationEntryPointTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authenticationException;

    @InjectMocks
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    private static final String EXPECTED_ERROR_MESSAGE = "Sorry, You're not authorized to access.";
    private static final String TEST_EXCEPTION_MESSAGE = "Authentication failed";

    @BeforeEach
    void setUp() {
        // Setup common mock behavior
        when(authenticationException.getMessage()).thenReturn(TEST_EXCEPTION_MESSAGE);
    }

    @Test
    void commence_ShouldSendUnauthorizedError_WhenAuthenticationExceptionOccurs() throws IOException, ServletException {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Assert
        verify(response, times(1)).sendError(
            HttpServletResponse.SC_UNAUTHORIZED, 
            EXPECTED_ERROR_MESSAGE
        );
    }

    @Test
    void commence_ShouldHandleAuthenticationExceptionWithNullMessage_Correctly() throws IOException, ServletException {
        // Arrange
        when(authenticationException.getMessage()).thenReturn(null);

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Assert
        verify(response, times(1)).sendError(
            HttpServletResponse.SC_UNAUTHORIZED, 
            EXPECTED_ERROR_MESSAGE
        );
    }

    @Test
    void commence_ShouldHandleAuthenticationExceptionWithEmptyMessage_Correctly() throws IOException, ServletException {
        // Arrange
        when(authenticationException.getMessage()).thenReturn("");

        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Assert
        verify(response, times(1)).sendError(
            HttpServletResponse.SC_UNAUTHORIZED, 
            EXPECTED_ERROR_MESSAGE
        );
    }

    @Test
    void commence_ShouldHandleIOException_WhenResponseSendErrorFails() throws IOException, ServletException {
        // Arrange
        doThrow(new IOException("Response write error")).when(response)
            .sendError(HttpServletResponse.SC_UNAUTHORIZED, EXPECTED_ERROR_MESSAGE);

        // Act & Assert
        assertThrows(IOException.class, () -> {
            jwtAuthenticationEntryPoint.commence(request, response, authenticationException);
        });
        
        verify(response, times(1)).sendError(
            HttpServletResponse.SC_UNAUTHORIZED, 
            EXPECTED_ERROR_MESSAGE
        );
    }

    @Test
    void commence_ShouldNotModifyRequest_WhenCalled() throws IOException, ServletException {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Assert
        verifyNoInteractions(request);
        verify(response, times(1)).sendError(
            HttpServletResponse.SC_UNAUTHORIZED, 
            EXPECTED_ERROR_MESSAGE
        );
    }

    @Test
    void commence_ShouldUseCorrectStatusCode_Always() throws IOException, ServletException {
        // Act
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Assert
        verify(response, times(1)).sendError(
            eq(HttpServletResponse.SC_UNAUTHORIZED), 
            eq(EXPECTED_ERROR_MESSAGE)
        );
        
        // Verify the status code value
        assertEquals(401, HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void commence_ShouldHandleMultipleConsecutiveCalls_Correctly() throws IOException, ServletException {
        // Act - Call multiple times
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);
        jwtAuthenticationEntryPoint.commence(request, response, authenticationException);

        // Assert
        verify(response, times(3)).sendError(
            HttpServletResponse.SC_UNAUTHORIZED, 
            EXPECTED_ERROR_MESSAGE
        );
    }

}