package org.redpill.alfresco.acav.repo.service.impl;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.lang.StringUtils;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.AcavNodeService;
import org.redpill.alfresco.acav.repo.utils.AcavUtilsImpl;
import org.springframework.stereotype.Component;

@Component("acav.acavNodeService")
public class AcavNodeServiceImpl implements AcavNodeService {

  @Resource(name = "SearchService")
  private SearchService _searchService;

  @Resource(name = "NodeService")
  private NodeService _nodeService;

  @Resource(name = "FileFolderService")
  private FileFolderService _fileFolderService;

  @Resource(name = "PermissionService")
  private PermissionService _permissionService;

  @Resource(name = "TransactionService")
  private TransactionService _transactionService;

  @Resource(name = "LockService")
  private LockService _lockService;

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.AcavNodeService#getRootNode()
   */
  @Override
  public NodeRef getRootNode() {
    String query = "PATH:\"/app:company_home/app:alfresco_clamav\"";

    ResultSet result = search(query);

    try {
      if (result.length() > 0) {
        return result.getNodeRef(0);
      }
    } finally {
      AcavUtilsImpl.closeQuietly(result);
    }

    NodeRef companyHomeNodeRef = getCompanyHomeNodeRef();

    ChildAssociationRef parent = _nodeService.getPrimaryParent(companyHomeNodeRef);

    String uri = parent.getQName().getNamespaceURI();
    String validLocalName = QName.createValidLocalName("alfresco_clamav");

    NodeRef acavStorageNode = _nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(uri, validLocalName), ContentModel.TYPE_FOLDER).getChildRef();

    _nodeService.setProperty(acavStorageNode, ContentModel.PROP_NAME, "Alfresco ClamAV");

    _lockService.unlock(acavStorageNode);

    return acavStorageNode;
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

  /**
   * Executes a Lucene search.
   * 
   * @param query
   *          query to execute
   * @return A ResultSet, has to be closed.
   */
  public ResultSet search(String query) {
    SearchParameters searchParameters = new SearchParameters();
    searchParameters.setLanguage(SearchService.LANGUAGE_LUCENE);
    searchParameters.setQuery(query);
    searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

    return _searchService.query(searchParameters);
  }

  /**
   * Fetches the Company Home nodeRef.
   * 
   * @return the company home nodeRef
   */
  private NodeRef getCompanyHomeNodeRef() {
    String query = "PATH:\"/app:company_home\"";

    ResultSet result = search(query);

    try {
      return result.getNodeRef(0);
    } finally {
      AcavUtilsImpl.closeQuietly(result);
    }
  }

}
