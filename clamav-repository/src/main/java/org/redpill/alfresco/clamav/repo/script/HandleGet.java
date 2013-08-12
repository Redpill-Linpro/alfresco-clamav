package org.redpill.alfresco.clamav.repo.script;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.service.ScanAction;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class HandleGet extends DeclarativeWebScript implements InitializingBean {

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

    Map<String, Object> result = new HashMap<String, Object>();

    return result;
  }

  public void setScanAction(ScanAction scanAction) {
    _scanAction = scanAction;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    ParameterCheck.mandatory("scanAction", _scanAction);
  }

}
