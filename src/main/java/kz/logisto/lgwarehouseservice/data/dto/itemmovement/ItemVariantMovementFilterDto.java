package kz.logisto.lgwarehouseservice.data.dto.itemmovement;

import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import java.time.LocalDateTime;
import java.util.UUID;

public record ItemVariantMovementFilterDto(
    UUID fromPointOfStorageId,
    UUID toPointOfStorageId,
    MovementType type,
    UUID itemVariantId,
    LocalDateTime from,
    LocalDateTime to
) { }
