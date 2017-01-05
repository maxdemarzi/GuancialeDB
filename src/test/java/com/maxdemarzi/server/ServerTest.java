package com.maxdemarzi.server;

import com.maxdemarzi.server.Server;
import org.jooby.test.JoobyRule;
import org.jooby.test.MockRouter;
import org.junit.ClassRule;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

/**
 * @author jooby generator
 */
public class ServerTest {

  /**
   * One app/server for all the test of this class. If you want to start/stop a new server per test,
   * remove the static modifier and replace the {@link ClassRule} annotation with {@link Rule}.
   */
  @ClassRule
  public static JoobyRule app = new JoobyRule(new Server());

  @Test
  public void integrationTest() {
    get("/")
        .then()
        .assertThat()
        .body(equalTo("Hello World!"))
        .statusCode(200)
        .contentType("text/html;charset=UTF-8");
  }

  @Test
  public void unitTest() throws Throwable {
    String result = new MockRouter(new Server())
        .get("/");

    assertEquals("Hello World!", result);
  }

}
