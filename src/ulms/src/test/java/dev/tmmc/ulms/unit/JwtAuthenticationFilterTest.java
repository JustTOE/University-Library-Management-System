package dev.tmmc.ulms.unit;

import dev.tmmc.ulms.objects.entities.User;
import dev.tmmc.ulms.objects.entities.enums.UserRole;
import dev.tmmc.ulms.security.JwtAuthenticationFilter;
import dev.tmmc.ulms.security.JwtPrincipal;
import dev.tmmc.ulms.security.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {

    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        StandardEnvironment env = new StandardEnvironment();
        env.setActiveProfiles("test");
        String secret = "test-secret-32-bytes-long-padding-padding-padding";
        jwtService = new JwtService(secret, 60L, env);
        Method init = JwtService.class.getDeclaredMethod("init");
        init.setAccessible(true);
        init.invoke(jwtService);
        filter = new JwtAuthenticationFilter(jwtService);
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private User user(int id, UserRole role) {
        User u = new User();
        u.setId(id);
        u.setEmail("u" + id + "@example.com");
        u.setRole(role);
        return u;
    }

    @Test
    void validTokenPopulatesSecurityContext() throws Exception {
        String token = jwtService.generateToken(user(7, UserRole.STUDENT));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, resp) -> {};

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request, response, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getPrincipal() instanceof JwtPrincipal);
        JwtPrincipal principal = (JwtPrincipal) auth.getPrincipal();
        assertEquals(7, principal.userId());
        assertEquals("STUDENT", principal.role());
        assertEquals(1, auth.getAuthorities().size());
        assertEquals("ROLE_STUDENT", auth.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void tamperedTokenLeavesContextEmpty() throws Exception {
        String token = jwtService.generateToken(user(7, UserRole.STUDENT));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token + "tamper");

        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", request,
                new MockHttpServletResponse(), (FilterChain) (r, p) -> {});

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void missingHeaderIsNoop() throws Exception {
        ReflectionTestUtils.invokeMethod(filter, "doFilterInternal", new MockHttpServletRequest(),
                new MockHttpServletResponse(), (FilterChain) (r, p) -> {});
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
