package org.redpill.alfresco.clamav.repo.jobs;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.redpill.alfresco.clamav.repo.service.UpdateVirusDatabaseService;

public class UpdateVirusDatabaseJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    final UpdateVirusDatabaseService updateVirusDatabaseService = (UpdateVirusDatabaseService) context.getJobDetail().getJobDataMap().get("updateVirusDatabaseService");

    AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        updateVirusDatabaseService.updateDatabase();

        return null;
      }
    });
  }

}
