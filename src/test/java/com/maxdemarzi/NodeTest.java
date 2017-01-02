package com.maxdemarzi;

import org.jooby.test.JoobyRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class NodeTest {

    private GuancialeDB db;

    @Rule
    public JoobyRule app = new JoobyRule(new Server());

    @Before
    public void setup() throws IOException {
        db = GuancialeDB.getInstance();
        db.addNode("emptyNode");
        HashMap<String, Object> prop =  new HashMap<>();
        prop.put("property", "Value");
        db.addNode("singlePropertyNode", prop);
        HashMap<String, Object> props =  new HashMap<>();
        props.put("city", "Chicago");
        props.put("prop", prop);
        db.addNode("complexPropertiesNode", props);
    }

    @Test
    public void integrationTestGetNodeNotThere() {
        when().
                get("/db/node/notThere").
        then().
                assertThat()
                .body("$", equalTo(new HashMap<>()))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetEmptyNode() {
        when().
                get("/db/node/emptyNode").
        then().
                assertThat()
                .body(equalTo("{}"))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetSinglePropertyNode() {
        HashMap<String, Object> prop =  new HashMap<>();
        prop.put("property", "Value");

        when().
                get("/db/node/singlePropertyNode").
        then().
                assertThat()
                .body("$", equalTo(prop))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetComplexPropertyNode() {
        HashMap<String, Object> prop =  new HashMap<>();
        prop.put("property", "Value");

        HashMap<String, Object> props =  new HashMap<>();
        props.put("city", "Chicago");
        props.put("prop", prop);

        when().
                get("/db/node/complexPropertiesNode").
        then().
                assertThat()
                .body("$", equalTo(props))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestCreateEmptyNode() {
        given().
                contentType("application/json").
                body("{}").
        when().
                post("/db/node/emptyNode").
        then().
                assertThat().
                statusCode(201);
    }

    @Test
    public void integrationTestCreateSinglePropertyNode() {
        HashMap<String, Object> prop =  new HashMap<>();
        prop.put("property", "Value");

        given().
                contentType("application/json").
                body(prop).
        when().
                post("/db/node/singlePropertyNode").
        then().
                assertThat().
                statusCode(201);
    }

    @Test
    public void integrationTestCreateComplexPropertyNode() {
        HashMap<String, Object> prop =  new HashMap<>();
        prop.put("property", "Value");

        HashMap<String, Object> props =  new HashMap<>();
        props.put("city", "Chicago");
        props.put("prop", prop);

        given()
                .contentType("application/json")
                .body(props).
        when()
                .post("/db/node/complexPropertiesNode").
        then()
                .assertThat()
                .statusCode(201);
    }
}
