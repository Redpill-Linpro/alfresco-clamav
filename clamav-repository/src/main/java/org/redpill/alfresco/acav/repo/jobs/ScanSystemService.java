package org.redpill.alfresco.acav.repo.jobs;

import java.util.List;

import org.redpill.alfresco.acav.repo.service.ScanAction;
import org.redpill.alfresco.acav.repo.service.ScanService;
import org.redpill.alfresco.acav.repo.utils.ScanResult;
import org.redpill.alfresco.acav.repo.utils.ScanSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("acav.scanSystemService")
public class ScanSystemService extends ClusteredExecuter {

  @Autowired
  @Qualifier("acav.daemonScanService")
  private ScanService _scanService;

  @Autowired
  private ScanAction _scanAction;

  @Override
  protected String getJobName() {
    return "ScanSystemService";
  }

  @Override
  protected void executeInternal() {
    final List<ScanSummary> scanSummaryList = _scanService.scanSystem();

    for (ScanSummary scanSummary : scanSummaryList) {
      for (ScanResult scanResult : scanSummary.getInfectedList()) {
        _scanAction.handleNode(scanResult.getNodeRef(), scanResult);
      }
    }
  }

}
