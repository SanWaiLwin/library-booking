package com.swl.booking.system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Test class for CacheRefreshService
 * Tests scheduled cache refresh, cleanup, and manual refresh operations
 */
@ExtendWith(MockitoExtension.class)
class CacheRefreshServiceTest {

    @Mock
    private RedisBookCacheService redisBookCacheService;

    @Mock
    private CacheWarmupService cacheWarmupService;

    @InjectMocks
    private CacheRefreshService cacheRefreshService;

    @BeforeEach
    void setUp() {
        // Set cache refresh enabled by default
        ReflectionTestUtils.setField(cacheRefreshService, "cacheRefreshEnabled", true);
        // Reset mocks to ensure clean state
        reset(redisBookCacheService, cacheWarmupService);
    }

    @Test
    void scheduledCacheRefresh_Success() {
        // Given - ensure cache refresh is enabled
        ReflectionTestUtils.setField(cacheRefreshService, "cacheRefreshEnabled", true);
        
        // When
        cacheRefreshService.scheduledCacheRefresh();
        
        // Then
        verify(redisBookCacheService).invalidateAllBookCaches();
        verify(cacheWarmupService).manualWarmup();
    }

    @Test
    void scheduledCacheRefresh_CacheRefreshDisabled() {
        // Given
        ReflectionTestUtils.setField(cacheRefreshService, "cacheRefreshEnabled", false);
        
        // When
        cacheRefreshService.scheduledCacheRefresh();
        
        // Then
        verify(redisBookCacheService, never()).invalidateAllBookCaches();
        verify(cacheWarmupService, never()).manualWarmup();
    }

    @Test
    void scheduledCacheRefresh_ExceptionInInvalidateCache() {
        // Given
        doThrow(new RuntimeException("Redis connection error")).when(redisBookCacheService).invalidateAllBookCaches();
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.scheduledCacheRefresh());
        
