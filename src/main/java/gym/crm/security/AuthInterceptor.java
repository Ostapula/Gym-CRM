package gym.crm.security;

import gym.crm.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String AUTH_USERNAME_ATTRIBUTE = "authUsername";
    private static final String BASIC_PREFIX = "Basic ";

    private final AuthenticationService authenticationService;

    public AuthInterceptor(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (isPublic(request)) {
            return true;
        }
        String[] credentials = extractBasicCredentials(request);
        if (credentials == null || !authenticationService.matches(credentials[0], credentials[1])) {
            log.warn("Unauthorized request method={} uri={}", request.getMethod(), request.getRequestURI());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"gym-crm\"");
            return false;
        }
        request.setAttribute(AUTH_USERNAME_ATTRIBUTE, credentials[0]);
        return true;
    }

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (HttpMethod.POST.matches(request.getMethod())
                && ("/trainees".equals(path) || "/trainers".equals(path))) {
            return true;
        }
        if (isDocumentationPath(path)) {
            return true;
        }
        return path.equals("/login") || path.startsWith("/login/");
    }

    private boolean isDocumentationPath(String path) {
        return path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    private String[] extractBasicCredentials(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BASIC_PREFIX)) {
            return null;
        }
        String encoded = header.substring(BASIC_PREFIX.length()).trim();
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        int separator = decoded.indexOf(':');
        if (separator < 0) {
            return null;
        }
        return new String[]{decoded.substring(0, separator), decoded.substring(separator + 1)};
    }
}
