package org.redpill.alfresco.clamav.repo.script;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.service.ScanAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.webscripts.annotations.Authentication;
import com.github.dynamicextensionsalfresco.webscripts.annotations.AuthenticationType;
import com.github.dynamicextensionsalfresco.webscripts.annotations.HttpMethod;
import com.github.dynamicextensionsalfresco.webscripts.annotations.RequestParam;
import com.github.dynamicextensionsalfresco.webscripts.annotations.Uri;
import com.github.dynamicextensionsalfresco.webscripts.annotations.WebScript;

@Component
@WebScript(description = "Handles the specific file with the virus name, name and nodeRef", families = { "Alfresco ClamAV" })
@Authentication(AuthenticationType.ADMIN)
public class HandleGet {

  @Autowired
  private ScanAction _scanAction;

  @Uri(method = HttpMethod.GET, value = "/org/redpill/alfresco/clamav/handle", defaultFormat = "json")
  public Map<String, Object> handleNode(@RequestParam String nodeRef, @RequestParam String name, @RequestParam String virusName, WebScriptResponse response) {
    ParameterCheck.mandatoryString("nodeRef", nodeRef);
    ParameterCheck.mandatoryString("name", name);
    ParameterCheck.mandatoryString("virusName", virusName);

    _scanAction.handleNode(nodeRef, name, virusName);

    Map<String, Object> result = new HashMap<String, Object>();

    return result;
  }

}
