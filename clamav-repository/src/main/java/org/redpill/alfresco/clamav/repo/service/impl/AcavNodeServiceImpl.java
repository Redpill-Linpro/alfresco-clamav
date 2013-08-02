package org.redpill.alfresco.clamav.repo.service.impl;

import java.util.Calendar;
import java.util.Date;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
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
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.redpill.alfresco.clamav.repo.utils.AcavUtils;
import org.springframework.beans.factory.InitializingBean;

public class AcavNodeServiceImpl implements AcavNodeService, InitializingBean {

  private SearchService _searchService;

  private NodeService _nodeService;

  private FileFolderService _fileFolderService;

  private PermissionService _permissionService;

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
      AcavUtils.closeQuietly(result);
    }

    NodeRef companyHomeNodeRef = getCompanyHomeNodeRef();

    ChildAssociationRef parent = _nodeService.getPrimaryParent(companyHomeNodeRef);

    String uri = parent.getQName().getNamespaceURI();
    String validLocalName = QName.createValidLocalName("alfresco_clamav");

    NodeRef acavStorageNode = _nodeService.createNode(companyHomeNodeRef, ContentModel.ASSOC_CONTAINS, QName.createQName(uri, validLocalName), ContentModel.TYPE_FOLDER).getChildRef();

    _nodeService.setProperty(acavStorageNode, ContentModel.PROP_NAME, "Alfresco ClamAV");

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

  private NodeRef getOrCreateSubFolder(final NodeRef parentNodeRef, final String subFolder) {
    NodeRef existingFolder = _nodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, subFolder);

    NodeRef subFolderNodeRef;

    if (existingFolder == null) {
      subFolderNodeRef = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>() {

        @Override
        public NodeRef doWork() throws Exception {
          FileInfo fileInfo = _fileFolderService.create(parentNodeRef, subFolder, ContentModel.TYPE_FOLDER);

          NodeRef nodeRef = fileInfo.getNodeRef();

          // inherit permissions
          _permissionService.setInheritParentPermissions(nodeRef, true);

          return nodeRef;
        }

      }, AuthenticationUtil.getSystemUserName());
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
  private ResultSet search(String query) {
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
      AcavUtils.closeQuietly(result);
    }
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setPermissionService(PermissionService permissionService) {
    _permissionService = permissionService;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("fileFolderService", _fileFolderService);
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("permissionService", _permissionService);
    ParameterCheck.mandatory("searchService", _searchService);
  }

}
