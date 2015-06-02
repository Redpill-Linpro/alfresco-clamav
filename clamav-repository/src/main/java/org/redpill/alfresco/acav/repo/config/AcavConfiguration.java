package org.redpill.alfresco.acav.repo.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.util.CronTriggerBean;
import org.alfresco.util.exec.RuntimeExec;
import org.apache.commons.lang.StringUtils;
import org.quartz.Scheduler;
import org.redpill.alfresco.acav.repo.jobs.ScanSystemJob;
import org.redpill.alfresco.acav.repo.jobs.ScanSystemService;
import org.redpill.alfresco.acav.repo.jobs.UpdateVirusDatabaseJob;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.redpill.alfresco.acav.repo.service.AcavNodeService;
import org.redpill.alfresco.acav.repo.service.SystemScanDirectoryRegistry;
import org.redpill.alfresco.acav.repo.service.UpdateVirusDatabaseService;
import org.redpill.alfresco.acav.repo.utils.SystemScanDirectoryRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.quartz.JobDetailBean;

import com.philvarner.clamavj.ClamScan;

@Configuration
public class AcavConfiguration {

  public static final String DEFAULT_UPDATE_CRON_EXPRESSION = "0 0 1 1/1 * ? *";

  @Value("${dir.contentstore}")
  private File _dirContentStore;

  @Value("${clamscan.exe}")
  private String _clamscanExe;

  @Value("${freshclam.exe}")
  private String _freshclamExe;

  @Autowired
  private SystemScanDirectoryRegistry _systemScanDirectoryRegistry;

  @Autowired
  private UpdateVirusDatabaseService _updateVirusDatabaseService;

  @Autowired
  @Qualifier("schedulerFactory")
  private Scheduler _scheduler;

  @Autowired
  private ScanSystemService _scanSystemService;

  @Autowired
  private AcavNodeService _acavNodeService;

  @Autowired
  @Qualifier("NodeService")
  private NodeService _nodeService;

  @Bean(name = "acav.systemScanDirectoryContentStore")
  public SystemScanDirectoryRegister firstRegister() {
    SystemScanDirectoryRegister register = new SystemScanDirectoryRegister();

    register.setSystemScanDirectory(_dirContentStore);
    register.setSystemScanDirectoryRegistry(_systemScanDirectoryRegistry);

    return register;
  }

  @Bean(name = "acav.systemScanDirectoryTemporary")
  public SystemScanDirectoryRegister secondRegister() {
    SystemScanDirectoryRegister register = new SystemScanDirectoryRegister();

    register.setSystemScanDirectory(new File("/tmp"));
    register.setSystemScanDirectoryRegistry(_systemScanDirectoryRegistry);

    return register;
  }

  @Bean(name = "acav.scanCommand")
  public RuntimeExec scanCommand() {
    RuntimeExec scanCommand = new RuntimeExec();

    String[] parameters = { _clamscanExe, "--tempdir=${tempdir}", "--log=${logfile}", "SPLIT:${options}", "${file_to_scan}" };

    Map<String, String[]> commandsByOS = new HashMap<String, String[]>();
    commandsByOS.put(".*", parameters);

    scanCommand.setCommandsAndArguments(commandsByOS);

    return scanCommand;
  }

  @Bean(name = "acav.scanCheckCommand")
  public RuntimeExec scanCheckCommand() {
    RuntimeExec scanCheckCommand = new RuntimeExec();

    String[] parameters = { _clamscanExe, "--version" };

    Map<String, String[]> commandsByOS = new HashMap<String, String[]>();
    commandsByOS.put(".*", parameters);

    scanCheckCommand.setCommandsAndArguments(commandsByOS);

    return scanCheckCommand;
  }

  @Bean(name = "acav.updateVirusDatabaseCommand")
  public RuntimeExec updateVirusDatabaseCommand() {
    RuntimeExec updateVirusDatabaseCommand = new RuntimeExec();

    String[] parameters = { _freshclamExe };

    Map<String, String[]> commandsByOS = new HashMap<String, String[]>();
    commandsByOS.put(".*", parameters);

    updateVirusDatabaseCommand.setCommandsAndArguments(commandsByOS);

    return updateVirusDatabaseCommand;
  }

  @Bean(name = "acav.updateVirusDatabaseCheckCommand")
  public RuntimeExec updateVirusDatabaseCheckCommand() {
    RuntimeExec updateVirusDatabaseCheckCommand = new RuntimeExec();

    String[] parameters = { _freshclamExe, "--version" };

    Map<String, String[]> commandsByOS = new HashMap<String, String[]>();
    commandsByOS.put(".*", parameters);

    updateVirusDatabaseCheckCommand.setCommandsAndArguments(commandsByOS);

    return updateVirusDatabaseCheckCommand;
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Bean(name = "acav.updateVirusDatabaseServiceJobDetail")
  public JobDetailBean updateVirusDatabaseServiceJobDetail() {
    JobDetailBean detail = new JobDetailBean();

    Map jobDataAsMap = new HashMap();
    jobDataAsMap.put("updateVirusDatabaseService", _updateVirusDatabaseService);

    detail.setJobClass(UpdateVirusDatabaseJob.class);
    detail.setJobDataAsMap(jobDataAsMap);

    return detail;
  }

  @Bean(name = "acav.updateVirusDatabaseServiceTrigger")
  @DependsOn({ "acav.dictionaryBootstrap", "acav.acavNodeService" })
  public CronTriggerBean updateVirusDatabaseServiceTrigger() {
    return AuthenticationUtil.runAsSystem(new RunAsWork<CronTriggerBean>() {

      @Override
      public CronTriggerBean doWork() throws Exception {
        NodeRef updateStatusNode = _acavNodeService.getUpdateStatusNode();

        String cronExpression = (String) _nodeService.getProperty(updateStatusNode, AcavModel.PROP_UPDATE_CRON);

        if (StringUtils.isBlank(cronExpression)) {
          cronExpression = DEFAULT_UPDATE_CRON_EXPRESSION;

          _nodeService.setProperty(updateStatusNode, AcavModel.PROP_UPDATE_CRON, cronExpression);
        }

        CronTriggerBean trigger = new CronTriggerBean();

        trigger.setJobDetail(updateVirusDatabaseServiceJobDetail());
        trigger.setScheduler(_scheduler);
        trigger.setCronExpression(cronExpression);

        return trigger;
      }

    });
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Bean(name = "acav.scanSystemServiceJobDetail")
  public JobDetailBean scanSystemServiceJobDetail() {
    JobDetailBean detail = new JobDetailBean();

    Map jobDataAsMap = new HashMap();
    jobDataAsMap.put("scanSystemService", _scanSystemService);

    detail.setJobClass(ScanSystemJob.class);
    detail.setJobDataAsMap(jobDataAsMap);

    return detail;
  }

  @Bean(name = "acav.scanSystemServiceTrigger")
  public CronTriggerBean scanSystemServiceTrigger() {
    CronTriggerBean trigger = new CronTriggerBean();

    trigger.setJobDetail(scanSystemServiceJobDetail());
    trigger.setScheduler(_scheduler);
    trigger.setCronExpression("0 0 2 1/1 * ? *");

    return trigger;
  }

  @Bean(name = "acav.clamScan")
  public ClamScan clamScan() {
    ClamScan clamScan = new ClamScan();

    clamScan.setHost("127.0.0.1");
    clamScan.setPort(3310);
    clamScan.setTimeout(60000);

    return clamScan;
  }

}
