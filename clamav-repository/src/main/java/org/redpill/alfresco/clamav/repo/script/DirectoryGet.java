package org.redpill.alfresco.clamav.repo.script;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.runnable.alfresco.webscripts.annotations.Authentication;
import nl.runnable.alfresco.webscripts.annotations.AuthenticationType;
import nl.runnable.alfresco.webscripts.annotations.HttpMethod;
import nl.runnable.alfresco.webscripts.annotations.Uri;
import nl.runnable.alfresco.webscripts.annotations.WebScript;

import org.redpill.alfresco.clamav.repo.service.SystemScanDirectoryRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component
@WebScript(description = "Fetches the directories from the Scan Registry", families = { "Alfresco ClamAV" })
@Authentication(AuthenticationType.ADMIN)
public class DirectoryGet {

  @Autowired
  private SystemScanDirectoryRegistry _systemScanDirectoryRegistry;

  @Uri(method = HttpMethod.GET, value = "/org/redpill/alfresco/clamav/directory", defaultFormat = "json")
  public Map<String, Object> handleTemplate(WebScriptResponse response) {
    Map<String, Object> model = new HashMap<String, Object>();

    List<String> directories = new ArrayList<String>();

    for (File directory : _systemScanDirectoryRegistry.getDirectories()) {
      directories.add(directory.getAbsolutePath());
    }

    model.put("directories", directories);

    return model;
  }

}
