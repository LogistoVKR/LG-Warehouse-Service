package kz.logisto.lgwarehouseservice.data.dto.item;

import jakarta.validation.constraints.Size;

public record UpdateItemDto(
    @Size(max = 255) String name,
    @Size(max = 255) String description
) { }
