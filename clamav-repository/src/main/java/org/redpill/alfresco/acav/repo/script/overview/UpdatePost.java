package org.redpill.alfresco.acav.repo.script.overview;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.acav.repo.utils.AcavUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class UpdatePost extends DeclarativeWebScript {
  
  private AcavUtils _acavUtils;

  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    _acavUtils.updateVirusDatabase();

    return new HashMap<String, Object>();
  }

  public void setAcavUtils(AcavUtils acavUtils) {
    _acavUtils = acavUtils;
  }

  @PostConstruct
  public void postConstruct() {
    ParameterCheck.mandatory("acavUtils", _acavUtils);
  }

}
