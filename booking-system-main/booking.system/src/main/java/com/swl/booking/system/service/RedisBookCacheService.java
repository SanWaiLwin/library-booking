package com.swl.booking.system.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.swl.booking.system.entity.Book;
import com.swl.booking.system.response.book.BookResponse;

/**
 * Redis cache service for managing book availability data
 * Provides caching layer for book operations to improve performance
 */
@Service
public class RedisBookCacheService {

    private static final Logger logger = LoggerFactory.getLogger(RedisBookCacheService.class);
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    // Cache configuration from properties
    @Value("${app.cache.book.key-prefix:booking:book}")
    private String keyPrefix;
    
    @Value("${app.cache.book.available-books.ttl:300}")
    private long availableBooksTtl;
    
    @Value("${app.cache.book.borrowed-books.ttl:600}")
    private long borrowedBooksTtl;
    
    @Value("${app.cache.book.book-detail.ttl:1800}")
    private long bookDetailTtl;
    
    // Cache key methods
    private String getAvailableBooksKey() {
        return keyPrefix + ":available";
    }
    
    private String getBorrowedBooksKey(Long userId) {
        return keyPrefix + ":borrowed:" + userId;
    }
    
    private String getBookDetailKey(Long bookId) {
        return keyPrefix + ":detail:" + bookId;
    }
    
