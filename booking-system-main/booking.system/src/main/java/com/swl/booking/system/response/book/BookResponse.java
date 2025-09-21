package com.swl.booking.system.response.book;

import java.io.Serializable;
import java.util.Date;

import com.swl.booking.system.entity.Book;
import lombok.Data;

@Data
public class BookResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String isbn;
    private String title;
    private String author;
    private boolean isAvailable;
    private Date createdTime;
    private Date updatedTime;

    public BookResponse(Book book) {
        if (book != null) {
            this.id = book.getId();
            this.isbn = book.getIsbn();
            this.title = book.getTitle();
            this.author = book.getAuthor();
            this.isAvailable = book.isAvailable();
            this.createdTime = book.getCreatedTime();
            this.updatedTime = book.getUpdatedTime();
        }
    }

    public BookResponse() {
    }
}