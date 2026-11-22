package cl.caredent.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "seguimientos")
public class Seguimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @Column(nullable = false)
    private LocalDate fecha;
    @Column(nullable = false, length = 200)
    private String titulo;
    @Column(length = 1000)
    private String descripcion;
    /** true = hito programado a futuro (control agendado). */
    @Column(nullable = false)
    private boolean futuro;

    public Seguimiento() {}

    public Seguimiento(Paciente paciente, LocalDate fecha, String titulo, String descripcion, boolean futuro) {
        this.paciente = paciente;
        this.fecha = fecha;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.futuro = futuro;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public LocalDate getFecha() { return fecha; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public boolean isFuturo() { return futuro; }
}
