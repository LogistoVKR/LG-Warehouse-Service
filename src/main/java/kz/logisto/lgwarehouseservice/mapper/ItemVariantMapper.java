package kz.logisto.lgwarehouseservice.mapper;

import kz.logisto.lgwarehouseservice.data.dto.itemvariant.CreateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.dto.itemvariant.UpdateItemVariantDto;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = SPRING)
public interface ItemVariantMapper {

  ItemVariantModel toModel(ItemVariant itemVariant);

  ItemVariant toEntity(CreateItemVariantDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
  void updateEntity(@MappingTarget ItemVariant itemVariant, UpdateItemVariantDto dto);
}
