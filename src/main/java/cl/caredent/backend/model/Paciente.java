package cl.caredent.backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(name = "pacientes")
public class Paciente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;
    @Column(nullable = false, length = 100)
    private String apellidos;
    @Column(nullable = false, unique = true, length = 15)
    private String rut;
    private LocalDate fechaNacimiento;
    @Column(length = 30)
    private String telefono;
    @Column(length = 120)
    private String correo;
    @Column(length = 200)
    private String direccion;
    /** activo | en-tratamiento | pendiente | inactivo */
    @Column(nullable = false, length = 20)
    private String estado = "pendiente";
    private LocalDate ultimaAtencion;

    @Embedded
    private Antecedentes antecedentes = new Antecedentes();
    @Embedded
    private FichaClinica fichaClinica = new FichaClinica();
    /** null cuando el paciente no tiene un control agendado. */
    @Embedded
    private ProximoControl proximoControl;

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha DESC, id DESC")
    @BatchSize(size = 50)
    private List<Diagnostico> diagnosticos = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fechaInicio DESC, id DESC")
    @BatchSize(size = 50)
    private List<Tratamiento> tratamientos = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha DESC, id DESC")
    @BatchSize(size = 50)
    private List<Atencion> atenciones = new ArrayList<>();

    @OneToMany(mappedBy = "paciente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("fecha ASC, id ASC")
    @BatchSize(size = 50)
    private List<Seguimiento> seguimiento = new ArrayList<>();

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getRut() { return rut; }
    public void setRut(String rut) { this.rut = rut; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getUltimaAtencion() { return ultimaAtencion; }
    public void setUltimaAtencion(LocalDate ultimaAtencion) { this.ultimaAtencion = ultimaAtencion; }
    public Antecedentes getAntecedentes() { return antecedentes; }
    public void setAntecedentes(Antecedentes antecedentes) { this.antecedentes = antecedentes; }
    public FichaClinica getFichaClinica() { return fichaClinica; }
    public void setFichaClinica(FichaClinica fichaClinica) { this.fichaClinica = fichaClinica; }
    public ProximoControl getProximoControl() { return proximoControl; }
    public void setProximoControl(ProximoControl proximoControl) { this.proximoControl = proximoControl; }
    public List<Diagnostico> getDiagnosticos() { return diagnosticos; }
    public List<Tratamiento> getTratamientos() { return tratamientos; }
    public List<Atencion> getAtenciones() { return atenciones; }
    public List<Seguimiento> getSeguimiento() { return seguimiento; }
}
