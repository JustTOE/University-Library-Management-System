package dev.tmmc.ulms.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tmmc.ulms.objects.controllers.AuthController;
import dev.tmmc.ulms.objects.dto.request.LoginRequest;
import dev.tmmc.ulms.objects.dto.request.RegisterRequest;
import dev.tmmc.ulms.objects.dto.response.AuthResponse;
import dev.tmmc.ulms.objects.dto.response.UserResponse;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.exceptions.AccountLockedException;
import dev.tmmc.ulms.objects.exceptions.EmailAlreadyExistsException;
import dev.tmmc.ulms.objects.exceptions.InvalidCredentialsException;
import dev.tmmc.ulms.objects.services.AuthService;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @MockitoBean private AuthService authService;
    @MockitoBean private UserService userService;
    @MockitoBean private JwtService jwtService;

    @Test
    void loginSuccessReturnsToken() throws Exception {
        AuthResponse response = new AuthResponse(
                "jwt-token", OffsetDateTime.now().plusMinutes(60), UserRole.STUDENT, 1, "Alice"
        );
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("alice@example.com", "password1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void loginBadPasswordReturns401() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("alice@example.com", "wrong"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void loginLockedReturns423WithRetryAfter() throws Exception {
        OffsetDateTime lockedUntil = OffsetDateTime.now().plusMinutes(15);
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AccountLockedException("Account locked", lockedUntil));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("alice@example.com", "wrong"))))
                .andExpect(status().isLocked())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.status").value(423))
                .andExpect(jsonPath("$.lockedUntil").exists());
    }

    @Test
    void registerSuccessReturns201() throws Exception {
        UserResponse user = new UserResponse(7, "Bob", "bob@example.com", "U-BOB", null, null, UserRole.STUDENT, true);
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "Bob", "bob@example.com", "U-BOB", null, "password1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("bob@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void registerDuplicateEmailReturns409() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("Email already registered: bob@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "Bob", "bob@example.com", "U-BOB", null, "password1"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void loginValidationFailsOnMissingFields() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void registerValidationFailsOnShortPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegisterRequest(
                                "Bob", "bob@example.com", "U-BOB", null, "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }
}
