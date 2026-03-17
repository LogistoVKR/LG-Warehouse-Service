package kz.logisto.lgwarehouseservice.data.dto.itemvariant;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record CreateItemVariantDto(
    @Size(max = 255) @NotBlank String sku,
    @Size(max = 255) String barcode,
    @NotNull UUID itemId,
    @Digits(integer = 8, fraction = 2) @DecimalMin(value = "0.00") @NotNull BigDecimal price,
    @NotNull Currency currency
) { }