    @Autowired
    public RedisBookCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * Cache available books list
     * @param books List of available books
     */
    public void cacheAvailableBooks(List<BookResponse> books) {
        try {
            String key = getAvailableBooksKey();
            redisTemplate.delete(key);
            if (!books.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, books.toArray());
                redisTemplate.expire(key, availableBooksTtl, TimeUnit.SECONDS);
            }
            logger.debug("Cached {} available books with TTL {} seconds", books.size(), availableBooksTtl);
        } catch (Exception e) {
            logger.error("Error caching available books: {}", e.getMessage());
        }
    }
    
    /**
     * Get available books from cache
     * @return List of cached available books, null if not cached
     */
    @SuppressWarnings("unchecked")
    public List<BookResponse> getCachedAvailableBooks() {
        try {
            List<Object> cachedBooks = redisTemplate.opsForList().range(getAvailableBooksKey(), 0, -1);
            if (cachedBooks != null && !cachedBooks.isEmpty()) {
                logger.debug("Retrieved {} available books from cache", cachedBooks.size());
                return cachedBooks.stream()
                    .map(obj -> (BookResponse) obj)
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error retrieving available books from cache: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Cache borrowed books for a user
     * @param userId User ID
     * @param books List of borrowed books
     */
    public void cacheBorrowedBooks(Long userId, List<BookResponse> books) {
        try {
            String key = getBorrowedBooksKey(userId);
            redisTemplate.delete(key);
            if (!books.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, books.toArray());
                redisTemplate.expire(key, borrowedBooksTtl, TimeUnit.SECONDS);
            }
            logger.debug("Cached {} borrowed books for user {} with TTL {} seconds", books.size(), userId, borrowedBooksTtl);
        } catch (Exception e) {
            logger.error("Error caching borrowed books for user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Get borrowed books for a user from cache
     * @param userId User ID
     * @return List of cached borrowed books, null if not cached
     */
    @SuppressWarnings("unchecked")
    public List<BookResponse> getCachedBorrowedBooks(Long userId) {
        try {
            String key = getBorrowedBooksKey(userId);
            List<Object> cachedBooks = redisTemplate.opsForList().range(key, 0, -1);
            if (cachedBooks != null && !cachedBooks.isEmpty()) {
                logger.debug("Retrieved {} borrowed books from cache for user {}", cachedBooks.size(), userId);
                return cachedBooks.stream()
                    .map(obj -> (BookResponse) obj)
                    .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error retrieving borrowed books from cache for user {}: {}", userId, e.getMessage());
        }
        return null;
    }
    
    /**
     * Cache individual book details
     * @param book Book to cache
     */
    public void cacheBookDetail(BookResponse book) {
        try {
            String key = getBookDetailKey(book.getId());
            redisTemplate.opsForValue().set(key, book, bookDetailTtl, TimeUnit.SECONDS);
            logger.debug("Cached book detail for book ID: {} with TTL {} seconds", book.getId(), bookDetailTtl);
        } catch (Exception e) {
            logger.error("Error caching book detail for book ID {}: {}", book.getId(), e.getMessage());
        }
    }
    
    /**
     * Get book details from cache
     * @param bookId Book ID
     * @return Cached book details, null if not cached
     */
    public BookResponse getCachedBookDetail(Long bookId) {
        try {
            String key = getBookDetailKey(bookId);
            Object cachedBook = redisTemplate.opsForValue().get(key);
            if (cachedBook != null) {
                logger.debug("Retrieved book detail from cache for book ID: {}", bookId);
                return (BookResponse) cachedBook;
            }
        } catch (Exception e) {
            logger.error("Error retrieving book detail from cache for book ID {}: {}", bookId, e.getMessage());
        }
        return null;
    }
    
    /**
     * Add book to borrowed books set (for quick availability check)
     * @param bookId Book ID
     */
    public void addToBorrowedBooks(Long bookId) {
        try {
            String key = keyPrefix + ":borrowed-set";
            redisTemplate.opsForSet().add(key, bookId);
            redisTemplate.expire(key, borrowedBooksTtl, TimeUnit.SECONDS);
            logger.debug("Added book ID {} to borrowed books set", bookId);
        } catch (Exception e) {
            logger.error("Error adding book ID {} to borrowed books set: {}", bookId, e.getMessage());
        }
    }
    
    /**
     * Remove book from borrowed books set
     * @param bookId Book ID
     */
    public void removeFromBorrowedBooks(Long bookId) {
        try {
            String key = keyPrefix + ":borrowed-set";
            redisTemplate.opsForSet().remove(key, bookId);
            logger.debug("Removed book ID {} from borrowed books set", bookId);
        } catch (Exception e) {
            logger.error("Error removing book ID {} from borrowed books set: {}", bookId, e.getMessage());
        }
    }
    
    /**
     * Check if book is borrowed (quick availability check)
     * @param bookId Book ID
     * @return true if book is borrowed, false otherwise
     */
    public boolean isBookBorrowed(Long bookId) {
        try {
            String key = keyPrefix + ":borrowed-set";
            Boolean isBorrowed = redisTemplate.opsForSet().isMember(key, bookId);
            logger.debug("Book ID {} borrowed status from cache: {}", bookId, isBorrowed);
            return Boolean.TRUE.equals(isBorrowed);
        } catch (Exception e) {
            logger.error("Error checking borrowed status for book ID {}: {}", bookId, e.getMessage());
            return false; // Default to false if cache fails
        }
    }
    
    /**
     * Invalidate all book-related caches
     */
    public void invalidateAllBookCaches() {
        try {
            // Get all keys matching book cache patterns
            Set<String> allKeys = redisTemplate.keys(keyPrefix + ":*");
            
            // Delete all matching keys
            if (allKeys != null && !allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
            }
            
            logger.info("Invalidated all book caches");
        } catch (Exception e) {
            logger.error("Error invalidating book caches: {}", e.getMessage());
        }
    }
    
    /**
     * Invalidate user-specific borrowed books cache
     * @param userId User ID
     */
    public void invalidateUserBorrowedBooksCache(Long userId) {
        try {
            String key = getBorrowedBooksKey(userId);
            redisTemplate.delete(key);
            logger.debug("Invalidated borrowed books cache for user {}", userId);
        } catch (Exception e) {
            logger.error("Error invalidating borrowed books cache for user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Invalidate available books cache
     */
    public void invalidateAvailableBooksCache() {
        try {
            redisTemplate.delete(getAvailableBooksKey());
            logger.debug("Invalidated available books cache");
        } catch (Exception e) {
            logger.error("Error invalidating available books cache: {}", e.getMessage());
        }
    }
    
    /**
     * Invalidate specific book detail cache
     * @param bookId Book ID
     */
    public void invalidateBookDetailCache(Long bookId) {
        try {
            String key = getBookDetailKey(bookId);
            redisTemplate.delete(key);
            logger.debug("Invalidated book detail cache for book ID: {}", bookId);
        } catch (Exception e) {
            logger.error("Error invalidating book detail cache for book ID {}: {}", bookId, e.getMessage());
        }
    }
    
    /**
     * Warm up cache with initial data
     * @param availableBooks List of available books
     * @param borrowedBookIds Set of borrowed book IDs
     */
    public void warmUpCache(List<BookResponse> availableBooks, Set<Long> borrowedBookIds) {
        try {
            // Cache borrowed books set
            if (borrowedBookIds != null && !borrowedBookIds.isEmpty()) {
                String borrowedSetKey = keyPrefix + ":borrowed-set";
                redisTemplate.delete(borrowedSetKey);
                redisTemplate.opsForSet().add(borrowedSetKey, borrowedBookIds.toArray());
                redisTemplate.expire(borrowedSetKey, borrowedBooksTtl, TimeUnit.SECONDS);
            }
            
            // Cache individual book details
            for (BookResponse book : availableBooks) {
                cacheBookDetail(book);
            }
            
            logger.info("Cache warmed up with {} book details and {} borrowed books", 
                       availableBooks.size(), borrowedBookIds != null ? borrowedBookIds.size() : 0);
        } catch (Exception e) {
            logger.error("Error warming up cache: {}", e.getMessage());
        }
    }
}