package kz.logisto.lgwarehouseservice.data.dto.itemvariantpointofstorage;

import kz.logisto.lgwarehouseservice.data.enums.MovementType;
import java.util.UUID;

public record ItemVariantPointOfStorageDto(MovementType type, UUID fromPointOfStorageId,
                                           UUID toPointOfStorageId, UUID itemVariantId,
                                           int quantity) { }
