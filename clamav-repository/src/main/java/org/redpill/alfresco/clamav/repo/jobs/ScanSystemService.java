package org.redpill.alfresco.clamav.repo.jobs;

import java.util.List;

import javax.annotation.Resource;

import org.redpill.alfresco.clamav.repo.service.ScanAction;
import org.redpill.alfresco.clamav.repo.service.ScanService;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.annotations.RunAsSystem;
import com.github.dynamicextensionsalfresco.annotations.Transactional;

@Component("acav.scanSystemService")
public class ScanSystemService extends ClusteredExecuter {

  @Resource(name = "acav.daemonScanService")
  private ScanService _scanService;

  @Autowired
  private ScanAction _scanAction;

  @Override
  protected String getJobName() {
    return "ScanSystemService";
  }

  @Override
  @Transactional
  @RunAsSystem
  protected void executeInternal() {
    final List<ScanSummary> scanSummaryList = _scanService.scanSystem();

    for (ScanSummary scanSummary : scanSummaryList) {
      for (ScanResult scanResult : scanSummary.getInfectedList()) {
        _scanAction.handleNode(scanResult.getNodeRef(), scanResult);
      }
    }
  }

}
