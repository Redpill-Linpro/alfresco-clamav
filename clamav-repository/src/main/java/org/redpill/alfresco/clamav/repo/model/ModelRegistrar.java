package org.redpill.alfresco.clamav.repo.model;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;

import nl.runnable.alfresco.models.RepositoryModelRegistrar;

@ManagedBean("modelRegistrar")
public class ModelRegistrar extends RepositoryModelRegistrar {

  @PostConstruct
  public void postConstruct() {
    registerModels();
  }

}