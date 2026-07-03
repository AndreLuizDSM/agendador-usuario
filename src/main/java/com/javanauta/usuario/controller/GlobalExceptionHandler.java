package com.javanauta.usuario.controller;


import com.javanauta.usuario.infrastructure.exceptions.ConflictException;
import com.javanauta.usuario.infrastructure.exceptions.IllegalArgumentException;
import com.javanauta.usuario.infrastructure.exceptions.ResourceNotFound;
import com.javanauta.usuario.infrastructure.exceptions.UnauthorizedException;
import com.javanauta.usuario.infrastructure.security.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFound ex,
                                                                   HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildError(ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                request.getRequestURI(),
                "Not Found"));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflictException(ConflictException ex,
                                                                    HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(buildError(ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                request.getRequestURI(),
                "Conflit"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnauthorizedException(UnauthorizedException ex,
                                                                        HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(buildError(ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI(),
                "Unauthorized"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgumentException(IllegalArgumentException ex,
                                                                           HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildError(ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                "Bad Request"));
    }

    private ErrorResponseDTO buildError (String message, int status, String path, String erro) {
        // Criar um erroDTO para passar detalhadamente o erro com o Token JWT
        return  ErrorResponseDTO.builder()
                .timeStamp(LocalDateTime.now())
                .status(status)
                .message(message)
                .path(path)
                .erro(erro)
                .build();
    }
}
