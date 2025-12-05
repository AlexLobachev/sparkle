package com.example.sparkle.sparkle.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ErrorHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errors);
    }

    /**
     *
     * Обработка Conflict (409 Conflict)
     */

    @ExceptionHandler(Conflict.class)
    public ResponseEntity<Map<String, String>> handleUserConflict(Conflict e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getErrorMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }



    /**
     *
     * Обработка NotFound (404 Not Found)
     */

    @ExceptionHandler(NotFound.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(NotFound e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getErrorMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }
    /**
     *
     * Обработка BadRequest (400 Bad Request)
     */

    @ExceptionHandler(BadRequest.class)
    public ResponseEntity<Map<String, String>> handleUserBadRequest(BadRequest e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getErrorMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
    /**
     *
     * Обработка NoContent (204 No Content)
     */

    @ExceptionHandler(NoContent.class)
    public ResponseEntity<Map<String, String>> handleUserNoContent(NoContent e) {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT).build();

    }
    /**
     *
     * Обработка Forbidden (403 No Content)
     */

    @ExceptionHandler(Forbidden.class)
    public ResponseEntity<Map<String, String>> handleUserForbidden(Forbidden e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getErrorMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN).body(error);

    }
    /**
     *
     * Общая обработка любых других исключений (500 Internal Server Error)
     */

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e){
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllExceptions(Exception e, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/ws")) {
            // Логируем, но не возвращаем ответ — пусть SockJS сам закроется
            System.out.println("WebSocket error (not handled): " + e.getMessage());
            return null; // Spring не отправит ответ
        }

        Map<String, String> error = new HashMap<>();
        error.put("error", "Произошла внутренняя ошибка");
        e.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }




}

