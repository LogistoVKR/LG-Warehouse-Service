package kz.logisto.lgwarehouseservice.scheduler;

import kz.logisto.lgwarehouseservice.service.OzonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OzonSyncScheduler {

  private final OzonService ozonService;

  @Scheduled(fixedRate = 60000)
  public void syncAll() {
    log.info("Started item sync");
    ozonService.syncAllProducts();
    log.info("Started warehouse sync");
    ozonService.syncAllWarehouses();
  }
}
