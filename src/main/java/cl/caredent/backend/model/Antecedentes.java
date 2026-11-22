package cl.caredent.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Antecedentes {
    @Column(name = "antecedentes_medicos", length = 2000)
    private String medicos;
    @Column(name = "antecedentes_odontologicos", length = 2000)
    private String odontologicos;
    @Column(name = "alergias", length = 1000)
    private String alergias;

    public Antecedentes() {}

    public Antecedentes(String medicos, String odontologicos, String alergias) {
        this.medicos = medicos;
        this.odontologicos = odontologicos;
        this.alergias = alergias;
    }

    public String getMedicos() { return medicos; }
    public void setMedicos(String medicos) { this.medicos = medicos; }
    public String getOdontologicos() { return odontologicos; }
    public void setOdontologicos(String odontologicos) { this.odontologicos = odontologicos; }
    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }
}
