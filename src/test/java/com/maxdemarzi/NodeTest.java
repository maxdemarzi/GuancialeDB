package com.maxdemarzi;

import org.jooby.test.JoobyRule;
import org.jooby.test.MockRouter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static io.restassured.RestAssured.get;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

public class NodeTest {

    @Rule
    public static JoobyRule app = new JoobyRule(new Server());

    @Before

    @Test
    public void integrationTest() {
        get("/db/node/max")
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
