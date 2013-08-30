package org.redpill.alfresco.clamav.repo.service;

import nl.runnable.alfresco.annotations.RunAsSystem;
import nl.runnable.alfresco.annotations.Transactional;

public interface StatusService {

  @RunAsSystem
  @Transactional(requiresNew = true)
  void writeInitialScanStatus();

  @RunAsSystem
  @Transactional(requiresNew = true)
  void writeInitialUpdateStatus();

  @RunAsSystem
  @Transactional(requiresNew = true)
  void writeFinalScanStatus();

  @RunAsSystem
  @Transactional(requiresNew = true)
  void writeFinalUpdateStatus();

}
