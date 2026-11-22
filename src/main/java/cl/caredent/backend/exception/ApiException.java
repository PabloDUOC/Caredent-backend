package cl.caredent.backend.exception;

import org.springframework.http.HttpStatus;

/** Error de negocio con el codigo HTTP que debe devolver la API. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static ApiException noEncontrado(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

    public static ApiException conflicto(String message) {
        return new ApiException(HttpStatus.CONFLICT, message);
    }

    public static ApiException solicitudInvalida(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }
}
