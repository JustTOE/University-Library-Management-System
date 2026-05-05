package dev.tmmc.ulms.integration;

import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.objects.repositories.UserRepository;
import dev.tmmc.ulms.objects.services.UserService;
import dev.tmmc.ulms.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ActuatorEndpointsTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private UserService userService;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAllInBatch();
    }

    @Test
    void healthAccessibleAnonymously() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void metricsRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void metricsAccessibleWithAdminToken() throws Exception {
        User admin = TestFixtures.user();
        admin.setRole(UserRole.ADMIN);
        userService.save(admin, "password1");

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + admin.getEmail() + "\",\"password\":\"password1\"}"))
                .andExpect(status().isOk())
                .andReturn();
        String body = login.getResponse().getContentAsString();
        int start = body.indexOf("\"token\":\"") + "\"token\":\"".length();
        int end = body.indexOf('"', start);
        String token = body.substring(start, end);

        mockMvc.perform(get("/actuator/metrics")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }
}
