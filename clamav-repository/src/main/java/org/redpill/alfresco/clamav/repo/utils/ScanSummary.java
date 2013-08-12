package org.redpill.alfresco.clamav.repo.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScanSummary {

  private int _knownViruses;

  private String _engineVersion;

  private int _scannedDirectories;

  private int _scannedFiles;

  private int _infectedFiles;

  private String _dataScanned;

  private String _dataRead;

  private String _time;

  private List<ScanResult> _infectedList = new ArrayList<ScanResult>();

  private File _directory;

  public int getKnownViruses() {
    return _knownViruses;
  }

  public void setKnownViruses(int knownViruses) {
    _knownViruses = knownViruses;
  }

  public String getEngineVersion() {
    return _engineVersion;
  }

  public void setEngineVersion(String engineVersion) {
    _engineVersion = engineVersion;
  }

  public int getScannedDirectories() {
    return _scannedDirectories;
  }

  public void setScannedDirectories(int scannedDirectories) {
    _scannedDirectories = scannedDirectories;
  }

  public int getScannedFiles() {
    return _scannedFiles;
  }

  public void setScannedFiles(int scannedFiles) {
    _scannedFiles = scannedFiles;
  }

  public int getInfectedFiles() {
    return _infectedFiles;
  }

  public void setInfectedFiles(int infectedFiles) {
    _infectedFiles = infectedFiles;
  }

  public String getDataScanned() {
    return _dataScanned;
  }

  public void setDataScanned(String dataScanned) {
    _dataScanned = dataScanned;
  }

  public String getDataRead() {
    return _dataRead;
  }

  public void setDataRead(String dataRead) {
    _dataRead = dataRead;
  }

  public String getTime() {
    return _time;
  }

  public void setTime(String time) {
    _time = time;
  }

  public List<ScanResult> getInfectedList() {
    return _infectedList;
  }

  public void setInfectedList(List<ScanResult> infectedList) {
    _infectedList = infectedList;
  }

  public void addScanResult(ScanResult scanResult) {
    _infectedList.add(scanResult);
  }

  public File getDirectory() {
    return _directory;
  }

  public void setDirectory(File directory) {
    _directory = directory;
  }

}
