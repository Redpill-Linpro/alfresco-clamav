package org.redpill.alfresco.clamav.repo.service;

import java.io.File;
import java.util.List;

public interface SystemScanDirectoryRegistry {

  /**
   * Adds a directory to the system directory registry. Checks whether the directory exists or not.
   * 
   * @param directory
   *          to add
   */
  void addDirectory(File directory);

  /**
   * Gets a list of directories which is in the registry.
   * 
   * @return a list of directories.
   */
  List<File> getDirectories();

}
