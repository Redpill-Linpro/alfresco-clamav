package org.redpill.alfresco.acav.test.repo.ft;

import static com.jayway.restassured.RestAssured.given;

import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

public abstract class AbstractAcavFunctionalTest extends AbstractRepoFunctionalTest {

  public void enableAcav() {
    given()
      .baseUri(getBaseUri())
      .expect().statusCode(200)
      .when().post("/org/redpill/alfresco/acav/overview/enable");
  }
  
  public void disableAcav() {
    given()
      .baseUri(getBaseUri())
      .expect().statusCode(200)
      .when().post("/org/redpill/alfresco/acav/overview/disable");
  }
  
}
