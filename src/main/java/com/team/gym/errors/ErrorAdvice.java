package com.team.gym.errors;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ErrorAdvice {
  public record Err(String error) {}

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Err> badReq(IllegalArgumentException e){
    String code = e.getMessage();
    HttpStatus status = switch (code){
      case "invalid_credentials" -> HttpStatus.UNAUTHORIZED;
      case "email_taken", "ssn_taken", "not_found" -> HttpStatus.BAD_REQUEST;
      default -> HttpStatus.BAD_REQUEST;
    };
    return ResponseEntity.status(status).body(new Err(code));
  }

  @ExceptionHandler(Unauthorized.class)
  public ResponseEntity<Err> unauth(Unauthorized e){
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new Err("unauthorized"));
  }
}
