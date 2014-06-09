package org.redpill.alfresco.clamav.repo.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;

import com.github.dynamicextensionsalfresco.annotations.RunAsSystem;

public interface ScanService {

  /**
   * Scans a specific nodeRef for viruses.
   * 
   * @param nodeRef
   *          nodeRef to scan
   * @return true if virus found, false otherwise
   */
  @RunAsSystem
  ScanSummary scanNode(NodeRef nodeRef);

  /**
   * Scans content for viruses.
   * 
   * @param contentReader
   *          contentReader to scan
   * @return true if virus found, false otherwise
   */
  @RunAsSystem
  ScanSummary scanContent(ContentReader contentReader);

  /**
   * Scans a File for viruses.
   * 
   * @param file
   *          file to scan
   * @return true if virus found, false otherwise
   */
  @RunAsSystem
  ScanSummary scanFile(File file);

  /**
   * Scans a File for viruses.
   * 
   * @param file
   *          file to scan
   * @param writeStatus
   *          if to write status or not
   * @return true if virus found, false otherwise
   */
  @RunAsSystem
  ScanSummary scanFile(File file, boolean writeStatus);

  /**
   * Scans a stream for viruses.
   * 
   * @param inputStream
   *          inputStream to scan
   * @return true if virus found, false otherwise
   */
  @RunAsSystem
  ScanSummary scanStream(InputStream inputStream);

  /**
   * Scans the entire system with pre-configured directories.
   * 
   * @return Either null or a (maybe empty) list of ScanResult objects.
   */
  @RunAsSystem
  List<ScanSummary> scanSystem();

  /**
   * Scans the specified directory.
   * 
   * @param directory
   *          to scan
   * @return Either null or a (maybe empty) list of ScanResult objects.
   */
  @RunAsSystem
  ScanSummary scanSystem(File directory);

  @RunAsSystem
  List<ScanResult> scanSite(SiteInfo site);

}
