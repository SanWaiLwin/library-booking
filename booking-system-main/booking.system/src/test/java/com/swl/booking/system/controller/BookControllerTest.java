package com.swl.booking.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swl.booking.system.request.book.BookRegisterRequest;
import com.swl.booking.system.request.book.BorrowBookRequest;
import com.swl.booking.system.request.book.ReturnBookRequest;
import com.swl.booking.system.response.book.BookResponse;
import com.swl.booking.system.response.book.BookListResponse;
import com.swl.booking.system.security.UserPrincipal;
import com.swl.booking.system.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserPrincipal userPrincipal;

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private BookRegisterRequest bookRegisterRequest;
    private BorrowBookRequest borrowBookRequest;
    private ReturnBookRequest returnBookRequest;
    private BookResponse bookResponse;
    private BookListResponse bookListResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
        objectMapper = new ObjectMapper();

        // Setup test data
        bookRegisterRequest = new BookRegisterRequest();
        bookRegisterRequest.setTitle("Test Book");
        bookRegisterRequest.setAuthor("Test Author");
        bookRegisterRequest.setIsbn("1234567890123");

        borrowBookRequest = new BorrowBookRequest();
        borrowBookRequest.setBookId(1L);

        returnBookRequest = new ReturnBookRequest();
        returnBookRequest.setBookId(1L);

        bookResponse = new BookResponse();
        bookResponse.setId(1L);
        bookResponse.setTitle("Test Book");
        bookResponse.setAuthor("Test Author");
        bookResponse.setIsbn("1234567890123");
        bookResponse.setAvailable(true);

        bookListResponse = new BookListResponse(Arrays.asList(bookResponse));
    }

    @Test
    void registerBook_Success_WhenUserIsAdmin() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.isSuperAdmin()).thenReturn(true);
        when(bookService.registerBook(any(BookRegisterRequest.class))).thenReturn(bookResponse);

        // When
        mockMvc.perform(post("/api/auth/book/register-book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRegisterRequest))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"))
                .andExpect(jsonPath("$.isbn").value("1234567890123"))
                .andExpect(jsonPath("$.available").value(true));

        // Then
        verify(bookService).registerBook(any(BookRegisterRequest.class));
        verify(userPrincipal).isSuperAdmin();
    }

    @Test
    void registerBook_ThrowsAccessDeniedException_WhenUserIsNotAdmin() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.isSuperAdmin()).thenReturn(false);

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            bookController.registerBook(bookRegisterRequest, authentication);
        });

        verify(bookService, never()).registerBook(any(BookRegisterRequest.class));
        verify(userPrincipal).isSuperAdmin();
    }

    @Test
    void getAvailableBooks_Success() throws Exception {
        // Given
        when(bookService.getAvailableBooksResponse()).thenReturn(bookListResponse);

        // When
        mockMvc.perform(get("/api/auth/book/available-book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].id").value(1L))
                .andExpect(jsonPath("$.books[0].title").value("Test Book"))
                .andExpect(jsonPath("$.books[0].author").value("Test Author"));

        // Then
        verify(bookService).getAvailableBooksResponse();
    }

    @Test
    void getAllBooks_Success() throws Exception {
        // Given
        when(bookService.getAllBooks()).thenReturn(bookListResponse);

        // When
        mockMvc.perform(get("/api/auth/book/all-book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].id").value(1L))
                .andExpect(jsonPath("$.books[0].title").value("Test Book"));

        // Then
        verify(bookService).getAllBooks();
    }

    @Test
    void borrowBook_Success() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(1L);
        String expectedMessage = "Book borrowed successfully";
        when(bookService.borrowBook(any(BorrowBookRequest.class), eq(1L))).thenReturn(expectedMessage);

        // When
        mockMvc.perform(post("/api/auth/book/borrow-book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowBookRequest))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));

        // Then
        verify(bookService).borrowBook(any(BorrowBookRequest.class), eq(1L));
        verify(userPrincipal).getId();
    }

    @Test
    void borrowBook_ThrowsException_WhenBookNotAvailable() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(1L);
        when(bookService.borrowBook(any(BorrowBookRequest.class), eq(1L)))
                .thenThrow(new RuntimeException("Book not available"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            bookController.borrowBook(borrowBookRequest, authentication);
        });

        verify(bookService).borrowBook(any(BorrowBookRequest.class), eq(1L));
    }

    @Test
    void returnBook_Success() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(1L);
        String expectedMessage = "Book returned successfully";
        when(bookService.returnBook(any(ReturnBookRequest.class), eq(1L))).thenReturn(expectedMessage);

        // When
        mockMvc.perform(post("/api/auth/book/return-book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnBookRequest))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));

        // Then
        verify(bookService).returnBook(any(ReturnBookRequest.class), eq(1L));
        verify(userPrincipal).getId();
    }

    @Test
    void returnBook_ThrowsException_WhenBookNotBorrowed() {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(1L);
        when(bookService.returnBook(any(ReturnBookRequest.class), eq(1L)))
                .thenThrow(new RuntimeException("Book not borrowed by user"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            bookController.returnBook(returnBookRequest, authentication);
        });

        verify(bookService).returnBook(any(ReturnBookRequest.class), eq(1L));
    }

    @Test
    void getMyBorrowedBooks_Success() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(1L);
        when(bookService.getBorrowedBooks(eq(1L))).thenReturn(bookListResponse);

        // When
        mockMvc.perform(get("/api/auth/book/my-borrowed")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].id").value(1L))
                .andExpect(jsonPath("$.books[0].title").value("Test Book"));

        // Then
        verify(bookService).getBorrowedBooks(eq(1L));
        verify(userPrincipal).getId();
    }

    @Test
    void getMyBorrowedBooks_ReturnsEmptyList_WhenNoBorrowedBooks() throws Exception {
        // Given
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(1L);
        BookListResponse emptyResponse = new BookListResponse(Collections.emptyList());
        when(bookService.getBorrowedBooks(eq(1L))).thenReturn(emptyResponse);

        // When
        mockMvc.perform(get("/api/auth/book/my-borrowed")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isEmpty());

        // Then
        verify(bookService).getBorrowedBooks(eq(1L));
    }
}