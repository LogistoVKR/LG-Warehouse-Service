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

  @Scheduled(fixedRate = 300_000)
  public void syncAll() {
    log.info("Started item sync");
    ozonService.syncAllProducts();
    log.info("Started warehouse sync");
    ozonService.syncAllWarehouses();
  }

  @Scheduled(fixedRate = 300_000)
  public void pushStocks() {
    log.info("Started stock push to Ozon");
    ozonService.syncAllStocksToOzon();
  }

  @Scheduled(fixedRate = 600_000)
  public void pullPostings() {
    log.info("Started posting pull from Ozon");
    ozonService.pullAllPostings();
  }

  @Scheduled(fixedRate = 1_800_000)
  public void pullReturns() {
    log.info("Started returns pull from Ozon");
    ozonService.pullAllReturns();
  }

  @Scheduled(fixedRate = 3_600_000)
  public void reconcileStocks() {
    log.info("Started stock reconciliation with Ozon");
    ozonService.reconcileAllStocks();
  }
}
