package kz.logisto.lgwarehouseservice.data.repository;

import kz.logisto.lgwarehouseservice.data.entity.ItemVariantMovement;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemVariantMovementRepository extends JpaRepository<ItemVariantMovement, UUID>,
    JpaSpecificationExecutor<ItemVariantMovement> {

}
