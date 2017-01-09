package com.maxdemarzi.server;

import com.maxdemarzi.GuancialeDB;
import org.jooby.test.JoobyRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class NodePropertiesTest {
    private GuancialeDB db;

    @Rule
    public JoobyRule app = new JoobyRule(new Server());

    @Before
    public void setup() throws IOException {
        db = GuancialeDB.getInstance();
        db.clear();

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Max");
        properties.put("age", 37);

        db.addNode("node1", properties);
        db.addNode("node2");
        db.addNode("node3");
        db.addRelationship("FOLLOWS", "node1", "node2");
        db.addRelationship("FOLLOWS", "node1", "node3");
    }

    @Test
    public void integrationTestGetNodePropertyNotThere() {
        when().
                get("/db/node/node0/property/not_there").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestGetNodeProperty() {
        when().
                get("/db/node/node1/property/name").
        then().
                assertThat().
                body(equalTo("\"Max\"")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetNodeIntegerProperty() {
        when().
                get("/db/node/node1/property/age").
        then().
                assertThat().
                body(equalTo("37")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestPutNodePropertyNotThere() {
        when().
                put("/db/node/node0/property/not_there").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestPutNodeProperty() {
        HashMap<String, Object> prop = new HashMap<>();
        prop.put("weight", 200);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Max");
        properties.put("age", 37);
        properties.put("weight", 200);

        given().
                contentType("application/json").
                body(200).
        when().
                put("/db/node/node1/property/weight").
        then().
                assertThat().
                body("$", equalTo(properties)).
                statusCode(201).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestPutNodeProperties() {
        HashMap<String, Object> prop = new HashMap<>();
        prop.put("weight", 200);
        prop.put("age", 38);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Max");
        properties.put("age", 38);
        properties.put("weight", 200);

        given().
                contentType("application/json").
                body(prop).
        when().
                put("/db/node/node1/properties").
        then().
                assertThat().
                body("$", equalTo(properties)).
                statusCode(201).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestDeleteNodePropertyNotThere() {
        when().
                delete("/db/node/node0/property/not_there").
                then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestDeleteNodeProperty() {
        when().
                delete("/db/node/node1/property/name").
                then().
                assertThat().
                statusCode(204);
    }

    @Test
    public void integrationTestDeleteNodeInvalidProperty() {
        when().
                delete("/db/node/node1/property/not_there").
                then().
                assertThat().
                statusCode(304);
    }

    @Test
    public void integrationTestDeleteNodePropertiesNotThere() {
        when().
                delete("/db/node/notThere/properties").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestDeleteNodeProperties() {
        when().
                delete("/db/node/node1/properties").
        then().
                assertThat().
                statusCode(204);
    }
}
