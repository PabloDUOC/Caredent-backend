package cl.caredent.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tratamientos")
public class Tratamiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(nullable = false, length = 150)
    private String nombre;
    /** pendiente | en-curso | finalizado */
    @Column(nullable = false, length = 20)
    private String estado;
    private LocalDate fechaInicio;
    @Column(length = 100)
    private String profesional;
    @Column(length = 1000)
    private String descripcion;

    public Tratamiento() {}

    public Tratamiento(Paciente paciente, String nombre, String estado, LocalDate fechaInicio, String profesional, String descripcion) {
        this.paciente = paciente;
        this.nombre = nombre;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.profesional = profesional;
        this.descripcion = descripcion;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public String getProfesional() { return profesional; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
