package cl.caredent.backend.service;

import cl.caredent.backend.dto.HoraDtos.*;
import cl.caredent.backend.exception.ApiException;
import cl.caredent.backend.model.HoraAgendada;
import cl.caredent.backend.model.Paciente;
import cl.caredent.backend.model.Rol;
import cl.caredent.backend.model.Usuario;
import cl.caredent.backend.repository.HoraAgendadaRepository;
import cl.caredent.backend.repository.PacienteRepository;
import cl.caredent.backend.repository.UsuarioRepository;
import cl.caredent.backend.security.AuthUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Agenda de horas (recepcionista). */
@Service
public class HoraService {
    private final HoraAgendadaRepository horas;
    private final PacienteRepository pacientes;
    private final UsuarioRepository usuarios;

    public HoraService(HoraAgendadaRepository horas, PacienteRepository pacientes, UsuarioRepository usuarios) {
        this.horas = horas;
        this.pacientes = pacientes;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<HoraDto> listar() {
        return horas.findAllByOrderByFechaAscHoraAsc().stream().map(HoraService::aDto).toList();
    }

    @Transactional
    public HoraDto agendar(HoraRequest req, AuthUser user) {
        Paciente paciente = pacientes.findById(req.pacienteId())
                .orElseThrow(() -> ApiException.noEncontrado("Paciente no encontrado."));
        if ("inactivo".equals(paciente.getEstado())) {
            throw ApiException.solicitudInvalida("No se pueden agendar horas para pacientes inactivos.");
        }
        Usuario profesional = usuarios.findByIdAndEliminadoElIsNull(req.profesionalId())
                .filter(u -> u.getRol() == Rol.PROFESIONAL)
                .orElseThrow(() -> ApiException.noEncontrado("Profesional no encontrado."));
        if (horas.existsByProfesionalIdAndFechaAndHora(profesional.getId(), req.fecha(), req.hora())) {
            throw ApiException.conflicto("Ese profesional ya tiene una hora agendada en ese horario.");
        }
        HoraAgendada h = new HoraAgendada();
        h.setPaciente(paciente);
        h.setProfesional(profesional);
        h.setFecha(req.fecha());
        h.setHora(req.hora());
        h.setMotivo(req.motivo().trim());
        h.setAgendadaPor(user.nombre());
        return aDto(horas.save(h));
    }

    @Transactional
    public void anular(Long id) {
        HoraAgendada h = horas.findById(id).orElseThrow(() -> ApiException.noEncontrado("Hora no encontrada."));
        horas.delete(h);
    }

    private static HoraDto aDto(HoraAgendada h) {
        Paciente p = h.getPaciente();
        return new HoraDto(h.getId(), p.getId(), p.getNombre() + " " + p.getApellidos(),
                h.getProfesional().getId(), h.getProfesional().getNombre(),
                h.getFecha(), h.getHora(), h.getMotivo(), h.getAgendadaPor());
    }
}
