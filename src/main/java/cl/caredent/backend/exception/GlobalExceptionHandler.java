package cl.caredent.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Convierte cualquier error en una respuesta JSON uniforme: {status, error, message, path, errores}. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> api(ApiException ex, HttpServletRequest req) {
        return cuerpo(ex.getStatus(), ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validacion(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> errores.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        String mensaje = errores.isEmpty() ? "Datos invalidos." : errores.values().iterator().next();
        return cuerpo(HttpStatus.BAD_REQUEST, mensaje, req, errores);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, Object>> cuerpoInvalido(Exception ex, HttpServletRequest req) {
        return cuerpo(HttpStatus.BAD_REQUEST, "La solicitud tiene un formato invalido (revisa fechas y tipos de dato).", req, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> denegado(AccessDeniedException ex, HttpServletRequest req) {
        return cuerpo(HttpStatus.FORBIDDEN, "No tienes permisos para realizar esta accion.", req, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridad(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violacion de integridad: {}", ex.getMostSpecificCause().getMessage());
        return cuerpo(HttpStatus.CONFLICT, "La operacion viola una restriccion de datos (registro duplicado o en uso).", req, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> metodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return cuerpo(HttpStatus.METHOD_NOT_ALLOWED, "Metodo HTTP no permitido para este recurso.", req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> noExiste(NoResourceFoundException ex, HttpServletRequest req) {
        return cuerpo(HttpStatus.NOT_FOUND, "Recurso no encontrado.", req, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generico(Exception ex, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), ex);
        return cuerpo(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor.", req, null);
    }

    private ResponseEntity<Map<String, Object>> cuerpo(HttpStatus status, String mensaje, HttpServletRequest req, Map<String, String> errores) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", mensaje);
        body.put("path", req.getRequestURI());
        if (errores != null && !errores.isEmpty()) body.put("errores", errores);
        return ResponseEntity.status(status).body(body);
    }
}
