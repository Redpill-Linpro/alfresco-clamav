package org.redpill.alfresco.clamav.repo.script;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.WebScriptSession;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

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

  public ResponseHelper redirectToService(String path) {
    Assert.hasText(path);
    if (path.startsWith("/") == false) {
      path = "/" + path;
    }
    _response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY); // 302
    _response.setHeader("Location", _request.getServiceContextPath() + path);
    return this;
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

}
