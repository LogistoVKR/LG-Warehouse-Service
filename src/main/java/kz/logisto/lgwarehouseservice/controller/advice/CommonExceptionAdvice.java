package kz.logisto.lgwarehouseservice.controller.advice;

import kz.logisto.lgwarehouseservice.data.model.ExceptionModel;
import kz.logisto.lgwarehouseservice.exception.NotEqualValuesException;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.exception.UpdateItemVariantPointOfStorageException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionAdvice {

  @ExceptionHandler({ NotFoundException.class, EntityNotFoundException.class })
  public ResponseEntity<ExceptionModel> handleNotFound(Exception exception) {
    return handleException(HttpStatus.NOT_FOUND, exception);
  }

  @ExceptionHandler(NotEqualValuesException.class)
  public ResponseEntity<ExceptionModel> handleNotEqualValues(NotEqualValuesException exception) {
    return handleException(HttpStatus.BAD_REQUEST, exception);
  }

  @ExceptionHandler(UpdateItemVariantPointOfStorageException.class)
  public ResponseEntity<ExceptionModel> handleUpdateItemVariantPointOfStorage(
      UpdateItemVariantPointOfStorageException exception) {
    return handleException(HttpStatus.BAD_REQUEST, exception);
  }

  private ResponseEntity<ExceptionModel> handleException(HttpStatus status, Exception exception) {
    if (StringUtils.hasText(exception.getMessage())) {
      return ResponseEntity.status(status).body(new ExceptionModel(exception.getMessage()));
    }
    return ResponseEntity.status(status).build();
  }
}
