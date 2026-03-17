package kz.logisto.lgwarehouseservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kz.logisto.lgwarehouseservice.service.CurrencyService;
import java.util.Currency;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/currencies")
@Tag(name = "Currencies controller")
public class CurrencyController {

  private final CurrencyService currencyService;

  @GetMapping
  public ResponseEntity<List<Currency>> getAll() {
    return ResponseEntity.ok(currencyService.findAll());
  }
}
