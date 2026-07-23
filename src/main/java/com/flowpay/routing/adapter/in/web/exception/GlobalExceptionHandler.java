package com.flowpay.routing.adapter.in.web.exception;

import com.flowpay.routing.domain.exception.AgentUnavailableException;
import com.flowpay.routing.domain.exception.InvalidStateTransitionException;
import com.flowpay.routing.domain.exception.MaxCapacityExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, InvalidStateTransitionException.class})
    public ProblemDetail handleIllegalStateException(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MaxCapacityExceededException.class)
    public ProblemDetail handleMaxCapacityExceeded(MaxCapacityExceededException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AgentUnavailableException.class)
    public ProblemDetail handleAgentUnavailable(AgentUnavailableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        // Tratar mensagem amigável para exclusão com FK constraint
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Não é possível excluir este registro pois existem dependências vinculadas a ele.");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno: " + e.getMessage());
    }
}
