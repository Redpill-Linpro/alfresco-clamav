package org.redpill.alfresco.clamav.repo.service;

import com.github.dynamicextensionsalfresco.annotations.RunAsSystem;
import com.github.dynamicextensionsalfresco.annotations.Transactional;

public interface UpdateVirusDatabaseService {

  /**
   * Updates the virus database.
   */
  @Transactional
  @RunAsSystem
  void updateDatabase();

}
