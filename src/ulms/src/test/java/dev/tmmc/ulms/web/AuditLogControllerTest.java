package dev.tmmc.ulms.web;

import dev.tmmc.ulms.objects.controllers.AuditLogController;
import dev.tmmc.ulms.objects.entities.AuditLog;
import dev.tmmc.ulms.objects.entities.enums.AuditAction;
import dev.tmmc.ulms.objects.repositories.AuditLogRepository;
import dev.tmmc.ulms.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuditLogControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuditLogRepository auditLogRepository;
    @MockitoBean private JwtService jwtService;

    private AuditLog entry(long id, AuditAction action, Integer actorId) {
        AuditLog log = new AuditLog();
        log.setId(id);
        log.setOccurredAt(OffsetDateTime.now());
        log.setAction(action);
        log.setActorId(actorId);
        log.setActorEmail("user" + actorId + "@example.com");
        log.setDetail(null);
        return log;
    }

    @Test
    void listReturnsPageWithoutFilters() throws Exception {
        Page<AuditLog> page = new PageImpl<>(List.of(
                entry(1L, AuditAction.LOGIN_SUCCESS, 7),
                entry(2L, AuditAction.LOGIN_FAILURE, 8)
        ));
        when(auditLogRepository.findFiltered(isNull(), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/audit-log"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].action").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$.content[1].action").value("LOGIN_FAILURE"));
    }

    @Test
    void listForwardsActionAndActorFilters() throws Exception {
        when(auditLogRepository.findFiltered(eq(AuditAction.LOGIN_LOCKED), eq(42), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entry(3L, AuditAction.LOGIN_LOCKED, 42))));

        mockMvc.perform(get("/api/audit-log").param("action", "LOGIN_LOCKED").param("actorId", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].actorId").value(42))
                .andExpect(jsonPath("$.content[0].action").value("LOGIN_LOCKED"));

        verify(auditLogRepository).findFiltered(eq(AuditAction.LOGIN_LOCKED), eq(42), any(Pageable.class));
    }

    @Test
    void listClampsPageSizeAbove200() throws Exception {
        when(auditLogRepository.findFiltered(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/audit-log").param("size", "9999"))
                .andExpect(status().isOk());

        org.mockito.ArgumentCaptor<Pageable> captor = org.mockito.ArgumentCaptor.forClass(Pageable.class);
        verify(auditLogRepository).findFiltered(isNull(), isNull(), captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals(200, captor.getValue().getPageSize());
    }
}
