package cl.caredent.backend.config;

import cl.caredent.backend.model.*;
import cl.caredent.backend.repository.PacienteRepository;
import cl.caredent.backend.repository.UsuarioRepository;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Carga los usuarios y pacientes de demostracion (los mismos que tenia el frontend) si las tablas estan vacias. */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String CAMILA = "Dra. Camila Rivas";
    private static final String IGNACIO = "Dr. Ignacio Fuentes";

    private final UsuarioRepository usuarios;
    private final PacienteRepository pacientes;
    private final PasswordEncoder encoder;

    public DataSeeder(UsuarioRepository usuarios, PacienteRepository pacientes, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.pacientes = pacientes;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarios.count() == 0) {
            usuarios.save(new Usuario("admin", encoder.encode("admin123"), "Sofía Herrera", Rol.ADMINISTRADOR));
            usuarios.save(new Usuario("recepcion", encoder.encode("recepcion123"), "Valentina Muñoz", Rol.RECEPCIONISTA));
            usuarios.save(new Usuario("profesional", encoder.encode("profesional123"), CAMILA, Rol.PROFESIONAL));
            log.info("Usuarios de demostración creados (admin, recepcion, profesional).");
        }
        if (pacientes.count() == 0) {
            sembrarPacientes();
            log.info("Pacientes de demostración creados.");
        }
    }

    private static LocalDate f(String iso) {
        return LocalDate.parse(iso);
    }

    private Paciente base(String nombre, String apellidos, String rut, String nac, String tel, String correo, String dir,
                          String estado, String ultima, String med, String odo, String ale,
                          String grupo, String fichaFecha, String obs) {
        Paciente p = new Paciente();
        p.setNombre(nombre);
        p.setApellidos(apellidos);
        p.setRut(rut);
        p.setFechaNacimiento(f(nac));
        p.setTelefono(tel);
        p.setCorreo(correo);
        p.setDireccion(dir);
        p.setEstado(estado);
        p.setUltimaAtencion(f(ultima));
        p.setAntecedentes(new Antecedentes(med, odo, ale));
        p.setFichaClinica(new FichaClinica(grupo, f(fichaFecha), obs));
        return p;
    }

    private void sembrarPacientes() {
        Paciente juan = base("Juan", "Pérez Soto", "12.345.678-9", "1990-05-14", "+56 9 1234 5678", "juan.perez@ejemplo.cl",
                "Av. Siempre Viva 123, Santiago", "activo", "2026-08-20",
                "Hipertensión controlada con medicamento. Sin alergias a anestesia.",
                "Tratamiento de conducto en pieza 36 (2022). Uso de placa de descarga nocturna.", "Penicilina.",
                "O+", "2024-02-10", "Paciente colaborador. Buena higiene oral. Control de placa cada 6 meses.");
        juan.getDiagnosticos().add(new Diagnostico(juan, f("2026-08-20"), "Caries interproximal en pieza 26.", CAMILA));
        juan.getDiagnosticos().add(new Diagnostico(juan, f("2026-03-02"), "Gingivitis leve generalizada.", CAMILA));
        juan.getTratamientos().add(new Tratamiento(juan, "Obturación pieza 26", "en-curso", f("2026-08-20"), CAMILA, "Restauración con resina compuesta, sesión única."));
        juan.getTratamientos().add(new Tratamiento(juan, "Destartraje y pulido", "finalizado", f("2026-03-02"), CAMILA, "Profilaxis completa, control de placa bacteriana."));
        juan.getAtenciones().add(new Atencion(juan, f("2026-08-20"), "Control y obturación", CAMILA, "Paciente refiere sensibilidad leve al frío. Se indica pasta desensibilizante."));
        juan.getAtenciones().add(new Atencion(juan, f("2026-03-02"), "Destartraje", CAMILA, "Buena tolerancia al procedimiento. Se refuerza técnica de cepillado."));
        juan.getSeguimiento().add(new Seguimiento(juan, f("2026-03-02"), "Ingreso y diagnóstico inicial", "Se registran antecedentes y se detecta gingivitis leve.", false));
        juan.getSeguimiento().add(new Seguimiento(juan, f("2026-03-02"), "Destartraje y pulido", "Tratamiento finalizado sin complicaciones.", false));
        juan.getSeguimiento().add(new Seguimiento(juan, f("2026-08-20"), "Nueva caries detectada", "Diagnóstico de caries en pieza 26, inicio de obturación.", false));
        juan.getSeguimiento().add(new Seguimiento(juan, f("2026-09-15"), "Control de obturación", "Revisar adaptación marginal y sensibilidad.", true));
        juan.setProximoControl(new ProximoControl(f("2026-09-15"), "Control de obturación pieza 26", "pendiente"));

        Paciente maria = base("María", "Soto Vidal", "15.678.234-2", "1985-11-02", "+56 9 8765 4321", "maria.soto@ejemplo.cl",
                "Los Álamos 456, Providencia", "en-tratamiento", "2026-08-28",
                "Sin antecedentes mórbidos relevantes.", "Ortodoncia en la adolescencia. Bruxismo nocturno.", "Sin alergias conocidas.",
                "A+", "2023-07-19", "Usuaria de férula de relajación por bruxismo. Requiere seguimiento periodontal.");
        maria.getDiagnosticos().add(new Diagnostico(maria, f("2026-08-28"), "Periodontitis moderada, sector posterior inferior.", IGNACIO));
        maria.getTratamientos().add(new Tratamiento(maria, "Tratamiento periodontal", "en-curso", f("2026-08-28"), IGNACIO, "Raspaje y alisado radicular por cuadrantes, 4 sesiones planificadas."));
        maria.getAtenciones().add(new Atencion(maria, f("2026-08-28"), "Evaluación periodontal", IGNACIO, "Se realiza periodontograma. Inicio de plan de tratamiento por cuadrantes."));
        maria.getAtenciones().add(new Atencion(maria, f("2026-05-10"), "Control general", IGNACIO, "Sin hallazgos relevantes. Se refuerza uso de férula nocturna."));
        maria.getSeguimiento().add(new Seguimiento(maria, f("2026-05-10"), "Control general", "Evaluación de rutina, uso correcto de férula.", false));
        maria.getSeguimiento().add(new Seguimiento(maria, f("2026-08-28"), "Diagnóstico periodontal", "Se detecta periodontitis moderada, inicia tratamiento por cuadrantes.", false));
        maria.getSeguimiento().add(new Seguimiento(maria, f("2026-09-10"), "Raspaje cuadrante 2", "Continuación del tratamiento periodontal.", true));
        maria.setProximoControl(new ProximoControl(f("2026-09-10"), "Raspaje y alisado, cuadrante 2", "pendiente"));

        Paciente pedro = base("Pedro", "González Muñoz", "9.876.543-1", "1998-01-27", "+56 9 5544 3322", "pedro.gonzalez@ejemplo.cl",
                "Camino Real 789, Ñuñoa", "activo", "2026-07-05",
                "Asma leve, uso de inhalador ocasional.", "Extracción de terceros molares (2020).", "Sin alergias conocidas.",
                "B+", "2022-09-01", "Paciente sin controles hace más de un mes, se recomienda contactar.");
        pedro.getDiagnosticos().add(new Diagnostico(pedro, f("2026-07-05"), "Sin hallazgos patológicos activos.", CAMILA));
        pedro.getTratamientos().add(new Tratamiento(pedro, "Blanqueamiento dental", "finalizado", f("2026-07-05"), CAMILA, "Blanqueamiento en consulta, sesión única."));
        pedro.getAtenciones().add(new Atencion(pedro, f("2026-07-05"), "Blanqueamiento y control", CAMILA, "Procedimiento sin incidentes. Se indican cuidados post-blanqueamiento."));
        pedro.getSeguimiento().add(new Seguimiento(pedro, f("2026-07-05"), "Blanqueamiento dental", "Procedimiento realizado sin complicaciones.", false));

        Paciente camila = base("Camila", "Rojas Espinoza", "18.234.567-5", "2001-09-30", "+56 9 2233 4455", "camila.rojas@ejemplo.cl",
                "Pasaje Las Rosas 22, La Florida", "pendiente", "2026-06-12",
                "Sin antecedentes relevantes.", "Primera consulta en la clínica.", "Alergia al látex.",
                "O-", "2026-06-12", "Ficha recién creada, pendiente completar antecedentes familiares.");
        camila.getDiagnosticos().add(new Diagnostico(camila, f("2026-06-12"), "Caries múltiples en piezas posteriores, evaluación en curso.", IGNACIO));
        camila.getTratamientos().add(new Tratamiento(camila, "Plan de obturaciones múltiples", "pendiente", f("2026-09-05"), IGNACIO, "Plan de 3 sesiones para tratar caries detectadas."));
        camila.getAtenciones().add(new Atencion(camila, f("2026-06-12"), "Primera consulta", IGNACIO, "Evaluación inicial, se solicitan radiografías bite-wing."));
        camila.getSeguimiento().add(new Seguimiento(camila, f("2026-06-12"), "Primera consulta", "Evaluación inicial y solicitud de radiografías.", false));
        camila.getSeguimiento().add(new Seguimiento(camila, f("2026-09-05"), "Inicio plan de obturaciones", "Primera sesión del plan de tratamiento.", true));
        camila.setProximoControl(new ProximoControl(f("2026-09-05"), "Inicio plan de obturaciones", "pendiente"));

        Paciente diego = base("Diego", "Fuentes Araya", "14.111.222-8", "1979-04-18", "+56 9 6677 8899", "diego.fuentes@ejemplo.cl",
                "El Bosque 310, Macul", "inactivo", "2025-11-30",
                "Diabetes tipo 2, controlada.", "Uso de prótesis parcial removible superior.", "Sin alergias conocidas.",
                "AB+", "2021-03-15", "Paciente sin controles hace más de 6 meses.");
        diego.getDiagnosticos().add(new Diagnostico(diego, f("2025-11-30"), "Desadaptación leve de prótesis removible.", CAMILA));
        diego.getTratamientos().add(new Tratamiento(diego, "Ajuste de prótesis removible", "finalizado", f("2025-11-30"), CAMILA, "Rebase y ajuste oclusal de prótesis."));
        diego.getAtenciones().add(new Atencion(diego, f("2025-11-30"), "Ajuste de prótesis", CAMILA, "Se realiza rebase. Paciente indica mejor adaptación."));
        diego.getSeguimiento().add(new Seguimiento(diego, f("2025-11-30"), "Ajuste de prótesis", "Procedimiento realizado, se recomienda control en 6 meses.", false));

        pacientes.save(juan);
        pacientes.save(maria);
        pacientes.save(pedro);
        pacientes.save(camila);
        pacientes.save(diego);
    }
}
