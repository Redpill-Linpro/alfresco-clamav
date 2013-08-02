package org.redpill.alfresco.clamav.repo.model;

import org.alfresco.service.namespace.QName;

public interface AcavModel {

  public static final String ACAV_URI = "http://www.redpill-linpro.se/acav/model/1.0";

  public static final QName TYPE_SCAN_HISTORY = QName.createQName(ACAV_URI, "scanHistory");

  public static final QName ASPECT_SCANNED = QName.createQName(ACAV_URI, "scanned");

  public static final QName PROP_SCAN_DATE = QName.createQName(ACAV_URI, "scanDate");
  public static final QName PROP_SCAN_LOG_DATE = QName.createQName(ACAV_URI, "scanLogDate");
  public static final QName PROP_SCAN_LOG = QName.createQName(ACAV_URI, "scanLog");
  public static final QName PROP_SCAN_STATUS = QName.createQName(ACAV_URI, "scanStatus");
  public static final QName PROP_VIRUS_NAME = QName.createQName(ACAV_URI, "virusName");
  public static final QName PROP_SCAN_TYPE = QName.createQName(ACAV_URI, "scanType");

}
