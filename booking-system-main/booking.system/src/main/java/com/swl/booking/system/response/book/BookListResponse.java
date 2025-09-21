package com.swl.booking.system.response.book;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class BookListResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<BookResponse> books;
    private int totalBooks;

    public BookListResponse() {
    }

    public BookListResponse(List<BookResponse> books) {
        this.books = books;
        this.totalBooks = books != null ? books.size() : 0;
    }
}