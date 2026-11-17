package ml.apes.rh.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Centralisation de toutes les exceptions de l'API : un seul format de réponse (ApiError). */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> build(
        HttpStatus status,
        String message,
        HttpServletRequest req,
        Map<String, String> details
    ) {
        ApiError body = new ApiError(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            req.getRequestURI(),
            details
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), req, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> noResource(
        NoResourceFoundException ex,
        HttpServletRequest req
    ) {
        return build(HttpStatus.NOT_FOUND, "Ressource introuvable", req, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> business(BusinessException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getMessage(), req, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(
        MethodArgumentNotValidException ex,
        HttpServletRequest req
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getBindingResult()
            .getFieldErrors()
            .forEach(e -> details.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return build(
            HttpStatus.BAD_REQUEST,
            "Des champs sont invalides : " + String.join(" ; ", details.values()),
            req,
            details
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraint(
        ConstraintViolationException ex,
        HttpServletRequest req
    ) {
        Map<String, String> details = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
            details.putIfAbsent(v.getPropertyPath().toString(), v.getMessage())
        );
        return build(
            HttpStatus.BAD_REQUEST,
            "Des champs sont invalides : " + String.join(" ; ", details.values()),
            req,
            details
        );
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
    })
    public ResponseEntity<ApiError> badRequest(Exception ex, HttpServletRequest req) {
        return build(
            HttpStatus.BAD_REQUEST,
            "Requête invalide : vérifiez le format des données envoyées.",
            req,
            null
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(
        HttpRequestMethodNotSupportedException ex,
        HttpServletRequest req
    ) {
        return build(
            HttpStatus.METHOD_NOT_ALLOWED,
            "Méthode HTTP non autorisée sur cette ressource",
            req,
            null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integrity(
        DataIntegrityViolationException ex,
        HttpServletRequest req
    ) {
        log.warn("Violation d'intégrité : {}", ex.getMostSpecificCause().getMessage());
        return build(
            HttpStatus.CONFLICT,
            "Opération impossible : la valeur existe déjà ou l'élément est utilisé ailleurs dans l'application.",
            req,
            null
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> denied(AccessDeniedException ex, HttpServletRequest req) {
        return build(
            HttpStatus.FORBIDDEN,
            "Vous n'avez pas le droit d'effectuer cette action.",
            req,
            null
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> badCredentials(
        BadCredentialsException ex,
        HttpServletRequest req
    ) {
        return build(HttpStatus.UNAUTHORIZED, "Matricule ou mot de passe incorrect.", req, null);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> disabled(DisabledException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Ce compte est désactivé.", req, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> auth(AuthenticationException ex, HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, "Authentification requise.", req, null);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tooBig(
        MaxUploadSizeExceededException ex,
        HttpServletRequest req
    ) {
        return build(
            HttpStatus.PAYLOAD_TOO_LARGE,
            "Le fichier dépasse la taille maximale autorisée (10 Mo).",
            req,
            null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception ex, HttpServletRequest req) {
        log.error("Erreur inattendue sur {}", req.getRequestURI(), ex);
        return build(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Une erreur interne est survenue. Contactez l'administrateur.",
            req,
            null
        );
    }
}
