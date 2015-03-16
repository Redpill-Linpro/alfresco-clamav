package org.redpill.alfresco.acav.repo.service;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ScanCallback {
  
  void handleNode(NodeRef nodeRef);

}
