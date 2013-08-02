package org.redpill.alfresco.clamav.repo.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;

public interface ScanAction {

  void handleNode(NodeRef nodeRef, ScanResult scanResult);

  void removeScannedStuff(NodeRef nodeRef);

}
