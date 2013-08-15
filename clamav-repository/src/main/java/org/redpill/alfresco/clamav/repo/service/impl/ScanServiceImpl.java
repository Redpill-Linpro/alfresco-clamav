package org.redpill.alfresco.clamav.repo.service.impl;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.FileContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.alfresco.util.exec.RuntimeExec;
import org.alfresco.util.exec.RuntimeExec.ExecutionResult;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.redpill.alfresco.clamav.repo.service.NodeDao;
import org.redpill.alfresco.clamav.repo.service.ScanHistoryService;
import org.redpill.alfresco.clamav.repo.service.ScanService;
import org.redpill.alfresco.clamav.repo.service.SystemScanDirectoryRegistry;
import org.redpill.alfresco.clamav.repo.utils.AcavUtils;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary.ScanType;

public class ScanServiceImpl extends AbstractService implements ScanService {

  private static final Logger LOG = Logger.getLogger(ScanServiceImpl.class);

  private RuntimeExec _scanCommand;

  private RuntimeExec _checkCommand;

  private ContentService _contentService;

  private FileFolderService _fileFolderService;

  private boolean _enabled;

  private SystemScanDirectoryRegistry _systemScanDirectoryRegistry;

  private ScanHistoryService _scanHistoryService;

  private NodeDao _nodeDao;

