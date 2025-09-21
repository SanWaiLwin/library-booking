package com.swl.booking.system.ascept;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

/**
 * Test class for LoggingAspect
 * Tests AOP logging functionality for controller methods
 */
@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private Logger mockLogger;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @InjectMocks
    private LoggingAspect loggingAspect;

    @BeforeEach
    void setUp() {
        // Inject the mock logger into the aspect
        ReflectionTestUtils.setField(loggingAspect, "logger", mockLogger);
        
        // Reset mocks to ensure clean state
        reset(mockLogger, joinPoint, signature);
    }

    @Test
    void logMethodEntry_Success() {
        // Given
        String methodName = "getUserProfile";
        Object[] methodArgs = {1L, "testUser"};
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        
        // When
        loggingAspect.logMethodEntry(joinPoint);
        
        // Then
        verify(mockLogger).info("Method execution started: {} method with arguments: {}", methodName, methodArgs);
        verify(joinPoint).getSignature();
        verify(signature).getName();
        verify(joinPoint).getArgs();
    }

    @Test
    void logMethodEntry_NoArguments() {
        // Given
        String methodName = "getAllBooks";
        Object[] methodArgs = {};
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        
        // When
        loggingAspect.logMethodEntry(joinPoint);
        
        // Then
        verify(mockLogger).info("Method execution started: {} method with arguments: {}", methodName, methodArgs);
    }

    @Test
    void logMethodEntry_NullArguments() {
        // Given
        String methodName = "deleteBook";
        Object[] methodArgs = {null};
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        
        // When
        loggingAspect.logMethodEntry(joinPoint);
        
        // Then
        verify(mockLogger).info("Method execution started: {} method with arguments: {}", methodName, methodArgs);
    }

    @Test
    void logMethodExit_Success() {
        // Given
        String methodName = "borrowBook";
        Object[] methodArgs = {1L, 2L};
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        
        // When
        loggingAspect.logMethodExit(joinPoint);
        
        // Then
        verify(mockLogger).info("Method execution finished: {} method with arguments: {}", methodName, methodArgs);
        verify(joinPoint).getSignature();
        verify(signature).getName();
        verify(joinPoint).getArgs();
    }

    @Test
    void logMethodExit_NoArguments() {
        // Given
        String methodName = "getAvailableBooks";
        Object[] methodArgs = {};
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        
        // When
        loggingAspect.logMethodExit(joinPoint);
        
        // Then
        verify(mockLogger).info("Method execution finished: {} method with arguments: {}", methodName, methodArgs);
    }

    @Test
    void logMethodReturn_WithStringResult() {
        // Given
        String result = "Book borrowed successfully";
        
        // When
        loggingAspect.logMethodReturn(result);
        
        // Then
        verify(mockLogger).info("Method executed successfully. Return value: " + result);
    }

    @Test
    void logMethodReturn_WithObjectResult() {
        // Given
        Object result = new Object() {
            @Override
            public String toString() {
                return "BookResponse{id=1, title='Test Book'}";
            }
        };
        
        // When
        loggingAspect.logMethodReturn(result);
        
        // Then
        verify(mockLogger).info("Method executed successfully. Return value: " + result);
    }

    @Test
    void logMethodReturn_WithNullResult() {
        // Given
        Object result = null;
        
        // When
        loggingAspect.logMethodReturn(result);
        
        // Then
        verify(mockLogger).info("Method executed successfully. Return value: " + result);
    }

    @Test
    void logMethodReturn_WithBooleanResult() {
        // Given
        Boolean result = true;
        
        // When
        loggingAspect.logMethodReturn(result);
        
        // Then
        verify(mockLogger).info("Method executed successfully. Return value: " + result);
    }

    @Test
    void logMethodException_RuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Book not found");
        
        // When
        loggingAspect.logMethodException(exception);
        
        // Then
        verify(mockLogger).error("An exception occurred: " + exception.getMessage(), exception);
    }

    @Test
    void logMethodException_IllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid book ID");
        
        // When
        loggingAspect.logMethodException(exception);
        
        // Then
        verify(mockLogger).error("An exception occurred: " + exception.getMessage(), exception);
    }

    @Test
    void logMethodException_NullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("User cannot be null");
        
        // When
        loggingAspect.logMethodException(exception);
        
        // Then
        verify(mockLogger).error("An exception occurred: " + exception.getMessage(), exception);
    }

    @Test
    void logMethodException_ExceptionWithNullMessage() {
        // Given
        Exception exception = new Exception((String) null);
        
        // When
        loggingAspect.logMethodException(exception);
        
        // Then
        verify(mockLogger).error("An exception occurred: " + exception.getMessage(), exception);
    }

    @Test
    void logMethodException_CustomException() {
        // Given
        Exception exception = new Exception("Database connection failed");
        
        // When
        loggingAspect.logMethodException(exception);
        
        // Then
        verify(mockLogger).error("An exception occurred: " + exception.getMessage(), exception);
    }

    @Test
    void logMethodEntry_MultipleArguments() {
        // Given
        String methodName = "updateUserProfile";
        Object[] methodArgs = {1L, "newUsername", "new@email.com", true};
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        
        // When
        loggingAspect.logMethodEntry(joinPoint);
        
        // Then
        verify(mockLogger).info("Method execution started: {} method with arguments: {}", methodName, methodArgs);
    }

    @Test
    void logMethodExit_MultipleArguments() {
        // Given
        String methodName = "registerBook";
        Object[] methodArgs = {"Book Title", "Author Name", "ISBN123", 2023};
        
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(methodArgs);
        
        // When
        loggingAspect.logMethodExit(joinPoint);
        
        // Then
        verify(mockLogger).info("Method execution finished: {} method with arguments: {}", methodName, methodArgs);
    }

    @Test
    void logMethodReturn_WithComplexObject() {
        // Given
        Object result = new Object() {
            private final String status = "SUCCESS";
            private final int code = 200;
            
            @Override
            public String toString() {
                return "ApiResponse{status='" + status + "', code=" + code + "}";
            }
        };
        
        // When
        loggingAspect.logMethodReturn(result);
        
        // Then
        verify(mockLogger).info("Method executed successfully. Return value: " + result);
    }

    @Test
    void logMethodException_ChainedException() {
        // Given
        Exception cause = new RuntimeException("Root cause");
        Exception exception = new Exception("Wrapper exception", cause);
        
        // When
        loggingAspect.logMethodException(exception);
        
        // Then
        verify(mockLogger).error("An exception occurred: " + exception.getMessage(), exception);
    }
}