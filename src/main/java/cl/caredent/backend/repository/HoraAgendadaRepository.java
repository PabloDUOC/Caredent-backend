package cl.caredent.backend.repository;

import cl.caredent.backend.model.HoraAgendada;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HoraAgendadaRepository extends JpaRepository<HoraAgendada, Long> {
    boolean existsByProfesionalIdAndFechaAndHora(Long profesionalId, LocalDate fecha, String hora);

    List<HoraAgendada> findAllByOrderByFechaAscHoraAsc();

    @Modifying
    @Query("delete from HoraAgendada h where h.paciente.id = :id")
    void eliminarPorPaciente(@Param("id") Long pacienteId);

    @Modifying
    @Query("delete from HoraAgendada h where h.profesional.id = :id")
    void eliminarPorProfesional(@Param("id") Long profesionalId);
}
