package org.redpill.alfresco.clamav.repo.service.impl;

import java.io.File;
import java.io.InputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.FileContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.model.AcavModel;
import org.redpill.alfresco.clamav.repo.service.ScanService;
import org.redpill.alfresco.clamav.repo.service.StatusService;
import org.redpill.alfresco.clamav.repo.utils.AcavUtils;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractScanService extends AbstractService implements ScanService {

  @Autowired
  protected ContentService _contentService;

  @Autowired
  protected FileFolderService _fileFolderService;

  @Autowired
  protected StatusService _statusService;

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanNode(org.alfresco .service.cmr.repository.NodeRef)
   */
  @Override
  public ScanSummary scanNode(NodeRef nodeRef) {
    ParameterCheck.mandatory("nodeRef", nodeRef);

    if (!_nodeService.exists(nodeRef)) {
      // throw new InvalidNodeRefException(nodeRef);
      return null;
    }

    FileInfo fileInfo = _fileFolderService.getFileInfo(nodeRef);

    if (fileInfo.isFolder()) {
      return null;
    }

    ContentReader contentReader = _contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

    if (contentReader == null) {
      return null;
    }

    return scanContent(contentReader);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanContent(org.alfresco .service.cmr.repository.ContentReader)
   */
  @Override
  public ScanSummary scanContent(ContentReader contentReader) {
    ParameterCheck.mandatory("contentReader", contentReader);

    if (!contentReader.exists()) {
      return null;
    }

    if (contentReader instanceof FileContentReader) {
      FileContentReader fileContentReader = (FileContentReader) contentReader;

      return scanFile(fileContentReader.getFile());
    } else {
      return scanStream(contentReader.getContentInputStream());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanFile(java.io.File)
   */
  @Override
  public ScanSummary scanFile(File file) {
    return scanFile(file, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanFile(java.io.File)
   */
  @Override
  public abstract ScanSummary scanFile(File file, boolean writeStatus);

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanStream(java.io .InputStream)
   */
  @Override
  public ScanSummary scanStream(InputStream inputStream) {
    ParameterCheck.mandatory("inputStream", inputStream);

    File tempFile = AcavUtils.copy(inputStream);

    try {
      return scanFile(tempFile);
    } finally {
      tempFile.delete();
    }
  }

  public boolean lock() {
    NodeRef scanLockNode = _acavNodeService.getScanLockNode();

    LockStatus lockStatus = _lockService.getLockStatus(scanLockNode);

    if (lockStatus == LockStatus.LOCK_OWNER || lockStatus == LockStatus.LOCKED) {
      return false;
    }

    _lockService.lock(scanLockNode, LockType.NODE_LOCK, 30);

    return true;
  }

  public void unlock() {
    NodeRef scanLockNode = _acavNodeService.getScanLockNode();

    _lockService.unlock(scanLockNode);
  }

  public boolean isEnabled() {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    Boolean enabled = (Boolean) _nodeService.getProperty(systemStatusNode, AcavModel.PROP_ENABLED);
    enabled = enabled != null ? enabled : true;

    return isActive() && enabled;
  }

  public abstract boolean isActive();

}
