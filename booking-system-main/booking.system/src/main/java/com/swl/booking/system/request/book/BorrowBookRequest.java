package com.swl.booking.system.request.book;

import java.io.Serializable;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowBookRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "Book ID is required")
    @Min(value = 1, message = "Book ID must be greater than 0")
    private Long bookId;
}