package org.redpill.alfresco.acav.repo.script.overview;

import javax.annotation.PostConstruct;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.AcavNodeService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

public class AbstractToggleStatus extends DeclarativeWebScript {

  private AcavNodeService _acavNodeService;
  
  private NodeService _nodeService;
  
  protected void enable() {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    _nodeService.setProperty(systemStatusNode, AcavModel.PROP_ENABLED, true);
  }

  protected void disable() {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    _nodeService.setProperty(systemStatusNode, AcavModel.PROP_ENABLED, false);
  }

  public void setAcavNodeService(AcavNodeService acavNodeService) {
    _acavNodeService = acavNodeService;
  }
  
  public void setNodeService(NodeService nodeService) {
    _nodeService = nodeService;
  }
  
  @PostConstruct
  public void postConstruct() {
    ParameterCheck.mandatory("nodeService", _nodeService);
    ParameterCheck.mandatory("acavNodeService", _acavNodeService);
  }

}
