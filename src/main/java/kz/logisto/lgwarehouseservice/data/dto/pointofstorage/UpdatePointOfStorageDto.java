package kz.logisto.lgwarehouseservice.data.dto.pointofstorage;

import kz.logisto.lgwarehouseservice.data.enums.PointOfStorageType;
import jakarta.validation.constraints.Size;

public record UpdatePointOfStorageDto(
    @Size(max = 255) String name,
    @Size(max = 255) String description,
    @Size(max = 255) String location,
    PointOfStorageType type
) { }
