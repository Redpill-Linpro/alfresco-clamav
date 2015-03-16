package org.redpill.alfresco.acav.repo.node;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.version.VersionServicePolicies.AfterCreateVersionPolicy;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.transaction.TransactionService;
import org.redpill.alfresco.acav.repo.jobs.ClusteredExecuter;
import org.redpill.alfresco.acav.repo.service.ScanAction;
import org.redpill.alfresco.acav.repo.service.ScanService;
import org.redpill.alfresco.acav.repo.utils.ScanSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Niklas Ekman (niklas.ekman@gmail.com)
 */
@Component
public class VirusCheckerBehaviour implements OnCreateNodePolicy, AfterCreateVersionPolicy {

  private final static long DEFAULT_LOCK_TTL = 30000;

  @Autowired
  private ScanAction _scanAction;

  @Resource(name = "acav.daemonScanService")
  private ScanService _scanService;

  @Autowired
  private JobLockService _jobLockService;

  private long _lockTTL = DEFAULT_LOCK_TTL;

  @Autowired
  private RepositoryState _repositoryState;

  @Resource(name = "TransactionService")
  private TransactionService _transactionService;

  @Resource(name = "policyBehaviourFilter")
  private BehaviourFilter _behaviourFilter;

  @Autowired
  protected PolicyComponent _policyComponent;

  private static Boolean _initialized = false;

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode
   * (org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    checkForVirus(childAssocRef.getChildRef());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.alfresco.repo.version.VersionServicePolicies.AfterCreateVersionPolicy
   * #afterCreateVersion(org.alfresco.service.cmr.repository.NodeRef,
   * org.alfresco.service.cmr.version.Version)
   */
  @Override
  public void afterCreateVersion(final NodeRef versionableNode, Version version) {
    checkForVirus(versionableNode);
  }

  /**
   * Checks a nodeRef for viruses. If found, handles it too. Uses a clustered
   * executer that makes it cluster safe, i.e. only one node in the cluster will
   * check it.
   * 
   * @param nodeRef
   *          to check for virus.
   */
  private void checkForVirus(final NodeRef nodeRef) {
    final ClusteredExecuter executer = new ClusteredExecuter("VirusCheckerBehaviour") {

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

        _scanAction.removeScannedStuff(nodeRef);

        _scanAction.handleNode(nodeRef, scanSummary.getScannedList().get(0));
      }
    };

    executer.setJobLockService(_jobLockService);
    executer.setLockTTL(_lockTTL);
    executer.setRepositoryState(_repositoryState);
    executer.setTransactionService(_transactionService);

    _behaviourFilter.disableBehaviour();

    AuthenticationUtil.runAsSystem(new RunAsWork<Void>() {

      @Override
      public Void doWork() throws Exception {
        executer.execute();

        return null;
      }

    });
  }

  @PostConstruct
  public void postConstruct() {
    if (!_initialized) {
      _policyComponent.bindClassBehaviour(OnCreateNodePolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));

      _policyComponent.bindClassBehaviour(AfterCreateVersionPolicy.QNAME, ContentModel.TYPE_CONTENT, new JavaBehaviour(this, "afterCreateVersion", NotificationFrequency.TRANSACTION_COMMIT));

      _initialized = true;
    }
  }

}
