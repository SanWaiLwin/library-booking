package com.swl.booking.system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Service responsible for scheduled cache refresh and eviction
 * to maintain data consistency between Redis and database.
 */
@Service
public class CacheRefreshService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheRefreshService.class);
    
    @Autowired
    private RedisBookCacheService redisBookCacheService;
    
    @Autowired
    private CacheWarmupService cacheWarmupService;
    
    @Value("${app.cache.refresh.enabled:true}")
    private boolean cacheRefreshEnabled;
    
    /**
     * Scheduled cache refresh every 30 minutes
     * This helps maintain data consistency and refreshes expired cache entries
     */
    @Scheduled(fixedRate = 1800000, initialDelay = 1800000) // 30 minutes in milliseconds, wait 30 minutes before first run
    public void scheduledCacheRefresh() {
        if (!cacheRefreshEnabled) {
            logger.debug("Cache refresh is disabled");
            return;
        }
        
        try {
            logger.info("Starting scheduled cache refresh...");
            
            // Clear all existing cache to ensure fresh data
            redisBookCacheService.invalidateAllBookCaches();
            
            // Warm up cache with fresh data from database
            cacheWarmupService.manualWarmup();
            
            logger.info("Scheduled cache refresh completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during scheduled cache refresh: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Scheduled cache cleanup every hour
     * Removes expired keys and performs maintenance
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void scheduledCacheCleanup() {
        if (!cacheRefreshEnabled) {
            return;
        }
        
        try {
            logger.debug("Starting scheduled cache cleanup...");
            
            // Redis automatically handles TTL expiration, but we can log the cleanup
            logger.debug("Cache cleanup completed - expired keys automatically removed by Redis TTL");
            
        } catch (Exception e) {
            logger.error("Error during scheduled cache cleanup: {}", e.getMessage());
        }
    }
    
    /**
     * Manual cache refresh that can be triggered programmatically
     */
    public void manualCacheRefresh() {
        logger.info("Manual cache refresh initiated");
        scheduledCacheRefresh();
    }
    
    /**
     * Refresh cache for specific data types
     */
    public void refreshAvailableBooksCache() {
        try {
            redisBookCacheService.invalidateAvailableBooksCache();
            // The cache will be repopulated on next request
            logger.info("Available books cache invalidated and will be refreshed on next request");
        } catch (Exception e) {
            logger.error("Error refreshing available books cache: {}", e.getMessage());
        }
    }
    
    /**
     * Refresh cache for a specific user's borrowed books
     */
    public void refreshUserBorrowedBooksCache(Long userId) {
        try {
            redisBookCacheService.invalidateUserBorrowedBooksCache(userId);
            // The cache will be repopulated on next request
            logger.info("Borrowed books cache invalidated for user {} and will be refreshed on next request", userId);
        } catch (Exception e) {
            logger.error("Error refreshing borrowed books cache for user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Refresh cache for a specific book detail
     */
    public void refreshBookDetailCache(Long bookId) {
        try {
            redisBookCacheService.invalidateBookDetailCache(bookId);
            // Immediately refresh with current data
            cacheWarmupService.refreshBookCache(bookId);
            logger.info("Book detail cache refreshed for book ID: {}", bookId);
        } catch (Exception e) {
            logger.error("Error refreshing book detail cache for book ID {}: {}", bookId, e.getMessage());
        }
    }
}