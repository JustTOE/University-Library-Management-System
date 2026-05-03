package dev.tmmc.ulms.web;

import dev.tmmc.ulms.objects.controllers.ReservationController;
import dev.tmmc.ulms.objects.dto.response.ReservationResponse;
import dev.tmmc.ulms.objects.entities.Reservation;
import dev.tmmc.ulms.objects.entities.enums.ReservationStatus;
import dev.tmmc.ulms.objects.mapper.ReservationMapper;
import dev.tmmc.ulms.objects.services.ReservationService;
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

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void createReturnsMappedReservation() throws Exception {
        Reservation reservation = TestFixtures.reservation(TestFixtures.user(), TestFixtures.book(1, 0), ReservationStatus.ACTIVE);
        reservation.setId(6);
        ReservationResponse response = ReservationMapper.toResponse(reservation);

        when(reservationService.createReservationAsResponse(1, 2)).thenReturn(response);

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"bookId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(6))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.userName").value("Alice Student"));
    }

    @Test
    void createRejectsInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.bookId").value("Book ID is required"));
    }

    @Test
    void cancelReturnsCancelledReservation() throws Exception {
        Reservation reservation = TestFixtures.reservation(TestFixtures.user(), TestFixtures.book(1, 0), ReservationStatus.CANCELLED);
        reservation.setId(10);
        ReservationResponse response = ReservationMapper.toResponse(reservation);

        when(reservationService.cancelReservationAsResponse(10)).thenReturn(response);

        mockMvc.perform(delete("/api/reservations/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void getByUserReturns404WhenUserIsMissing() throws Exception {
        when(userService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reservations/user/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }

    @Test
    void getByIdReturns404WhenReservationIsMissing() throws Exception {
        when(reservationService.findByIdAsResponse(12)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reservations/12"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reservation not found: 12"));
    }
}
