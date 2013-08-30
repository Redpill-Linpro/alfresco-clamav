package org.redpill.alfresco.clamav.repo.service;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ScanCallback {
  
  void handleNode(NodeRef nodeRef);

}