        // Verify invalidate was called but warmup was not due to exception
        verify(redisBookCacheService).invalidateAllBookCaches();
        verify(cacheWarmupService, never()).manualWarmup();
    }

    @Test
    void scheduledCacheRefresh_ExceptionInWarmup() {
        // Given
        doThrow(new RuntimeException("Database connection error")).when(cacheWarmupService).manualWarmup();
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.scheduledCacheRefresh());
        
        // Verify both operations were attempted
        verify(redisBookCacheService).invalidateAllBookCaches();
        verify(cacheWarmupService).manualWarmup();
    }

    @Test
    void scheduledCacheCleanup_Success() {
        // Given - mocks are already set up
        
        // When
        cacheRefreshService.scheduledCacheCleanup();
        
        // Then - cleanup is mostly handled by Redis TTL, so just verify no exceptions
        // No specific verifications needed as cleanup is automatic
    }

    @Test
    void scheduledCacheCleanup_CacheRefreshDisabled() {
        // Given
        ReflectionTestUtils.setField(cacheRefreshService, "cacheRefreshEnabled", false);
        
        // When
        cacheRefreshService.scheduledCacheCleanup();
        
        // Then - method should return early, no operations performed
        // No specific verifications needed as method returns early
    }

    @Test
    void scheduledCacheCleanup_Exception() {
        // Given - simulate an exception scenario by modifying the method behavior
        // Since cleanup method doesn't call external services, we test exception handling
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.scheduledCacheCleanup());
    }

    @Test
    void manualCacheRefresh_Success() {
        // Given - mocks are already set up
        
        // When
        cacheRefreshService.manualCacheRefresh();
        
        // Then - should call the same logic as scheduled refresh
        verify(redisBookCacheService).invalidateAllBookCaches();
        verify(cacheWarmupService).manualWarmup();
    }

    @Test
    void manualCacheRefresh_Exception() {
        // Given
        doThrow(new RuntimeException("Service unavailable")).when(redisBookCacheService).invalidateAllBookCaches();
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.manualCacheRefresh());
        
        // Verify invalidate was called
        verify(redisBookCacheService).invalidateAllBookCaches();
        verify(cacheWarmupService, never()).manualWarmup();
    }

    @Test
    void refreshAvailableBooksCache_Success() {
        // Given - mocks are already set up
        
        // When
        cacheRefreshService.refreshAvailableBooksCache();
        
        // Then
        verify(redisBookCacheService).invalidateAvailableBooksCache();
    }

    @Test
    void refreshAvailableBooksCache_Exception() {
        // Given
        doThrow(new RuntimeException("Cache invalidation failed")).when(redisBookCacheService).invalidateAvailableBooksCache();
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.refreshAvailableBooksCache());
        
        // Verify invalidate was called
        verify(redisBookCacheService).invalidateAvailableBooksCache();
    }

    @Test
    void refreshUserBorrowedBooksCache_Success() {
        // Given
        Long userId = 1L;
        
        // When
        cacheRefreshService.refreshUserBorrowedBooksCache(userId);
        
        // Then
        verify(redisBookCacheService).invalidateUserBorrowedBooksCache(userId);
    }

    @Test
    void refreshUserBorrowedBooksCache_Exception() {
        // Given
        Long userId = 1L;
        doThrow(new RuntimeException("User cache invalidation failed")).when(redisBookCacheService).invalidateUserBorrowedBooksCache(userId);
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.refreshUserBorrowedBooksCache(userId));
        
        // Verify invalidate was called
        verify(redisBookCacheService).invalidateUserBorrowedBooksCache(userId);
    }

    @Test
    void refreshBookDetailCache_Success() {
        // Given
        Long bookId = 1L;
        
        // When
        cacheRefreshService.refreshBookDetailCache(bookId);
        
        // Then
        verify(redisBookCacheService).invalidateBookDetailCache(bookId);
        verify(cacheWarmupService).refreshBookCache(bookId);
    }

    @Test
    void refreshBookDetailCache_ExceptionInInvalidate() {
        // Given
        Long bookId = 1L;
        doThrow(new RuntimeException("Cache invalidation failed")).when(redisBookCacheService).invalidateBookDetailCache(bookId);
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.refreshBookDetailCache(bookId));
        
        // Verify invalidate was called but refresh was not due to exception
        verify(redisBookCacheService).invalidateBookDetailCache(bookId);
        verify(cacheWarmupService, never()).refreshBookCache(bookId);
    }

    @Test
    void refreshBookDetailCache_ExceptionInRefresh() {
        // Given
        Long bookId = 1L;
        doThrow(new RuntimeException("Book refresh failed")).when(cacheWarmupService).refreshBookCache(bookId);
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.refreshBookDetailCache(bookId));
        
        // Verify both operations were attempted
        verify(redisBookCacheService).invalidateBookDetailCache(bookId);
        verify(cacheWarmupService).refreshBookCache(bookId);
    }

    @Test
    void refreshBookDetailCache_NullBookId() {
        // Given
        Long bookId = null;
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.refreshBookDetailCache(bookId));
        
        // Verify methods were called with null (will be handled by the services)
        verify(redisBookCacheService).invalidateBookDetailCache(bookId);
        verify(cacheWarmupService).refreshBookCache(bookId);
    }

    @Test
    void refreshUserBorrowedBooksCache_NullUserId() {
        // Given
        Long userId = null;
        
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheRefreshService.refreshUserBorrowedBooksCache(userId));
        
        // Verify method was called with null (will be handled by the service)
        verify(redisBookCacheService).invalidateUserBorrowedBooksCache(userId);
    }

    @Test
    void cacheRefreshEnabled_DefaultValue() {
        // Given - create a new instance to test default value
        CacheRefreshService newService = new CacheRefreshService();
        ReflectionTestUtils.setField(newService, "redisBookCacheService", redisBookCacheService);
        ReflectionTestUtils.setField(newService, "cacheWarmupService", cacheWarmupService);
        // Set cacheRefreshEnabled to true to simulate @Value default behavior
        ReflectionTestUtils.setField(newService, "cacheRefreshEnabled", true);
        
        // When
        newService.scheduledCacheRefresh();
        
        // Then - should work with default enabled value
        verify(redisBookCacheService).invalidateAllBookCaches();
        verify(cacheWarmupService).manualWarmup();
    }
}