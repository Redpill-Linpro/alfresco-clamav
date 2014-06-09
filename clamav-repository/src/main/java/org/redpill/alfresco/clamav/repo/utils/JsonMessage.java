package org.redpill.alfresco.clamav.repo.utils;

import org.json.JSONObject;

import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebScriptRequest;
import com.github.dynamicextensionsalfresco.webscripts.AnnotationWebscriptResponse;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.JsonResolution;
import com.github.dynamicextensionsalfresco.webscripts.resolutions.ResolutionParameters;

public class JsonMessage extends JsonResolution {

  private final JSONObject _jsonObject;

  public JsonMessage(JSONObject jsonObject, Integer status) {
    super(status);

    _jsonObject = jsonObject;
  }

  @Override
  public void resolve(AnnotationWebScriptRequest request, AnnotationWebscriptResponse response, ResolutionParameters params) {
    try {
      super.resolve(request, response, params);

      response.getWriter().append(_jsonObject.toString(2));
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
