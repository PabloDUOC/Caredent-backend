package cl.caredent.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "diagnosticos")
public class Diagnostico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDate fecha;
    @Column(nullable = false, length = 500)
    private String descripcion;
    @Column(length = 100)
    private String profesional;

    public Diagnostico() {}

    public Diagnostico(Paciente paciente, LocalDate fecha, String descripcion, String profesional) {
        this.paciente = paciente;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.profesional = profesional;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public LocalDate getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getProfesional() { return profesional; }
}
