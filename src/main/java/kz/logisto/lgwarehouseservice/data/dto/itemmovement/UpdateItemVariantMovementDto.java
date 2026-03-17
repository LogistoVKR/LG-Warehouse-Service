package kz.logisto.lgwarehouseservice.data.dto.itemmovement;

import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.UUID;

public record UpdateItemVariantMovementDto(
    UUID fromPointOfStorageId,
    UUID toPointOfStorageId,
    UUID itemVariantId,
    @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal pricePerItem,
    Currency currency,
    Integer quantity,
    @Size(max = 255) String reason,
    MovementType type,
    LocalDateTime created
) { }
