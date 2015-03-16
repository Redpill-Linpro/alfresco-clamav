package org.redpill.alfresco.acav.test.repo.ft;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;

public class SaveCronPostFunctionalTest extends AbstractRepoFunctionalTest {
  
  @Before
  public void setUp() {
    RestAssured.defaultParser = Parser.JSON;
    RestAssured.authentication = preemptive().basic("admin", "admin");
    // RestAssured.proxy("localhost", 8888);
  }

  @After
  public void tearDown() {
    RestAssured.reset();
  }

  @Test
  public void testDirectoryGet() {
    String site = "testite_" + System.currentTimeMillis();
    
    createSite(site);
    
    try {
      RestAssured.requestContentType(ContentType.JSON);
      RestAssured.responseContentType(ContentType.JSON);
      
      String cronExpression1 = "0 0 1 1/1 * ? *";
      String cronExpression2 = "0 0 2 1/1 * ? *";
      
      given()
          .baseUri(getBaseUri())
          .pathParam("cronExpression", cronExpression1)
          .expect().statusCode(307)
          .expect()
          .when().post("/org/redpill/alfresco/acav/overview/savecron?cronExpression={cronExpression}");

      given()
          .baseUri(getBaseUri())
          .expect().statusCode(200)
          .expect().body("update.cron_expression", equalTo(cronExpression1))
          .when().get("/org/redpill/alfresco/acav/overview");

      given()
          .baseUri(getBaseUri())
          .pathParam("cronExpression", cronExpression2)
          .expect().statusCode(307)
          .expect()
          .when().post("/org/redpill/alfresco/acav/overview/savecron?cronExpression={cronExpression}");

      given()
          .baseUri(getBaseUri())
          .expect().statusCode(200)
          .expect().body("update.cron_expression", equalTo(cronExpression2))
          .when().get("/org/redpill/alfresco/acav/overview");
    } catch (Throwable ex) {
      ex.printStackTrace();
    } finally {
      deleteSite(site);
    }
  }

}
