package org.redpill.alfresco.clamav.repo.utils;

import java.io.File;

import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.service.SystemScanDirectoryRegistry;
import org.springframework.beans.factory.InitializingBean;

public class SystemScanDirectoryRegister implements InitializingBean {

  private SystemScanDirectoryRegistry _systemScanDirectoryRegistry;

  private File _systemScanDirectory;

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("systemScanDirectoryRegistry", _systemScanDirectoryRegistry);

    _systemScanDirectoryRegistry.addDirectory(_systemScanDirectory);
  }

  public void setSystemScanDirectoryRegistry(SystemScanDirectoryRegistry systemScanDirectoryRegistry) {
    _systemScanDirectoryRegistry = systemScanDirectoryRegistry;
  }

  public void setSystemScanDirectory(File systemScanDirectory) {
    _systemScanDirectory = systemScanDirectory;
  }

}
