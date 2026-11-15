package com.amani.energytrading.web;

import com.amani.energytrading.trade.TradeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Returns RFC 7807 "problem+json" bodies for every error: validation errors and malformed
 * requests are handled by the parent class, business errors below.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TradeNotFoundException.class)
    public ProblemDetail handleTradeNotFound(TradeNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Trade not found");
        return problem;
    }
}
