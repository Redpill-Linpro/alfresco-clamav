package org.redpill.alfresco.acav.repo.script;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.acav.repo.service.ScanAction;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HandleGet extends DeclarativeWebScript {

  private ScanAction _scanAction;

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    String nodeRef = req.getParameter("nodeRef");
    String name = req.getParameter("name");
    String virusName = req.getParameter("virusName");

    ParameterCheck.mandatoryString("nodeRef", nodeRef);
    ParameterCheck.mandatoryString("name", name);
    ParameterCheck.mandatoryString("virusName", virusName);

    _scanAction.handleNode(nodeRef, name, virusName);

    return new HashMap<String, Object>();
  }

  public void setScanAction(ScanAction scanAction) {
    _scanAction = scanAction;
  }

  @PostConstruct
  public void postConstruct() {
    ParameterCheck.mandatory("scanAction", _scanAction);
  }

}
