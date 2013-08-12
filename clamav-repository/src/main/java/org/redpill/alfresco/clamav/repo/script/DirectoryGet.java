package org.redpill.alfresco.clamav.repo.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.service.SystemScanDirectoryRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DirectoryGet extends DeclarativeWebScript implements InitializingBean {

  private SystemScanDirectoryRegistry _systemScanDirectoryRegistry;

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    Map<String, Object> result = new HashMap<String, Object>();

    List<String> directories = new ArrayList<String>();

    for (File directory : _systemScanDirectoryRegistry.getDirectories()) {
      directories.add(directory.getAbsolutePath());
    }

    result.put("directories", directories);

    return result;
  }

  public void setSystemScanDirectoryRegistry(SystemScanDirectoryRegistry systemScanDirectoryRegistry) {
    _systemScanDirectoryRegistry = systemScanDirectoryRegistry;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("systemScanDirectoryRegistry", _systemScanDirectoryRegistry);
  }

}
