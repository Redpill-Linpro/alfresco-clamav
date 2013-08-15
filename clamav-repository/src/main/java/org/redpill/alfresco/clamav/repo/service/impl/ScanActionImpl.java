package org.redpill.alfresco.clamav.repo.service.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.model.AcavModel;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.redpill.alfresco.clamav.repo.service.ScanAction;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.springframework.beans.factory.InitializingBean;

public class ScanActionImpl implements ScanAction, InitializingBean {

  private NodeService _nodeService;

  private FileFolderService _fileFolderService;

  private SearchService _searchService;

  private BehaviourFilter _behaviourFilter;

  private AcavNodeService _acavNodeService;

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanAction#handleNode(java.lang.String, java.lang.String)
   */
  @Override
  public void handleNode(String nodeRef, String name, String virusName) {
    ParameterCheck.mandatoryString("nodeRef", nodeRef);
    ParameterCheck.mandatoryString("virusName", virusName);
    ParameterCheck.mandatoryString("name", name);

    ScanResult scanResult = new ScanResult();
    scanResult.setDate(new Date());
    scanResult.setFound(true);
    scanResult.setNodeRef(new NodeRef(nodeRef));
    scanResult.setVirusName(virusName);
    scanResult.setName(name);

    handleNode(nodeRef, scanResult);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanAction#handleNode(java.lang.String, org.redpill.alfresco.clamav.repo.utils.ScanResult)
   */
  @Override
  public void handleNode(String nodeRef, ScanResult scanResult) {
    handleNode(new NodeRef(nodeRef), scanResult);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanAction#handleNode(org.alfresco.service.cmr.repository.NodeRef, java.lang.String)
   */
  @Override
  public void handleNode(NodeRef nodeRef, String name, String virusName) {
    ScanResult scanResult = new ScanResult();
    scanResult.setDate(new Date());
    scanResult.setFound(true);
    scanResult.setNodeRef(nodeRef);
    scanResult.setVirusName(virusName);
    scanResult.setName(name);

    handleNode(nodeRef, scanResult);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanAction#handleNode(org.alfresco.service.cmr.repository.NodeRef, org.redpill.alfresco.clamav.repo.utils.ScanResult)
   */
  @Override
  public void handleNode(NodeRef nodeRef, ScanResult scanResult) {
    ParameterCheck.mandatory("nodeRef", nodeRef);
    ParameterCheck.mandatory("scanResult", scanResult);

    // if the document already has the aspect, just exit
    if (_nodeService.hasAspect(nodeRef, AcavModel.ASPECT_SCANNED)) {
      return;
    }

    // if it's a document already in the acavInfectedStore, just exit
    if ("acavInfectedStore".equals(nodeRef.getStoreRef().getIdentifier())) {
      return;
    }

    updateScannedProperties(nodeRef, scanResult);

    if (scanResult.isFound()) {
      handleInfectedNode(nodeRef);
    }
  }

  @Override
  public void handleNodes(ScanSummary scanSummary) {
    ParameterCheck.mandatory("scanSummary", scanSummary);

    for (ScanResult scanResult : scanSummary.getScannedList()) {
      handleNode(scanResult.getNodeRef(), scanResult);
    }
  }

  @Override
  public void removeScannedStuff(NodeRef nodeRef) {
    ParameterCheck.mandatory("nodeRef", nodeRef);

    if (!_nodeService.exists(nodeRef)) {
      return;
    }

    if (!_nodeService.hasAspect(nodeRef, AcavModel.ASPECT_SCANNED)) {
      return;
    }

    _nodeService.removeAspect(nodeRef, AcavModel.ASPECT_SCANNED);
  }

  private void updateScannedProperties(NodeRef nodeRef, ScanResult scanResult) {
    Map<QName, Serializable> properties = new HashMap<QName, Serializable>();

    properties.put(AcavModel.PROP_SCAN_DATE, scanResult.getDate());
    properties.put(AcavModel.PROP_SCAN_STATUS, scanResult.isFound() ? "INFECTED" : "CLEAN");
    properties.put(AcavModel.PROP_VIRUS_NAME, scanResult.getVirusName());

    _nodeService.addAspect(nodeRef, AcavModel.ASPECT_SCANNED, properties);
  }

  private void handleInfectedNode(NodeRef nodeRef) {
    NodeRef virusVaultNodeRef = _acavNodeService.createFolderStructure(_acavNodeService.getVirusVaultNodeRef());

    String name = (String) _nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

    _behaviourFilter.disableBehaviour();

    try {
      _fileFolderService.copy(nodeRef, virusVaultNodeRef, getUniqueName(virusVaultNodeRef, name));
    } catch (Exception ex) {
      throw new AlfrescoRuntimeException(ex.getMessage(), ex);
    } finally {
      _behaviourFilter.enableBehaviour();
    }

    _nodeService.removeProperty(nodeRef, ContentModel.PROP_CONTENT);
  }

  private String getUniqueName(NodeRef destNodeRef, String name) {
    NodeRef existingFile = _nodeService.getChildByName(destNodeRef, ContentModel.ASSOC_CONTAINS, name);

    if (existingFile != null) {
      // Find a new unique name for clashing names
      int counter = 1;
      String tmpFilename = name;
      int dotIndex;

      while (existingFile != null) {
        dotIndex = name.lastIndexOf(".");

        if (dotIndex == 0) {
          // File didn't have a proper 'name' instead it had just a
          // suffix and
          // started with a ".", create "1.txt"
          tmpFilename = counter + name;
        } else if (dotIndex > 0) {
          // Filename contained ".", create "filename-1.txt"
          tmpFilename = name.substring(0, dotIndex) + "-" + counter + name.substring(dotIndex);
        } else {
          // Filename didn't contain a dot at all, create "filename-1"
          tmpFilename = name + "-" + counter;
        }

        existingFile = _nodeService.getChildByName(destNodeRef, ContentModel.ASSOC_CONTAINS, tmpFilename);

        counter++;
      }

      name = tmpFilename;
    }

    return name;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("searchService", _searchService);
    ParameterCheck.mandatory("fileFolderService", _fileFolderService);
    ParameterCheck.mandatory("behaviourFilter", _behaviourFilter);
    ParameterCheck.mandatory("acavNodeService", _acavNodeService);
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setBehaviourFilter(BehaviourFilter behaviourFilter) {
    _behaviourFilter = behaviourFilter;
  }

  public void setAcavNodeService(AcavNodeService acavNodeService) {
    _acavNodeService = acavNodeService;
  }

}
