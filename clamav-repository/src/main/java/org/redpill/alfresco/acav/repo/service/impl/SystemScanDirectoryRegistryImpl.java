package org.redpill.alfresco.acav.repo.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.redpill.alfresco.acav.repo.service.SystemScanDirectoryRegistry;
import org.springframework.stereotype.Component;

@Component(value = "acav.systemScanDirectoryRegistry")
public class SystemScanDirectoryRegistryImpl implements SystemScanDirectoryRegistry {

  private List<File> _directories = new ArrayList<File>();

  @Override
  public void addDirectory(File directory) {
    if (directory == null) {
      return;
    }

    if (!directory.exists()) {
      throw new RuntimeException("The directory '" + directory.getAbsolutePath() + "' does not exist!");
    }

    _directories.add(directory);
  }

  @Override
  public List<File> getDirectories() {
    return _directories;
  }

}
