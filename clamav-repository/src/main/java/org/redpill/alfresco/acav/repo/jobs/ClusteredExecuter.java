package org.redpill.alfresco.acav.repo.jobs;

import org.alfresco.repo.admin.RepositoryState;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.redpill.alfresco.acav.repo.model.AcavModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public abstract class ClusteredExecuter {

  private static final Logger LOG = Logger.getLogger(ClusteredExecuter.class);

  protected static long DEFAULT_LOCK_TTL = 30000L;

  protected ThreadLocal<String> _lockThreadLocal = new ThreadLocal<String>();

  @Autowired
  protected JobLockService _jobLockService;

  protected long _lockTTL = DEFAULT_LOCK_TTL;

  @Autowired
  @Qualifier("TransactionService")
  protected TransactionService _transactionService;

  @Autowired
  protected RepositoryState _repositoryState;

  protected String _localName;

  public ClusteredExecuter() {
    _localName = this.getClass().getSimpleName();
  }

  public ClusteredExecuter(String localName) {
    _localName = localName;
  }

  public void execute() {
    // Bypass if the system is in read-only mode
    if (_transactionService.isReadOnly()) {
      LOG.debug(getJobName() + " bypassed; the system is read-only.");

      return;
    }

    // Bypass if the system is bootstrapping
    if (_repositoryState != null && _repositoryState.isBootstrapping()) {
      LOG.debug(getJobName() + " bypassed; the system is bootstrapping.");

      return;
    }

    // bypass if there already exists a lock
    if (hasLock()) {
      LOG.debug(getJobName() + " bypassed; a lock already exists.");

      return;
    }

    try {
      createLock();

      LOG.debug(getJobName() + " started.");

      refreshLock();

      executeInternal();

      // Done
      if (LOG.isDebugEnabled()) {
        LOG.debug("   " + getJobName() + " completed.");
      }
    } catch (LockAcquisitionException e) {
      // Job being done by another process
      if (LOG.isDebugEnabled()) {
        LOG.debug("   " + getJobName() + " already underway.");
      }
    } catch (VmShutdownListener.VmShutdownException e) {
      // Aborted
      if (LOG.isDebugEnabled()) {
        LOG.debug("   " + getJobName() + " aborted.");
      }
    } finally {
      try {
        releaseLock();
      } catch (Exception ex) {
        // just swallow the exception here...
      }
    }
  }

  /**
   * Attempts to get the lock. If it fails, the current transaction is marked
   * for rollback.
   */
  protected void refreshLock() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Refreshing lock");
    }
    String lockToken = _lockThreadLocal.get();

    if (StringUtils.isBlank(lockToken)) {
      throw new IllegalArgumentException("Must provide existing lockToken");
    }

    _jobLockService.refreshLock(lockToken, getLockQName(), _lockTTL);
  }

  protected void releaseLock() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Releasing lock");
    }
    String lockToken = _lockThreadLocal.get();

    if (StringUtils.isBlank(lockToken)) {
      throw new IllegalArgumentException("Must provide existing lockToken");
    }

    _jobLockService.releaseLock(lockToken, getLockQName());

    _lockThreadLocal.remove();
  }

  /**
   * Method for checking whether a lock exists or not. There are currently no
   * good ways of doing this, so a try...catch procedure is used.
   * 
   * @return true if a lock exists, false otherwise
   */
  protected boolean hasLock() {
    boolean hasLock = false;

    String lockToken = _lockThreadLocal.get();

    if (StringUtils.isBlank(lockToken)) {
      return false;
    }

    RetryingTransactionCallback<String> txnWork = new RetryingTransactionCallback<String>() {

      @Override
      public String execute() throws Exception {
        return _jobLockService.getLock(getLockQName(), _lockTTL);
      }

    };

    try {
      lockToken = _transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);
    } catch (Exception ex) {
      hasLock = true;
    } finally {
      // silently release the lock, ignoring all errors
      try {
        _jobLockService.releaseLock(lockToken, getLockQName());
      } catch (Exception ex) {
      }
    }

    return hasLock;
  }

  protected String createLock() {
    String lockToken = _lockThreadLocal.get();

    if (StringUtils.isBlank(lockToken)) {
      RetryingTransactionCallback<String> txnWork = new RetryingTransactionCallback<String>() {

        @Override
        public String execute() throws Exception {
          return _jobLockService.getLock(getLockQName(), _lockTTL);
        }

      };

      lockToken = _transactionService.getRetryingTransactionHelper().doInTransaction(txnWork, false, true);

      _lockThreadLocal.set(lockToken);
    }

    return lockToken;
  }

  protected abstract String getJobName();

  protected abstract void executeInternal();

  protected QName getLockQName() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Lock QName is: " + _localName);
    }

    return QName.createQName(AcavModel.ACAV_CORE_URI, _localName);
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

}
