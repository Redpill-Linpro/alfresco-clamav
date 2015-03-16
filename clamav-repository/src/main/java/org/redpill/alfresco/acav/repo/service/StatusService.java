package org.redpill.alfresco.acav.repo.service;

public interface StatusService {

  void writeInitialScanStatus();

  void writeInitialUpdateStatus();

  void writeFinalScanStatus();

  void writeFinalUpdateStatus();

}
