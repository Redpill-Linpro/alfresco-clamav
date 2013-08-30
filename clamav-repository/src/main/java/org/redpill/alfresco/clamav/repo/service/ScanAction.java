package org.redpill.alfresco.clamav.repo.service;

import nl.runnable.alfresco.annotations.RunAsSystem;

import org.alfresco.service.cmr.repository.NodeRef;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;

public interface ScanAction {

  @RunAsSystem
  void handleNode(String nodeRef, String name, String virusName);

  @RunAsSystem
  void handleNode(String nodeRef, ScanResult scanResult);

  @RunAsSystem
  void handleNode(NodeRef nodeRef, String name, String virusName);

  @RunAsSystem
  void handleNode(NodeRef nodeRef, ScanResult scanResult);

  @RunAsSystem
  void handleNodes(ScanSummary scanSummary);

  @RunAsSystem
  void removeScannedStuff(NodeRef nodeRef);

}
