package cl.caredent.backend.security;

import cl.caredent.backend.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Genera y verifica JWT firmados con HMAC-SHA256. Hay dos tipos: "access" (corto) y "refresh" (largo). */
@Service
public class JwtService {
    public static final String TIPO_ACCESS = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessMs;
    private final long refreshMs;

    public JwtService(
            @Value("${caredent.jwt.secret}") String secret,
            @Value("${caredent.jwt.access-minutes}") long accessMinutes,
            @Value("${caredent.jwt.refresh-days}") long refreshDays) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres.");
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.accessMs = accessMinutes * 60_000L;
        this.refreshMs = refreshDays * 24L * 60L * 60_000L;
    }

    public String generarAccessToken(Usuario u) {
        return construir(u, TIPO_ACCESS, accessMs);
    }

    public String generarRefreshToken(Usuario u) {
        return construir(u, TIPO_REFRESH, refreshMs);
    }

    public long segundosAccess() {
        return accessMs / 1000;
    }

    private String construir(Usuario u, String tipo, long duracionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(u.getUsuario())
                .claim("uid", u.getId())
                .claim("rol", u.getRol().name())
                .claim("tipo", tipo)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracionMs))
                .signWith(key)
                .compact();
    }

    /** Verifica firma y expiracion. Lanza JwtException si el token no es valido. */
    public Claims leer(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public static Long idDe(Claims claims) {
        Object uid = claims.get("uid");
        return uid instanceof Number n ? n.longValue() : null;
    }

    public static String tipoDe(Claims claims) {
        Object tipo = claims.get("tipo");
        return tipo == null ? null : tipo.toString();
    }
}
