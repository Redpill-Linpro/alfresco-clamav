package org.redpill.alfresco.clamav.repo.service.impl;

import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;

import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.redpill.alfresco.clamav.repo.service.AcavNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:alfresco/subsystems/acav/default/acav-service-context.xml", "classpath:test-acav-service-context.xml" })
public class ScanServiceIntegrationTest {

  @Autowired
  private ScanServiceImpl _scanService;

  @Test
  public void testScanSucess() throws IOException {
    InputStream eicar1 = this.getClass().getResourceAsStream("/eicar.com");
    InputStream eicar2 = this.getClass().getResourceAsStream("/eicar.txt");
    InputStream eicar3 = this.getClass().getResourceAsStream("/eicarcom2.zip");

    AcavNodeService acavNodeService = Mockito.mock(AcavNodeService.class);
    LockService lockService = Mockito.mock(LockService.class);

    _scanService.setAcavNodeService(acavNodeService);
    _scanService.setLockService(lockService);

    final NodeRef rootNode = new NodeRef("workspace", "SpacesStore", "rootnodeid");

    when(acavNodeService.getRootNode()).thenReturn(rootNode);
    when(lockService.getLockStatus(rootNode)).thenReturn(LockStatus.NO_LOCK);

    Assert.assertTrue(_scanService.scanStream(eicar1) != null);
    Assert.assertTrue(_scanService.scanStream(eicar2) != null);
    Assert.assertFalse(_scanService.scanStream(eicar3) == null);
  }

}
