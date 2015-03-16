package org.redpill.alfresco.acav.repo.script.overview;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class DisablePost extends AbstractToggleStatus {
  
  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    disable();
    
    return new HashMap<String, Object>();
  }
  
}
