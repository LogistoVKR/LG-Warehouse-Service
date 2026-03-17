package kz.logisto.lgwarehouseservice.data.dto.pointofstorage;

import kz.logisto.lgwarehouseservice.data.enums.PointOfStorageType;

public record PointOfStorageFilterDto(String name, String location, PointOfStorageType type,
                                      Boolean or) { }
