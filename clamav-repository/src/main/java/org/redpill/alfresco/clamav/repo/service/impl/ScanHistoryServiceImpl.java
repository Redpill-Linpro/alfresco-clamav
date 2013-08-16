package org.redpill.alfresco.clamav.repo.service.impl;

import java.util.Date;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.model.AcavModel;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.redpill.alfresco.clamav.repo.service.ScanHistoryService;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.springframework.beans.factory.InitializingBean;

public class ScanHistoryServiceImpl implements ScanHistoryService, InitializingBean {

  private AcavNodeService _acavNodeService;

  private NodeService _nodeService;

  private FileFolderService _fileFolderService;

  @Override
  public void record(final ScanSummary scanSummary) {
    ParameterCheck.mandatory("scanType", scanSummary);

    final String scanType = scanSummary.getScanType().name();

    AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {

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

        return null;
      }
    });
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
