package org.redpill.alfresco.acav.repo.service;

import org.redpill.alfresco.acav.repo.utils.ScanSummary;

public interface ScanHistoryService {

  void record(ScanSummary scanSummary);

}
