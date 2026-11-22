package cl.caredent.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public class FichaClinica {
    @Column(name = "grupo_sanguineo", length = 20)
    private String grupoSanguineo;
    @Column(name = "ficha_fecha_creacion")
    private LocalDate fechaCreacion;
    @Column(name = "ficha_observaciones", length = 2000)
    private String observaciones;

    public FichaClinica() {}

    public FichaClinica(String grupoSanguineo, LocalDate fechaCreacion, String observaciones) {
        this.grupoSanguineo = grupoSanguineo;
        this.fechaCreacion = fechaCreacion;
        this.observaciones = observaciones;
    }

    public String getGrupoSanguineo() { return grupoSanguineo; }
    public void setGrupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; }
    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
