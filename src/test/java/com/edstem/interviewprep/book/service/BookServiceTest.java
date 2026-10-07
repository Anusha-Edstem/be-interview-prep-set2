package com.edstem.interviewprep.book.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.edstem.interviewprep.book.dto.request.BorrowBookRequest;
import com.edstem.interviewprep.book.dto.request.CreateBookRequest;
import com.edstem.interviewprep.book.dto.request.UpdateBookRequest;
import com.edstem.interviewprep.book.dto.response.BookResponse;
import com.edstem.interviewprep.book.entity.Book;
import com.edstem.interviewprep.book.entity.BookStatus;
import com.edstem.interviewprep.book.exception.BookAlreadyBorrowedException;
import com.edstem.interviewprep.book.exception.BookNotBorrowedException;
import com.edstem.interviewprep.book.exception.BookNotFoundException;
import com.edstem.interviewprep.book.exception.BorrowedBookDeletionException;
import com.edstem.interviewprep.book.exception.IsbnAlreadyRegisteredException;
import com.edstem.interviewprep.book.repository.BookRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

  private static final Instant FIXED_NOW = Instant.parse("2026-03-01T10:15:30Z");

  @Mock private BookRepository bookRepository;

  private BookService bookService;

  @BeforeEach
  void setUp() {
    bookService = new BookService(bookRepository, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
  }

  @Test
  void addingABookStoresItAsAvailable() {
    CreateBookRequest request =
        new CreateBookRequest("Dune", "Frank Herbert", "9780441013593", 1965);
    when(bookRepository.existsByIsbn("9780441013593")).thenReturn(false);
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

    BookResponse created = bookService.createBook(request);

    assertThat(created.title()).isEqualTo("Dune");
    assertThat(created.status()).isEqualTo(BookStatus.AVAILABLE);
    assertThat(created.borrowedBy()).isNull();
  }

  @Test
  void addingABookNormalisesTheIsbnBeforeStoringIt() {
    CreateBookRequest request = new CreateBookRequest("Dune", "Frank Herbert", " 978x0441 ", 1965);
    when(bookRepository.existsByIsbn("978X0441")).thenReturn(false);
    when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

    bookService.createBook(request);

    ArgumentCaptor<Book> saved = ArgumentCaptor.forClass(Book.class);
    verify(bookRepository).save(saved.capture());
    assertThat(saved.getValue().getIsbn()).isEqualTo("978X0441");
  }

  @Test
  void addingABookWithAnIsbnThatIsAlreadyRegisteredFails() {
    CreateBookRequest request =
        new CreateBookRequest("Dune", "Frank Herbert", "9780441013593", 1965);
    when(bookRepository.existsByIsbn("9780441013593")).thenReturn(true);

    assertThatThrownBy(() -> bookService.createBook(request))
        .isInstanceOf(IsbnAlreadyRegisteredException.class)
        .hasMessageContaining("9780441013593");

    verify(bookRepository, never()).save(any(Book.class));
  }

  @Test
  void borrowingAnAvailableBookRecordsTheMemberAndTheTime() {
    UUID id = UUID.randomUUID();
    Book book = availableBook();
    when(bookRepository.findByIdForUpdate(id)).thenReturn(Optional.of(book));

    BookResponse borrowed = bookService.borrowBook(id, new BorrowBookRequest("member-7"));

    assertThat(borrowed.status()).isEqualTo(BookStatus.BORROWED);
    assertThat(borrowed.borrowedBy()).isEqualTo("member-7");
    assertThat(borrowed.borrowedAt()).isEqualTo(FIXED_NOW);
  }

  @Test
  void borrowingABookThatIsAlreadyBorrowedFails() {
    UUID id = UUID.randomUUID();
    Book book = availableBook();
    book.markBorrowed("member-1", FIXED_NOW);
    when(bookRepository.findByIdForUpdate(id)).thenReturn(Optional.of(book));

    BookAlreadyBorrowedException thrown =
        assertThrows(
            BookAlreadyBorrowedException.class,
            () -> bookService.borrowBook(id, new BorrowBookRequest("member-2")));

    assertThat(thrown.getStatus()).isEqualTo(HttpStatus.CONFLICT);
    assertThat(thrown.getCode()).isEqualTo("BOOK_ALREADY_BORROWED");
    assertThat(book.getBorrowedBy()).isEqualTo("member-1");
  }

  @Test
  void borrowingABookThatDoesNotExistFails() {
    UUID id = UUID.randomUUID();
    when(bookRepository.findByIdForUpdate(id)).thenReturn(Optional.empty());

    assertThrows(
        BookNotFoundException.class,
        () -> bookService.borrowBook(id, new BorrowBookRequest("member-2")));
  }

  @Test
  void returningABorrowedBookMakesItAvailableAgain() {
    UUID id = UUID.randomUUID();
    Book book = availableBook();
    book.markBorrowed("member-1", FIXED_NOW);
    when(bookRepository.findByIdForUpdate(id)).thenReturn(Optional.of(book));

    BookResponse returned = bookService.returnBook(id);

    assertThat(returned.status()).isEqualTo(BookStatus.AVAILABLE);
    assertThat(returned.borrowedBy()).isNull();
    assertThat(returned.borrowedAt()).isNull();
  }

  @Test
  void returningABookThatWasNeverBorrowedFails() {
    UUID id = UUID.randomUUID();
    when(bookRepository.findByIdForUpdate(id)).thenReturn(Optional.of(availableBook()));

    assertThrows(BookNotBorrowedException.class, () -> bookService.returnBook(id));
  }

  @Test
  void deletingABookThatIsCurrentlyBorrowedFails() {
    UUID id = UUID.randomUUID();
    Book book = availableBook();
    book.markBorrowed("member-1", FIXED_NOW);
    when(bookRepository.findById(id)).thenReturn(Optional.of(book));

    assertThrows(BorrowedBookDeletionException.class, () -> bookService.deleteBook(id));

    verify(bookRepository, never()).delete(any(Book.class));
  }

  @Test
  void deletingAnAvailableBookRemovesIt() {
    UUID id = UUID.randomUUID();
    Book book = availableBook();
    when(bookRepository.findById(id)).thenReturn(Optional.of(book));

    bookService.deleteBook(id);

    verify(bookRepository).delete(book);
  }

  @Test
  void gettingABookThatDoesNotExistFails() {
    UUID id = UUID.randomUUID();
    when(bookRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(BookNotFoundException.class, () -> bookService.getBook(id));
  }

  @Test
  void updatingABookToAnIsbnHeldByAnotherBookFails() {
    UUID id = UUID.randomUUID();
    when(bookRepository.findById(id)).thenReturn(Optional.of(availableBook()));
    when(bookRepository.existsByIsbnAndIdNot("9999999999", id)).thenReturn(true);

    assertThrows(
        IsbnAlreadyRegisteredException.class,
        () ->
            bookService.updateBook(
                id, new UpdateBookRequest("Dune", "Frank Herbert", "9999999999", 1965)));
  }

  private Book availableBook() {
    return Book.create("Dune", "Frank Herbert", "9780441013593", 1965);
  }
}
