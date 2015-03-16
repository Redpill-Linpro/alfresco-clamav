package org.redpill.alfresco.acav.repo.script.overview;

import java.util.HashMap;
import java.util.Map;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

public class EmptyGet extends DeclarativeWebScript {
  
  @Override
  protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
    return new HashMap<String, Object>();
  }

}
