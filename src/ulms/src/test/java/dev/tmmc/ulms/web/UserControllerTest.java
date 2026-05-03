package dev.tmmc.ulms.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tmmc.ulms.objects.controllers.UserController;
import dev.tmmc.ulms.objects.dto.request.CreateUserRequest;
import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.security.JwtService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private UserService userService;
    @MockitoBean private JwtService jwtService;

    @BeforeEach
    void setUpPrincipal() {
        TestFixtures.withPrincipal(UserRole.ADMIN, 99);
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
    void getAllReturnsList() throws Exception {
        when(userService.findAll()).thenReturn(List.of(existing(1), existing(2)));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getByIdReturnsUser() throws Exception {
        when(userService.findById(1)).thenReturn(Optional.of(existing(1)));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        when(userService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found: 99"));
    }

    @Test
    void createReturnsMappedUser() throws Exception {
        when(userService.save(any(User.class), eq("password1"))).thenAnswer(inv -> {
            User user = inv.getArgument(0);
            user.setId(5);
            return user;
        });

        CreateUserRequest request = new CreateUserRequest(
                "Carol", "carol@example.com", null, "S-1", null, UserRole.LIBRARIAN, "password1"
        );
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.role").value("LIBRARIAN"));
    }

    @Test
    void deleteReturns204() throws Exception {
        when(userService.findById(3)).thenReturn(Optional.of(existing(3)));

        mockMvc.perform(delete("/api/users/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateReturnsActiveUser() throws Exception {
        User user = existing(4);
        user.setActive(true);
        when(userService.activate(4)).thenReturn(user);

        mockMvc.perform(put("/api/users/4/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void deactivateReturnsDeactivatedUser() throws Exception {
        User user = existing(4);
        user.setActive(false);
        when(userService.deactivate(4)).thenReturn(user);

        mockMvc.perform(put("/api/users/4/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4));
    }
}
