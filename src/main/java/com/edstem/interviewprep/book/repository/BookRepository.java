package com.edstem.interviewprep.book.repository;

import com.edstem.interviewprep.book.entity.Book;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, UUID> {

  boolean existsByIsbn(String isbn);

  boolean existsByIsbnAndIdNot(String isbn, UUID id);

  @Query(
      """
      select b from Book b
      where lower(b.title) like lower(concat('%', :term, '%'))
         or lower(b.author) like lower(concat('%', :term, '%'))
      """)
  Page<Book> search(@Param("term") String term, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select b from Book b where b.id = :id")
  Optional<Book> findByIdForUpdate(@Param("id") UUID id);
}
