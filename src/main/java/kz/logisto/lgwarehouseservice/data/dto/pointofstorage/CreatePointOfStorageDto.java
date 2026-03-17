package kz.logisto.lgwarehouseservice.data.dto.pointofstorage;

import kz.logisto.lgwarehouseservice.data.enums.PointOfStorageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreatePointOfStorageDto(
    @Size(max = 255) @NotBlank String name,
    @Size(max = 255) String description,
    @Size(max = 255) String location,
    @NotNull UUID organizationId,
    @NotNull PointOfStorageType type
) { }
