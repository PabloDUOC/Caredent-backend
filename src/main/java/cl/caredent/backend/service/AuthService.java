package cl.caredent.backend.service;

import cl.caredent.backend.dto.AuthDtos.*;
import cl.caredent.backend.exception.ApiException;
import cl.caredent.backend.model.Rol;
import cl.caredent.backend.model.Usuario;
import cl.caredent.backend.repository.UsuarioRepository;
import cl.caredent.backend.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final String CREDENCIALES_INVALIDAS = "Usuario o contraseña incorrectos.";

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarios.findByUsuarioAndEliminadoElIsNull(req.usuario().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS));
        if (!encoder.matches(req.clave(), u.getClave())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, CREDENCIALES_INVALIDAS);
        }
        if (req.rol() != null && !req.rol().isBlank()) {
            Rol pedido = Rol.desdeTexto(req.rol());
            if (pedido != u.getRol()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Ese usuario corresponde al rol \"" + u.getRol().getEtiqueta() + "\".");
            }
        }
        return emitir(u);
    }

    /** Renovacion: canjea un refresh token valido por un nuevo par de tokens (rotacion). */
    @Transactional(readOnly = true)
    public AuthResponse renovar(RefreshRequest req) {
        try {
            Claims claims = jwt.leer(req.refreshToken());
            if (!JwtService.TIPO_REFRESH.equals(JwtService.tipoDe(claims))) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "El token entregado no es un refresh token.");
            }
            Usuario u = usuarios.findByIdAndEliminadoElIsNull(JwtService.idDe(claims))
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "El usuario ya no existe."));
            return emitir(u);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "La sesión expiró. Inicia sesión nuevamente.");
        }
    }

    public UsuarioSesion sesion(Long usuarioId) {
        Usuario u = usuarios.findByIdAndEliminadoElIsNull(usuarioId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "El usuario ya no existe."));
        return aSesion(u);
    }

    private AuthResponse emitir(Usuario u) {
        return new AuthResponse(jwt.generarAccessToken(u), jwt.generarRefreshToken(u), "Bearer", jwt.segundosAccess(), aSesion(u));
    }

    private static UsuarioSesion aSesion(Usuario u) {
        return new UsuarioSesion(u.getId(), u.getUsuario(), u.getNombre(), u.getRol().valorFront());
    }
}
