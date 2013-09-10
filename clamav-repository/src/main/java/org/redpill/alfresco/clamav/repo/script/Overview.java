package org.redpill.alfresco.clamav.repo.script;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import nl.runnable.alfresco.webscripts.annotations.Attribute;
import nl.runnable.alfresco.webscripts.annotations.Authentication;
import nl.runnable.alfresco.webscripts.annotations.AuthenticationType;
import nl.runnable.alfresco.webscripts.annotations.HttpMethod;
import nl.runnable.alfresco.webscripts.annotations.RequestParam;
import nl.runnable.alfresco.webscripts.annotations.Uri;
import nl.runnable.alfresco.webscripts.annotations.WebScript;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.CronTriggerBean;
import org.apache.commons.lang.StringUtils;
import org.quartz.CronTrigger;
import org.redpill.alfresco.clamav.repo.model.AcavModel;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Component;

@Component
@WebScript(description = "Gets ovewview information from the repository", families = { "Alfresco ClamAV" })
@Authentication(AuthenticationType.ADMIN)
public class Overview {

  @Autowired
  private AcavNodeService _acavNodeService;

  @Autowired
  private NodeService _nodeService;

  @Resource(name = "acav.updateVirusDatabaseServiceTrigger")
  private CronTriggerBean _updateCronTriggerBean;

  @Uri(method = HttpMethod.GET, value = "/org/redpill/alfresco/clamav/overview", defaultFormat = "json")
  public Map<String, Object> index(WebScriptResponse response) {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();
    NodeRef updateStatusNode = _acavNodeService.getUpdateStatusNode();

    Boolean enabled = (Boolean) _nodeService.getProperty(systemStatusNode, AcavModel.PROP_ENABLED);
    Date virusDefinitions = (Date) _nodeService.getProperty(updateStatusNode, AcavModel.PROP_LAST_UPDATE);
    Date lastScan = new Date();
    String status = (String) _nodeService.getProperty(systemStatusNode, AcavModel.PROP_STATUS);
    String updateCronExpression = (String) _nodeService.getProperty(updateStatusNode, AcavModel.PROP_UPDATE_CRON);

    Map<String, Object> model = new HashMap<String, Object>();

    model.put("enabled", enabled == null ? true : enabled.booleanValue());
    model.put("virus_definitions", virusDefinitions != null ? virusDefinitions.getTime() : 0);
    model.put("last_scan", lastScan != null ? lastScan.getTime() : 0);
    model.put("status", status);
    model.put("update_cron_expression", updateCronExpression);

    return model;
  }

  @Uri(method = HttpMethod.GET, value = "/org/redpill/alfresco/clamav/overview/empty", defaultFormat = "html")
  public Map<String, Object> empty(WebScriptResponse response) {
    return new HashMap<String, Object>();
  }

  @Uri(method = HttpMethod.POST, value = "/org/redpill/alfresco/clamav/overview/enable")
  public void enable(@Attribute final ResponseHelper responseHelper) {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    _nodeService.setProperty(systemStatusNode, AcavModel.PROP_ENABLED, true);

    responseHelper.redirectToService("/org/redpill/alfresco/clamav/overview/empty");
  }

  @Uri(method = HttpMethod.POST, value = "/org/redpill/alfresco/clamav/overview/disable")
  public void disable(@Attribute final ResponseHelper responseHelper) {
    NodeRef systemStatusNode = _acavNodeService.getSystemStatusNode();

    _nodeService.setProperty(systemStatusNode, AcavModel.PROP_ENABLED, false);

    responseHelper.redirectToService("/org/redpill/alfresco/clamav/overview/empty");
  }

  @Uri(method = HttpMethod.POST, value = "/org/redpill/alfresco/clamav/overview/savecron")
  public void saveCronExpression(@Attribute final ResponseHelper responseHelper, @RequestParam String cronExpression) {
    if (StringUtils.isBlank(cronExpression)) {
      responseHelper.redirectToService("/org/redpill/alfresco/clamav/overview/empty");
    }

    NodeRef updateStatusNode = _acavNodeService.getUpdateStatusNode();

    _nodeService.setProperty(updateStatusNode, AcavModel.PROP_UPDATE_CRON, cronExpression);

    _updateCronTriggerBean.setCronExpression(cronExpression);

    try {
      String jobName = _updateCronTriggerBean.getJobDetail().getName();
      String jobGroup = _updateCronTriggerBean.getJobDetail().getGroup();

      CronTrigger trigger = (CronTrigger) _updateCronTriggerBean.getTrigger();
      trigger.setJobName("kalle");

      _updateCronTriggerBean.destroy();

      _updateCronTriggerBean.getScheduler().rescheduleJob(jobName, jobGroup, trigger);
      _updateCronTriggerBean.afterPropertiesSet();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    responseHelper.redirectToService("/org/redpill/alfresco/clamav/overview/empty");
  }

  @Uri(method = HttpMethod.POST, value = "/org/redpill/alfresco/clamav/overview/update")
  public void update(@Attribute final ResponseHelper responseHelper) {
    try {
      String jobName = _updateCronTriggerBean.getJobDetail().getName();
      String jobGroup = _updateCronTriggerBean.getJobDetail().getGroup();
      _updateCronTriggerBean.getScheduler().triggerJob(jobName, jobGroup);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    responseHelper.redirectToService("/org/redpill/alfresco/clamav/overview/empty");
  }

  @Attribute
  protected ResponseHelper getResponseHelper(final WebScriptRequest request, final WebScriptResponse response) {
    return new ResponseHelper(request, response);
  }

}
