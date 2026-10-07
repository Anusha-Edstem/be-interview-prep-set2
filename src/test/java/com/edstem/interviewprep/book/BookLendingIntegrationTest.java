package com.edstem.interviewprep.book;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.edstem.interviewprep.book.repository.BookRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookLendingIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private BookRepository bookRepository;

  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  void emptyTheCatalogue() {
    bookRepository.deleteAll();
  }

  @Test
  void aBookCannotBeBorrowedTwiceUntilItIsReturned() throws Exception {
    String id = addBook("Dune", "Frank Herbert", "9780441013593", 1965);

    mockMvc
        .perform(borrowRequest(id, "member-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("BORROWED"))
        .andExpect(jsonPath("$.borrowedBy").value("member-1"));

    mockMvc
        .perform(borrowRequest(id, "member-2"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("BOOK_ALREADY_BORROWED"))
        .andExpect(jsonPath("$.status").value(409));

    mockMvc
        .perform(post("/api/v1/books/{id}/return", id))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("AVAILABLE"));

    mockMvc
        .perform(borrowRequest(id, "member-2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.borrowedBy").value("member-2"));
  }

  @Test
  void aBorrowedBookCannotBeDeletedUntilItIsReturned() throws Exception {
    String id = addBook("Neuromancer", "William Gibson", "9780441569595", 1984);
    mockMvc.perform(borrowRequest(id, "member-1")).andExpect(status().isOk());

    mockMvc
        .perform(delete("/api/v1/books/{id}", id))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("BOOK_CURRENTLY_BORROWED"));

    mockMvc.perform(post("/api/v1/books/{id}/return", id)).andExpect(status().isOk());

    mockMvc.perform(delete("/api/v1/books/{id}", id)).andExpect(status().isNoContent());
    mockMvc.perform(get("/api/v1/books/{id}", id)).andExpect(status().isNotFound());
  }

  @Test
  void aSecondBookWithTheSameIsbnIsRejected() throws Exception {
    addBook("Dune", "Frank Herbert", "9780441013593", 1965);

    mockMvc
        .perform(
            post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyFor("Dune Reprint", "Frank Herbert", "9780441013593", 2021)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("ISBN_ALREADY_REGISTERED"));
  }

  @Test
  void theCatalogueIsSearchableByTitleAndByAuthor() throws Exception {
    addBook("Dune", "Frank Herbert", "9780441013593", 1965);
    addBook("Neuromancer", "William Gibson", "9780441569595", 1984);

    mockMvc
        .perform(get("/api/v1/books").param("search", "gibson"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].title").value("Neuromancer"));

    mockMvc
        .perform(get("/api/v1/books").param("search", "dune"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1))
        .andExpect(jsonPath("$.content[0].author").value("Frank Herbert"));
  }

  private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder borrowRequest(
      String id, String memberId) {
    return post("/api/v1/books/{id}/borrow", id)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"memberId\":\"" + memberId + "\"}");
  }

  private String addBook(String title, String author, String isbn, int publishedYear)
      throws Exception {
    String payload =
        mockMvc
            .perform(
                post("/api/v1/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bodyFor(title, author, isbn, publishedYear)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
    JsonNode created = objectMapper.readTree(payload);
    return created.get("id").asText();
  }

  private String bodyFor(String title, String author, String isbn, int publishedYear) {
    return """
        {"title":"%s","author":"%s","isbn":"%s","publishedYear":%d}
        """
        .formatted(title, author, isbn, publishedYear);
  }
}
