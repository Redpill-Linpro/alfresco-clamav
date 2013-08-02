package org.redpill.alfresco.clamav.repo.service.impl;

import java.util.Date;

import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.model.AcavModel;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.redpill.alfresco.clamav.repo.service.ScanHistoryService;
import org.springframework.beans.factory.InitializingBean;

public class ScanHistoryServiceImpl implements ScanHistoryService, InitializingBean {

  private AcavNodeService _acavNodeService;

  private NodeService _nodeService;

  private FileFolderService _fileFolderService;

  @Override
  public void system(String log) {
    generic(ScanType.SYSTEM, log);
  }

  @Override
  public void single(String log) {
    generic(ScanType.SINGLE, log);
  }

  @Override
  public void generic(ScanType scanType, String log) {
    ParameterCheck.mandatory("scanType", scanType);
    ParameterCheck.mandatoryString("log", log);

    NodeRef folderNodeRef = _acavNodeService.createFolderStructure(_acavNodeService.getScanHistoryFolderNodeRef());

    NodeRef logNodeRef = _fileFolderService.create(folderNodeRef, scanType.name() + "_scan_" + System.currentTimeMillis(), AcavModel.TYPE_SCAN_HISTORY).getNodeRef();

    _nodeService.setProperty(logNodeRef, AcavModel.PROP_SCAN_LOG_DATE, new Date());
    _nodeService.setProperty(logNodeRef, AcavModel.PROP_SCAN_LOG, log);

    switch (scanType) {
      case SINGLE: {
        _nodeService.setProperty(logNodeRef, AcavModel.PROP_SCAN_TYPE, ScanType.SINGLE.name());
      }
      case SYSTEM: {
        _nodeService.setProperty(logNodeRef, AcavModel.PROP_SCAN_TYPE, ScanType.SYSTEM.name());
      }
      default: {
      }
    }
  }

  public void setAcavNodeService(AcavNodeService acavNodeService) {
    _acavNodeService = acavNodeService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("acavNodeService", _acavNodeService);
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("fileFolderService", _fileFolderService);
  }

}
