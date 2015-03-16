package org.redpill.alfresco.acav.repo.service;

import java.util.List;

public interface NodeDao {

  List<Integer> selectByContentUrls(List<String> contentUrls);

}
