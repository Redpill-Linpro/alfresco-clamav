package org.redpill.alfresco.clamav.repo.service.impl;

import static org.mockito.Mockito.when;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/subsystems/acav/default/acav-service-context.xml", "classpath:test-acav-service-context.xml" })
public class UpdateVirusDatabaseServiceIntegrationTest {

  @Autowired
  private UpdateVirusDatabaseServiceImpl updateVirusDatabaseService;

  @Test
  public void update() {
    AcavNodeService acavNodeService = Mockito.mock(AcavNodeService.class);
    LockService lockService = Mockito.mock(LockService.class);

    updateVirusDatabaseService.setAcavNodeService(acavNodeService);
    updateVirusDatabaseService.setLockService(lockService);

    final NodeRef rootNode = new NodeRef("workspace", "SpacesStore", "rootnodeid");

    when(acavNodeService.getRootNode()).thenReturn(rootNode);
    when(lockService.getLockStatus(rootNode)).thenReturn(LockStatus.NO_LOCK);

    updateVirusDatabaseService.updateDatabase();
  }

}
