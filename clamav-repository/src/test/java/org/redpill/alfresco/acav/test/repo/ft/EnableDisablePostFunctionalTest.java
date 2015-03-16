package org.redpill.alfresco.acav.test.repo.ft;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.preemptive;
import static org.hamcrest.Matchers.equalTo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;

public class EnableDisablePostFunctionalTest extends AbstractAcavFunctionalTest {
  
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

      disableAcav();

      given()
          .baseUri(getBaseUri())
          .expect().statusCode(200)
          .expect().body("antivirus.enabled", equalTo(false))
          .when().get("/org/redpill/alfresco/acav/overview");

      enableAcav();

      given()
          .baseUri(getBaseUri())
          .expect().statusCode(200)
          .expect().body("antivirus.enabled", equalTo(true))
          .when().get("/org/redpill/alfresco/acav/overview");
    } catch (Throwable ex) {
      ex.printStackTrace();
    } finally {
      deleteSite(site);
    }
  }

}
