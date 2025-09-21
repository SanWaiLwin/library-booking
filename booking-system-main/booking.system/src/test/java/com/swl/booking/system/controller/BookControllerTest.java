package com.swl.booking.system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swl.booking.system.request.book.BookRegisterRequest;
import com.swl.booking.system.request.book.BorrowBookRequest;
import com.swl.booking.system.request.book.ReturnBookRequest;
import com.swl.booking.system.response.book.BookResponse;
import com.swl.booking.system.response.book.BookListResponse;
import com.swl.booking.system.entity.Book;
import com.swl.booking.system.entity.User;
import com.swl.booking.system.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookRegisterRequest bookRegisterRequest;
    private BorrowBookRequest borrowBookRequest;
    private ReturnBookRequest returnBookRequest;
    private BookResponse bookResponse;
    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
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

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setQuantity(5);
        book.setAvailableQuantity(5);

        user = new User();
        user.setId(1L);
        user.setName("testuser");
        user.setEmail("test@example.com");
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void registerBook_Success() throws Exception {
        when(bookService.registerBook(any(BookRegisterRequest.class))).thenReturn(bookResponse);

        mockMvc.perform(post("/api/auth/book/register-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"));

        verify(bookService).registerBook(any(BookRegisterRequest.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void registerBook_Forbidden() throws Exception {
        mockMvc.perform(post("/api/auth/book/register-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRegisterRequest)))
                .andExpect(status().isForbidden());

        verify(bookService, never()).registerBook(any(BookRegisterRequest.class));
    }

    @Test
    @WithMockUser
    void getAvailableBooks_Success() throws Exception {
        BookListResponse bookListResponse = new BookListResponse(Arrays.asList(bookResponse));
        when(bookService.getAvailableBooksResponse()).thenReturn(bookListResponse);

        mockMvc.perform(get("/api/auth/book/available-book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].title").value("Test Book"))
                .andExpect(jsonPath("$.books[0].author").value("Test Author"))
                .andExpect(jsonPath("$.books[0].isbn").value("1234567890123"));

        verify(bookService).getAvailableBooksResponse();
    }

    @Test
    @WithMockUser
    void getAllBooks_Success() throws Exception {
        BookListResponse bookListResponse = new BookListResponse(Arrays.asList(bookResponse));
        when(bookService.getAllBooks()).thenReturn(bookListResponse);

        mockMvc.perform(get("/api/auth/book/all-book"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].title").value("Test Book"))
                .andExpect(jsonPath("$.books[0].author").value("Test Author"));

        verify(bookService).getAllBooks();
    }
    @Test
    @WithMockUser
    void borrowBook_Success() throws Exception {
        when(bookService.borrowBook(any(BorrowBookRequest.class), anyLong()))
                .thenReturn("Book borrowed successfully");

        mockMvc.perform(post("/api/auth/book/borrow-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowBookRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Book borrowed successfully"));

        verify(bookService).borrowBook(any(BorrowBookRequest.class), anyLong());
    }

    @Test
    @WithMockUser
    void borrowBook_BookNotAvailable() throws Exception {
        when(bookService.borrowBook(any(BorrowBookRequest.class), anyLong()))
                .thenThrow(new RuntimeException("Book not available"));

        mockMvc.perform(post("/api/auth/book/borrow-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowBookRequest)))
                .andExpect(status().isBadRequest());

        verify(bookService).borrowBook(any(BorrowBookRequest.class), anyLong());
    }

    @Test
    @WithMockUser
    void returnBook_Success() throws Exception {
        when(bookService.returnBook(any(ReturnBookRequest.class), anyLong())).thenReturn("Book returned successfully");

        mockMvc.perform(post("/api/auth/book/return-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnBookRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string("Book returned successfully"));

        verify(bookService).returnBook(any(ReturnBookRequest.class), anyLong());
    }

    @Test
    @WithMockUser
    void returnBook_BookNotBorrowed() throws Exception {
        when(bookService.returnBook(any(ReturnBookRequest.class), anyLong()))
                .thenThrow(new RuntimeException("Book not borrowed by user"));

        mockMvc.perform(post("/api/auth/book/return-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnBookRequest)))
                .andExpect(status().isBadRequest());

        verify(bookService).returnBook(any(ReturnBookRequest.class), anyLong());
    }

    @Test
    @WithMockUser
    void getMyBorrowedBooks_Success() throws Exception {
        BookListResponse bookListResponse = new BookListResponse(Arrays.asList(bookResponse));
        when(bookService.getBorrowedBooks(anyLong())).thenReturn(bookListResponse);

        mockMvc.perform(get("/api/auth/book/my-borrowed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].title").value("Test Book"))
                .andExpect(jsonPath("$.books[0].author").value("Test Author"));

        verify(bookService).getBorrowedBooks(anyLong());
    }

    @Test
    void registerBook_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/book/register-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRegisterRequest)))
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).registerBook(any(BookRegisterRequest.class));
    }

    @Test
    void borrowBook_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/book/borrow-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(borrowBookRequest)))
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).borrowBook(any(BorrowBookRequest.class), anyLong());
    }

    @Test
    void returnBook_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/book/return-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnBookRequest)))
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).returnBook(any(ReturnBookRequest.class), anyLong());
    }

    @Test
    void getMyBorrowedBooks_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/auth/book/my-borrowed"))
                .andExpect(status().isUnauthorized());

        verify(bookService, never()).getBorrowedBooks(anyLong());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void registerBook_InvalidInput() throws Exception {
        BookRegisterRequest invalidRequest = new BookRegisterRequest();
        // Missing required fields

        mockMvc.perform(post("/api/auth/book/register-book")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}