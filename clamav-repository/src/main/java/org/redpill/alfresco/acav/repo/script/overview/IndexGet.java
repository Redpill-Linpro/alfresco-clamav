package org.redpill.alfresco.acav.repo.script.overview;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.AcavNodeService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class IndexGet extends DeclarativeWebScript {

  private AcavNodeService _acavNodeService;

  private NodeService _nodeService;

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();
    NodeRef updateStatusNode = _acavNodeService.getUpdateStatusNode();

    Boolean enabled = (Boolean) _nodeService.getProperty(systemStatusNode, AcavModel.PROP_ENABLED);
    Date virusDefinitions = (Date) _nodeService.getProperty(updateStatusNode, AcavModel.PROP_LAST_UPDATE);
    Date lastScan = new Date();
    String acavStatus = (String) _nodeService.getProperty(systemStatusNode, AcavModel.PROP_STATUS);
    String updateCronExpression = (String) _nodeService.getProperty(updateStatusNode, AcavModel.PROP_UPDATE_CRON);

    Map<String, Object> model = new HashMap<String, Object>();

    model.put("enabled", enabled == null ? true : enabled.booleanValue());
    model.put("virus_definitions", virusDefinitions != null ? virusDefinitions.getTime() : 0);
    model.put("last_scan", lastScan != null ? lastScan.getTime() : 0);
    model.put("acavStatus", acavStatus);
    model.put("update_cron_expression", updateCronExpression);

    return model;
  }

  public void setAcavNodeService(AcavNodeService acavNodeService) {
    _acavNodeService = acavNodeService;
  }

  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }

  @PostConstruct
  public void postConstruct() {
    ParameterCheck.mandatory("acavNodeService", _acavNodeService);
    ParameterCheck.mandatory("nodeService", _nodeService);
  }

}
