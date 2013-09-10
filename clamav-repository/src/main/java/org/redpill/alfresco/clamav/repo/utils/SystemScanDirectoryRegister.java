package org.redpill.alfresco.clamav.repo.utils;

import java.io.File;

import org.redpill.alfresco.clamav.repo.service.SystemScanDirectoryRegistry;
import org.springframework.beans.factory.InitializingBean;

public class SystemScanDirectoryRegister implements InitializingBean {

  private SystemScanDirectoryRegistry _systemScanDirectoryRegistry;

  private File _systemScanDirectory;

  @Override
  public void afterPropertiesSet() throws Exception {
    _systemScanDirectoryRegistry.addDirectory(_systemScanDirectory);
  }

  public void setSystemScanDirectory(File systemScanDirectory) {
    _systemScanDirectory = systemScanDirectory;
  }

  public void setSystemScanDirectoryRegistry(SystemScanDirectoryRegistry systemScanDirectoryRegistry) {
    _systemScanDirectoryRegistry = systemScanDirectoryRegistry;
  }

}