  private SearchService _searchService;

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
    return scanFile(file, null);
  }

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

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanSystem()
   */
  @Override
  public List<ScanSummary> scanSystem() {
    List<ScanSummary> result = new ArrayList<ScanSummary>();

    List<File> directories = _systemScanDirectoryRegistry.getDirectories();

    for (File directory : directories) {
      ScanSummary scanSummary = scanSystem(directory);

      if (scanSummary == null) {
        continue;
      }

      result.add(scanSummary);
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.ScanService#scanSystem(java.io.File)
   */
  @Override
  public ScanSummary scanSystem(File directory) {
    if (!directory.exists()) {
      return null;
    }

    if (!_enabled) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scan Service not enabled, skipping...");
      }

      return null;
    }

    NodeRef rootNode = _acavNodeService.getRootNode();

    if (_lockService.getLockStatus(rootNode) != LockStatus.NO_LOCK) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("The Alfresco ClamAV system is currently locked...");
      }

      return null;
    }

    _lockService.lock(rootNode, LockType.NODE_LOCK, 30);

    try {
      File logFile = TempFileProvider.createTempFile("acav_scan_", ".log");

      Map<String, String> properties = new HashMap<String, String>();
      properties.put(KEY_TEMPDIR, TempFileProvider.getTempDir().getAbsolutePath());
      properties.put(KEY_LOGFILE, logFile.getAbsolutePath());
      properties.put(KEY_FILE, directory.getAbsolutePath());
      properties.put(KEY_OPTIONS, "-r -i");

      ExecutionResult result = _scanCommand.execute(properties);

      String logMessage = getLogMessage(logFile);

      if (result.getExitValue() == 2) {
        throw new AlfrescoRuntimeException(logMessage);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("\n\n" + logMessage + "\n\n");
      }

      writeLogMessage(logMessage);

      LineIterator iterator = IOUtils.lineIterator(new StringReader(logMessage));

      List<String> contentUrls = new ArrayList<String>();
      List<String> foundList = new ArrayList<String>();

      ScanSummary scanSummary = new ScanSummary();

      scanSummary.setScannedObject(directory);

      scanSummary.setScanType(ScanType.SYSTEM);

      while (iterator.hasNext()) {
        String line = iterator.nextLine();

        if (!line.startsWith(directory.getAbsolutePath())) {
          parseScanSummary(scanSummary, line);

          continue;
        }

        String contentUrl = StringUtils.split(line, ":")[0];
        contentUrl = StringUtils.replace(contentUrl, directory.getAbsolutePath(), "store:/");
        contentUrls.add(contentUrl);

        foundList.add(line);
      }

      List<Integer> ids = _nodeDao.selectByContentUrls(contentUrls);

      if (ids == null || ids.size() == 0) {
        return null;
      }

      String query = "";

      for (Integer id : ids) {
        if (StringUtils.isNotBlank(query)) {
          query = query + " OR ";
        }

        query = query + "@sys\\:node-dbid:" + id;
      }

      query = "(" + query + ")";
      query = query + " AND !@acavc\\:scanStatus:\"INFECTED\"";

      ResultSet nodes = _searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, query);

      try {
        for (int x = 0; x < nodes.length(); x++) {
          NodeRef nodeRef = nodes.getNodeRef(x);

          ContentData content = (ContentData) _nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);

          if (content == null) {
            continue;
          }

          String contentUrl = content.getContentUrl();
          contentUrl = StringUtils.replace(contentUrl, "store://", "");

          ScanResult scanResult = new ScanResult();

          for (String foundEntry : foundList) {
            if (!StringUtils.contains(foundEntry, contentUrl)) {
              continue;
            }

            String virusName = StringUtils.split(foundEntry, ":")[1];
            virusName = StringUtils.replace(virusName, "FOUND", "").trim();
            scanResult.setVirusName(virusName);
          }

          scanResult.setDate(new Date());
          scanResult.setFound(true);
          scanResult.setNodeRef(nodeRef);
          scanResult.setName((String) _nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));

          scanSummary.addInfected(scanResult);
        }
      } finally {
        AcavUtils.closeQuietly(nodes);
      }

      _scanHistoryService.record(scanSummary);

      return scanSummary;
    } finally {
      _lockService.unlock(rootNode);
    }
  }

  private void parseScanSummary(ScanSummary scanSummary, String line) {
    line = line.toLowerCase();

    if (line.startsWith("known viruses:")) {
      scanSummary.setKnownViruses(Integer.parseInt(StringUtils.split(line, ":")[1].trim()));
    } else if (line.startsWith("engine version:")) {
      scanSummary.setEngineVersion(StringUtils.split(line, ":")[1].trim());
    } else if (line.startsWith("scanned directories:")) {
      scanSummary.setScannedDirectories(Integer.parseInt(StringUtils.split(line, ":")[1].trim()));
    } else if (line.startsWith("scanned files:")) {
      scanSummary.setScannedFiles(Integer.parseInt(StringUtils.split(line, ":")[1].trim()));
    } else if (line.startsWith("infected files:")) {
      scanSummary.setInfectedFiles(Integer.parseInt(StringUtils.split(line, ":")[1].trim()));
    } else if (line.startsWith("data scanned:")) {
      scanSummary.setDataScanned(StringUtils.split(line, ":")[1].trim());
    } else if (line.startsWith("data read:")) {
      scanSummary.setDataRead(StringUtils.split(line, ":")[1].trim());
    } else if (line.startsWith("time:")) {
      scanSummary.setTime(StringUtils.split(line, ":")[1].trim());
    }
  }

  private String extractVirusName(String filePath, String logMessage) {
    ParameterCheck.mandatoryString("filePath", filePath);
    ParameterCheck.mandatoryString("logMessage", logMessage);

    LineIterator iterator = new LineIterator(new StringReader(logMessage));

    while (iterator.hasNext()) {
      String line = iterator.nextLine();

      if (!line.startsWith(filePath)) {
        continue;
      }

      line = StringUtils.replace(line, filePath + ":", "");
      line = StringUtils.replace(line, "FOUND", "");

      return line.trim();
    }

    return null;
  }

  private ScanSummary scanFile(File file, String options) {
    if (!_enabled) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Scan Service not enabled, skipping...");
      }

      return null;
    }

    ParameterCheck.mandatory("file", file);

    if (!file.exists()) {
      throw new AlfrescoRuntimeException("File '" + file.getAbsolutePath() + "' does not exist");
    }

    if (file.isDirectory()) {
      return null;
    }

    NodeRef rootNode = _acavNodeService.getRootNode();

    if (_lockService.getLockStatus(rootNode) != LockStatus.NO_LOCK) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("The Alfresco ClamAV system is currently locked...");
      }

      return null;
    }

    _lockService.lock(rootNode, LockType.NODE_LOCK, 30);

    try {
      File logFile = TempFileProvider.createTempFile("acav_scan_", ".log");

      Map<String, String> properties = new HashMap<String, String>();
      properties.put(KEY_TEMPDIR, TempFileProvider.getTempDir().getAbsolutePath());
      properties.put(KEY_LOGFILE, logFile.getAbsolutePath());
      properties.put(KEY_FILE, file.getAbsolutePath());
      properties.put(KEY_OPTIONS, options == null ? "" : options);

      ExecutionResult result = _scanCommand.execute(properties);

      String logMessage = getLogMessage(logFile);

      if (result.getExitValue() == 2) {
        throw new AlfrescoRuntimeException(logMessage);
      }

      if (LOG.isDebugEnabled()) {
        LOG.debug("\n\n" + logMessage + "\n\n");
      }

      writeLogMessage(logMessage);

      ScanSummary scanSummary = new ScanSummary();

      scanSummary.setScanType(ScanType.SINGLE);

      ScanResult scanResult = new ScanResult();
      scanResult.setFound(result.getExitValue() == 1);
      scanResult.setDate(new Date());

      if (scanResult.isFound()) {
        scanResult.setVirusName(extractVirusName(file.getAbsolutePath(), logMessage));
      }

      scanSummary.addScanned(scanResult);

      LineIterator iterator = IOUtils.lineIterator(new StringReader(logMessage));

      scanSummary.setScannedObject(file);

      while (iterator.hasNext()) {
        String line = iterator.nextLine();

        parseScanSummary(scanSummary, line);
      }

      _scanHistoryService.record(scanSummary);

      return scanSummary;
    } finally {
      _lockService.unlock(rootNode);
    }
  }

  public void setScanCommand(RuntimeExec scanCommand) {
    _scanCommand = scanCommand;
  }

  public void setContentService(ContentService contentService) {
    _contentService = contentService;
  }

  public void setFileFolderService(FileFolderService fileFolderService) {
    _fileFolderService = fileFolderService;
  }

  public void setCheckCommand(RuntimeExec checkCommand) {
    _checkCommand = checkCommand;
  }

  public void setSystemScanDirectoryRegistry(SystemScanDirectoryRegistry systemScanDirectoryRegistry) {
    _systemScanDirectoryRegistry = systemScanDirectoryRegistry;
  }

  public void setScanHistoryService(ScanHistoryService scanHistoryService) {
    _scanHistoryService = scanHistoryService;
  }

  public void setNodeDao(NodeDao nodeDao) {
    _nodeDao = nodeDao;
  }

  public void setSearchService(SearchService searchService) {
    _searchService = searchService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.redpill.alfresco.clamav.repo.service.impl.AbstractService#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    ParameterCheck.mandatory("scanCommand", _scanCommand);
    ParameterCheck.mandatory("contentService", _contentService);
    ParameterCheck.mandatory("fileFolderService", _fileFolderService);
    ParameterCheck.mandatory("checkCommand", _checkCommand);
    ParameterCheck.mandatory("scanHistoryService", _scanHistoryService);
    ParameterCheck.mandatory("systemScanDirectoryRegistry", _systemScanDirectoryRegistry);
    ParameterCheck.mandatory("nodeDao", _nodeDao);
    ParameterCheck.mandatory("searchService", _searchService);

    _enabled = _checkCommand.execute().getExitValue() == 0;
  }

}
