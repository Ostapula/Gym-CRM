package gym.crm.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {
    @Mock
    private JwtEncoder jwtEncoder;
    @InjectMocks
    private TokenService service;

    private Authentication authentication(String... authorities) {
        List<SimpleGrantedAuthority> granted = Stream.of(authorities)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new UsernamePasswordAuthenticationToken("john", "pass", granted);
    }

    private Jwt jwt() {
        return Jwt.withTokenValue("signed-jwt")
                .header("alg", "RS256")
                .subject("john")
                .build();
    }

    @Test
    void returnsEncodedTokenValue() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt());

        String token = service.generateToken(authentication("ROLE_TRAINEE"));

        assertEquals("signed-jwt", token);
    }

    @Test
    void buildsClaimsWithIssuerAndRoles() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt());

        service.generateToken(authentication("ROLE_TRAINEE", "ROLE_ADMIN"));

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();

        assertEquals("self", Objects.requireNonNull(claims.getClaim("iss")).toString());
        List<String> roles = claims.getClaim("roles");
        assertEquals(List.of("ROLE_TRAINEE", "ROLE_ADMIN"), roles);
    }

    @Test
    void tokenExpiresOneHourAfterItIsIssued() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt());

        service.generateToken(authentication("ROLE_TRAINER"));

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        JwtClaimsSet claims = captor.getValue().getClaims();

        Instant issuedAt = Objects.requireNonNull(claims.getClaim("iat"));
        Instant expiresAt = Objects.requireNonNull(claims.getClaim("exp"));
        assertEquals(3600, expiresAt.getEpochSecond() - issuedAt.getEpochSecond());
        assertTrue(expiresAt.isAfter(issuedAt));
    }

    @Test
    void includesEmptyRolesWhenAuthenticationHasNoAuthorities() {
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt());

        service.generateToken(authentication());

        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
        verify(jwtEncoder).encode(captor.capture());
        List<String> roles = Objects.requireNonNull(captor.getValue().getClaims().getClaim("roles"));
        assertTrue(roles.isEmpty());
    }
}
