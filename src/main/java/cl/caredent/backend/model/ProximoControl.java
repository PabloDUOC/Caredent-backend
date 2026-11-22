package cl.caredent.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public class ProximoControl {
    @Column(name = "control_fecha")
    private LocalDate fecha;
    @Column(name = "control_motivo", length = 300)
    private String motivo;
    @Column(name = "control_estado", length = 20)
    private String estado;

    public ProximoControl() {}

    public ProximoControl(LocalDate fecha, String motivo, String estado) {
        this.fecha = fecha;
        this.motivo = motivo;
        this.estado = estado;
    }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
