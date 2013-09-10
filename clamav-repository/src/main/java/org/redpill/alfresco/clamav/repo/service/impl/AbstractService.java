package org.redpill.alfresco.clamav.repo.service.impl;

import java.io.File;
import java.io.IOException;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.io.FileUtils;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractService {

  protected static final String KEY_OPTIONS = "options";
  protected static final String KEY_LOGFILE = "logfile";
  protected static final String KEY_TEMPDIR = "tempdir";
  protected static final String KEY_FILE = "file_to_scan";

  @Autowired
  protected LockService _lockService;

  @Autowired
  protected NodeService _nodeService;

  @Autowired
  protected AcavNodeService _acavNodeService;

  protected void writeLogMessage(String logMessage) {
    // TODO write the log message somewhere
  }

  protected String getLogMessage(File logFile) {
    try {
      return FileUtils.readFileToString(logFile);
    } catch (IOException ex) {
      throw new RuntimeException("Couldn't read log file " + logFile.getAbsolutePath());
    }
  }

}
