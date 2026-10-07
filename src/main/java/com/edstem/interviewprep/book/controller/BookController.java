package com.edstem.interviewprep.book.controller;

import com.edstem.interviewprep.book.dto.request.BorrowBookRequest;
import com.edstem.interviewprep.book.dto.request.CreateBookRequest;
import com.edstem.interviewprep.book.dto.request.UpdateBookRequest;
import com.edstem.interviewprep.book.dto.response.BookResponse;
import com.edstem.interviewprep.book.service.BookService;
import com.edstem.interviewprep.common.dto.response.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

  private final BookService bookService;

  public BookController(BookService bookService) {
    this.bookService = bookService;
  }

  @PostMapping
  public ResponseEntity<BookResponse> createBook(@Valid @RequestBody CreateBookRequest request) {
    BookResponse created = bookService.createBook(request);
    return ResponseEntity.created(URI.create("/api/v1/books/" + created.id())).body(created);
  }

  @GetMapping
  public ResponseEntity<PageResponse<BookResponse>> listBooks(
      @RequestParam(required = false) String search,
      @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC)
          Pageable pageable) {
    return ResponseEntity.ok(bookService.listBooks(search, pageable));
  }

  @GetMapping("/{id}")
  public ResponseEntity<BookResponse> getBook(@PathVariable UUID id) {
    return ResponseEntity.ok(bookService.getBook(id));
  }

  @PutMapping("/{id}")
  public ResponseEntity<BookResponse> updateBook(
      @PathVariable UUID id, @Valid @RequestBody UpdateBookRequest request) {
    return ResponseEntity.ok(bookService.updateBook(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
    bookService.deleteBook(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/borrow")
  public ResponseEntity<BookResponse> borrowBook(
      @PathVariable UUID id, @Valid @RequestBody BorrowBookRequest request) {
    return ResponseEntity.ok(bookService.borrowBook(id, request));
  }

  @PostMapping("/{id}/return")
  public ResponseEntity<BookResponse> returnBook(@PathVariable UUID id) {
    return ResponseEntity.ok(bookService.returnBook(id));
  }
}
