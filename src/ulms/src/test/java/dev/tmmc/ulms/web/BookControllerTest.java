package dev.tmmc.ulms.web;

import dev.tmmc.ulms.objects.controllers.BookController;
import dev.tmmc.ulms.objects.entities.Book;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.services.BookService;
import dev.tmmc.ulms.security.JwtService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private BookService bookService;
    @MockitoBean private JwtService jwtService;

    @BeforeEach
    void setUpPrincipal() {
        TestFixtures.withPrincipal(UserRole.STUDENT, 1);
    }

    @AfterEach
    void clearPrincipal() {
        TestFixtures.clearPrincipal();
    }

    private Book seededBook() {
        Book book = TestFixtures.book(10, 8);
        book.setId(42);
        return book;
    }

    @Test
    void searchByTitlePartialMatchReturnsBook() throws Exception {
        Book book = seededBook();
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookService.searchByFilters(eq("DiSt"), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/books/search").param("title", "DiSt"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(book.getTitle()))
                .andExpect(jsonPath("$.content[0].availableCopies").value(8));
    }

    @Test
    void searchByAuthorPartialMatchReturnsBook() throws Exception {
        Book book = seededBook();
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookService.searchByFilters(eq(null), eq("auth"), eq(null), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/books/search").param("author", "auth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].author").value(book.getAuthor()));
    }

    @Test
    void searchBlankAuthorAndSubjectAreNormalisedToNull() throws Exception {
        // The HTML search form GET-submits empty fields as ""; those must be
        // treated as "no filter" (null), not as an exact match on an empty string.
        Book book = seededBook();
        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookService.searchByFilters(eq("Effective Java"), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/books/search")
                        .param("title", "Effective Java")
                        .param("author", "")
                        .param("subject", "  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value(book.getTitle()));

        verify(bookService).searchByFilters(eq("Effective Java"), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void getAllPassesSortPageable() throws Exception {
        Book book = seededBook();
        when(bookService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(book)));

        mockMvc.perform(get("/api/books").param("sort", "title,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(bookService).findAll(captor.capture());
        Sort.Order order = captor.getValue().getSort().getOrderFor("title");
        assertTrue(order != null, "expected a sort order on 'title'");
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void searchResponseIncludesAvailableCopies() throws Exception {
        Book book = seededBook();
        when(bookService.searchByFilters(any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(book)));

        mockMvc.perform(get("/api/books/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].availableCopies").value(8))
                .andExpect(jsonPath("$.content[0].totalCopies").value(10));
    }
}
