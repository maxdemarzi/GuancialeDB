package com.maxdemarzi;

import org.jooby.test.JoobyRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class NodeTest {

    private GuancialeDB db;

    @Rule
    public JoobyRule app = new JoobyRule(new Server());

    @Before
    public void setup() throws IOException {
        db = GuancialeDB.getInstance();
        db.addNode("emptyNode");
        db.addNode("singlePropertyNode", "props");
        HashMap<String, Object> props =  new HashMap<>();
        props.put("city", "Chicago");
        db.addNode("complexPropertiesNode", props);
    }

    @Test
    public void integrationTestGetNodeNotThere() {
        get("/db/node/notThere")
                .then()
                .assertThat()
                .body("$", equalTo(new HashMap<>()))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetEmptyNode() {
        get("/db/node/emptyNode")
                .then()
                .assertThat()
                .body(equalTo("\"\""))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetSinglePropertyNode() {
        get("/db/node/singlePropertyNode")
                .then()
                .assertThat()
                .body(equalTo("\"props\""))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetComplexPropertyNode() {
        HashMap<String, Object> props =  new HashMap<>();
        props.put("city", "Chicago");

        get("/db/node/complexPropertiesNode")
                .then()
                .assertThat()
                .body("$", equalTo(props))
                .statusCode(200)
                .contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestCreateEmptyNode() {
        given()
                .contentType("application/json").
        when()
                .post("/db/node/emptyNode").
        then()
                .assertThat()
                .statusCode(201);
    }

    @Test
    public void integrationTestCreateSinglePropertyNode() {
        given()
                .contentType("application/json")
                .body("[\"props\"]").
        when()
                .post("/db/node/singlePropertyNode").
        then()
                .assertThat()
                .statusCode(201);
    }

    @Test
    public void integrationTestCreateComplexPropertyNode() {
        HashMap<String, Object> props =  new HashMap<>();
        props.put("city", "Chicago");

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
