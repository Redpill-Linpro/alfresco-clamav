package org.redpill.alfresco.clamav.repo.node;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.version.VersionServicePolicies.AfterCreateVersionPolicy;
import org.alfresco.repo.version.VersionServicePolicies.BeforeCreateVersionPolicy;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.transaction.TransactionService;
import org.redpill.alfresco.clamav.repo.jobs.ClusteredExecuter;
import org.redpill.alfresco.clamav.repo.service.ScanAction;
import org.redpill.alfresco.clamav.repo.service.ScanService;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.springframework.beans.factory.InitializingBean;

public class VirusCheckerBehaviour implements OnCreateNodePolicy, AfterCreateVersionPolicy, BeforeCreateVersionPolicy, InitializingBean {

  private PolicyComponent _policyComponent;

  private ScanAction _scanAction;

  private ScanService _scanService;

  private JobLockService _jobLockService;

  private long _lockTTL;

  private RepositoryState _repositoryState;

  private TransactionService _transactionService;

  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(ChildAssociationRef childAssocRef) {
    checkForVirus(childAssocRef.getChildRef());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.repo.version.VersionServicePolicies.AfterCreateVersionPolicy#afterCreateVersion(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.version.Version)
   */
  @Override
  public void afterCreateVersion(NodeRef versionableNode, Version version) {
    checkForVirus(versionableNode);
  }

  @Override
  public void beforeCreateVersion(final NodeRef versionableNode) {
    ClusteredExecuter executer = new ClusteredExecuter("VirusCheckerBehaviour") {

      @Override
      protected String getJobName() {
        return "VirusCheckerBehaviour.beforeCreateVersion";
      }

      @Override
      protected void executeInternal() {
        _scanAction.removeScannedStuff(versionableNode);
      }
    };

    executer.setJobLockService(_jobLockService);
    executer.setLockTTL(_lockTTL);
    executer.setRepositoryState(_repositoryState);
    executer.setTransactionService(_transactionService);

    executer.execute();
  }

  /**
   * Checks a nodeRef for viruses. If found, handles it too. Uses a clustered executer that makes it cluster safe, i.e. only one node in the cluster will check it.
   * 
   * @param nodeRef
   *          to check for virus.
   */
  private void checkForVirus(final NodeRef nodeRef) {
    ClusteredExecuter executer = new ClusteredExecuter("VirusCheckerBehaviour") {

      @Override
      protected String getJobName() {
        return "VirusCheckerBehaviour.checkForVirus";
      }

      @Override
      protected void executeInternal() {
        ScanSummary scanSummary;

        try {
          scanSummary = _scanService.scanNode(nodeRef);
        } catch (Exception ex) {
          ex.printStackTrace();
          return;
        }

        if (scanSummary == null) {
          return;
        }

        _scanAction.handleNode(nodeRef, scanSummary.getScannedList().get(0));
      }
    };

    executer.setJobLockService(_jobLockService);
    executer.setLockTTL(_lockTTL);
    executer.setRepositoryState(_repositoryState);
    executer.setTransactionService(_transactionService);

    executer.execute();
  }

  public void setPolicyComponent(PolicyComponent policyComponent) {
    _policyComponent = policyComponent;
  }

  public void setScanService(ScanService scanService) {
    _scanService = scanService;
  }

  public void setScanAction(ScanAction scanAction) {
    _scanAction = scanAction;
  }

  public void setJobLockService(JobLockService jobLockService) {
    _jobLockService = jobLockService;
  }

  public void setLockTTL(long lockTTL) {
    _lockTTL = lockTTL;
  }

  public void setRepositoryState(RepositoryState repositoryState) {
    _repositoryState = repositoryState;
  }

  public void setTransactionService(TransactionService transactionService) {
    _transactionService = transactionService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    _policyComponent.bindClassBehaviour(AfterCreateVersionPolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "afterCreateVersion", NotificationFrequency.TRANSACTION_COMMIT));
    _policyComponent.bindClassBehaviour(BeforeCreateVersionPolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "beforeCreateVersion", NotificationFrequency.TRANSACTION_COMMIT));
  }

}
