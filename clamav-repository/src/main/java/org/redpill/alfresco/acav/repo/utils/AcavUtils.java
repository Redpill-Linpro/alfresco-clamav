package org.redpill.alfresco.acav.repo.utils;

public interface AcavUtils {

  /**
   * Updates the cron expression for the update virus database job
   */
  void updateCronExpression(String cronExpression);
  
  /**
   * Triggers a job which updates the virus database
   */
  void updateVirusDatabase();

}
