package org.redpill.alfresco.acav.repo.service;

import org.alfresco.service.cmr.repository.NodeRef;
import org.redpill.alfresco.acav.repo.utils.ScanResult;
import org.redpill.alfresco.acav.repo.utils.ScanSummary;

public interface ScanAction {

  void handleNode(String nodeRef, String name, String virusName);

  void handleNode(String nodeRef, ScanResult scanResult);

  void handleNode(NodeRef nodeRef, String name, String virusName);

  void handleNode(NodeRef nodeRef, ScanResult scanResult);

  void handleNodes(ScanSummary scanSummary);

  void removeScannedStuff(NodeRef nodeRef);

}
