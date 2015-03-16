package org.redpill.alfresco.acav.repo.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.acav.repo.service.SystemScanDirectoryRegistry;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DirectoryGet extends DeclarativeWebScript {

  private SystemScanDirectoryRegistry _systemScanDirectoryRegistry;

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    Map<String, Object> model = new HashMap<String, Object>();

    List<String> directories = new ArrayList<String>();

    for (File directory : _systemScanDirectoryRegistry.getDirectories()) {
      directories.add(directory.getAbsolutePath());
    }

    model.put("directories", directories);

    return model;
  }

  // must use a setter based injection here. Annotation based injection will not
  // pick up the dependency...
  public void setSystemScanDirectoryRegistry(SystemScanDirectoryRegistry systemScanDirectoryRegistry) {
    _systemScanDirectoryRegistry = systemScanDirectoryRegistry;
  }

  @PostConstruct
  public void postConstruct() {
    ParameterCheck.mandatory("systemScanDirectoryRegistry", _systemScanDirectoryRegistry);
  }

}
