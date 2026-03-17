package kz.logisto.lgwarehouseservice.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import kz.logisto.lgwarehouseservice.data.entity.ItemVariantPointOfStorage;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantPointOfStorageModel;
import org.mapstruct.Mapper;

@Mapper(componentModel = SPRING)
public interface ItemVariantPointOfStorageMapper {

  ItemVariantPointOfStorageModel toModel(ItemVariantPointOfStorage itemVariantPointOfStorage);
}
