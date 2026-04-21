package kz.logisto.lgwarehouseservice.data.repository;

import kz.logisto.lgwarehouseservice.data.entity.ItemVariantPointOfStorage;
import kz.logisto.lgwarehouseservice.data.entity.key.ItemVariantPointOfStorageId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemVariantPointOfStorageRepository extends
    JpaRepository<ItemVariantPointOfStorage, ItemVariantPointOfStorageId> {

  @Query("select ivpos from ItemVariantPointOfStorage ivpos where ivpos.id.itemVariantId = :itemVariantId")
  List<ItemVariantPointOfStorage> findByItemVariantId(UUID itemVariantId);

  @Query("select ivpos from ItemVariantPointOfStorage ivpos where ivpos.id.itemVariantId in :itemVariantIds")
  List<ItemVariantPointOfStorage> findByIdItemVariantIdIn(
      @Param("itemVariantIds") Set<UUID> itemVariantIds);

  @Modifying
  @Query(value = """
      insert into mc_warehouse_service.item_variant_point_of_storage (item_variant_id, point_of_storage_id, quantity, reserved)
      values (:itemVariantId, :pointOfStorageId, :quantity, 0)
      on conflict (item_variant_id, point_of_storage_id)
      do update set quantity = mc_warehouse_service.item_variant_point_of_storage.quantity + :quantity
      """, nativeQuery = true)
  int increment(UUID itemVariantId, UUID pointOfStorageId, int quantity);

  @Modifying
  @Query(value = """
      insert into mc_warehouse_service.item_variant_point_of_storage(item_variant_id, point_of_storage_id, quantity, reserved) 
      values  (:itemVariantId, :pointOfStorageId, -:quantity, 0)
      on conflict (item_variant_id, point_of_storage_id)
      do update set quantity = mc_warehouse_service.item_variant_point_of_storage.quantity - :quantity
      """, nativeQuery = true)
  int decrement(UUID itemVariantId, UUID pointOfStorageId, int quantity);


  @Modifying
  @Query(value = """
      insert into mc_warehouse_service.item_variant_point_of_storage(item_variant_id, point_of_storage_id, quantity, reserved)
      values (:itemVariantId, :pointOfStorageId, -:quantity, :quantity)
      on conflict (item_variant_id, point_of_storage_id)
      do update set quantity = mc_warehouse_service.item_variant_point_of_storage.quantity - :quantity, reserved = mc_warehouse_service.item_variant_point_of_storage.reserved + :quantity
      """, nativeQuery = true)
  int reserve(UUID itemVariantId, UUID pointOfStorageId, int quantity);

  @Modifying
  @Query(value = """
      insert into mc_warehouse_service.item_variant_point_of_storage(item_variant_id, point_of_storage_id, quantity, reserved)
      values (:itemVariantId, :pointOfStorageId, :quantity, -:quantity)
      on conflict (item_variant_id, point_of_storage_id)
      do update set quantity = mc_warehouse_service.item_variant_point_of_storage.quantity + :quantity, reserved = mc_warehouse_service.item_variant_point_of_storage.reserved - :quantity
      """, nativeQuery = true)
  int release(UUID itemVariantId, UUID pointOfStorageId, int quantity);
}
