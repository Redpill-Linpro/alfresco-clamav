package org.redpill.alfresco.clamav.repo.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;

public interface ScanAction {

  void handleNode(String nodeRef, String name, String virusName);

  void handleNode(String nodeRef, ScanResult scanResult);

  void handleNode(NodeRef nodeRef, String name, String virusName);

  void handleNode(NodeRef nodeRef, ScanResult scanResult);

  void removeScannedStuff(NodeRef nodeRef);

}
