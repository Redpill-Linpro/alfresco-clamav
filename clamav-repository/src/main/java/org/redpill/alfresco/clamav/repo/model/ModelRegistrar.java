package org.redpill.alfresco.clamav.repo.model;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.dynamicextensionsalfresco.models.M2ModelListFactoryBean;
import com.github.dynamicextensionsalfresco.models.M2ModelResource;
import com.github.dynamicextensionsalfresco.models.RepositoryModelRegistrar;

@Component("modelRegistrar")
public class ModelRegistrar extends RepositoryModelRegistrar implements InitializingBean {

  @Autowired
  M2ModelListFactoryBean _modelListFactoryBean;

  @Override
  public void afterPropertiesSet() throws Exception {
    List<M2ModelResource> m2ModelResources = _modelListFactoryBean.getObject();

    setModels(m2ModelResources);

    registerModels();
  }

}