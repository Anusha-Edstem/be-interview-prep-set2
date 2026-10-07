package com.edstem.interviewprep.book.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.interviewprep.book.dto.request.BorrowBookRequest;
import com.edstem.interviewprep.book.dto.response.BookResponse;
import com.edstem.interviewprep.book.entity.BookStatus;
import com.edstem.interviewprep.book.exception.BookAlreadyBorrowedException;
import com.edstem.interviewprep.book.exception.BookNotFoundException;
import com.edstem.interviewprep.book.exception.BorrowedBookDeletionException;
import com.edstem.interviewprep.book.service.BookService;
import com.edstem.interviewprep.common.dto.response.PageResponse;
import java.time.Year;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BookController.class)
class BookControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BookService bookService;

  @Test
  void addingABookReturnsCreatedWithItsLocation() throws Exception {
    UUID id = UUID.randomUUID();
    when(bookService.createBook(any()))
        .thenReturn(
            new BookResponse(
                id,
                "Dune",
                "Frank Herbert",
                "9780441013593",
                1965,
                BookStatus.AVAILABLE,
                null,
                null));

    mockMvc
        .perform(
            post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title":"Dune","author":"Frank Herbert",\
                    "isbn":"9780441013593","publishedYear":1965}
                    """))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/api/v1/books/" + id))
        .andExpect(jsonPath("$.status").value("AVAILABLE"));
  }

  @Test
  void addingABookWithoutATitleIsRejectedWithAFieldError() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title":"  ","author":"Frank Herbert",\
                    "isbn":"9780441013593","publishedYear":1965}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.path").value("/api/v1/books"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
  }

  @Test
  void addingABookPublishedInTheFutureIsRejected() throws Exception {
    int nextYear = Year.now().getValue() + 1;

    mockMvc
        .perform(
            post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"title":"Dune","author":"Frank Herbert",\
                    "isbn":"9780441013593","publishedYear":%d}
                    """
                        .formatted(nextYear)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
        .andExpect(jsonPath("$.fieldErrors[0].field").value("publishedYear"))
        .andExpect(jsonPath("$.fieldErrors[0].message").value("must not be in the future"));
  }

  @Test
  void borrowingABookThatIsAlreadyBorrowedReturnsConflict() throws Exception {
    UUID id = UUID.randomUUID();
    when(bookService.borrowBook(eq(id), any(BorrowBookRequest.class)))
        .thenThrow(new BookAlreadyBorrowedException(id));

    mockMvc
        .perform(
            post("/api/v1/books/{id}/borrow", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"memberId":"member-2"}
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.code").value("BOOK_ALREADY_BORROWED"))
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("already borrowed")))
        .andExpect(jsonPath("$.path").value("/api/v1/books/" + id + "/borrow"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void borrowingWithoutAMemberIsRejected() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/books/{id}/borrow", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"memberId":""}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.fieldErrors[0].field").value("memberId"));
  }

  @Test
  void deletingABookThatIsCurrentlyBorrowedReturnsConflict() throws Exception {
    UUID id = UUID.randomUUID();
    doThrow(new BorrowedBookDeletionException(id)).when(bookService).deleteBook(id);

    mockMvc
        .perform(delete("/api/v1/books/{id}", id))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("BOOK_CURRENTLY_BORROWED"));
  }

  @Test
  void gettingABookThatDoesNotExistReturnsNotFound() throws Exception {
    UUID id = UUID.randomUUID();
    when(bookService.getBook(id)).thenThrow(new BookNotFoundException(id));

    mockMvc
        .perform(get("/api/v1/books/{id}", id))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
  }

  @Test
  void listingBooksPassesTheSearchTermToTheService() throws Exception {
    when(bookService.listBooks(eq("dune"), any(Pageable.class)))
        .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true));

    mockMvc.perform(get("/api/v1/books").param("search", "dune")).andExpect(status().isOk());

    verify(bookService).listBooks(eq("dune"), any(Pageable.class));
  }
}
