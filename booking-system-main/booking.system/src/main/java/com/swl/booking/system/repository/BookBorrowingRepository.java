package com.swl.booking.system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swl.booking.system.entity.Book;
import com.swl.booking.system.entity.BookBorrowing;
import com.swl.booking.system.entity.User;

@Repository
public interface BookBorrowingRepository extends JpaRepository<BookBorrowing, Long> {
    
    Optional<BookBorrowing> findByBorrowerAndBookAndIsReturnedFalse(User borrower, Book book);
    
    List<BookBorrowing> findByBorrowerId(Long borrowerId);

    List<BookBorrowing> findByIsReturnedFalse();
}