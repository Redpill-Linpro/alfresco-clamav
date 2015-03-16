package org.redpill.alfresco.acav.test.repo.ft;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.preemptive;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;

public class EmptyGetFunctionalTest extends AbstractRepoFunctionalTest {
  
  private final static Logger LOG = Logger.getLogger(EmptyGetFunctionalTest.class);

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
    RestAssured.requestContentType(ContentType.JSON);
    RestAssured.responseContentType(ContentType.JSON);
    
    Response response = given()
        .baseUri(getBaseUri())
        .expect().statusCode(200)
        .expect()
        .when().get("/org/redpill/alfresco/acav/overview/empty");
    
    if (LOG.isDebugEnabled()) {
      response.prettyPrint();
    }
  }

}
