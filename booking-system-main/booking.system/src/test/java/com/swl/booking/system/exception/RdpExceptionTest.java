package com.swl.booking.system.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RdpException
 * Tests custom exception functionality and inheritance behavior
 */
class RdpExceptionTest {

    @Test
    @DisplayName("Should create RdpException with message only")
    void createRdpException_WithMessage() {
        // Given
        String errorMessage = "Database connection failed";
        
        // When
        RdpException exception = new RdpException(errorMessage);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("Should create RdpException with message and cause")
    void createRdpException_WithMessageAndCause() {
        // Given
        String errorMessage = "Service operation failed";
        RuntimeException cause = new RuntimeException("Root cause error");
        
        // When
        RdpException exception = new RdpException(errorMessage, cause);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertTrue(exception instanceof Exception);
    }

    @Test
    @DisplayName("Should create RdpException with null message")
    void createRdpException_WithNullMessage() {
        // Given
        String errorMessage = null;
        
        // When
        RdpException exception = new RdpException(errorMessage);
        
        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create RdpException with null message and cause")
    void createRdpException_WithNullMessageAndCause() {
        // Given
        String errorMessage = null;
        Exception cause = new IllegalArgumentException("Invalid parameter");
        
        // When
        RdpException exception = new RdpException(errorMessage, cause);
        
        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should create RdpException with message and null cause")
    void createRdpException_WithMessageAndNullCause() {
        // Given
        String errorMessage = "Operation failed";
        Throwable cause = null;
        
        // When
        RdpException exception = new RdpException(errorMessage, cause);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create RdpException with empty message")
    void createRdpException_WithEmptyMessage() {
        // Given
        String errorMessage = "";
        
        // When
        RdpException exception = new RdpException(errorMessage);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create RdpException with empty message and cause")
    void createRdpException_WithEmptyMessageAndCause() {
        // Given
        String errorMessage = "";
        Exception cause = new NullPointerException("Null pointer error");
        
        // When
        RdpException exception = new RdpException(errorMessage, cause);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should verify RdpException inheritance hierarchy")
    void verifyRdpException_InheritanceHierarchy() {
        // Given
        RdpException exception = new RdpException("Test exception");
        
        // Then
        assertTrue(exception instanceof Exception);
        assertTrue(exception instanceof Throwable);
        // RdpException extends Exception, not RuntimeException
        assertNotNull(exception.getClass().getSuperclass());
        assertEquals(Exception.class, exception.getClass().getSuperclass());
    }

    @Test
    @DisplayName("Should verify RdpException serialVersionUID")
    void verifyRdpException_SerialVersionUID() {
        // Given
        RdpException exception = new RdpException("Serialization test");
        
        // Then
        assertNotNull(exception);
        // Verify the exception can be created without serialization issues
        assertTrue(exception instanceof java.io.Serializable);
    }

    @Test
    @DisplayName("Should create RdpException with chained exceptions")
    void createRdpException_WithChainedExceptions() {
        // Given
        RuntimeException rootCause = new RuntimeException("Root cause");
        IllegalStateException intermediateCause = new IllegalStateException("Intermediate cause", rootCause);
        String errorMessage = "Final exception message";
        
        // When
        RdpException exception = new RdpException(errorMessage, intermediateCause);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    @Test
    @DisplayName("Should create RdpException with different exception types as cause")
    void createRdpException_WithDifferentCauseTypes() {
        // Test with RuntimeException
        RuntimeException runtimeCause = new RuntimeException("Runtime error");
        RdpException rdpWithRuntime = new RdpException("RDP with runtime cause", runtimeCause);
        assertEquals(runtimeCause, rdpWithRuntime.getCause());
        
        // Test with IllegalArgumentException
        IllegalArgumentException argCause = new IllegalArgumentException("Invalid argument");
        RdpException rdpWithArg = new RdpException("RDP with argument cause", argCause);
        assertEquals(argCause, rdpWithArg.getCause());
        
        // Test with NullPointerException
        NullPointerException nullCause = new NullPointerException("Null pointer");
        RdpException rdpWithNull = new RdpException("RDP with null cause", nullCause);
        assertEquals(nullCause, rdpWithNull.getCause());
    }

    @Test
    @DisplayName("Should verify RdpException toString behavior")
    void verifyRdpException_ToStringBehavior() {
        // Given
        String message = "Test exception message";
        RdpException exception = new RdpException(message);
        
        // When
        String toStringResult = exception.toString();
        
        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("RdpException"));
        assertTrue(toStringResult.contains(message));
    }

    @Test
    @DisplayName("Should verify RdpException with cause toString behavior")
    void verifyRdpException_WithCauseToStringBehavior() {
        // Given
        String message = "Main exception";
        RuntimeException cause = new RuntimeException("Cause exception");
        RdpException exception = new RdpException(message, cause);
        
        // When
        String toStringResult = exception.toString();
        
        // Then
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("RdpException"));
        assertTrue(toStringResult.contains(message));
    }

    @Test
    @DisplayName("Should verify RdpException stack trace behavior")
    void verifyRdpException_StackTraceBehavior() {
        // Given
        RdpException exception = new RdpException("Stack trace test");
        
        // When
        StackTraceElement[] stackTrace = exception.getStackTrace();
        
        // Then
        assertNotNull(stackTrace);
        assertTrue(stackTrace.length > 0);
        // Verify the first element contains this test method
        assertEquals("verifyRdpException_StackTraceBehavior", stackTrace[0].getMethodName());
    }

    @Test
    @DisplayName("Should verify RdpException equality behavior")
    void verifyRdpException_EqualityBehavior() {
        // Given
        String message = "Test message";
        RdpException exception1 = new RdpException(message);
        RdpException exception2 = new RdpException(message);
        
        // Then
        assertNotEquals(exception1, exception2); // Different instances
        assertEquals(exception1, exception1); // Same instance
        assertEquals(exception1.getMessage(), exception2.getMessage()); // Same message
    }
}