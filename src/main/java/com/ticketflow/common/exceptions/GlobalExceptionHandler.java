package com.ticketflow.common.exceptions;

import com.ticketflow.common.dtos.FieldErrorDTO;
import com.ticketflow.common.dtos.StandardErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardErrorDTO> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<FieldErrorDTO> fieldErrors = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new FieldErrorDTO(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        HttpStatus status = HttpStatus.BAD_REQUEST;
        StandardErrorDTO error = new StandardErrorDTO(
            status.value(),
            status.getReasonPhrase(),
            "Erro de validação nos campos informados.",
            request.getRequestURI(),
            fieldErrors
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardErrorDTO> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        StandardErrorDTO error = new StandardErrorDTO(
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<StandardErrorDTO> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;
        StandardErrorDTO error = new StandardErrorDTO(
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<StandardErrorDTO> handleOptimisticLockingFailureException(OptimisticLockingFailureException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CONFLICT;
        List<FieldErrorDTO> fieldErrors = List.of(
            new FieldErrorDTO("version", "Versão do registro desatualizada (conflito de concorrência).")
        );
        StandardErrorDTO error = new StandardErrorDTO(
            status.value(),
            status.getReasonPhrase(),
            "O chamado já foi assumido por outro atendente ou foi modificado simultaneamente.",
            request.getRequestURI(),
            fieldErrors
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardErrorDTO> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        StandardErrorDTO error = new StandardErrorDTO(
            status.value(),
            status.getReasonPhrase(),
            "Acesso negado para o recurso solicitado.",
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardErrorDTO> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        StandardErrorDTO error = new StandardErrorDTO(
            status.value(),
            status.getReasonPhrase(),
            "Credenciais inválidas ou sessão expirada.",
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<StandardErrorDTO> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        StandardErrorDTO error = new StandardErrorDTO(
            status.value(),
            status.getReasonPhrase(),
            ex.getReason() != null ? ex.getReason() : ex.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardErrorDTO> handleGenericException(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        StandardErrorDTO error = new StandardErrorDTO(
            status.value(),
            status.getReasonPhrase(),
            "Ocorreu um erro interno inesperado no servidor.",
            request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }
}
