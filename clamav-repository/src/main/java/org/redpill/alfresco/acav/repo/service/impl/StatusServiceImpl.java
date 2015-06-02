package org.redpill.alfresco.acav.repo.service.impl;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.ScanService;
import org.redpill.alfresco.acav.repo.service.StatusService;
import org.redpill.alfresco.acav.repo.utils.ScanSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class StatusServiceImpl extends AbstractService implements StatusService {

  @Autowired
  @Qualifier("acav.commandLineScanService")
  private ScanService _commandLineScanService;

  @Override
  public void writeInitialScanStatus() {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    _nodeService.setProperty(systemStatusNode, AcavModel.PROP_STATUS, "SCANNING");
  }

  @Override
  public void writeInitialUpdateStatus() {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    _nodeService.setProperty(systemStatusNode, AcavModel.PROP_STATUS, "UPDATING");
  }

  @Override
  public void writeFinalScanStatus() {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    _nodeService.setProperty(systemStatusNode, AcavModel.PROP_STATUS, "IDLE");
  }

  @Override
  public void writeFinalUpdateStatus() {
    File file = TempFileProvider.createTempFile("tempfile", ".tmp");

    try {
      ScanSummary summary = _commandLineScanService.scanFile(file, false);

      NodeRef updateStatusNode = _acavNodeService.getUpdateStatusNode();

      Map<QName, Serializable> properties = _nodeService.getProperties(updateStatusNode);

      properties.put(AcavModel.PROP_KNOWN_VIRUSES, summary.getKnownViruses());
      properties.put(AcavModel.PROP_ENGINE_VERSION, summary.getEngineVersion());
      properties.put(AcavModel.PROP_LAST_UPDATE, new Date());

      _nodeService.setProperties(updateStatusNode, properties);

      NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

      _nodeService.setProperty(systemStatusNode, AcavModel.PROP_STATUS, "IDLE");
    } finally {
      file.delete();
    }
  }

}
