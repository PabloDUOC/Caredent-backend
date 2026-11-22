package cl.caredent.backend.repository;

import cl.caredent.backend.model.Rol;
import cl.caredent.backend.model.Usuario;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuarioAndEliminadoElIsNull(String usuario);

    Optional<Usuario> findByIdAndEliminadoElIsNull(Long id);

    boolean existsByUsuario(String usuario);

    List<Usuario> findByRolInAndEliminadoElIsNullOrderByNombreAsc(Collection<Rol> roles);

    List<Usuario> findByRolInAndEliminadoElIsNotNullOrderByEliminadoElDesc(Collection<Rol> roles);

    List<Usuario> findByRolAndEliminadoElIsNullOrderByNombreAsc(Rol rol);
}
