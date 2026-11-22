package cl.caredent.backend.model;

public enum Rol {
    ADMINISTRADOR("Administrador"),
    RECEPCIONISTA("Recepcionista"),
    PROFESIONAL("Médico / Profesional");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    /** El frontend usa el rol en minusculas: "administrador", "recepcionista", "profesional". */
    public String valorFront() {
        return name().toLowerCase();
    }

    public static Rol desdeTexto(String texto) {
        try {
            return Rol.valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
    }
}
