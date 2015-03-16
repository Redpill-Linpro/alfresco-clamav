package org.redpill.alfresco.acav.repo.model;

import org.alfresco.service.namespace.QName;

public interface AcavModel {

  public static final String ACAV_CORE_URI = "http://www.redpill-linpro.se/acav/model/core/1.0";
  public static final String ACAV_HISTORY_URI = "http://www.redpill-linpro.se/acav/model/history/1.0";
  public static final String ACAV_UPDATE_URI = "http://www.redpill-linpro.se/acav/model/update/1.0";
  public static final String ACAV_SYSTEM_URI = "http://www.redpill-linpro.se/acav/model/system/1.0";

  public static final QName TYPE_SCAN_HISTORY = QName.createQName(ACAV_HISTORY_URI, "scanHistory");
  public static final QName TYPE_UPDATE_STATUS = QName.createQName(ACAV_UPDATE_URI, "update_status");
  public static final QName TYPE_SYSTEM_STATUS = QName.createQName(ACAV_SYSTEM_URI, "system_status");

  public static final QName ASPECT_SCANNED = QName.createQName(ACAV_CORE_URI, "scanned");

  public static final QName PROP_SCAN_DATE = QName.createQName(ACAV_CORE_URI, "scanDate");
  public static final QName PROP_SCAN_STATUS = QName.createQName(ACAV_CORE_URI, "scanStatus");
  public static final QName PROP_VIRUS_NAME = QName.createQName(ACAV_CORE_URI, "virusName");

  public static final QName PROP_LOG_DATE = QName.createQName(ACAV_HISTORY_URI, "log_date");
  public static final QName PROP_DATA_READ = QName.createQName(ACAV_HISTORY_URI, "data_read");
  public static final QName PROP_SCANNED_DIRECTORIES = QName.createQName(ACAV_HISTORY_URI, "scanned_directories");
  public static final QName PROP_SCANNED_FILES = QName.createQName(ACAV_HISTORY_URI, "scanned_files");
  public static final QName PROP_INFECTED_FILES = QName.createQName(ACAV_HISTORY_URI, "infected_files");
  public static final QName PROP_DATA_SCANNED = QName.createQName(ACAV_HISTORY_URI, "data_scanned");
  public static final QName PROP_TIME = QName.createQName(ACAV_HISTORY_URI, "time");
  public static final QName PROP_SCANNED_OBJECT = QName.createQName(ACAV_HISTORY_URI, "scanned_object");
  public static final QName PROP_SCAN_TYPE = QName.createQName(ACAV_HISTORY_URI, "scan_type");

  public static final QName PROP_KNOWN_VIRUSES = QName.createQName(ACAV_UPDATE_URI, "known_viruses");
  public static final QName PROP_ENGINE_VERSION = QName.createQName(ACAV_UPDATE_URI, "engine_version");
  public static final QName PROP_LAST_UPDATE = QName.createQName(ACAV_UPDATE_URI, "last_update");
  public static final QName PROP_UPDATE_CRON = QName.createQName(ACAV_UPDATE_URI, "cron");

  public static final QName PROP_STATUS = QName.createQName(ACAV_SYSTEM_URI, "status");
  public static final QName PROP_ENABLED = QName.createQName(ACAV_SYSTEM_URI, "enabled");

}
