package org.redpill.alfresco.clamav.repo.service;

import nl.runnable.alfresco.annotations.RunAsSystem;
import nl.runnable.alfresco.annotations.Transactional;

public interface UpdateVirusDatabaseService {

  /**
   * Updates the virus database.
   */
  @Transactional
  @RunAsSystem
  void updateDatabase();

}
