package org.redpill.alfresco.acav.test.repo.ft;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.RestAssured.preemptive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.redpill.alfresco.test.AbstractRepoFunctionalTest;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.parsing.Parser;
import com.jayway.restassured.response.Response;

public class HandleGetFunctionalTest extends AbstractRepoFunctionalTest {

  private final static Logger LOG = Logger.getLogger(HandleGetFunctionalTest.class);

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
  public void testHandleGet() throws JSONException {
    String site = "testite_" + System.currentTimeMillis();
    String username = "testuser_" + System.currentTimeMillis();
    String password = "testuser";

    Map<String, String> properties = new HashMap<String, String>();
    properties.put("vgr:responsibility_code", "Foobar");
    
    List<String> groups = new ArrayList<>();

    try {
      createUser(username, password, "Test", "Perfect", "test@perfect.com", properties, groups);
      
      RestAssured.authentication = preemptive().basic(username, password);

      createSite(site);

      RestAssured.requestContentType(ContentType.JSON);
      RestAssured.responseContentType(ContentType.JSON);

      String nodeRef = uploadDocument("eicar.com", site);
      
      RestAssured.authentication = preemptive().basic("admin", "admin");

      Response response = given()
          .baseUri(getBaseUri())
          .pathParam("nodeRef", nodeRef)
          .pathParam("name", "eicar.com")
          .pathParam("virusName", "eicar")
          .expect().statusCode(200)
          .when().get("/org/redpill/alfresco/acav/handle?nodeRef={nodeRef}&name={name}&virusName={virusName}");

      if (LOG.isDebugEnabled()) {
        response.prettyPrint();
      }
    } catch (Throwable ex) {
      ex.printStackTrace();
    } finally {
      deleteSite(site);

      RestAssured.authentication = preemptive().basic("admin", "admin");

      deleteUser(username);
    }
  }
  
  @Override
  protected void deleteSite(String shortName) {
    try {
      super.deleteSite(shortName);
    } catch (Throwable ex) {
    }
  }
  
  @Override
  protected void deleteUser(String username) {
    try {
      super.deleteUser(username);
    } catch (Throwable ex) {
    }
  }

}
