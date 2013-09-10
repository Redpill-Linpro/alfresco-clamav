package org.redpill.alfresco.clamav.repo.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.redpill.alfresco.clamav.repo.service.ScanHistoryService;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary.ScanType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.philvarner.clamavj.ClamScan;
import com.philvarner.clamavj.ScanResult.Status;

@Component("acav.daemonScanService")
public class DaemonScanServiceImpl extends AbstractScanService {

  private static final Logger LOG = Logger.getLogger(DaemonScanServiceImpl.class);

  @Autowired
  private ScanHistoryService _scanHistoryService;

  @Autowired
  private SearchService _searchService;

  @Autowired
  private SiteService _siteService;

  @Autowired
  private ClamScan _clamScan;

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanSystem()
   */
  @Override
  public List<ScanSummary> scanSystem() {
    List<SiteInfo> sites = _siteService.listSites(null, null);

    for (SiteInfo site : sites) {
      scanSite(site);
    }

    return null;
  }

  @Override
  public List<ScanResult> scanSite(SiteInfo site) {

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanSystem(java.io.File)
   */
  @Override
  public ScanSummary scanSystem(File directory) {
    return null;
  }

  @Override
  public ScanSummary scanFile(File file, boolean writeStatus) {
    if (!isEnabled()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scan Service not enabled, skipping...");
      }

      return null;
    }

    ParameterCheck.mandatory("file", file);

    if (!file.exists()) {
      throw new AlfrescoRuntimeException("File '" + file.getAbsolutePath() + "' does not exist");
    }

    if (file.isDirectory()) {
      return null;
    }

    if (!lock()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("The Alfresco ClamAV system is currently locked...");
      }

      return null;
    }

    try {
      if (writeStatus) {
        _statusService.writeInitialScanStatus();
      }

      com.philvarner.clamavj.ScanResult result = _clamScan.scan(file);

      if (result.getStatus() == Status.ERROR) {
        throw new AlfrescoRuntimeException(result.getResult());
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("\n\n" + result.getResult() + "\n\n");
      }

      writeLogMessage(result.getResult());

      ScanSummary scanSummary = new ScanSummary();

      scanSummary.setScanType(ScanType.SINGLE);

      ScanResult scanResult = new ScanResult();
      scanResult.setFound(StringUtils.isNotBlank(result.getSignature()));
      scanResult.setDate(new Date());

      if (scanResult.isFound()) {
        scanResult.setVirusName(result.getSignature());
      }

      scanSummary.addScanned(scanResult);

      scanSummary.setScannedObject(file);

      _scanHistoryService.record(scanSummary);

      return scanSummary;
    } finally {
      unlock();

      if (writeStatus) {
        _statusService.writeFinalScanStatus();
      }
    }
  }

  @Override
  public boolean isActive() {
    return _clamScan.ping();
  }

}
