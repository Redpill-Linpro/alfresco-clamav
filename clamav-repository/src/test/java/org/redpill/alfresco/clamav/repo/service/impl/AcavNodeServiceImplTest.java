package org.redpill.alfresco.clamav.repo.service.impl;

import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class AcavNodeServiceImplTest {

  @Tested
  AcavNodeServiceImpl acavNodeService;

  @Injectable
  SearchService searchService;

  @Injectable
  NodeService nodeService;

  @Test
  public void getRootNode() {
    final NodeRef foobar = new NodeRef("workspace", "SpaceStore", "alfresco_clamav_root_node");

    new NonStrictExpectations() {

      ResultSet resultSet;
      ChildAssociationRef parent;
      ChildAssociationRef rootNode;

      {
        searchService.query((SearchParameters) any);
        returns(resultSet);

        resultSet.length();
        returns(0);

        nodeService.getPrimaryParent((NodeRef) any);
        returns(parent);

        parent.getQName();
        returns(ContentModel.ASSOC_CONTAINS);

        nodeService.createNode((NodeRef) any, ContentModel.ASSOC_CONTAINS, (QName) any, ContentModel.TYPE_FOLDER);
        returns(rootNode);

        rootNode.getChildRef();
        returns(foobar);
      }
    };

    NodeRef rootNode = acavNodeService.getRootNode();

    Assert.assertEquals(foobar.toString(), rootNode.toString());
  }
}
