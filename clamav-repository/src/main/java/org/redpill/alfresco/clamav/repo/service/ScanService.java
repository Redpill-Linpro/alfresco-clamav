package org.redpill.alfresco.clamav.repo.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;

public interface ScanService {

  /**
   * Scans a specific nodeRef for viruses.
   * 
   * @param nodeRef
   *          nodeRef to scan
   * @return true if virus found, false otherwise
   */
  ScanSummary scanNode(NodeRef nodeRef);

  /**
   * Scans content for viruses.
   * 
   * @param contentReader
   *          contentReader to scan
   * @return true if virus found, false otherwise
   */
  ScanSummary scanContent(ContentReader contentReader);

  /**
   * Scans a File for viruses.
   * 
   * @param file
   *          file to scan
   * @return true if virus found, false otherwise
   */
  ScanSummary scanFile(File file);

  /**
   * Scans a stream for viruses.
   * 
   * @param inputStream
   *          inputStream to scan
   * @return true if virus found, false otherwise
   */
  ScanSummary scanStream(InputStream inputStream);

  /**
   * Scans the entire system with pre-configured directories.
   * 
   * @return Either null or a (maybe empty) list of ScanResult objects.
   */
  List<ScanSummary> scanSystem();

  /**
   * Scans the specified directory.
   * 
   * @param directory
   *          to scan
   * @return Either null or a (maybe empty) list of ScanResult objects.
   */
  ScanSummary scanSystem(File directory);

  List<ScanResult> scanSite(SiteInfo site);

}
