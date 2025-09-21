package com.swl.booking.system.service;

import com.swl.booking.system.entity.Book;
import com.swl.booking.system.entity.BookBorrowing;
import com.swl.booking.system.entity.User;
import com.swl.booking.system.repository.BookBorrowingRepository;
import com.swl.booking.system.repository.BookRepository;
import com.swl.booking.system.response.book.BookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service responsible for warming up Redis cache with initial book data
 * on application startup to improve performance for first requests.
 */
@Service
public class CacheWarmupService {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheWarmupService.class);
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BookBorrowingRepository bookBorrowingRepository;
    
    @Autowired
    private RedisBookCacheService redisBookCacheService;
    
    /**
     * Warm up cache when application is ready
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void warmupCache() {
        logger.info("Starting cache warmup process...");
        
        try {
            // Warm up available books cache
            warmupAvailableBooks();
            
            // Warm up book details cache for popular books
            warmupBookDetails();
            
            // Warm up borrowed books tracking
            warmupBorrowedBooksTracking();
            
            logger.info("Cache warmup completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during cache warmup: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Warm up available books cache
     */
    private void warmupAvailableBooks() {
        try {
            List<Book> availableBooks = bookRepository.findByIsAvailableTrue();
            List<BookResponse> bookResponses = availableBooks.stream()
                    .map(BookResponse::new)
                    .collect(Collectors.toList());
            
            redisBookCacheService.cacheAvailableBooks(bookResponses);
            logger.info("Warmed up available books cache with {} books", bookResponses.size());
            
        } catch (Exception e) {
            logger.error("Error warming up available books cache: {}", e.getMessage());
        }
    }
    
    /**
     * Warm up book details cache for all books
     */
    private void warmupBookDetails() {
        try {
            List<Book> allBooks = bookRepository.findAll();
            
            for (Book book : allBooks) {
                BookResponse bookResponse = new BookResponse(book);
                redisBookCacheService.cacheBookDetail(bookResponse);
            }
            
            logger.info("Warmed up book details cache for {} books", allBooks.size());
            
        } catch (Exception e) {
            logger.error("Error warming up book details cache: {}", e.getMessage());
        }
    }
    
    /**
     * Warm up borrowed books tracking and user-specific caches
     */
    private void warmupBorrowedBooksTracking() {
        try {
            // Get all active borrowings
            List<BookBorrowing> activeBorrowings = bookBorrowingRepository.findByIsReturnedFalse();
            
            // Group by user ID
            Map<User, List<BookBorrowing>> borrowingsByUser = activeBorrowings.stream()
                    .collect(Collectors.groupingBy(BookBorrowing::getBorrower));
            
            // Cache borrowed books for each user
            for (Map.Entry<User, List<BookBorrowing>> entry : borrowingsByUser.entrySet()) {
                Long userId = entry.getKey().getId();
                List<BookResponse> userBorrowedBooks = entry.getValue().stream()
                        .map(borrowing -> new BookResponse(borrowing.getBook()))
                        .collect(Collectors.toList());
                
                redisBookCacheService.cacheBorrowedBooks(userId, userBorrowedBooks);
            }
            
            // Warm up borrowed books set for quick availability checks
            Set<Long> borrowedBookIds = activeBorrowings.stream()
                    .map(borrowing -> borrowing.getBook().getId())
                    .collect(Collectors.toSet());
            
            // Get available books for warmup
            List<Book> allBooks = bookRepository.findAll();
            List<BookResponse> availableBooks = allBooks.stream()
                    .map(BookResponse::new)
                    .collect(Collectors.toList());
            
            redisBookCacheService.warmUpCache(availableBooks, borrowedBookIds);
            
            logger.info("Warmed up borrowed books cache for {} users with {} total borrowed books", 
                    borrowingsByUser.size(), activeBorrowings.size());
            
        } catch (Exception e) {
            logger.error("Error warming up borrowed books tracking: {}", e.getMessage());
        }
    }
    
    /**
     * Manual cache warmup method that can be called programmatically
     */
    public void manualWarmup() {
        logger.info("Manual cache warmup initiated");
        warmupCache();
    }
    
    /**
     * Refresh cache for a specific book
     */
    public void refreshBookCache(Long bookId) {
        try {
            Book book = bookRepository.findById(bookId).orElse(null);
            if (book != null) {
                BookResponse bookResponse = new BookResponse(book);
                redisBookCacheService.cacheBookDetail(bookResponse);
                logger.debug("Refreshed cache for book ID: {}", bookId);
            }
        } catch (Exception e) {
            logger.error("Error refreshing cache for book ID {}: {}", bookId, e.getMessage());
        }
    }
}