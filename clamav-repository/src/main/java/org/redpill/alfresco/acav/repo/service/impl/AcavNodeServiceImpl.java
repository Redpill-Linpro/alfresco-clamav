package org.redpill.alfresco.acav.repo.service.impl;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.AcavNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("acav.acavNodeService")
public class AcavNodeServiceImpl implements AcavNodeService {

  @Autowired
  @Qualifier("NodeService")
  private NodeService _nodeService;

  @Autowired
  @Qualifier("FileFolderService")
  private FileFolderService _fileFolderService;

  @Autowired
  @Qualifier("PermissionService")
  private PermissionService _permissionService;

  @Autowired
  @Qualifier("TransactionService")
  private TransactionService _transactionService;

  @Autowired
  @Qualifier("LockService")
  private LockService _lockService;

  @Autowired
  private Repository _repository;

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.AcavNodeService#getRootNode()
   */
  @Override
  public NodeRef getRootNode() {
    NodeRef companyHome = _repository.getCompanyHome();

    NodeRef root = _fileFolderService.searchSimple(companyHome, "Alfresco ClamAV");

    if (root == null) {
      root = _fileFolderService.create(companyHome, "alfresco_clamav", ContentModel.TYPE_FOLDER).getNodeRef();

      _nodeService.setProperty(root, ContentModel.PROP_NAME, "Alfresco ClamAV");

      _lockService.unlock(root);
    }

    return root;
  }

  @Override
  public NodeRef getVirusVaultNodeRef() {
    NodeRef rootNode = getRootNode();

    NodeRef virusVault = _fileFolderService.searchSimple(rootNode, "ACAV Virus Vault");

    if (virusVault == null) {
      virusVault = _fileFolderService.create(rootNode, "acav_virus_vault", ContentModel.TYPE_FOLDER).getNodeRef();

      _nodeService.setProperty(virusVault, ContentModel.PROP_NAME, "ACAV Virus Vault");

      _permissionService.setInheritParentPermissions(virusVault, false);
    }

    return virusVault;
  }

  @Override
  public NodeRef getScanHistoryFolderNodeRef() {
    NodeRef rootNode = getRootNode();

    NodeRef scanHistoryNodeRef = _fileFolderService.searchSimple(rootNode, "ACAV Scan History");

    if (scanHistoryNodeRef == null) {
      scanHistoryNodeRef = _fileFolderService.create(rootNode, "acav_scan_history", ContentModel.TYPE_FOLDER).getNodeRef();

      _nodeService.setProperty(scanHistoryNodeRef, ContentModel.PROP_NAME, "ACAV Scan History");

      _permissionService.setInheritParentPermissions(scanHistoryNodeRef, false);
    }

    return scanHistoryNodeRef;
  }

  @Override
  public NodeRef createFolderStructure(NodeRef parentNodeRef) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.MONTH) + 1), 2, "0");
    String day = StringUtils.leftPad(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)), 2, "0");

    NodeRef yearFolder = getOrCreateSubFolder(parentNodeRef, year);
    NodeRef monthFolder = getOrCreateSubFolder(yearFolder, month);
    NodeRef dayFolder = getOrCreateSubFolder(monthFolder, day);

    return dayFolder;
  }

  @Override
  public NodeRef getUpdateStatusNode() {
    NodeRef rootNode = getRootNode();

    NodeRef updateStatusNodeRef = _nodeService.getChildByName(rootNode, ContentModel.ASSOC_CONTAINS, "update_status");

    if (updateStatusNodeRef != null) {
      return updateStatusNodeRef;
    }

    return _fileFolderService.create(rootNode, "update_status", AcavModel.TYPE_UPDATE_STATUS).getNodeRef();
  }

  @Override
  public NodeRef getScanLockNode() {
    return createLockNode("scan_lock");
  }

  @Override
  public NodeRef getUpdateLockNode() {
    return createLockNode("update_lock");
  }

  @Override
  public NodeRef getSystemStatusNode() {
    NodeRef rootNode = getRootNode();

    NodeRef systemStatusNode = _nodeService.getChildByName(rootNode, ContentModel.ASSOC_CONTAINS, "system_status");

    if (systemStatusNode != null) {
      return systemStatusNode;
    }

    return _fileFolderService.create(rootNode, "system_status", AcavModel.TYPE_SYSTEM_STATUS).getNodeRef();
  }

  private NodeRef createLockNode(final String lockNodeName) {
    NodeRef rootNode = getRootNode();

    NodeRef lockNode = _nodeService.getChildByName(rootNode, ContentModel.ASSOC_CONTAINS, lockNodeName);

    if (lockNode != null) {
      return lockNode;
    }

    return _fileFolderService.create(rootNode, lockNodeName, ContentModel.TYPE_CONTENT).getNodeRef();
  }

  private NodeRef getOrCreateSubFolder(NodeRef parentNodeRef, String subFolder) {
    NodeRef existingFolder = _nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, subFolder);

    NodeRef subFolderNodeRef;

    if (existingFolder == null) {
      FileInfo fileInfo = _fileFolderService.create(parentNodeRef, subFolder, ContentModel.TYPE_FOLDER);

      subFolderNodeRef = fileInfo.getNodeRef();

      // inherit permissions
      _permissionService.setInheritParentPermissions(subFolderNodeRef, true);
    } else {
      subFolderNodeRef = existingFolder;
    }

    return subFolderNodeRef;
  }

}
