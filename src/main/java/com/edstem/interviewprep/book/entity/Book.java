package com.edstem.interviewprep.book.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "title", nullable = false, length = 255)
  private String title;

  @Column(name = "author", nullable = false, length = 255)
  private String author;

  @Column(name = "isbn", nullable = false, unique = true, length = 32)
  private String isbn;

  @Column(name = "published_year", nullable = false)
  private int publishedYear;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private BookStatus status;

  @Column(name = "borrowed_by", length = 100)
  private String borrowedBy;

  @Column(name = "borrowed_at")
  private Instant borrowedAt;

  @CreationTimestamp
  @Column(name = "created_date", nullable = false, updatable = false)
  private Instant createdDate;

  protected Book() {}

  private Book(String title, String author, String isbn, int publishedYear) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.publishedYear = publishedYear;
    this.status = BookStatus.AVAILABLE;
  }

  public static Book create(String title, String author, String isbn, int publishedYear) {
    return new Book(title, author, isbn, publishedYear);
  }

  public void update(String title, String author, String isbn, int publishedYear) {
    this.title = title;
    this.author = author;
    this.isbn = isbn;
    this.publishedYear = publishedYear;
  }

  public boolean isBorrowed() {
    return status == BookStatus.BORROWED;
  }

  public void markBorrowed(String memberId, Instant borrowedAt) {
    this.status = BookStatus.BORROWED;
    this.borrowedBy = memberId;
    this.borrowedAt = borrowedAt;
  }

  public void markReturned() {
    this.status = BookStatus.AVAILABLE;
    this.borrowedBy = null;
    this.borrowedAt = null;
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getAuthor() {
    return author;
  }

  public String getIsbn() {
    return isbn;
  }

  public int getPublishedYear() {
    return publishedYear;
  }

  public BookStatus getStatus() {
    return status;
  }

  public String getBorrowedBy() {
    return borrowedBy;
  }

  public Instant getBorrowedAt() {
    return borrowedAt;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }
}
