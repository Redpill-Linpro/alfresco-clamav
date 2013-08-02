package org.redpill.alfresco.clamav.repo.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;

public interface ScanService {

  /**
   * Scans a specific nodeRef for viruses.
   * 
   * @param nodeRef
   *          nodeRef to scan
   * @return true if virus found, false otherwise
   */
  ScanResult scanNode(NodeRef nodeRef);

  /**
   * Scans content for viruses.
   * 
   * @param contentReader
   *          contentReader to scan
   * @return true if virus found, false otherwise
   */
  ScanResult scanContent(ContentReader contentReader);

  /**
   * Scans a File for viruses.
   * 
   * @param file
   *          file to scan
   * @return true if virus found, false otherwise
   */
  ScanResult scanFile(File file);

  /**
   * Scans a stream for viruses.
   * 
   * @param inputStream
   *          inputStream to scan
   * @return true if virus found, false otherwise
   */
  ScanResult scanStream(InputStream inputStream);

  /**
   * Scans the entire system with pre-configured directories.
   * 
   * @return Either null or a (maybe empty) list of ScanResult objects.
   */
  List<ScanResult> scanSystem();

  /**
   * Scans the specified directory.
   * 
   * @param directory
   *          to scan
   * @return Either null or a (maybe empty) list of ScanResult objects.
   */
  List<ScanResult> scanSystem(File directory);

}
