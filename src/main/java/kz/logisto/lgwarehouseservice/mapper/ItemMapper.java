package kz.logisto.lgwarehouseservice.mapper;

import kz.logisto.lgwarehouseservice.data.dto.item.CreateItemDto;
import kz.logisto.lgwarehouseservice.data.dto.item.UpdateItemDto;
import kz.logisto.lgwarehouseservice.data.entity.Item;
import kz.logisto.lgwarehouseservice.data.model.ItemModel;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = SPRING)
public interface ItemMapper {

  ItemModel toModel(Item item);

  Item toEntity(CreateItemDto dto);

  @BeanMapping(nullValuePropertyMappingStrategy = IGNORE)
  void updateEntity(@MappingTarget Item item, UpdateItemDto dto);
}
