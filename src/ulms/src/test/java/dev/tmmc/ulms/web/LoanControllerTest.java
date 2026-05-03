package dev.tmmc.ulms.web;

import dev.tmmc.ulms.objects.controllers.LoanController;
import dev.tmmc.ulms.objects.dto.response.LoanResponse;
import dev.tmmc.ulms.objects.entities.Loan;
import dev.tmmc.ulms.objects.entities.enums.LoanStatus;
import dev.tmmc.ulms.objects.exceptions.BookNotAvailableException;
import dev.tmmc.ulms.objects.mapper.LoanMapper;
import dev.tmmc.ulms.objects.services.LoanService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.security.JwtService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoanController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanService loanService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void borrowReturnsMappedLoanResponse() throws Exception {
        Loan loan = TestFixtures.loan(TestFixtures.user(), TestFixtures.book(1, 0), LoanStatus.ACTIVE, LocalDate.now().plusDays(7));
        loan.setId(5);
        LoanResponse response = LoanMapper.toResponse(loan);

        when(loanService.borrowBookAsResponse(1, 2)).thenReturn(response);

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"bookId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.userName").value("Alice Student"))
                .andExpect(jsonPath("$.bookTitle").value("Distributed Systems"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void borrowRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bookId").value("Book ID is required"));
    }

    @Test
    void getByUserReturns404WhenUserDoesNotExist() throws Exception {
        when(userService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/loans/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }

    @Test
    void borrowReturnsConflictWhenBookCannotBeBorrowed() throws Exception {
        when(loanService.borrowBookAsResponse(1, 2)).thenThrow(new BookNotAvailableException("Book is not available"));

        mockMvc.perform(post("/api/loans/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"bookId\":2}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Book is not available"));
    }
}
