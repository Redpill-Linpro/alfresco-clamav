package org.redpill.alfresco.acav.repo.service.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.AcavNodeService;
import org.redpill.alfresco.acav.repo.service.ScanAction;
import org.redpill.alfresco.acav.repo.utils.ScanResult;
import org.redpill.alfresco.acav.repo.utils.ScanSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component(value = "acav.scanAction")
public class ScanActionImpl implements ScanAction {

  @Autowired
  @Qualifier("NodeService")
  private NodeService _nodeService;

  @Autowired
  @Qualifier("FileFolderService")
  private FileFolderService _fileFolderService;

  @Autowired
  @Qualifier("SearchService")
  private SearchService _searchService;

  @Autowired
  @Qualifier("policyBehaviourFilter")
  private BehaviourFilter _behaviourFilter;

  @Autowired
  private AcavNodeService _acavNodeService;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.redpill.alfresco.clamav.repo.service.ScanAction#handleNode(java.lang
   * .String, java.lang.String)
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
   * @see
   * org.redpill.alfresco.clamav.repo.service.ScanAction#handleNode(java.lang
   * .String, org.redpill.alfresco.clamav.repo.utils.ScanResult)
   */
  @Override
  public void handleNode(String nodeRef, ScanResult scanResult) {
    handleNode(new NodeRef(nodeRef), scanResult);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.redpill.alfresco.clamav.repo.service.ScanAction#handleNode(org.alfresco
   * .service.cmr.repository.NodeRef, java.lang.String)
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
   * @see
   * org.redpill.alfresco.clamav.repo.service.ScanAction#handleNode(org.alfresco
   * .service.cmr.repository.NodeRef,
   * org.redpill.alfresco.clamav.repo.utils.ScanResult)
   */
  @Override
  public void handleNode(final NodeRef nodeRef, final ScanResult scanResult) {
    ParameterCheck.mandatory("nodeRef", nodeRef);
    ParameterCheck.mandatory("scanResult", scanResult);

    AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        // if the document already has the aspect, just exit
        if (_nodeService.hasAspect(nodeRef, AcavModel.ASPECT_SCANNED)) {
          return null;
        }

        // if it's a document already in the acavInfectedStore, just exit
        if ("acavInfectedStore".equals(nodeRef.getStoreRef().getIdentifier())) {
          return null;
        }

        updateScannedProperties(nodeRef, scanResult);

        if (scanResult.isFound()) {
          handleInfectedNode(nodeRef);
        }

        return null;
      }
    });

  }

  @Override
  public void handleNodes(ScanSummary scanSummary) {
    ParameterCheck.mandatory("scanSummary", scanSummary);

    for (ScanResult scanResult : scanSummary.getScannedList()) {
      handleNode(scanResult.getNodeRef(), scanResult);
    }
  }

  @Override
  public void removeScannedStuff(final NodeRef nodeRef) {
    ParameterCheck.mandatory("nodeRef", nodeRef);

    AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        if (!_nodeService.exists(nodeRef)) {
          return null;
        }

        if (!_nodeService.hasAspect(nodeRef, AcavModel.ASPECT_SCANNED)) {
          return null;
        }

        _nodeService.removeAspect(nodeRef, AcavModel.ASPECT_SCANNED);

        return null;
      }
    });
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

}
