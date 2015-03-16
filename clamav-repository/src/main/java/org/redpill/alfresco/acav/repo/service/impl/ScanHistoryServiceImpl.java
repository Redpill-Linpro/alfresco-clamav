package org.redpill.alfresco.acav.repo.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.AcavNodeService;
import org.redpill.alfresco.acav.repo.service.ScanHistoryService;
import org.redpill.alfresco.acav.repo.utils.ScanSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScanHistoryServiceImpl implements ScanHistoryService {

  @Autowired
  private AcavNodeService _acavNodeService;

  @Resource(name = "NodeService")
  private NodeService _nodeService;

  @Resource(name = "FileFolderService")
  private FileFolderService _fileFolderService;

  @Override
  public void record(final ScanSummary scanSummary) {
    ParameterCheck.mandatory("scanType", scanSummary);

    final String scanType = scanSummary.getScanType().name();

    String currentUsername = AuthenticationUtil.getFullyAuthenticatedUser();

    AuthenticationUtil.setFullyAuthenticatedUser(AuthenticationUtil.SYSTEM_USER_NAME);

    try {
      NodeRef folderNodeRef = _acavNodeService.createFolderStructure(_acavNodeService.getScanHistoryFolderNodeRef());

      NodeRef logNodeRef = _fileFolderService.create(folderNodeRef, scanType + "_scan_" + System.currentTimeMillis(), AcavModel.TYPE_SCAN_HISTORY).getNodeRef();

      _nodeService.setProperty(logNodeRef, AcavModel.PROP_LOG_DATE, new Date());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_DATA_READ, scanSummary.getDataRead());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_DATA_SCANNED, scanSummary.getDataScanned());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_ENGINE_VERSION, scanSummary.getEngineVersion());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_INFECTED_FILES, scanSummary.getInfectedFiles());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_KNOWN_VIRUSES, scanSummary.getKnownViruses());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_SCANNED_DIRECTORIES, scanSummary.getScannedDirectories());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_SCANNED_FILES, scanSummary.getScannedFiles());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_SCANNED_OBJECT, scanSummary.getScannedObject().getAbsolutePath());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_TIME, scanSummary.getTime());
      _nodeService.setProperty(logNodeRef, AcavModel.PROP_SCAN_TYPE, scanType);
    } finally {
      AuthenticationUtil.setFullyAuthenticatedUser(currentUsername);
    }
  }

}
