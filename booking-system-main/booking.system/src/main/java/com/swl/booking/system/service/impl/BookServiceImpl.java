package com.swl.booking.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.swl.booking.system.dto.BookDto;
import com.swl.booking.system.entity.Book;
import com.swl.booking.system.entity.BookBorrowing;
import com.swl.booking.system.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.swl.booking.system.service.BookService;
import com.swl.booking.system.service.RedisBookCacheService;

@Service
@Transactional
public class BookServiceImpl implements BookService {

    private static final Logger logger = LoggerFactory.getLogger(BookServiceImpl.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookBorrowingRepository bookBorrowingRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RedisBookCacheService redisBookCacheService;

    @Override
    public BookResponse registerBook(BookRegisterRequest request) {
        if (bookRepository.countByIsbn(request.getIsbn()) > 0) {
            throw new AlreadyExitException("Book with ISBN " + request.getIsbn() + " already exists");
        }

        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setAvailable(true);
        book.setCreatedTime(new Date());
        book.setUpdatedTime(new Date());

        Book savedBook = bookRepository.save(book);
        return new BookResponse(savedBook);
    }

    @Override
    public String registerBook(BookDto bookDto) {
        if (bookRepository.countByIsbn(bookDto.getIsbn()) > 0) {
            throw new AlreadyExitException("Book with ISBN " + bookDto.getIsbn() + " already exists");
        }

        Book book = new Book();
        book.setIsbn(bookDto.getIsbn());
        book.setTitle(bookDto.getTitle());
        book.setAuthor(bookDto.getAuthor());
        book.setAvailable(true);
        book.setCreatedTime(new Date());
        book.setUpdatedTime(new Date());

        bookRepository.save(book);
        return "Book registered successfully";
    }

    @Override
    public List<Book> getAvailableBooks() {
        return bookRepository.findByIsAvailableTrue();
    }
    
    @Override
    public BookListResponse getAvailableBooksResponse() {
        List<BookResponse> cachedBooks = redisBookCacheService.getCachedAvailableBooks();
        if (cachedBooks != null) {
            logger.debug("Retrieved {} available books from Redis cache", cachedBooks.size());
            return new BookListResponse(cachedBooks);
        }

        logger.debug("Cache miss - fetching available books from database");
        List<Book> availableBooks = bookRepository.findByIsAvailableTrue();
        List<BookResponse> bookResponses = availableBooks.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());

        redisBookCacheService.cacheAvailableBooks(bookResponses);
        logger.debug("Cached {} available books in Redis", bookResponses.size());
        
        return new BookListResponse(bookResponses);
    }

    @Override
    public BookListResponse getAllBooks() {
        List<Book> allBooks = bookRepository.findAll();
        List<BookResponse> bookResponses = allBooks.stream()
                .map(BookResponse::new)
                .collect(Collectors.toList());
        return new BookListResponse(bookResponses);
    }

    @Override
    public String borrowBook(BorrowBookRequest request, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new ResponseInfoException("User not found");
        }

        Optional<Book> bookOpt = bookRepository.findById(request.getBookId());
        if (!bookOpt.isPresent()) {
            throw new ResponseInfoException("Book not found");
        }

        Book book = bookOpt.get();
        User user = userOpt.get();

        if (!book.isAvailable()) {
            throw new ResponseInfoException("Book is not available for borrowing");
        }

        Optional<BookBorrowing> existingBorrowing = bookBorrowingRepository
                .findByBorrowerAndBookAndIsReturnedFalse(user, book);
        if (existingBorrowing.isPresent()) {
            throw new AlreadyExitException("You have already borrowed this book");
        }

        BookBorrowing borrowing = new BookBorrowing();
        borrowing.setBorrower(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(new Date());
        borrowing.setReturned(false);
        borrowing.setCreatedTime(new Date());
        borrowing.setUpdatedTime(new Date());

        bookBorrowingRepository.save(borrowing);
        logger.info("Borrowing record created for user {} and book {}", userId, book.getId());

        book.setAvailable(false);
        book.setUpdatedTime(new Date());
        bookRepository.save(book);
        logger.info("Book {} marked as unavailable in database", book.getId());

        redisBookCacheService.invalidateAvailableBooksCache();
        redisBookCacheService.invalidateUserBorrowedBooksCache(userId);
        redisBookCacheService.invalidateBookDetailCache(book.getId());

        BookResponse updatedBookResponse = new BookResponse(book);
        redisBookCacheService.cacheBookDetail(updatedBookResponse);
        logger.info("Redis cache synchronized after borrowing book {}", book.getId());

        return "Book borrowed successfully";
    }

    @Override
    public String returnBook(ReturnBookRequest request, Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (!userOpt.isPresent()) {
            throw new ResponseInfoException("User not found");
        }

        Optional<Book> bookOpt = bookRepository.findById(request.getBookId());
        if (!bookOpt.isPresent()) {
            throw new ResponseInfoException("Book not found");
        }

        Book book = bookOpt.get();
        User user = userOpt.get();

        Optional<BookBorrowing> borrowingOpt = bookBorrowingRepository
                .findByBorrowerAndBookAndIsReturnedFalse(user, book);
        if (!borrowingOpt.isPresent()) {
            throw new ResponseInfoException("No active borrowing record found for this book");
        }

        BookBorrowing borrowing = borrowingOpt.get();
        borrowing.setReturnDate(new Date());
        borrowing.setReturned(true);
        borrowing.setUpdatedTime(new Date());

        bookBorrowingRepository.save(borrowing);
        logger.info("Borrowing record marked as returned for user {} and book {}", userId, book.getId());

        book.setAvailable(true);
        book.setUpdatedTime(new Date());
        bookRepository.save(book);
        logger.info("Book {} marked as available in database", book.getId());

        redisBookCacheService.invalidateAvailableBooksCache();
        redisBookCacheService.invalidateUserBorrowedBooksCache(userId);
        redisBookCacheService.invalidateBookDetailCache(book.getId());

        BookResponse updatedBookResponse = new BookResponse(book);
        redisBookCacheService.cacheBookDetail(updatedBookResponse);
        logger.info("Redis cache synchronized after returning book {}", book.getId());

        return "Book returned successfully";
    }

    @Override
    public BookListResponse getBorrowedBooks(Long userId) {
        List<BookResponse> cachedBooks = redisBookCacheService.getCachedBorrowedBooks(userId);
        if (cachedBooks != null) {
            logger.debug("Retrieved {} borrowed books from Redis cache for user {}", cachedBooks.size(), userId);
            return new BookListResponse(cachedBooks);
        }

        logger.debug("Cache miss - fetching borrowed books from database for user {}", userId);
        List<BookBorrowing> borrowings = bookBorrowingRepository.findByBorrowerId(userId);
        List<BookResponse> bookResponses = borrowings.stream()
                .filter(borrowing -> !borrowing.isReturned())
                .map(borrowing -> new BookResponse(borrowing.getBook()))
                .collect(Collectors.toList());

        redisBookCacheService.cacheBorrowedBooks(userId, bookResponses);
        logger.debug("Cached {} borrowed books in Redis for user {}", bookResponses.size(), userId);
        
        return new BookListResponse(bookResponses);
    }

}