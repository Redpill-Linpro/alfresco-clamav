package org.redpill.alfresco.clamav.repo.node;

import javax.annotation.Resource;

import nl.runnable.alfresco.behaviours.annotations.Behaviour;
import nl.runnable.alfresco.behaviours.annotations.Event;

import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.version.VersionServicePolicies.AfterCreateVersionPolicy;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.transaction.TransactionService;
import org.redpill.alfresco.clamav.repo.jobs.ClusteredExecuter;
import org.redpill.alfresco.clamav.repo.service.ScanAction;
import org.redpill.alfresco.clamav.repo.service.ScanService;
import org.redpill.alfresco.clamav.repo.utils.ScanSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author niklas
 * 
 */
@Component
@Behaviour(value = { "cm:content" }, event = Event.COMMIT)
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

  @Autowired
  private TransactionService _transactionService;

  @Resource(name = "policyBehaviourFilter")
  private BehaviourFilter _behaviourFilter;

  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
   */
  @Override
  public void onCreateNode(final ChildAssociationRef childAssocRef) {
    checkForVirus(childAssocRef.getChildRef());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.alfresco.repo.version.VersionServicePolicies.AfterCreateVersionPolicy#afterCreateVersion(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.version.Version)
   */
  @Override
  public void afterCreateVersion(final NodeRef versionableNode, Version version) {
    checkForVirus(versionableNode);
  }

  // @Override
  // public void beforeCreateVersion(final NodeRef versionableNode) {
  // ClusteredExecuter executer = new ClusteredExecuter("VirusCheckerBehaviour") {
  //
  // @Override
  // protected String getJobName() {
  // return "VirusCheckerBehaviour.beforeCreateVersion";
  // }
  //
  // @Override
  // protected void executeInternal() {
  // _scanAction.removeScannedStuff(versionableNode);
  // }
  // };
  //
  // executer.setJobLockService(_jobLockService);
  // executer.setLockTTL(_lockTTL);
  // executer.setRepositoryState(_repositoryState);
  // executer.setTransactionService(_transactionService);
  //
  // executer.execute();
  // }

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

        _scanAction.removeScannedStuff(nodeRef);

        _scanAction.handleNode(nodeRef, scanSummary.getScannedList().get(0));
      }
    };

    executer.setJobLockService(_jobLockService);
    executer.setLockTTL(_lockTTL);
    executer.setRepositoryState(_repositoryState);
    executer.setTransactionService(_transactionService);

    _behaviourFilter.disableBehaviour();

    executer.execute();
  }

}
