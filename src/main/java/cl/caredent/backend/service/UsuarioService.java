package cl.caredent.backend.service;

import cl.caredent.backend.dto.UsuarioDtos.*;
import cl.caredent.backend.exception.ApiException;
import cl.caredent.backend.model.Rol;
import cl.caredent.backend.model.Usuario;
import cl.caredent.backend.repository.HoraAgendadaRepository;
import cl.caredent.backend.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Regla de negocio: el administrador solo gestiona usuarios "profesional" y "recepcionista". */
@Service
public class UsuarioService {
    private static final List<Rol> GESTIONABLES = List.of(Rol.PROFESIONAL, Rol.RECEPCIONISTA);

    private final UsuarioRepository usuarios;
    private final HoraAgendadaRepository horas;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, HoraAgendadaRepository horas, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.horas = horas;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listarActivos() {
        return usuarios.findByRolInAndEliminadoElIsNullOrderByNombreAsc(GESTIONABLES).stream().map(UsuarioService::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listarPapelera() {
        return usuarios.findByRolInAndEliminadoElIsNotNullOrderByEliminadoElDesc(GESTIONABLES).stream().map(UsuarioService::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ProfesionalDto> listarProfesionales() {
        return usuarios.findByRolAndEliminadoElIsNullOrderByNombreAsc(Rol.PROFESIONAL).stream()
                .map(u -> new ProfesionalDto(u.getId(), u.getNombre())).toList();
    }

    @Transactional
    public UsuarioDto crear(UsuarioRequest req) {
        Rol rol = Rol.desdeTexto(req.rol());
        if (rol == null || !GESTIONABLES.contains(rol)) {
            throw ApiException.solicitudInvalida("Solo se pueden crear usuarios con rol profesional o recepcionista.");
        }
        String nombreUsuario = req.usuario().trim().toLowerCase();
        if (usuarios.existsByUsuario(nombreUsuario)) {
            throw ApiException.conflicto("Ese nombre de usuario ya existe (puede estar en la papelera).");
        }
        Usuario u = new Usuario(nombreUsuario, encoder.encode(req.clave()), req.nombre().trim(), rol);
        return aDto(usuarios.save(u));
    }

    /** Borrado logico: el usuario pasa a la papelera y ya no puede iniciar sesion. */
    @Transactional
    public void enviarAPapelera(Long id) {
        Usuario u = gestionable(id);
        if (u.getEliminadoEl() != null) throw ApiException.conflicto("El usuario ya está en la papelera.");
        u.setEliminadoEl(LocalDateTime.now());
    }

    @Transactional
    public UsuarioDto restaurar(Long id) {
        Usuario u = gestionable(id);
        if (u.getEliminadoEl() == null) throw ApiException.conflicto("El usuario no está en la papelera.");
        u.setEliminadoEl(null);
        return aDto(u);
    }

    @Transactional
    public void eliminarDefinitivo(Long id) {
        Usuario u = gestionable(id);
        if (u.getEliminadoEl() == null) {
            throw ApiException.solicitudInvalida("Primero envía el usuario a la papelera.");
        }
        horas.eliminarPorProfesional(u.getId());
        usuarios.delete(u);
    }

    private Usuario gestionable(Long id) {
        Usuario u = usuarios.findById(id).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado."));
        if (!GESTIONABLES.contains(u.getRol())) {
            throw new ApiException(org.springframework.http.HttpStatus.FORBIDDEN, "Los administradores no se pueden gestionar desde aquí.");
        }
        return u;
    }

    private static UsuarioDto aDto(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNombre(), u.getUsuario(), u.getRol().valorFront(), u.getEliminadoEl());
    }
}
