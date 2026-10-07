package com.edstem.interviewprep.book.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.edstem.interviewprep.book.entity.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class BookRepositoryTest {

  @Autowired private BookRepository bookRepository;

  @BeforeEach
  void seedCatalogue() {
    bookRepository.save(Book.create("Dune", "Frank Herbert", "9780441013593", 1965));
    bookRepository.save(Book.create("Dune Messiah", "Frank Herbert", "9780441172696", 1969));
    bookRepository.save(Book.create("Neuromancer", "William Gibson", "9780441569595", 1984));
    bookRepository.flush();
  }

  @Test
  void searchMatchesAFragmentOfTheTitle() {
    Page<Book> found = bookRepository.search("neuro", PageRequest.of(0, 10));

    assertThat(found.getContent()).extracting(Book::getTitle).containsExactly("Neuromancer");
  }

  @Test
  void searchMatchesAFragmentOfTheAuthor() {
    Page<Book> found = bookRepository.search("herbert", PageRequest.of(0, 10));

    assertThat(found.getContent())
        .extracting(Book::getTitle)
        .containsExactlyInAnyOrder("Dune", "Dune Messiah");
  }

  @Test
  void searchIgnoresCase() {
    Page<Book> found = bookRepository.search("DUNE", PageRequest.of(0, 10));

    assertThat(found.getTotalElements()).isEqualTo(2);
  }

  @Test
  void searchReturnsNothingWhenNeitherTitleNorAuthorMatches() {
    Page<Book> found = bookRepository.search("asimov", PageRequest.of(0, 10));

    assertThat(found.getContent()).isEmpty();
  }

  @Test
  void twoBooksCannotShareAnIsbn() {
    Book duplicate = Book.create("Dune Reprint", "Frank Herbert", "9780441013593", 2021);

    assertThatThrownBy(
            () -> {
              bookRepository.save(duplicate);
              bookRepository.flush();
            })
        .isInstanceOf(Exception.class);
  }

  @Test
  void anIsbnThatIsAlreadyStoredIsReportedAsExisting() {
    assertThat(bookRepository.existsByIsbn("9780441013593")).isTrue();
    assertThat(bookRepository.existsByIsbn("0000000000000")).isFalse();
  }
}
