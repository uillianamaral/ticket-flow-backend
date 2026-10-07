package com.ticketflow.common.exceptions;

import com.ticketflow.common.dtos.StandardErrorDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerUnitTest {

    private GlobalExceptionHandler exceptionHandler;
    private HttpServletRequest request;

    @SuppressWarnings("unused")
    private void dummyMethod(String param) {}

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/tickets");
    }

    @Test
    @DisplayName("Deve capturar ResourceNotFoundException e retornar HTTP 404")
    void shouldHandleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Chamado", "123");

        ResponseEntity<StandardErrorDTO> response = exceptionHandler.handleResourceNotFoundException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains("Chamado não encontrado");
        assertThat(response.getBody().path()).isEqualTo("/api/tickets");
    }

    @Test
    @DisplayName("Deve capturar OptimisticLockingFailureException e retornar HTTP 409 com detalhes")
    void shouldHandleOptimisticLockingFailureException() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Conflito de versão");

        ResponseEntity<StandardErrorDTO> response = exceptionHandler.handleOptimisticLockingFailureException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().message()).contains("assumido por outro atendente");
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("version");
    }

    @Test
    @DisplayName("Deve capturar BusinessException e retornar HTTP 422")
    void shouldHandleBusinessException() {
        BusinessException ex = new BusinessException("Transição inválida de status");

        ResponseEntity<StandardErrorDTO> response = exceptionHandler.handleBusinessException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(422);
        assertThat(response.getBody().message()).isEqualTo("Transição inválida de status");
    }

    @Test
    @DisplayName("Deve capturar AccessDeniedException e retornar HTTP 403")
    void shouldHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Acesso negado");

        ResponseEntity<StandardErrorDTO> response = exceptionHandler.handleAccessDeniedException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(403);
    }

    @Test
    @DisplayName("Deve capturar MethodArgumentNotValidException e retornar HTTP 400 com lista de erros de campo")
    void shouldHandleMethodArgumentNotValidException() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "ticketDTO");
        bindingResult.addError(new FieldError("ticketDTO", "title", "O título é obrigatório"));

        Method method = GlobalExceptionHandlerUnitTest.class.getDeclaredMethod("dummyMethod", String.class);
        MethodParameter methodParameter = new MethodParameter(method, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<StandardErrorDTO> response = exceptionHandler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().fieldErrors()).hasSize(1);
        assertThat(response.getBody().fieldErrors().get(0).field()).isEqualTo("title");
        assertThat(response.getBody().fieldErrors().get(0).message()).isEqualTo("O título é obrigatório");
    }
}
