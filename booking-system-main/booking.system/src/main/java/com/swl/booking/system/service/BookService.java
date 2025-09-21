package com.swl.booking.system.service;

import com.swl.booking.system.entity.Book;
import com.swl.booking.system.request.book.BookRegisterRequest;
import com.swl.booking.system.request.book.BorrowBookRequest;
import com.swl.booking.system.request.book.ReturnBookRequest;
import com.swl.booking.system.response.book.BookListResponse;
import com.swl.booking.system.response.book.BookResponse;

public interface BookService {

    BookResponse registerBook(BookRegisterRequest request);
    
    BookListResponse getAvailableBooksResponse();

    BookListResponse getAllBooks();

    String borrowBook(BorrowBookRequest request, Long userId);

    String returnBook(ReturnBookRequest request, Long userId);

    BookListResponse getBorrowedBooks(Long userId);
}