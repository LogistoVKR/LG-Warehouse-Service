package kz.logisto.lgwarehouseservice.data.repository;

import kz.logisto.lgwarehouseservice.data.entity.PointOfStorage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PointOfStorageRepository extends JpaRepository<PointOfStorage, UUID>,
    JpaSpecificationExecutor<PointOfStorage> {

  int countByOrganizationId(UUID organizationId);
}
