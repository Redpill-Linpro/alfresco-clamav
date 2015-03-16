package org.redpill.alfresco.acav.repo.service.impl;

import java.util.List;

import org.mybatis.spring.SqlSessionTemplate;
import org.redpill.alfresco.acav.repo.service.NodeDao;
import org.springframework.stereotype.Component;

@Component
public class NodeDaoImpl implements NodeDao {

  private static final String SELECT_NODES_BY_CONTENT_URLS = "acav.node.select_GetNodesByContentUrl";

  private SqlSessionTemplate _sqlSessionTemplate;

  @Override
  public List<Integer> selectByContentUrls(List<String> contentUrls) {
    if (contentUrls == null) {
      return null;
    }

    if (contentUrls.size() == 0) {
      return null;
    }

    @SuppressWarnings("unchecked")
    List<Integer> result = (List<Integer>) _sqlSessionTemplate.selectList(SELECT_NODES_BY_CONTENT_URLS, contentUrls);

    return result;
  }

}
