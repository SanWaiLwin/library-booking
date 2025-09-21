package com.swl.booking.system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swl.booking.system.request.book.BookRegisterRequest;
import com.swl.booking.system.request.book.BorrowBookRequest;
import com.swl.booking.system.request.book.ReturnBookRequest;
import com.swl.booking.system.response.book.BookListResponse;
import com.swl.booking.system.response.book.BookResponse;
import com.swl.booking.system.security.UserPrincipal;
import com.swl.booking.system.service.BookService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth/book")
@Validated
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("/register-book")
    @Operation(summary = "Register a new book (Admin only)", description = "Add a new book to the library system - requires admin privileges")
    public ResponseEntity<BookResponse> registerBook(@Valid @RequestBody BookRegisterRequest request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Check if user is admin
        if (!userPrincipal.isSuperAdmin()) {
            throw new AccessDeniedException("Admin privileges required");
        }
        
        BookResponse response = bookService.registerBook(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-book")
    @Operation(summary = "Get available books", description = "Retrieve all books that are currently available for borrowing")
    public ResponseEntity<BookListResponse> getAvailableBooks() {
        BookListResponse response = bookService.getAvailableBooksResponse();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-book")
    public ResponseEntity<BookListResponse> getAllBooks() {
        BookListResponse response = bookService.getAllBooks();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/borrow-book")
    @Operation(summary = "Borrow a book", description = "Borrow an available book")
    public ResponseEntity<String> borrowBook(@Valid @RequestBody BorrowBookRequest request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        String message = bookService.borrowBook(request, userId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/return-book")
    @Operation(summary = "Return a book", description = "Return a borrowed book")
    public ResponseEntity<String> returnBook(@Valid @RequestBody ReturnBookRequest request, Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        String message = bookService.returnBook(request, userId);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/my-borrowed")
    @Operation(summary = "Get my borrowed books", description = "Retrieve books currently borrowed by the authenticated user")
    public ResponseEntity<BookListResponse> getMyBorrowedBooks(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();
        BookListResponse response = bookService.getBorrowedBooks(userId);
        return ResponseEntity.ok(response);
    }
}