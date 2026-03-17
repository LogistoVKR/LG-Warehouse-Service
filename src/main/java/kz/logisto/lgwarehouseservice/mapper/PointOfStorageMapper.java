package kz.logisto.lgwarehouseservice.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.CreatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.dto.pointofstorage.UpdatePointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import kz.logisto.lgwarehouseservice.data.model.PointOfStorageModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = SPRING)
public interface PointOfStorageMapper {

  PointOfStorageModel toModel(PointOfStorage pointOfStorage);

  PointOfStorage toEntity(CreatePointOfStorageDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
  void updateEntity(@MappingTarget PointOfStorage entity, UpdatePointOfStorageDto dto);
}
