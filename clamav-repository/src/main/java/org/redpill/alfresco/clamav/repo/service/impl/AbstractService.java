package org.redpill.alfresco.clamav.repo.service.impl;

import java.io.File;
import java.io.IOException;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.io.FileUtils;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractService implements InitializingBean {

  protected static final String KEY_OPTIONS = "options";
  protected static final String KEY_LOGFILE = "logfile";
  protected static final String KEY_TEMPDIR = "tempdir";
  protected static final String KEY_FILE = "file_to_scan";

  protected LockService _lockService;

  protected NodeService _nodeService;

  protected AcavNodeService _acavNodeService;

  protected void writeLogMessage(String logMessage) {
    // TODO write the log message somewhere
  }

  public void setLockService(LockService lockService) {
    _lockService = lockService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setAcavNodeService(AcavNodeService acavNodeService) {
    _acavNodeService = acavNodeService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("lockService", _lockService);
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("acavNodeService", _acavNodeService);
  }

  protected String getLogMessage(File logFile) {
    try {
      return FileUtils.readFileToString(logFile);
    } catch (IOException ex) {
      throw new RuntimeException("Couldn't read log file " + logFile.getAbsolutePath());
    }
  }

}
