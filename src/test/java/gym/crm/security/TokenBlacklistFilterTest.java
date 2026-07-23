package gym.crm.security;

import gym.crm.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistFilterTest {
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private FilterChain filterChain;
    @InjectMocks
    private TokenBlacklistFilter filter;

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/trainees");
        return request;
    }

    @Test
    void proceedsWhenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = request();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void proceedsForNonBearerScheme() throws Exception {
        MockHttpServletRequest request = request();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic abc");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenBlacklistService);
    }

    @Test
    void proceedsWhenBearerTokenNotBlacklisted() throws Exception {
        MockHttpServletRequest request = request();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(tokenBlacklistService.isBlacklisted("good-token")).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void rejectsWhenBearerTokenBlacklisted() throws Exception {
        MockHttpServletRequest request = request();
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(tokenBlacklistService.isBlacklisted("bad-token")).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        assertTrue(response.getContentAsString().contains("invalidated"));
        verify(filterChain, never()).doFilter(request, response);
    }
}
