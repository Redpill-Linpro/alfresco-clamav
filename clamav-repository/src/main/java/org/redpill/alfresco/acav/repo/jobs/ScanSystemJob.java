package org.redpill.alfresco.acav.repo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ScanSystemJob implements Job {

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    final ClusteredExecuter scanSystemService = (ClusteredExecuter) context.getJobDetail().getJobDataMap().get("scanSystemService");

    scanSystemService.execute();
  }

}
