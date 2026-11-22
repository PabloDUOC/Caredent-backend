package cl.caredent.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String usuario;

    /** Hash BCrypt, nunca la clave en texto plano. */
    @Column(nullable = false, length = 100)
    private String clave;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    /** Borrado logico (papelera): null = activo. */
    private LocalDateTime eliminadoEl;

    public Usuario() {}

    public Usuario(String usuario, String clave, String nombre, Rol rol) {
        this.usuario = usuario;
        this.clave = clave;
        this.nombre = nombre;
        this.rol = rol;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public LocalDateTime getEliminadoEl() { return eliminadoEl; }
    public void setEliminadoEl(LocalDateTime eliminadoEl) { this.eliminadoEl = eliminadoEl; }
}
