package cl.caredent.backend.service;

import cl.caredent.backend.dto.PacienteDtos.*;
import cl.caredent.backend.exception.ApiException;
import cl.caredent.backend.model.*;
import cl.caredent.backend.repository.HoraAgendadaRepository;
import cl.caredent.backend.repository.PacienteRepository;
import cl.caredent.backend.security.AuthUser;
import cl.caredent.backend.util.RutUtil;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Logica de negocio de pacientes, fichas clinicas, tratamientos, diagnosticos y controles. */
@Service
public class PacienteService {
    private static final Set<String> ESTADOS_PACIENTE = Set.of("activo", "en-tratamiento", "pendiente", "inactivo");
    private static final Set<String> ESTADOS_TRATAMIENTO = Set.of("pendiente", "en-curso", "finalizado");
    private static final String POR_REGISTRAR = "Por registrar";

    private final PacienteRepository pacientes;
    private final HoraAgendadaRepository horas;

    public PacienteService(PacienteRepository pacientes, HoraAgendadaRepository horas) {
        this.pacientes = pacientes;
        this.horas = horas;
    }

    /* ------------------------------ Lectura ------------------------------ */

    @Transactional(readOnly = true)
    public List<PacienteDto> listar() {
        return pacientes.findAll(Sort.by(Sort.Direction.ASC, "id")).stream().map(PacienteService::aDto).toList();
    }

    @Transactional(readOnly = true)
    public List<PacienteResumenDto> listarResumen() {
        return pacientes.findAll(Sort.by(Sort.Direction.ASC, "apellidos", "nombre")).stream()
                .map(p -> new PacienteResumenDto(p.getId(), p.getNombre(), p.getApellidos(), p.getRut(), p.getEstado())).toList();
    }

    @Transactional(readOnly = true)
    public PacienteDto obtener(Long id) {
        return aDto(buscar(id));
    }

    /* ------------------------------ Paciente ------------------------------ */

    @Transactional
    public PacienteDto crear(PacienteRequest req) {
        String rut = RutUtil.normalizar(req.rut());
        if (!RutUtil.digitoVerificadorValido(rut)) {
            throw ApiException.solicitudInvalida("El dígito verificador del RUT no es correcto.");
        }
        if (pacientes.existsByRut(rut)) {
            throw ApiException.conflicto("Ya existe un paciente con ese RUT.");
        }
        LocalDate hoy = LocalDate.now();
        Paciente p = new Paciente();
        p.setRut(rut);
        p.setEstado("pendiente");
        aplicarDatos(p, req);
        p.setFichaClinica(new FichaClinica(grupo(req.grupoSanguineo()), hoy, "Ficha creada al momento del registro."));
        p.getSeguimiento().add(new Seguimiento(p, hoy, "Ingreso al sistema", "Paciente registrado en Caredent.", false));
        String motivo = req.motivoConsulta() == null ? "" : req.motivoConsulta().trim();
        if (!motivo.isEmpty()) {
            p.getAtenciones().add(new Atencion(p, hoy, "Primera consulta", "Por asignar", motivo));
        }
        return aDto(pacientes.save(p));
    }

    @Transactional
    public PacienteDto modificar(Long id, PacienteRequest req) {
        Paciente p = buscar(id);
        String rut = RutUtil.normalizar(req.rut());
        // Igual que antes en el front: si el RUT no cambia no se revalida (fichas de demostracion con RUT ficticio).
        if (!rut.equals(p.getRut())) {
            if (!RutUtil.digitoVerificadorValido(rut)) {
                throw ApiException.solicitudInvalida("El dígito verificador del RUT no es correcto.");
            }
            if (pacientes.existsByRutAndIdNot(rut, id)) {
                throw ApiException.conflicto("Ya existe un paciente con ese RUT.");
            }
            p.setRut(rut);
        }
        aplicarDatos(p, req);
        if (req.estado() != null && !req.estado().isBlank()) {
            if (!ESTADOS_PACIENTE.contains(req.estado())) {
                throw ApiException.solicitudInvalida("Estado de paciente no válido.");
            }
            p.setEstado(req.estado());
        }
        p.getFichaClinica().setGrupoSanguineo(grupo(req.grupoSanguineo()));
        return aDto(p);
    }

    @Transactional
    public void eliminar(Long id) {
        Paciente p = buscar(id);
        horas.eliminarPorPaciente(id);
        pacientes.delete(p);
    }

    /* ------------------------------ Tratamientos ------------------------------ */

    @Transactional
    public TratamientoDto agregarTratamiento(Long pacienteId, TratamientoRequest req, AuthUser user) {
        Paciente p = buscar(pacienteId);
        Tratamiento t = new Tratamiento(p, req.nombre().trim(), estadoTratamiento(req.estado()), LocalDate.now(),
                user.nombre(), textoOVacio(req.descripcion()));
        p.getTratamientos().add(t);
        pacientes.saveAndFlush(p);
        return aDto(t);
    }

    @Transactional
    public TratamientoDto modificarTratamiento(Long pacienteId, Long tratamientoId, TratamientoRequest req) {
        Tratamiento t = tratamiento(buscar(pacienteId), tratamientoId);
        t.setNombre(req.nombre().trim());
        t.setDescripcion(textoOVacio(req.descripcion()));
        t.setEstado(estadoTratamiento(req.estado()));
        return aDto(t);
    }

    @Transactional
    public void eliminarTratamiento(Long pacienteId, Long tratamientoId) {
        Paciente p = buscar(pacienteId);
        p.getTratamientos().remove(tratamiento(p, tratamientoId));
    }

    /* ------------------------------ Diagnosticos ------------------------------ */

