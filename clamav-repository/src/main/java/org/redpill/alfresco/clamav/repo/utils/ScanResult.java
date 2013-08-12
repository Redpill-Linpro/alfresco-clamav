package org.redpill.alfresco.clamav.repo.utils;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

public class ScanResult {

  private boolean _found = false;

  private Date _date = new Date();

  private String _virusName = null;

  private NodeRef _nodeRef = null;

  private String _name = null;

  public boolean isFound() {
    return _found;
  }

  public void setFound(boolean found) {
    _found = found;
  }

  public Date getDate() {
    return _date;
  }

  public void setDate(Date date) {
    _date = date;
  }

  public String getVirusName() {
    return _virusName;
  }

  public void setVirusName(String virusName) {
    _virusName = virusName;
  }

  public NodeRef getNodeRef() {
    return _nodeRef;
  }

  public void setNodeRef(NodeRef nodeRef) {
    _nodeRef = nodeRef;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

}
