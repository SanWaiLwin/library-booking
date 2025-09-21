package com.swl.booking.system.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.swl.booking.system.response.book.BookResponse;

@ExtendWith(MockitoExtension.class)
class RedisBookCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private SetOperations<String, Object> setOperations;

    @InjectMocks
    private RedisBookCacheService redisBookCacheService;

    private BookResponse testBookResponse;
    private List<BookResponse> testBookResponses;
    private final String keyPrefix = "booking:book";
    private final long availableBooksTtl = 300L;
    private final long borrowedBooksTtl = 600L;
    private final long bookDetailTtl = 1800L;

    @BeforeEach
    void setUp() {
        // Set up mock operations with lenient stubbing to avoid unnecessary stubbing exceptions
        lenient().when(redisTemplate.opsForList()).thenReturn(listOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForSet()).thenReturn(setOperations);

        // Set up test data
        testBookResponse = new BookResponse();
        testBookResponse.setId(1L);
        testBookResponse.setTitle("Test Book");
        testBookResponse.setAuthor("Test Author");
        testBookResponse.setIsbn("1234567890123");
        testBookResponse.setAvailable(true);
        testBookResponse.setCreatedTime(new Date());
        testBookResponse.setUpdatedTime(new Date());

        testBookResponses = Arrays.asList(testBookResponse);

        // Set private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(redisBookCacheService, "keyPrefix", keyPrefix);
        ReflectionTestUtils.setField(redisBookCacheService, "availableBooksTtl", availableBooksTtl);
        ReflectionTestUtils.setField(redisBookCacheService, "borrowedBooksTtl", borrowedBooksTtl);
        ReflectionTestUtils.setField(redisBookCacheService, "bookDetailTtl", bookDetailTtl);
    }

    @Test
    void cacheAvailableBooks_Success() {
        // Given
        String expectedKey = keyPrefix + ":available";
        
        // When
        redisBookCacheService.cacheAvailableBooks(testBookResponses);
        
        // Then
        verify(redisTemplate).delete(expectedKey);
        verify(listOperations).rightPushAll(expectedKey, testBookResponses.toArray());
        verify(redisTemplate).expire(expectedKey, availableBooksTtl, TimeUnit.SECONDS);
    }

    @Test
    void cacheAvailableBooks_EmptyList() {
        // Given
        String expectedKey = keyPrefix + ":available";
        List<BookResponse> emptyList = new ArrayList<>();
        
        // When
        redisBookCacheService.cacheAvailableBooks(emptyList);
        
        // Then
        verify(redisTemplate).delete(expectedKey);
        verify(listOperations, never()).rightPushAll(any(), (Object[]) any());
        verify(redisTemplate, never()).expire(any(), anyLong(), any());
    }

    @Test
    void getCachedAvailableBooks_CacheHit() {
        // Given
        String expectedKey = keyPrefix + ":available";
        List<Object> cachedObjects = Arrays.asList((Object) testBookResponse);
        when(listOperations.range(expectedKey, 0, -1)).thenReturn(cachedObjects);
        
        // When
        List<BookResponse> result = redisBookCacheService.getCachedAvailableBooks();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBookResponse.getId(), result.get(0).getId());
        verify(listOperations).range(expectedKey, 0, -1);
    }

    @Test
    void getCachedAvailableBooks_CacheMiss() {
        // Given
        String expectedKey = keyPrefix + ":available";
        when(listOperations.range(expectedKey, 0, -1)).thenReturn(null);
        
        // When
        List<BookResponse> result = redisBookCacheService.getCachedAvailableBooks();
        
        // Then
        assertNull(result);
        verify(listOperations).range(expectedKey, 0, -1);
    }

    @Test
    void getCachedAvailableBooks_EmptyCache() {
        // Given
        String expectedKey = keyPrefix + ":available";
        when(listOperations.range(expectedKey, 0, -1)).thenReturn(new ArrayList<>());
        
        // When
        List<BookResponse> result = redisBookCacheService.getCachedAvailableBooks();
        
        // Then
        assertNull(result);
        verify(listOperations).range(expectedKey, 0, -1);
    }

    @Test
    void cacheBorrowedBooks_Success() {
        // Given
        Long userId = 1L;
        String expectedKey = keyPrefix + ":borrowed:" + userId;
        
        // When
        redisBookCacheService.cacheBorrowedBooks(userId, testBookResponses);
        
        // Then
        verify(redisTemplate).delete(expectedKey);
        verify(listOperations).rightPushAll(expectedKey, testBookResponses.toArray());
        verify(redisTemplate).expire(expectedKey, borrowedBooksTtl, TimeUnit.SECONDS);
    }

    @Test
    void cacheBorrowedBooks_EmptyList() {
        // Given
        Long userId = 1L;
        String expectedKey = keyPrefix + ":borrowed:" + userId;
        List<BookResponse> emptyList = new ArrayList<>();
        
        // When
        redisBookCacheService.cacheBorrowedBooks(userId, emptyList);
        
        // Then
        verify(redisTemplate).delete(expectedKey);
        verify(listOperations, never()).rightPushAll(any(), (Object[]) any());
        verify(redisTemplate, never()).expire(any(), anyLong(), any());
    }

    @Test
    void getCachedBorrowedBooks_CacheHit() {
        // Given
        Long userId = 1L;
        String expectedKey = keyPrefix + ":borrowed:" + userId;
        List<Object> cachedObjects = Arrays.asList((Object) testBookResponse);
        when(listOperations.range(expectedKey, 0, -1)).thenReturn(cachedObjects);
        
        // When
        List<BookResponse> result = redisBookCacheService.getCachedBorrowedBooks(userId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testBookResponse.getId(), result.get(0).getId());
        verify(listOperations).range(expectedKey, 0, -1);
    }

    @Test
    void getCachedBorrowedBooks_CacheMiss() {
        // Given
        Long userId = 1L;
        String expectedKey = keyPrefix + ":borrowed:" + userId;
        when(listOperations.range(expectedKey, 0, -1)).thenReturn(null);
        
        // When
        List<BookResponse> result = redisBookCacheService.getCachedBorrowedBooks(userId);
        
        // Then
        assertNull(result);
        verify(listOperations).range(expectedKey, 0, -1);
    }

    @Test
    void cacheBookDetail_Success() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":detail:" + bookId;
        
        // When
        redisBookCacheService.cacheBookDetail(testBookResponse);
        
        // Then
        verify(valueOperations).set(expectedKey, testBookResponse, bookDetailTtl, TimeUnit.SECONDS);
    }

    @Test
    void getCachedBookDetail_CacheHit() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":detail:" + bookId;
        when(valueOperations.get(expectedKey)).thenReturn(testBookResponse);
        
        // When
        BookResponse result = redisBookCacheService.getCachedBookDetail(bookId);
        
        // Then
        assertNotNull(result);
        assertEquals(testBookResponse.getId(), result.getId());
        assertEquals(testBookResponse.getTitle(), result.getTitle());
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void getCachedBookDetail_CacheMiss() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":detail:" + bookId;
        when(valueOperations.get(expectedKey)).thenReturn(null);
        
        // When
        BookResponse result = redisBookCacheService.getCachedBookDetail(bookId);
        
        // Then
        assertNull(result);
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void addToBorrowedBooks_Success() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":borrowed-set";
        
        // When
        redisBookCacheService.addToBorrowedBooks(bookId);
        
        // Then
        verify(setOperations).add(expectedKey, bookId);
        verify(redisTemplate).expire(expectedKey, borrowedBooksTtl, TimeUnit.SECONDS);
    }

    @Test
    void removeFromBorrowedBooks_Success() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":borrowed-set";
        
        // When
        redisBookCacheService.removeFromBorrowedBooks(bookId);
        
        // Then
        verify(setOperations).remove(expectedKey, bookId);
    }

    @Test
    void isBookBorrowed_ReturnsTrue() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":borrowed-set";
        when(setOperations.isMember(expectedKey, bookId)).thenReturn(true);
        
        // When
        boolean result = redisBookCacheService.isBookBorrowed(bookId);
        
        // Then
        assertTrue(result);
        verify(setOperations).isMember(expectedKey, bookId);
    }

    @Test
    void isBookBorrowed_ReturnsFalse() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":borrowed-set";
        when(setOperations.isMember(expectedKey, bookId)).thenReturn(false);
        
        // When
        boolean result = redisBookCacheService.isBookBorrowed(bookId);
        
        // Then
        assertFalse(result);
        verify(setOperations).isMember(expectedKey, bookId);
    }

    @Test
    void isBookBorrowed_ReturnsNull() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":borrowed-set";
        when(setOperations.isMember(expectedKey, bookId)).thenReturn(null);
        
        // When
        boolean result = redisBookCacheService.isBookBorrowed(bookId);
        
        // Then
        assertFalse(result);
        verify(setOperations).isMember(expectedKey, bookId);
    }

    @Test
    void invalidateAllBookCaches_Success() {
        // Given
        String keyPattern = keyPrefix + ":*";
        Set<String> mockKeys = Set.of(
            keyPrefix + ":available",
            keyPrefix + ":borrowed:1",
            keyPrefix + ":detail:1"
        );
        when(redisTemplate.keys(keyPattern)).thenReturn(mockKeys);
        
        // When
        redisBookCacheService.invalidateAllBookCaches();
        
        // Then
        verify(redisTemplate).keys(keyPattern);
        verify(redisTemplate).delete(mockKeys);
    }

    @Test
    void invalidateAllBookCaches_NoKeys() {
        // Given
        String keyPattern = keyPrefix + ":*";
        when(redisTemplate.keys(keyPattern)).thenReturn(null);
        
        // When
        redisBookCacheService.invalidateAllBookCaches();
        
        // Then
        verify(redisTemplate).keys(keyPattern);
        verify(redisTemplate, never()).delete(any(Set.class));
    }

    @Test
    void invalidateUserBorrowedBooksCache_Success() {
        // Given
        Long userId = 1L;
        String expectedKey = keyPrefix + ":borrowed:" + userId;
        
        // When
        redisBookCacheService.invalidateUserBorrowedBooksCache(userId);
        
        // Then
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    void invalidateAvailableBooksCache_Success() {
        // Given
        String expectedKey = keyPrefix + ":available";
        
        // When
        redisBookCacheService.invalidateAvailableBooksCache();
        
        // Then
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    void invalidateBookDetailCache_Success() {
        // Given
        Long bookId = 1L;
        String expectedKey = keyPrefix + ":detail:" + bookId;
        
        // When
        redisBookCacheService.invalidateBookDetailCache(bookId);
        
        // Then
        verify(redisTemplate).delete(expectedKey);
    }

    @Test
    void warmUpCache_Success() {
        // Given
        Set<Long> borrowedBookIds = Set.of(2L, 3L);
        String borrowedSetKey = keyPrefix + ":borrowed-set";
        
        // When
        redisBookCacheService.warmUpCache(testBookResponses, borrowedBookIds);
        
        // Then
        // Verify borrowed books set operations
        verify(redisTemplate).delete(borrowedSetKey);
        verify(setOperations).add(borrowedSetKey, borrowedBookIds.toArray());
        verify(redisTemplate).expire(borrowedSetKey, borrowedBooksTtl, TimeUnit.SECONDS);
        
        // Verify individual book details caching
        for (BookResponse book : testBookResponses) {
            String bookDetailKey = keyPrefix + ":detail:" + book.getId();
            verify(valueOperations).set(bookDetailKey, book, bookDetailTtl, TimeUnit.SECONDS);
        }
    }

    @Test
    void warmUpCache_EmptyBorrowedBooks() {
        // Given
        Set<Long> emptyBorrowedBookIds = new HashSet<>();
        String borrowedSetKey = keyPrefix + ":borrowed-set";
        
        // When
        redisBookCacheService.warmUpCache(testBookResponses, emptyBorrowedBookIds);
        
        // Then
        // Verify no borrowed books set operations for empty set
        verify(redisTemplate, never()).delete(borrowedSetKey);
        verify(setOperations, never()).add(eq(borrowedSetKey), any());
        
        // Verify individual book details caching still happens
        for (BookResponse book : testBookResponses) {
            String bookDetailKey = keyPrefix + ":detail:" + book.getId();
            verify(valueOperations).set(bookDetailKey, book, bookDetailTtl, TimeUnit.SECONDS);
        }
    }

    @Test
    void warmUpCache_NullBorrowedBooks() {
        // Given
        String borrowedSetKey = keyPrefix + ":borrowed-set";
        
        // When
        redisBookCacheService.warmUpCache(testBookResponses, null);
        
        // Then
        // Verify no borrowed books set operations for null set
        verify(redisTemplate, never()).delete(borrowedSetKey);
        verify(setOperations, never()).add(eq(borrowedSetKey), any());
        
        // Verify individual book details caching still happens
        for (BookResponse book : testBookResponses) {
            String bookDetailKey = keyPrefix + ":detail:" + book.getId();
            verify(valueOperations).set(bookDetailKey, book, bookDetailTtl, TimeUnit.SECONDS);
        }
    }
}