    @Transactional
    public DiagnosticoDto agregarDiagnostico(Long pacienteId, DiagnosticoRequest req, AuthUser user) {
        Paciente p = buscar(pacienteId);
        Diagnostico d = new Diagnostico(p, req.fecha() != null ? req.fecha() : LocalDate.now(), req.descripcion().trim(), user.nombre());
        p.getDiagnosticos().add(d);
        pacientes.saveAndFlush(p);
        return aDto(d);
    }

    @Transactional
    public void eliminarDiagnostico(Long pacienteId, Long diagnosticoId) {
        Paciente p = buscar(pacienteId);
        Diagnostico d = p.getDiagnosticos().stream().filter(x -> x.getId().equals(diagnosticoId)).findFirst()
                .orElseThrow(() -> ApiException.noEncontrado("Diagnóstico no encontrado."));
        p.getDiagnosticos().remove(d);
    }

    /* ------------------------------ Controles ------------------------------ */

    /** Crea o reemplaza el proximo control y deja un hito "futuro" en el seguimiento. */
    @Transactional
    public ControlDto establecerControl(Long pacienteId, ControlRequest req) {
        Paciente p = buscar(pacienteId);
        p.getSeguimiento().removeIf(Seguimiento::isFuturo);
        p.getSeguimiento().add(new Seguimiento(p, req.fecha(), req.motivo().trim(), "Control programado.", true));
        p.setProximoControl(new ProximoControl(req.fecha(), req.motivo().trim(), "pendiente"));
        return aDto(p.getProximoControl());
    }

    @Transactional
    public void eliminarControl(Long pacienteId) {
        Paciente p = buscar(pacienteId);
        p.getSeguimiento().removeIf(Seguimiento::isFuturo);
        p.setProximoControl(null);
    }

    /* ------------------------------ Auxiliares ------------------------------ */

    private Paciente buscar(Long id) {
        return pacientes.findById(id).orElseThrow(() -> ApiException.noEncontrado("Paciente no encontrado."));
    }

    private static Tratamiento tratamiento(Paciente p, Long id) {
        return p.getTratamientos().stream().filter(t -> t.getId().equals(id)).findFirst()
                .orElseThrow(() -> ApiException.noEncontrado("Tratamiento no encontrado."));
    }

    private static String estadoTratamiento(String estado) {
        if (estado == null || estado.isBlank()) return "pendiente";
        if (!ESTADOS_TRATAMIENTO.contains(estado)) throw ApiException.solicitudInvalida("Estado de tratamiento no válido.");
        return estado;
    }

    private static String grupo(String g) {
        return g == null || g.isBlank() ? POR_REGISTRAR : g.trim();
    }

    private static String textoOVacio(String s) {
        return s == null ? "" : s.trim();
    }

    private static String valorOPorDefecto(String valor, String porDefecto) {
        return valor == null || valor.isBlank() ? porDefecto : valor.trim();
    }

    private static void aplicarDatos(Paciente p, PacienteRequest req) {
        p.setNombre(req.nombre().trim());
        p.setApellidos(req.apellidos().trim());
        p.setFechaNacimiento(req.fechaNacimiento());
        p.setTelefono(req.telefono().trim());
        p.setCorreo(req.correo().trim());
        p.setDireccion(valorOPorDefecto(req.direccion(), POR_REGISTRAR));
        AntecedentesDto a = req.antecedentes();
        p.setAntecedentes(new Antecedentes(
                valorOPorDefecto(a == null ? null : a.medicos(), "Por registrar."),
                valorOPorDefecto(a == null ? null : a.odontologicos(), "Por registrar."),
                valorOPorDefecto(a == null ? null : a.alergias(), "Sin alergias conocidas.")));
    }

    /* ------------------------------ Mapeo a DTO ------------------------------ */

    static PacienteDto aDto(Paciente p) {
        Antecedentes a = p.getAntecedentes();
        FichaClinica f = p.getFichaClinica();
        return new PacienteDto(
                p.getId(), p.getNombre(), p.getApellidos(), p.getRut(), p.getFechaNacimiento(),
                p.getTelefono(), p.getCorreo(), p.getDireccion(), p.getEstado(), p.getUltimaAtencion(),
                new AntecedentesDto(a.getMedicos(), a.getOdontologicos(), a.getAlergias()),
                new FichaClinicaDto(f.getGrupoSanguineo(), f.getFechaCreacion(), f.getObservaciones()),
                p.getDiagnosticos().stream().map(PacienteService::aDto).toList(),
                p.getTratamientos().stream().map(PacienteService::aDto).toList(),
                p.getAtenciones().stream().map(x -> new AtencionDto(x.getId(), x.getFecha(), x.getMotivo(), x.getProfesional(), x.getNotas())).toList(),
                p.getSeguimiento().stream().map(s -> new SeguimientoDto(s.getId(), s.getFecha(), s.getTitulo(), s.getDescripcion(), s.isFuturo())).toList(),
                p.getProximoControl() == null || p.getProximoControl().getFecha() == null ? null : aDto(p.getProximoControl()));
    }

    private static DiagnosticoDto aDto(Diagnostico d) {
        return new DiagnosticoDto(d.getId(), d.getFecha(), d.getDescripcion(), d.getProfesional());
    }

    private static TratamientoDto aDto(Tratamiento t) {
        return new TratamientoDto(t.getId(), t.getNombre(), t.getEstado(), t.getFechaInicio(), t.getProfesional(), t.getDescripcion());
    }

    private static ControlDto aDto(ProximoControl c) {
        return new ControlDto(c.getFecha(), c.getMotivo(), c.getEstado());
    }
}
