package cl.caredent.backend.util;

import java.util.regex.Pattern;

/** Validacion de RUT chileno con formato 12.345.678-9. */
public final class RutUtil {
    public static final Pattern FORMATO = Pattern.compile("^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$");

    private RutUtil() {}

    public static boolean formatoValido(String rut) {
        return rut != null && FORMATO.matcher(rut.trim()).matches();
    }

    /** Verifica el digito verificador (modulo 11). */
    public static boolean digitoVerificadorValido(String rut) {
        String[] partes = rut.replace(".", "").toUpperCase().split("-");
        String cuerpo = partes[0];
        int suma = 0;
        int mult = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * mult;
            mult = mult == 7 ? 2 : mult + 1;
        }
        int r = 11 - (suma % 11);
        String esperado = r == 11 ? "0" : r == 10 ? "K" : String.valueOf(r);
        return partes[1].equals(esperado);
    }

    /** Deja el RUT en mayusculas y sin espacios, para que la unicidad no dependa de la escritura. */
    public static String normalizar(String rut) {
        return rut.trim().toUpperCase();
    }
}
