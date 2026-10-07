package com.edstem.interviewprep.book.service;

import com.edstem.interviewprep.book.dto.request.BorrowBookRequest;
import com.edstem.interviewprep.book.dto.request.CreateBookRequest;
import com.edstem.interviewprep.book.dto.request.UpdateBookRequest;
import com.edstem.interviewprep.book.dto.response.BookResponse;
import com.edstem.interviewprep.book.entity.Book;
import com.edstem.interviewprep.book.exception.BookAlreadyBorrowedException;
import com.edstem.interviewprep.book.exception.BookNotBorrowedException;
import com.edstem.interviewprep.book.exception.BookNotFoundException;
import com.edstem.interviewprep.book.exception.BorrowedBookDeletionException;
import com.edstem.interviewprep.book.exception.IsbnAlreadyRegisteredException;
import com.edstem.interviewprep.book.mapper.BookMapper;
import com.edstem.interviewprep.book.repository.BookRepository;
import com.edstem.interviewprep.common.dto.response.PageResponse;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

  private final BookRepository bookRepository;
  private final Clock clock;

  public BookService(BookRepository bookRepository, Clock clock) {
    this.bookRepository = bookRepository;
    this.clock = clock;
  }

  @Transactional
  public BookResponse createBook(CreateBookRequest request) {
    String isbn = normalizeIsbn(request.isbn());
    if (bookRepository.existsByIsbn(isbn)) {
      throw new IsbnAlreadyRegisteredException(isbn);
    }
    Book book = Book.create(request.title(), request.author(), isbn, request.publishedYear());
    return BookMapper.toResponse(bookRepository.save(book));
  }

  @Transactional(readOnly = true)
  public PageResponse<BookResponse> listBooks(String search, Pageable pageable) {
    Page<Book> books =
        search == null || search.isBlank()
            ? bookRepository.findAll(pageable)
            : bookRepository.search(search.trim(), pageable);
    return PageResponse.from(books.map(BookMapper::toResponse));
  }

  @Transactional(readOnly = true)
  public BookResponse getBook(UUID id) {
    return BookMapper.toResponse(findBookOrThrow(id));
  }

  @Transactional
  public BookResponse updateBook(UUID id, UpdateBookRequest request) {
    Book book = findBookOrThrow(id);
    String isbn = normalizeIsbn(request.isbn());
    if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
      throw new IsbnAlreadyRegisteredException(isbn);
    }
    book.update(request.title(), request.author(), isbn, request.publishedYear());
    return BookMapper.toResponse(book);
  }

  @Transactional
  public void deleteBook(UUID id) {
    Book book = findBookOrThrow(id);
    if (book.isBorrowed()) {
      throw new BorrowedBookDeletionException(id);
    }
    bookRepository.delete(book);
  }

  @Transactional
  public BookResponse borrowBook(UUID id, BorrowBookRequest request) {
    Book book = findBookForUpdateOrThrow(id);
    if (book.isBorrowed()) {
      throw new BookAlreadyBorrowedException(id);
    }
    book.markBorrowed(request.memberId(), Instant.now(clock));
    return BookMapper.toResponse(book);
  }

  @Transactional
  public BookResponse returnBook(UUID id) {
    Book book = findBookForUpdateOrThrow(id);
    if (!book.isBorrowed()) {
      throw new BookNotBorrowedException(id);
    }
    book.markReturned();
    return BookMapper.toResponse(book);
  }

  private Book findBookOrThrow(UUID id) {
    return bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
  }

  private Book findBookForUpdateOrThrow(UUID id) {
    return bookRepository.findByIdForUpdate(id).orElseThrow(() -> new BookNotFoundException(id));
  }

  private String normalizeIsbn(String isbn) {
    return isbn.trim().toUpperCase(Locale.ROOT);
  }
}
