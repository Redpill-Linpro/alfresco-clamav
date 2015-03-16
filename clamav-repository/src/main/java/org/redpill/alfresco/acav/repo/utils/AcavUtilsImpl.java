package org.redpill.alfresco.acav.repo.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Resource;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.util.CronTriggerBean;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.io.IOUtils;
import org.quartz.CronTrigger;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.AcavNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("acav.acavUtils")
public class AcavUtilsImpl implements AcavUtils {

  @Autowired
  private NodeService _nodeService;

  @Autowired
  private AcavNodeService _acavNodeService;

  @Resource(name = "acav.updateVirusDatabaseServiceTrigger")
  private CronTriggerBean _updateCronTriggerBean;

  public static void closeQuietly(ResultSet resultSet) {
    try {
      resultSet.close();
    } catch (Exception ex) {
      // just swallow...
    }
  }

  public static File copy(InputStream inputStream) {
    ParameterCheck.mandatory("inputStream", inputStream);

    File tempFile = TempFileProvider.createTempFile("acav_tempfile_", ".tmp");

    OutputStream outputStream = null;

    try {
      outputStream = new FileOutputStream(tempFile);

      IOUtils.copy(inputStream, outputStream);

      return tempFile;
    } catch (Exception ex) {
      tempFile.delete();

      throw new AlfrescoRuntimeException(ex.getMessage(), ex);
    } finally {
      IOUtils.closeQuietly(inputStream);
      IOUtils.closeQuietly(outputStream);
    }
  }

  @Override
  public void updateCronExpression(String cronExpression) {
    ParameterCheck.mandatory("cronExpression", cronExpression);

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
  }

  @Override
  public void updateVirusDatabase() {
    try {
      String jobName = _updateCronTriggerBean.getJobDetail().getName();
      String jobGroup = _updateCronTriggerBean.getJobDetail().getGroup();
      
      _updateCronTriggerBean.getScheduler().triggerJob(jobName, jobGroup);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }    
  }

}
