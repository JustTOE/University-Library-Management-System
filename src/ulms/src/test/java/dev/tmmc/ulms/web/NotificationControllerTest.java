package dev.tmmc.ulms.web;

import dev.tmmc.ulms.objects.controllers.NotificationController;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.services.NotificationService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.security.JwtService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private NotificationService notificationService;
    @MockitoBean private UserService userService;
    @MockitoBean private JwtService jwtService;

    @BeforeEach
    void setUpPrincipal() {
        TestFixtures.withPrincipal(UserRole.STUDENT, 7);
    }

    @AfterEach
    void clearPrincipal() {
        TestFixtures.clearPrincipal();
    }

    private User existing(int id) {
        User user = TestFixtures.user();
        user.setId(id);
        return user;
    }

    @Test
    void getUnreadCountReturnsCount() throws Exception {
        when(userService.findById(7)).thenReturn(Optional.of(existing(7)));
        when(notificationService.unreadCountFor(any(User.class))).thenReturn(3L);

        mockMvc.perform(get("/api/notifications/user/7/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    void getUnreadCountReturnsZeroWhenNoUnread() throws Exception {
        when(userService.findById(7)).thenReturn(Optional.of(existing(7)));
        when(notificationService.unreadCountFor(any(User.class))).thenReturn(0L);

        mockMvc.perform(get("/api/notifications/user/7/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void getUnreadCountReturns404WhenUserMissing() throws Exception {
        when(userService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notifications/user/99/unread-count"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }
}
