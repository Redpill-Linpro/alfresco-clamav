package org.redpill.alfresco.clamav.repo.jobs;

import java.util.List;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.util.ParameterCheck;
import org.redpill.alfresco.clamav.repo.service.ScanAction;
import org.redpill.alfresco.clamav.repo.service.ScanService;
import org.redpill.alfresco.clamav.repo.utils.ScanResult;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;

public class ScanSystemService extends ClusteredExecuter {

  private ScanService _scanService;

  private ScanAction _scanAction;

  @Override
  protected String getJobName() {
    return "ScanSystemService";
  }

  @Override
  protected void executeInternal() {
    final List<ScanSummary> scanSummaryList = AuthenticationUtil.runAsSystem(new RunAsWork<List<ScanSummary>>() {

      @Override
      public List<ScanSummary> doWork() throws Exception {
        return _scanService.scanSystem();
      }

    });

    RetryingTransactionCallback<Void> callback = new RetryingTransactionCallback<Void>() {

      @Override
      public Void execute() throws Throwable {
        return AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

          @Override
          public Void doWork() throws Exception {

            for (ScanSummary scanSummary : scanSummaryList) {
              for (ScanResult scanResult : scanSummary.getInfectedList()) {
                _scanAction.handleNode(scanResult.getNodeRef(), scanResult);
              }
            }

            return null;
          }

        });
      }
    };

    _transactionService.getRetryingTransactionHelper().doInTransaction(callback, false, true);
  }

  public void setScanService(ScanService scanService) {
    _scanService = scanService;
  }

  public void setScanAction(ScanAction scanAction) {
    _scanAction = scanAction;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    super.afterPropertiesSet();

    ParameterCheck.mandatory("scanService", _scanService);
    ParameterCheck.mandatory("scanAction", _scanAction);
  }

}
