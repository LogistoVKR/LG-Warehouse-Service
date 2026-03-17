package kz.logisto.lgwarehouseservice.data.dto.itemvariant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Currency;

public record UpdateItemVariantDto(
    @Size(max = 255) String sku,
    @Size(max = 255) String barcode,
    @Digits(integer = 8, fraction = 2) @DecimalMin(value = "0.00") BigDecimal price,
    Currency currency
) { }
