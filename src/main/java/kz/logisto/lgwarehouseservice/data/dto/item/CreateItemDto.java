package kz.logisto.lgwarehouseservice.data.dto.item;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateItemDto(
    @Size(max = 255) @NotBlank String name,
    @Size(max = 255) String description,
    @NotNull UUID organizationId
) { }
