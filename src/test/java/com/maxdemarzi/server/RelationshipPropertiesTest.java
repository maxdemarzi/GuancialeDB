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

public class RelationshipPropertiesTest {

    private GuancialeDB db;

    @Rule
    public JoobyRule app = new JoobyRule(new Server());

    @Before
    public void setup() throws IOException {
        db = GuancialeDB.getInstance();
        db.clear();

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("stars", 5);
        properties.put("since", "2017-01-07");

        db.addNode("node1");
        db.addNode("node2");
        db.addNode("node3");
        db.addRelationship("FOLLOWS", "node1", "node2");
        db.addRelationship("FOLLOWS", "node1", "node3", properties);
    }

    @Test
    public void integrationTestGetRelationshipPropertyNotThere() {
        when().
                get("/db/relationship/FOLLOWS/node0/node1/property/not_there").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestGetRelationshipPropertyInvalidProperty() {
        when().
                get("/db/relationship/FOLLOWS/node1/node3/property/not_there").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestGetRelationshipProperty() {
        when().
                get("/db/relationship/FOLLOWS/node1/node3/property/since").
        then().
                assertThat().
                body(equalTo("\"2017-01-07\"")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetRelationshipIntegerProperty() {
        when().
                get("/db/relationship/FOLLOWS/node1/node3/property/stars").
        then().
                assertThat().
                body(equalTo("5")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestPutRelationshipPropertyNotThere() {
        when().
                put("/db/relationship/FOLLOWS/node0/node3/property/stars").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestPutRelationshipProperty() {
        HashMap<String, Object> prop = new HashMap<>();
        prop.put("archived", true);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("stars", 5);
        properties.put("since", "2017-01-07");
        properties.put("archived", true);

        given().
                contentType("application/json").
                body(true).
                when().
                put("/db/relationship/FOLLOWS/node1/node3/property/archived").
                then().
                assertThat().
                body("$", equalTo(properties)).
                statusCode(201).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestPutRelationshipProperties() {
        HashMap<String, Object> prop = new HashMap<>();
        prop.put("stars", 4);
        prop.put("archived", true);

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("stars", 4);
        properties.put("since", "2017-01-07");
        properties.put("archived", true);

        given().
                contentType("application/json").
                body(prop).
                when().
                put("/db/relationship/FOLLOWS/node1/node3/properties").
                then().
                assertThat().
                body("$", equalTo(properties)).
                statusCode(201).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestDeleteRelationshipPropertyNotThere() {
        when().
                delete("/db/relationship/FOLLOWS/node0/node1/property/not_there").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestDeleteRelationshipProperty() {
        when().
                delete("/db/relationship/FOLLOWS/node1/node3/property/since").
        then().
                assertThat().
                statusCode(204);
    }

    @Test
    public void integrationTestDeleteRelationshipPropertyInvalidProperty() {
        when().
                delete("/db/relationship/FOLLOWS/node1/node3/property/not_there").
        then().
                assertThat().
                statusCode(304);
    }

    @Test
    public void integrationTestDeleteRelationshipProperties() {
        when().
                delete("/db/relationship/FOLLOWS/node1/node3/properties").
                then().
                assertThat().
                statusCode(204);
    }

}
