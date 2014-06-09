package org.redpill.alfresco.clamav.repo.script;

import java.io.IOException;

import org.json.JSONObject;
import org.redpill.alfresco.clamav.repo.utils.JsonMessage;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WebScriptSession;
import org.springframework.util.StringUtils;

import com.github.dynamicextensionsalfresco.webscripts.resolutions.Resolution;

/**
 * Helper for handling responses.
 * 
 * @author Laurens Fridael
 * 
 */
class ResponseHelper {
  private final WebScriptRequest _request;

  private final WebScriptResponse _response;

  ResponseHelper(final WebScriptRequest request, final WebScriptResponse response) {
    _request = request;
    _response = response;
  }

  public void setFlashVariable(final String name, final Object value) {
    _request.getRuntime().getSession().setValue(name, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T getFlashVariable(final String name) {
    final WebScriptSession session = _request.getRuntime().getSession();
    final T value = (T) session.getValue(name);
    session.removeValue(name);
    return value;
  }

  public ResponseHelper status(final int status, final String message) throws IOException {
    _response.setStatus(status);
    if (StringUtils.hasText(message)) {
      _response.getWriter().write(message);
    }
    return this;
  }

  public ResponseHelper status(final int status) throws IOException {
    return status(status, null);
  }

  public ResponseHelper noCache() {
    _response.setHeader("Cache-Control", "no-cache, nostore");
    return this;
  }

  public Resolution returnEmptyJsonResult() {
    return new JsonMessage(new JSONObject(), 200);
  }

}
