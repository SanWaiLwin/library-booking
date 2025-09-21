package com.swl.booking.system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.test.util.ReflectionTestUtils;

import com.swl.booking.system.entity.Book;
import com.swl.booking.system.entity.BookBorrowing;
import com.swl.booking.system.entity.User;
import com.swl.booking.system.repository.BookBorrowingRepository;
import com.swl.booking.system.repository.BookRepository;
import com.swl.booking.system.response.book.BookResponse;

@ExtendWith(MockitoExtension.class)
class CacheWarmupServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookBorrowingRepository bookBorrowingRepository;

    @Mock
    private RedisBookCacheService redisBookCacheService;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    @InjectMocks
    private CacheWarmupService cacheWarmupService;

    private Book testBook1;
    private Book testBook2;
    private Book testBook3;
    private List<Book> testBooks;
    private List<Book> availableBooks;
    
    private User testUser1;
    private User testUser2;
    private BookBorrowing testBorrowing1;
    private BookBorrowing testBorrowing2;
    private List<BookBorrowing> activeBorrowings;

    @BeforeEach
    void setUp() {
        // Set up test books
        testBook1 = new Book();
        testBook1.setId(1L);
        testBook1.setTitle("Test Book 1");
        testBook1.setAuthor("Test Author 1");
        testBook1.setIsbn("1234567890123");
        testBook1.setAvailable(true);
        testBook1.setCreatedTime(new Date());
        testBook1.setUpdatedTime(new Date());

        testBook2 = new Book();
        testBook2.setId(2L);
        testBook2.setTitle("Test Book 2");
        testBook2.setAuthor("Test Author 2");
        testBook2.setIsbn("1234567890124");
        testBook2.setAvailable(true);
        testBook2.setCreatedTime(new Date());
        testBook2.setUpdatedTime(new Date());

        testBook3 = new Book();
        testBook3.setId(3L);
        testBook3.setTitle("Test Book 3");
        testBook3.setAuthor("Test Author 3");
        testBook3.setIsbn("1234567890125");
        testBook3.setAvailable(false);
        testBook3.setCreatedTime(new Date());
        testBook3.setUpdatedTime(new Date());

        testBooks = Arrays.asList(testBook1, testBook2, testBook3);
        availableBooks = Arrays.asList(testBook1, testBook2);

        // Set up test users
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setName("testuser1");
        testUser1.setEmail("test1@example.com");

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setName("testuser2");
        testUser2.setEmail("test2@example.com");

        // Set up test borrowings
        testBorrowing1 = new BookBorrowing();
        testBorrowing1.setId(1L);
        testBorrowing1.setBook(testBook3);
        testBorrowing1.setBorrower(testUser1);
        testBorrowing1.setReturned(false);
        testBorrowing1.setBorrowDate(new Date());

        testBorrowing2 = new BookBorrowing();
        testBorrowing2.setId(2L);
        testBorrowing2.setBook(testBook2);
        testBorrowing2.setBorrower(testUser2);
        testBorrowing2.setReturned(false);
        testBorrowing2.setBorrowDate(new Date());

        activeBorrowings = Arrays.asList(testBorrowing1, testBorrowing2);
    }

    @Test
    void warmupCache_Success() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenReturn(availableBooks);
        when(bookRepository.findAll()).thenReturn(testBooks);
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenReturn(activeBorrowings);

        // When
        cacheWarmupService.warmupCache();

        // Then
        // Verify available books caching
        verify(bookRepository).findByIsAvailableTrue();
        verify(redisBookCacheService).cacheAvailableBooks(any(List.class));

        // Verify book details caching
        verify(bookRepository, times(2)).findAll(); // Called in warmupBookDetails and warmupBorrowedBooksTracking
        verify(redisBookCacheService, times(3)).cacheBookDetail(any(BookResponse.class));

        // Verify borrowed books tracking
        verify(bookBorrowingRepository).findByIsReturnedFalse();
        verify(redisBookCacheService).cacheBorrowedBooks(eq(1L), any(List.class));
        verify(redisBookCacheService).cacheBorrowedBooks(eq(2L), any(List.class));
        verify(redisBookCacheService).warmUpCache(any(List.class), any(Set.class));
    }

    @Test
    void warmupCache_ExceptionInAvailableBooks() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenThrow(new RuntimeException("Database error"));
        when(bookRepository.findAll()).thenReturn(testBooks);
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenReturn(activeBorrowings);

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheWarmupService.warmupCache());

        // Verify other operations still proceed
        verify(bookRepository, times(2)).findAll(); // Called in warmupBookDetails and warmupBorrowedBooksTracking
        verify(bookBorrowingRepository).findByIsReturnedFalse();
    }

    @Test
    void warmupCache_ExceptionInBookDetails() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenReturn(availableBooks);
        when(bookRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenReturn(activeBorrowings);

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheWarmupService.warmupCache());

        // Verify other operations still proceed
        verify(bookRepository).findByIsAvailableTrue();
        verify(bookBorrowingRepository).findByIsReturnedFalse();
    }

    @Test
    void warmupCache_ExceptionInBorrowedBooks() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenReturn(availableBooks);
        when(bookRepository.findAll()).thenReturn(testBooks);
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenThrow(new RuntimeException("Database error"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheWarmupService.warmupCache());

        // Verify other operations still proceed
        verify(bookRepository).findByIsAvailableTrue();
        verify(bookRepository, times(1)).findAll(); // Only called in warmupBookDetails (warmupBorrowedBooksTracking fails early)
     }

    @Test
    void warmupAvailableBooks_Success() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenReturn(availableBooks);

        // When
        ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupAvailableBooks");

        // Then
        verify(bookRepository).findByIsAvailableTrue();
        verify(redisBookCacheService).cacheAvailableBooks(argThat(books -> 
            books.size() == 2 && 
            books.stream().anyMatch(book -> ((BookResponse) book).getTitle().equals("Test Book 1")) &&
            books.stream().anyMatch(book -> ((BookResponse) book).getTitle().equals("Test Book 2"))
        ));
    }

    @Test
    void warmupAvailableBooks_EmptyList() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenReturn(Collections.emptyList());

        // When
        ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupAvailableBooks");

        // Then
        verify(bookRepository).findByIsAvailableTrue();
        verify(redisBookCacheService).cacheAvailableBooks(argThat(books -> books.isEmpty()));
    }

    @Test
    void warmupAvailableBooks_Exception() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenThrow(new RuntimeException("Database connection failed"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupAvailableBooks"));

        // Verify repository was called
        verify(bookRepository).findByIsAvailableTrue();
        // Verify cache service was not called due to exception
         verify(redisBookCacheService, never()).cacheAvailableBooks(any());
     }

    @Test
    void warmupBookDetails_Success() {
        // Given
        when(bookRepository.findAll()).thenReturn(testBooks);

        // When
        ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupBookDetails");

        // Then
        verify(bookRepository, times(1)).findAll(); // Only called once in warmupBookDetails method
        verify(redisBookCacheService, times(3)).cacheBookDetail(any(BookResponse.class));
        
        // Verify each book was cached
        verify(redisBookCacheService).cacheBookDetail(argThat(book -> book.getId().equals(1L)));
        verify(redisBookCacheService).cacheBookDetail(argThat(book -> book.getId().equals(2L)));
        verify(redisBookCacheService).cacheBookDetail(argThat(book -> book.getId().equals(3L)));
    }

    @Test
    void warmupBookDetails_EmptyList() {
        // Given
        when(bookRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupBookDetails");

        // Then
        verify(bookRepository, times(1)).findAll(); // Only called once in warmupBookDetails method
        verify(redisBookCacheService, never()).cacheBookDetail(any(BookResponse.class));
    }

    @Test
    void warmupBookDetails_Exception() {
        // Given
        when(bookRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupBookDetails"));

        // Verify repository was called
        verify(bookRepository, times(1)).findAll(); // Only called once in warmupBookDetails method
        // Verify cache service was not called due to exception
         verify(redisBookCacheService, never()).cacheBookDetail(any(BookResponse.class));
     }

    @Test
    void warmupBorrowedBooksTracking_Success() {
        // Given
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenReturn(activeBorrowings);
        when(bookRepository.findAll()).thenReturn(testBooks);

        // When
        ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupBorrowedBooksTracking");

        // Then
        verify(bookBorrowingRepository).findByIsReturnedFalse();
        verify(bookRepository, times(1)).findAll(); // Only called once in warmupBorrowedBooksTracking method
        
        // Verify borrowed books cached for each user
        verify(redisBookCacheService).cacheBorrowedBooks(eq(1L), argThat(books -> 
            books.size() == 1 && ((BookResponse) books.get(0)).getId().equals(3L)
        ));
        verify(redisBookCacheService).cacheBorrowedBooks(eq(2L), argThat(books -> 
            books.size() == 1 && ((BookResponse) books.get(0)).getId().equals(2L)
        ));
        
        // Verify warmup cache called with borrowed book IDs
        verify(redisBookCacheService).warmUpCache(any(List.class), argThat(borrowedIds -> 
            borrowedIds.contains(2L) && borrowedIds.contains(3L) && borrowedIds.size() == 2
        ));
    }

    @Test
    void warmupBorrowedBooksTracking_EmptyBorrowings() {
        // Given
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenReturn(Collections.emptyList());
        when(bookRepository.findAll()).thenReturn(testBooks);

        // When
        ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupBorrowedBooksTracking");

        // Then
        verify(bookBorrowingRepository).findByIsReturnedFalse();
        verify(bookRepository, times(1)).findAll(); // Only called once in warmupBorrowedBooksTracking method
        
        // Verify no user-specific caching
        verify(redisBookCacheService, never()).cacheBorrowedBooks(anyLong(), any());
        
        // Verify warmup cache called with empty borrowed book IDs
        verify(redisBookCacheService).warmUpCache(any(List.class), argThat(borrowedIds -> 
            borrowedIds.isEmpty()
        ));
    }

    @Test
    void warmupBorrowedBooksTracking_Exception() {
        // Given
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenThrow(new RuntimeException("Database error"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> ReflectionTestUtils.invokeMethod(cacheWarmupService, "warmupBorrowedBooksTracking"));

        // Verify repository was called
        verify(bookBorrowingRepository).findByIsReturnedFalse();
        // Verify cache service was not called due to exception
         verify(redisBookCacheService, never()).cacheBorrowedBooks(anyLong(), any());
         verify(redisBookCacheService, never()).warmUpCache(any(), any());
     }

    @Test
    void manualWarmup_Success() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenReturn(availableBooks);
        when(bookRepository.findAll()).thenReturn(testBooks);
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenReturn(activeBorrowings);

        // When
        cacheWarmupService.manualWarmup();

        // Then - verify all warmup operations are called
        verify(bookRepository).findByIsAvailableTrue();
        verify(bookRepository, times(2)).findAll(); // Called twice: once for book details, once for borrowed books tracking
        verify(bookBorrowingRepository).findByIsReturnedFalse();
        verify(redisBookCacheService).cacheAvailableBooks(any(List.class));
        verify(redisBookCacheService, times(3)).cacheBookDetail(any(BookResponse.class));
        verify(redisBookCacheService, times(2)).cacheBorrowedBooks(anyLong(), any(List.class));
        verify(redisBookCacheService).warmUpCache(any(List.class), any(Set.class));
    }

    @Test
    void refreshBookCache_Success() {
        // Given
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(testBook1));

        // When
        cacheWarmupService.refreshBookCache(bookId);

        // Then
        verify(bookRepository).findById(bookId);
        verify(redisBookCacheService).cacheBookDetail(argThat(book -> book.getId().equals(bookId)));
    }

    @Test
    void refreshBookCache_BookNotFound() {
        // Given
        Long bookId = 999L;
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        // When
        cacheWarmupService.refreshBookCache(bookId);

        // Then
        verify(bookRepository).findById(bookId);
        verify(redisBookCacheService, never()).cacheBookDetail(any(BookResponse.class));
    }

    @Test
    void refreshBookCache_Exception() {
        // Given
        Long bookId = 1L;
        when(bookRepository.findById(bookId)).thenThrow(new RuntimeException("Database error"));

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> cacheWarmupService.refreshBookCache(bookId));

        // Verify repository was called
        verify(bookRepository).findById(bookId);
        // Verify cache service was not called due to exception
        verify(redisBookCacheService, never()).cacheBookDetail(any(BookResponse.class));
    }

    @Test
    void applicationReadyEvent_TriggersWarmup() {
        // Given
        when(bookRepository.findByIsAvailableTrue()).thenReturn(availableBooks);
        when(bookRepository.findAll()).thenReturn(testBooks);
        when(bookBorrowingRepository.findByIsReturnedFalse()).thenReturn(activeBorrowings);

        // When
        cacheWarmupService.warmupCache();

        // Then - verify warmup is triggered
        verify(bookRepository).findByIsAvailableTrue();
        verify(bookRepository, times(2)).findAll();
        verify(bookBorrowingRepository).findByIsReturnedFalse();
        verify(redisBookCacheService).cacheAvailableBooks(any(List.class));
        verify(redisBookCacheService, times(3)).cacheBookDetail(any(BookResponse.class));
        verify(redisBookCacheService, times(2)).cacheBorrowedBooks(anyLong(), any(List.class));
        verify(redisBookCacheService).warmUpCache(any(List.class), any(Set.class));
    }
}