package com.maxdemarzi.server;

import com.maxdemarzi.GuancialeDB;
import org.jooby.test.JoobyRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class RelationshipTest {
    private GuancialeDB db;

    @ClassRule
    public static JoobyRule app = new JoobyRule(new Server());

    @Before
    public void setup() throws IOException {
        db = GuancialeDB.getInstance();
        db.clear();

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("stars", 5);

        db.addNode("node1");
        db.addNode("node2");
        db.addNode("node3");
        db.addRelationship("FOLLOWS", "node1", "node2");
        db.addRelationship("FOLLOWS", "node1", "node3", properties);
    }

    @Test
    public void integrationTestGetRelationshipNotThere() {
        when().
                get("/db/relationship/FOLLOWS/node0/node1").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestGetEmptyRelationship() {
        when().
                get("/db/relationship/FOLLOWS/node1/node2").
        then().
                assertThat().
                body("$", equalTo(new HashMap<>())).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetSinglePropertyRelationship() {
        HashMap<String, Object> prop =  new HashMap<>();
        prop.put("stars", 5);

        when().
                get("/db/relationship/FOLLOWS/node1/node3").
        then().
                assertThat().
                body("$", equalTo(prop)).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestCreateEmptyRelationship() {
        given().
                contentType("application/json").
        when().
                post("/db/relationship/FOLLOWS/node2/node1").
        then().
                assertThat().
                body("$", equalTo(new HashMap<>())).
                statusCode(201).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestCreateSinglePropertyRelationship() {
        HashMap<String, Object> prop =  new HashMap<>();
        prop.put("stars", 5);

        given().
                contentType("application/json").
                body(prop).
        when().
                post("/db/relationship/FOLLOWS/node1/node3").
        then().
                assertThat().
                body("$", equalTo(prop)).
                statusCode(201).
                contentType("application/json;charset=UTF-8");
    }
    @Test
    public void integrationTestDeleteRelationshipNotThere() {
        when().
                delete("/db/relationship/NOT_THERE/node0/node1").
        then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestDeleteRelationship() {
        when().
                delete("/db/relationship/FOLLOWS/node1/node2").
        then().
                assertThat().
                statusCode(204);
    }

    @Test
    public void integrationTestGetRelationshipTypes() {
        when().
                get("/db/relationship/types").
                then().
                assertThat().
                body("$", equalTo(new ArrayList<String>(){{add("FOLLOWS");}})).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }
}
