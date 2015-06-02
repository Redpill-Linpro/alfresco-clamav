package org.redpill.alfresco.acav.repo.service.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.log4j.Logger;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.StatusService;
import org.redpill.alfresco.acav.repo.service.UpdateVirusDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("acav.updateVirusDatabaseService")
public class UpdateVirusDatabaseServiceImpl extends AbstractService implements UpdateVirusDatabaseService {

  private static final Logger LOG = Logger.getLogger(UpdateVirusDatabaseServiceImpl.class);

  @Autowired
  @Qualifier("acav.updateVirusDatabaseCommand")
  private RuntimeExec _updateCommand;

  @Autowired
  @Qualifier("acav.updateVirusDatabaseCheckCommand")
  private RuntimeExec _checkCommand;

  private boolean _active;

  @Autowired
  private StatusService _statusService;

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.UpdateVirusDatabaseService#
   * updateDatabase()
   */
  @Override
  public void updateDatabase() {
    if (!isEnabled()) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Update Virus Database not enabled");
      }
    }

    NodeRef lockNode = _acavNodeService.getUpdateLockNode();

    if (_lockService.getLockStatus(lockNode) != LockStatus.NO_LOCK) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("The Alfresco ClamAV system is currently locked...");
      }

      return;
    }

    _lockService.lock(lockNode, LockType.NODE_LOCK, 30);

    try {
      _statusService.writeInitialUpdateStatus();

      Map<String, String> properties = new HashMap<String, String>();

      File logFile = TempFileProvider.createTempFile("acav_update_virus_database_", ".log");

      properties.put(KEY_LOGFILE, logFile.getAbsolutePath());

      ExecutionResult result = _updateCommand.execute(properties);

      String logMessage = getLogMessage(logFile);

      if (result.getExitValue() != 0) {
        throw new AlfrescoRuntimeException(logMessage);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("\n\n" + logMessage + "\n\n");
      }

      writeLogMessage(logMessage);
    } finally {
      _lockService.unlock(lockNode);

      _statusService.writeFinalUpdateStatus();
    }
  }

  private boolean isEnabled() {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    Boolean enabled = (Boolean) _nodeService.getProperty(systemStatusNode, AcavModel.PROP_ENABLED);
    enabled = enabled != null ? enabled : true;

    return _active && enabled;
  }

  @PostConstruct
  public void postConstruct() {
    _active = _checkCommand.execute().getExitValue() == 0;
  }

}
