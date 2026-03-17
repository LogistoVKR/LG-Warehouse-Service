package kz.logisto.lgwarehouseservice.service.impl;

import kz.logisto.lgwarehouseservice.data.dto.itemvariantpointofstorage.ItemVariantPointOfStorageDto;
import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import kz.logisto.lgwarehouseservice.data.model.ItemVariantPointOfStorageModel;
import kz.logisto.lgwarehouseservice.data.repository.ItemVariantPointOfStorageRepository;
import kz.logisto.lgwarehouseservice.exception.NotFoundException;
import kz.logisto.lgwarehouseservice.exception.UpdateItemVariantPointOfStorageException;
import kz.logisto.lgwarehouseservice.mapper.ItemVariantPointOfStorageMapper;
import kz.logisto.lgwarehouseservice.service.AccessService;
import kz.logisto.lgwarehouseservice.service.ItemVariantPointOfStorageService;
import kz.logisto.lgwarehouseservice.service.ItemVariantService;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemVariantPointOfStorageServiceImpl implements ItemVariantPointOfStorageService {

  private final AccessService accessService;
  private final ItemVariantService itemVariantService;
  private final ItemVariantPointOfStorageMapper mapper;
  private final ItemVariantPointOfStorageRepository repository;

  @Override
  public List<ItemVariantPointOfStorageModel> getCountsByItemVariantId(UUID id,
      Principal principal) {
    ItemVariant itemVariant = itemVariantService.getOrThrow(id);
    accessService.isMemberOrThrow(principal.getName(), itemVariant.getOrganizationId());
    return repository.findByItemVariantId(id)
        .stream()
        .map(mapper::toModel)
        .toList();
  }

  @Override
  public void update(ItemVariantPointOfStorageDto dto) {
    switch (dto.type()) {
      case SALE -> sale(dto);
      case RETURN -> returnTo(dto);
      case RESERVE -> reserve(dto);
      case PURCHASE -> purchase(dto);
      case TRANSFER -> transfer(dto);
      case WRITE_OFF -> writeOff(dto);
      default -> throw new NotFoundException();
    }
  }

  @Override
  public void undoUpdate(ItemVariantPointOfStorageDto dto) {
    switch (dto.type()) {
      case SALE -> undoSale(dto);
      case RETURN -> undoReturnTo(dto);
      case RESERVE -> undoReserve(dto);
      case PURCHASE -> undoPurchase(dto);
      case TRANSFER -> undoTransfer(dto);
      case WRITE_OFF -> undoWriteOff(dto);
      default -> throw new NotFoundException();
    }
  }

  private void sale(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.fromPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    decrement(dto.itemVariantId(), dto.fromPointOfStorageId(), dto.quantity());
  }

  private void undoSale(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.fromPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    increment(dto.itemVariantId(), dto.fromPointOfStorageId(), dto.quantity());
  }

  private void returnTo(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.toPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    increment(dto.itemVariantId(), dto.toPointOfStorageId(), dto.quantity());
  }

  private void undoReturnTo(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.toPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    decrement(dto.itemVariantId(), dto.toPointOfStorageId(), dto.quantity());
  }

  private void reserve(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.fromPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    reserve(dto.itemVariantId(), dto.fromPointOfStorageId(), dto.quantity());
  }

  private void undoReserve(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.fromPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    release(dto.itemVariantId(), dto.fromPointOfStorageId(), dto.quantity());
  }

  private void purchase(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.toPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    increment(dto.itemVariantId(), dto.toPointOfStorageId(), dto.quantity());
  }

  private void undoPurchase(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.toPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    decrement(dto.itemVariantId(), dto.toPointOfStorageId(), dto.quantity());
  }

  private void transfer(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.fromPointOfStorageId() == null
        || dto.toPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    decrement(dto.itemVariantId(), dto.fromPointOfStorageId(), dto.quantity());
    increment(dto.itemVariantId(), dto.toPointOfStorageId(), dto.quantity());
  }

  private void undoTransfer(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.fromPointOfStorageId() == null
        || dto.toPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    decrement(dto.itemVariantId(), dto.toPointOfStorageId(), dto.quantity());
    increment(dto.itemVariantId(), dto.fromPointOfStorageId(), dto.quantity());
  }

  private void writeOff(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.fromPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    decrement(dto.itemVariantId(), dto.fromPointOfStorageId(), dto.quantity());
  }

  private void undoWriteOff(ItemVariantPointOfStorageDto dto) {
    if (dto.itemVariantId() == null || dto.fromPointOfStorageId() == null || dto.quantity() <= 0) {
      throw new UpdateItemVariantPointOfStorageException();
    }
    increment(dto.itemVariantId(), dto.fromPointOfStorageId(), dto.quantity());
  }

  private void increment(UUID itemVariantId, UUID pointOfStorageId, int quantity) {
    repository.increment(itemVariantId, pointOfStorageId, quantity);
  }

  private void decrement(UUID itemVariantId, UUID pointOfStorageId, int quantity) {
    repository.decrement(itemVariantId, pointOfStorageId, quantity);
  }

  private void reserve(UUID itemVariantId, UUID pointOfStorageId, int quantity) {
    repository.reserve(itemVariantId, pointOfStorageId, quantity);
  }

  private void release(UUID itemVariantId, UUID pointOfStorageId, int quantity) {
    repository.release(itemVariantId, pointOfStorageId, quantity);
  }
}
