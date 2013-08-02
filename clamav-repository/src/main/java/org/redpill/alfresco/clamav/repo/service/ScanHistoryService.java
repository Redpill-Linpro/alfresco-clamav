package org.redpill.alfresco.clamav.repo.service;

public interface ScanHistoryService {

  public enum ScanType {
    SYSTEM, SINGLE
  }

  void system(String log);

  void single(String log);

  void generic(ScanType logType, String log);

}
