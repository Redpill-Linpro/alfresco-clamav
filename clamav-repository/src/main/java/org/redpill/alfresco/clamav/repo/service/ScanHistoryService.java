package org.redpill.alfresco.clamav.repo.service;

import org.redpill.alfresco.clamav.repo.utils.ScanSummary;

public interface ScanHistoryService {

  void record(ScanSummary scanSummary);

}
