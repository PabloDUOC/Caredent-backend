package cl.caredent.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "atenciones")
public class Atencion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDate fecha;
    @Column(nullable = false, length = 200)
    private String motivo;
    @Column(length = 100)
    private String profesional;
    @Column(length = 2000)
    private String notas;

    public Atencion() {}

    public Atencion(Paciente paciente, LocalDate fecha, String motivo, String profesional, String notas) {
        this.paciente = paciente;
        this.fecha = fecha;
        this.motivo = motivo;
        this.profesional = profesional;
        this.notas = notas;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public LocalDate getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public String getProfesional() { return profesional; }
    public String getNotas() { return notas; }
}
