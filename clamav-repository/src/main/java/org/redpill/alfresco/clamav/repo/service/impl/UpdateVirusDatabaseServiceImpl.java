package org.redpill.alfresco.clamav.repo.service.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.redpill.alfresco.clamav.repo.service.UpdateVirusDatabaseService;
import org.springframework.util.Assert;

public class UpdateVirusDatabaseServiceImpl extends AbstractService implements UpdateVirusDatabaseService {

  private static final Logger LOG = Logger.getLogger(UpdateVirusDatabaseServiceImpl.class);

  private RuntimeExec _updateVirusDatabaseCommand;

  private RuntimeExec _updateVirusDatabaseCheckCommand;

  private String _freshClamConfigFile;

  private boolean _enabled = true;

  private String _datadir;

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.UpdateVirusDatabaseService#updateDatabase()
   */
  @Override
  public void updateDatabase() {
    if (!_enabled) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Update Virus Database not enabled");
      }
    }

    NodeRef rootNode = _acavNodeService.getRootNode();

    if (_lockService.getLockStatus(rootNode) != LockStatus.NO_LOCK) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("The Alfresco ClamAV system is currently locked...");
      }

      return;
    }

    _lockService.lock(rootNode, LockType.NODE_LOCK, 30);

    try {

      Map<String, String> properties = new HashMap<String, String>();

      File logFile = TempFileProvider.createTempFile("acav_update_virus_database_", ".log");

      properties.put(KEY_LOGFILE, logFile.getAbsolutePath());

      String options = "";

      if (StringUtils.isNotBlank(_freshClamConfigFile)) {
        options += " --config-file=" + _freshClamConfigFile;
      }

      properties.put(KEY_OPTIONS, options);

      ExecutionResult result = _updateVirusDatabaseCommand.execute(properties);

      String logMessage = getLogMessage(logFile);

      if (result.getExitValue() != 0) {
        throw new AlfrescoRuntimeException(logMessage);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("\n\n" + logMessage + "\n\n");
      }

      writeLogMessage(logMessage);
    } finally {
      _lockService.unlock(rootNode);
    }
  }

  public void setUpdateVirusDatabaseCommand(RuntimeExec updateVirusDatabaseCommand) {
    _updateVirusDatabaseCommand = updateVirusDatabaseCommand;
  }

  public void setFreshClamConfigFile(String freshClamConfigFile) {
    _freshClamConfigFile = freshClamConfigFile;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  public void setUpdateVirusDatabaseCheckCommand(RuntimeExec updateVirusDatabaseCheckCommand) {
    _updateVirusDatabaseCheckCommand = updateVirusDatabaseCheckCommand;
  }

  public void setDatadir(String datadir) {
    _datadir = datadir;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.impl.AbstractService#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    Assert.notNull(_updateVirusDatabaseCommand);
    Assert.notNull(_updateVirusDatabaseCheckCommand);
    Assert.hasText(_datadir);

    _enabled = _updateVirusDatabaseCheckCommand.execute().getExitValue() == 0;

    // create the directory
    File datadir = new File(_datadir);

    if (!datadir.exists()) {
      FileUtils.forceMkdir(new File(_datadir));
    }
  }

}
