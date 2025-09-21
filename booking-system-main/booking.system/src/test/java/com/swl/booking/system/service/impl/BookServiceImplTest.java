package com.swl.booking.system.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.swl.booking.system.entity.Book;
import com.swl.booking.system.entity.BookBorrowing;
import com.swl.booking.system.entity.User;
import com.swl.booking.system.exception.AlreadyExitException;
import com.swl.booking.system.exception.ResponseInfoException;
import com.swl.booking.system.repository.BookBorrowingRepository;
import com.swl.booking.system.repository.BookRepository;
import com.swl.booking.system.repository.UserRepository;
import com.swl.booking.system.request.book.BookRegisterRequest;
import com.swl.booking.system.request.book.BorrowBookRequest;
import com.swl.booking.system.request.book.ReturnBookRequest;
import com.swl.booking.system.response.book.BookListResponse;
import com.swl.booking.system.response.book.BookResponse;
import com.swl.booking.system.service.RedisBookCacheService;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookBorrowingRepository bookBorrowingRepository;

    @Mock
    private RedisBookCacheService redisBookCacheService;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book testBook;
    private User testUser;
    private BookBorrowing testBorrowing;
    private BookRegisterRequest bookRegisterRequest;
    private BorrowBookRequest borrowBookRequest;
    private ReturnBookRequest returnBookRequest;
    private BookResponse bookResponse;

    @BeforeEach
    void setUp() {
        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setIsbn("9781234567890");
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setAvailable(true);
        testBook.setCreatedTime(new Date());
        testBook.setUpdatedTime(new Date());

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("testuser");
        testUser.setEmail("test@example.com");

        // Setup test borrowing
        testBorrowing = new BookBorrowing();
        testBorrowing.setId(1L);
        testBorrowing.setBorrower(testUser);
        testBorrowing.setBook(testBook);
        testBorrowing.setBorrowDate(new Date());
        testBorrowing.setReturned(false);

        // Setup request objects
        bookRegisterRequest = new BookRegisterRequest();
        bookRegisterRequest.setIsbn("9781234567890");
        bookRegisterRequest.setTitle("Test Book");
        bookRegisterRequest.setAuthor("Test Author");

        borrowBookRequest = new BorrowBookRequest();
        borrowBookRequest.setBookId(1L);

        returnBookRequest = new ReturnBookRequest();
        returnBookRequest.setBookId(1L);

        // Setup response object
        bookResponse = new BookResponse(testBook);
    }

    @Test
    void registerBook_Success() {
        // Given
        when(bookRepository.countByIsbn(bookRegisterRequest.getIsbn())).thenReturn(0L);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        BookResponse result = bookService.registerBook(bookRegisterRequest);

        // Then
        assertNotNull(result);
        assertEquals(testBook.getId(), result.getId());
        assertEquals(testBook.getIsbn(), result.getIsbn());
        assertEquals(testBook.getTitle(), result.getTitle());
        assertEquals(testBook.getAuthor(), result.getAuthor());
        assertTrue(result.isAvailable());

        verify(bookRepository).countByIsbn(bookRegisterRequest.getIsbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void registerBook_ThrowsException_WhenBookAlreadyExists() {
        // Given
        when(bookRepository.countByIsbn(bookRegisterRequest.getIsbn())).thenReturn(1L);

        // When & Then
        AlreadyExitException exception = assertThrows(AlreadyExitException.class, () -> {
            bookService.registerBook(bookRegisterRequest);
        });

        assertEquals("Book with ISBN " + bookRegisterRequest.getIsbn() + " already exists", exception.getMessage());
        verify(bookRepository).countByIsbn(bookRegisterRequest.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getAvailableBooksResponse_CacheHit() {
        // Given
        List<BookResponse> cachedBooks = Arrays.asList(bookResponse);
        when(redisBookCacheService.getCachedAvailableBooks()).thenReturn(cachedBooks);

        // When
        BookListResponse result = bookService.getAvailableBooksResponse();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalBooks());
        assertEquals(1, result.getBooks().size());
        assertEquals(bookResponse.getId(), result.getBooks().get(0).getId());

        verify(redisBookCacheService).getCachedAvailableBooks();
        verify(bookRepository, never()).findByIsAvailableTrue();
        verify(redisBookCacheService, never()).cacheAvailableBooks(any());
    }

    @Test
    void getAvailableBooksResponse_CacheMiss() {
        // Given
        when(redisBookCacheService.getCachedAvailableBooks()).thenReturn(null);
        when(bookRepository.findByIsAvailableTrue()).thenReturn(Arrays.asList(testBook));

        // When
        BookListResponse result = bookService.getAvailableBooksResponse();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalBooks());
        assertEquals(1, result.getBooks().size());
        assertEquals(testBook.getId(), result.getBooks().get(0).getId());

        verify(redisBookCacheService).getCachedAvailableBooks();
        verify(bookRepository).findByIsAvailableTrue();
        verify(redisBookCacheService).cacheAvailableBooks(any());
    }

    @Test
    void getAllBooks_Success() {
        // Given
        when(bookRepository.findAll()).thenReturn(Arrays.asList(testBook));

        // When
        BookListResponse result = bookService.getAllBooks();

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalBooks());
        assertEquals(1, result.getBooks().size());
        assertEquals(testBook.getId(), result.getBooks().get(0).getId());

        verify(bookRepository).findAll();
    }

    @Test
    void borrowBook_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(borrowBookRequest.getBookId())).thenReturn(Optional.of(testBook));
        when(bookBorrowingRepository.findByBorrowerAndBookAndIsReturnedFalse(testUser, testBook))
                .thenReturn(Optional.empty());
        when(bookBorrowingRepository.save(any(BookBorrowing.class))).thenReturn(testBorrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        String result = bookService.borrowBook(borrowBookRequest, userId);

        // Then
        assertEquals("Book borrowed successfully", result);
        verify(userRepository).findById(userId);
        verify(bookRepository).findById(borrowBookRequest.getBookId());
        verify(bookBorrowingRepository).findByBorrowerAndBookAndIsReturnedFalse(testUser, testBook);
        verify(bookBorrowingRepository).save(any(BookBorrowing.class));
        verify(bookRepository).save(any(Book.class));
        verify(redisBookCacheService).invalidateAvailableBooksCache();
        verify(redisBookCacheService).invalidateUserBorrowedBooksCache(userId);
        verify(redisBookCacheService).invalidateBookDetailCache(testBook.getId());
        verify(redisBookCacheService).cacheBookDetail(any(BookResponse.class));
    }

    @Test
    void borrowBook_ThrowsException_WhenUserNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResponseInfoException exception = assertThrows(ResponseInfoException.class, () -> {
            bookService.borrowBook(borrowBookRequest, userId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(bookRepository, never()).findById(any());
    }

    @Test
    void borrowBook_ThrowsException_WhenBookNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(borrowBookRequest.getBookId())).thenReturn(Optional.empty());

        // When & Then
        ResponseInfoException exception = assertThrows(ResponseInfoException.class, () -> {
            bookService.borrowBook(borrowBookRequest, userId);
        });

        assertEquals("Book not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(bookRepository).findById(borrowBookRequest.getBookId());
    }

    @Test
    void borrowBook_ThrowsException_WhenBookNotAvailable() {
        // Given
        Long userId = 1L;
        testBook.setAvailable(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(borrowBookRequest.getBookId())).thenReturn(Optional.of(testBook));

        // When & Then
        ResponseInfoException exception = assertThrows(ResponseInfoException.class, () -> {
            bookService.borrowBook(borrowBookRequest, userId);
        });

        assertEquals("Book is not available for borrowing", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(bookRepository).findById(borrowBookRequest.getBookId());
    }

    @Test
    void borrowBook_ThrowsException_WhenAlreadyBorrowed() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(borrowBookRequest.getBookId())).thenReturn(Optional.of(testBook));
        when(bookBorrowingRepository.findByBorrowerAndBookAndIsReturnedFalse(testUser, testBook))
                .thenReturn(Optional.of(testBorrowing));

        // When & Then
        AlreadyExitException exception = assertThrows(AlreadyExitException.class, () -> {
            bookService.borrowBook(borrowBookRequest, userId);
        });

        assertEquals("You have already borrowed this book", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(bookRepository).findById(borrowBookRequest.getBookId());
        verify(bookBorrowingRepository).findByBorrowerAndBookAndIsReturnedFalse(testUser, testBook);
    }

    @Test
    void returnBook_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(returnBookRequest.getBookId())).thenReturn(Optional.of(testBook));
        when(bookBorrowingRepository.findByBorrowerAndBookAndIsReturnedFalse(testUser, testBook))
                .thenReturn(Optional.of(testBorrowing));
        when(bookBorrowingRepository.save(any(BookBorrowing.class))).thenReturn(testBorrowing);
        when(bookRepository.save(any(Book.class))).thenReturn(testBook);

        // When
        String result = bookService.returnBook(returnBookRequest, userId);

        // Then
        assertEquals("Book returned successfully", result);
        verify(userRepository).findById(userId);
        verify(bookRepository).findById(returnBookRequest.getBookId());
        verify(bookBorrowingRepository).findByBorrowerAndBookAndIsReturnedFalse(testUser, testBook);
        verify(bookBorrowingRepository).save(any(BookBorrowing.class));
        verify(bookRepository).save(any(Book.class));
        verify(redisBookCacheService).invalidateAvailableBooksCache();
        verify(redisBookCacheService).invalidateUserBorrowedBooksCache(userId);
        verify(redisBookCacheService).invalidateBookDetailCache(testBook.getId());
        verify(redisBookCacheService).cacheBookDetail(any(BookResponse.class));
    }

    @Test
    void returnBook_ThrowsException_WhenUserNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        ResponseInfoException exception = assertThrows(ResponseInfoException.class, () -> {
            bookService.returnBook(returnBookRequest, userId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(bookRepository, never()).findById(any());
    }

    @Test
    void returnBook_ThrowsException_WhenBookNotFound() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(returnBookRequest.getBookId())).thenReturn(Optional.empty());

        // When & Then
        ResponseInfoException exception = assertThrows(ResponseInfoException.class, () -> {
            bookService.returnBook(returnBookRequest, userId);
        });

        assertEquals("Book not found", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(bookRepository).findById(returnBookRequest.getBookId());
    }

    @Test
    void returnBook_ThrowsException_WhenNoActiveBorrowing() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(returnBookRequest.getBookId())).thenReturn(Optional.of(testBook));
        when(bookBorrowingRepository.findByBorrowerAndBookAndIsReturnedFalse(testUser, testBook))
                .thenReturn(Optional.empty());

        // When & Then
        ResponseInfoException exception = assertThrows(ResponseInfoException.class, () -> {
            bookService.returnBook(returnBookRequest, userId);
        });

        assertEquals("No active borrowing record found for this book", exception.getMessage());
        verify(userRepository).findById(userId);
        verify(bookRepository).findById(returnBookRequest.getBookId());
        verify(bookBorrowingRepository).findByBorrowerAndBookAndIsReturnedFalse(testUser, testBook);
    }

    @Test
    void getBorrowedBooks_CacheHit() {
        // Given
        Long userId = 1L;
        List<BookResponse> cachedBooks = Arrays.asList(bookResponse);
        when(redisBookCacheService.getCachedBorrowedBooks(userId)).thenReturn(cachedBooks);

        // When
        BookListResponse result = bookService.getBorrowedBooks(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalBooks());
        assertEquals(1, result.getBooks().size());
        assertEquals(bookResponse.getId(), result.getBooks().get(0).getId());

        verify(redisBookCacheService).getCachedBorrowedBooks(userId);
        verify(bookBorrowingRepository, never()).findByBorrowerId(any());
        verify(redisBookCacheService, never()).cacheBorrowedBooks(any(), any());
    }

    @Test
    void getBorrowedBooks_CacheMiss() {
        // Given
        Long userId = 1L;
        when(redisBookCacheService.getCachedBorrowedBooks(userId)).thenReturn(null);
        when(bookBorrowingRepository.findByBorrowerId(userId)).thenReturn(Arrays.asList(testBorrowing));

        // When
        BookListResponse result = bookService.getBorrowedBooks(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalBooks());
        assertEquals(1, result.getBooks().size());
        assertEquals(testBook.getId(), result.getBooks().get(0).getId());

        verify(redisBookCacheService).getCachedBorrowedBooks(userId);
        verify(bookBorrowingRepository).findByBorrowerId(userId);
        verify(redisBookCacheService).cacheBorrowedBooks(eq(userId), any());
    }
}