package org.redpill.alfresco.clamav.repo.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import nl.runnable.alfresco.webscripts.annotations.Authentication;
import nl.runnable.alfresco.webscripts.annotations.AuthenticationType;
import nl.runnable.alfresco.webscripts.annotations.HttpMethod;
import nl.runnable.alfresco.webscripts.annotations.RequestParam;
import nl.runnable.alfresco.webscripts.annotations.Uri;
import nl.runnable.alfresco.webscripts.annotations.WebScript;

import org.apache.commons.lang.StringUtils;
import org.redpill.alfresco.clamav.repo.service.ScanService;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component
@WebScript(description = "Scans a directory for viruses", families = { "Alfresco ClamAV" })
@Authentication(AuthenticationType.ADMIN)
public class ScanGet {

  @Resource(name = "acav.daemonScanService")
  private ScanService _scanService;

  @Uri(method = HttpMethod.GET, value = "/org/redpill/alfresco/clamav/scan", defaultFormat = "json")
  public Map<String, Object> scan(@RequestParam String directory, WebScriptResponse response) {
    List<ScanSummary> scanSummaryList = new ArrayList<ScanSummary>();

    if (StringUtils.isNotBlank(directory)) {
      ScanSummary scanSummary = _scanService.scanSystem(new File(directory));

      if (scanSummary != null) {
        scanSummaryList.add(scanSummary);
      }
    } else {
      scanSummaryList.addAll(_scanService.scanSystem());
    }

    Map<String, Object> result = new HashMap<String, Object>();

    result.put("scanSummaryList", scanSummaryList);

    return result;
  }

  public void setScanService(ScanService scanService) {
    _scanService = scanService;
  }

}
