package cl.caredent.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/** Hora agendada por recepcion para un paciente con un profesional. */
@Entity
@Table(name = "horas_agendadas",
        uniqueConstraints = @UniqueConstraint(name = "uk_hora_profesional", columnNames = {"profesional_id", "fecha", "hora"}))
public class HoraAgendada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profesional_id")
    private Usuario profesional;

    @Column(nullable = false)
    private LocalDate fecha;
    /** Formato HH:mm */
    @Column(nullable = false, length = 5)
    private String hora;
    @Column(nullable = false, length = 300)
    private String motivo;
    @Column(length = 100)
    private String agendadaPor;

    public HoraAgendada() {}

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }
    public Usuario getProfesional() { return profesional; }
    public void setProfesional(Usuario profesional) { this.profesional = profesional; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getAgendadaPor() { return agendadaPor; }
    public void setAgendadaPor(String agendadaPor) { this.agendadaPor = agendadaPor; }
}
