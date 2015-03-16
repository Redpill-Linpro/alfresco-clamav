package org.redpill.alfresco.acav.test.repo.ft;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.preemptive;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;

public class UpdateFunctionalTest extends AbstractAcavFunctionalTest {
  
  private final static Logger LOG = Logger.getLogger(UpdateFunctionalTest.class);

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
  public void testUpdatePost() {
    String site = "testite_" + System.currentTimeMillis();
    
    createSite(site);
    
    try {
      RestAssured.requestContentType(ContentType.JSON);
      RestAssured.responseContentType(ContentType.JSON);
      
      enableAcav();
      
      Response response = given()
          .baseUri(getBaseUri())
          .expect().statusCode(307)
          .expect()
          .when().post("/org/redpill/alfresco/acav/overview/update");

      if (LOG.isDebugEnabled()) {
        response.prettyPrint();
      }
    } catch (Throwable ex) {
      ex.printStackTrace();
    } finally {
      deleteSite(site);
    }
  }

}
