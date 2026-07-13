package gym.crm.security;

import gym.crm.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private AuthInterceptor interceptor;

    private static String basic(String password) {
        String token = Base64.getEncoder()
                .encodeToString(("john" + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod(method);
        request.setRequestURI(uri);
        return request;
    }

    @Test
    void allowsTraineeRegistrationWithoutCredentials() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request("POST", "/trainees"), response, new Object()));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void allowsTrainerRegistrationWithoutCredentials() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request("POST", "/trainers"), response, new Object()));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void allowsLoginEndpointsWithoutCredentials() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(request("GET", "/login"), response, new Object()));
        assertTrue(interceptor.preHandle(request("PUT", "/login/change-password"), response, new Object()));
        verifyNoInteractions(authenticationService);
    }

    @Test
    void rejectsProtectedEndpointWhenHeaderMissing() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean result = interceptor.preHandle(request("GET", "/trainees"), response, new Object());
        assertFalse(result);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        verifyNoInteractions(authenticationService);
    }

    @Test
    void rejectsProtectedEndpointWhenCredentialsInvalid() {
        MockHttpServletRequest request = request("GET", "/trainees");
        request.addHeader(HttpHeaders.AUTHORIZATION, basic("wrong"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authenticationService.matches("john", "wrong")).thenReturn(false);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertFalse(result);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    }

    @Test
    void rejectsMalformedAuthorizationHeader() {
        MockHttpServletRequest request = request("GET", "/trainees");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic not-base-64!!");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        verifyNoInteractions(authenticationService);
    }

    @Test
    void allowsProtectedEndpointAndExposesUsernameWhenCredentialsValid() {
        MockHttpServletRequest request = request("GET", "/trainees");
        request.addHeader(HttpHeaders.AUTHORIZATION, basic("pass"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(authenticationService.matches("john", "pass")).thenReturn(true);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals("john", request.getAttribute(AuthInterceptor.AUTH_USERNAME_ATTRIBUTE));
    }

    @Test
    void rejectsNonBasicScheme() {
        MockHttpServletRequest request = request("GET", "/trainees");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer sometoken");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertNull(request.getAttribute(AuthInterceptor.AUTH_USERNAME_ATTRIBUTE));
        verifyNoInteractions(authenticationService);
    }
}
