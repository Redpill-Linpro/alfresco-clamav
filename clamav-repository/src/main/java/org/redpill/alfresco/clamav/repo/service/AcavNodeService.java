package org.redpill.alfresco.clamav.repo.service;

import org.alfresco.service.cmr.repository.NodeRef;

public interface AcavNodeService {

  /**
   * Gets the root node for the Alfresco ClamAV service.
   * 
   * @return the root nodeRef
   */
  NodeRef getRootNode();

  NodeRef getVirusVaultNodeRef();

  NodeRef getScanHistoryFolderNodeRef();

  NodeRef createFolderStructure(NodeRef parentNodeRef);

}
