package kz.logisto.lgwarehouseservice.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import kz.logisto.lgwarehouseservice.data.dto.itemmovement.CreateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.dto.itemmovement.UpdateItemVariantMovementDto;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariantMovement;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantMovementModel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = SPRING)
public interface ItemVariantMovementMapper {

  ItemVariantMovement toEntity(CreateItemVariantMovementDto dto);

  void updateEntity(@MappingTarget ItemVariantMovement itemVariantMovement,
      UpdateItemVariantMovementDto dto);

  ItemVariantMovementModel toModel(ItemVariantMovement itemVariantMovement);
}
