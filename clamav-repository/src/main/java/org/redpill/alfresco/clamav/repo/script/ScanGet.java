package org.redpill.alfresco.clamav.repo.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.redpill.alfresco.clamav.repo.service.ScanService;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class ScanGet extends DeclarativeWebScript implements InitializingBean {

  private ScanService _scanService;

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    String directory = req.getParameter("directory");

    List<ScanSummary> scanSummaryList = new ArrayList<ScanSummary>();

    if (StringUtils.isNotBlank(directory)) {
      scanSummaryList.add(_scanService.scanSystem(new File(directory)));
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

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("scanService", _scanService);
  }

}
