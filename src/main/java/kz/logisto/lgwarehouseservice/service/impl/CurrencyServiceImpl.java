package kz.logisto.lgwarehouseservice.service.impl;

import kz.logisto.lgwarehouseservice.service.CurrencyService;
import java.util.Currency;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CurrencyServiceImpl implements CurrencyService {

  private static final List<Currency> CURRENCIES = List.of(
      Currency.getInstance("USD"),
      Currency.getInstance("EUR"),
      Currency.getInstance("GBP"),
      Currency.getInstance("KZT"),
      Currency.getInstance("RUB")
                                                          );

  @Override
  public List<Currency> findAll() {
    return CURRENCIES;
  }
}
