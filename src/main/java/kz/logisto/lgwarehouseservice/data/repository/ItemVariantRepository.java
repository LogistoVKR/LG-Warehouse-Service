package kz.logisto.lgwarehouseservice.data.repository;

import kz.logisto.lgwarehouseservice.data.entity.ItemVariant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemVariantRepository extends JpaRepository<ItemVariant, UUID> {

  Page<ItemVariant> findAllByItemId(UUID itemId, Pageable pageable);

  int countByItem_OrganizationId(UUID organizationId);
}
