package com.maxdemarzi;

import org.jooby.test.JoobyRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class RelationshipTest {
    private GuancialeDB db;

    @Rule
    public JoobyRule app = new JoobyRule(new Server());

    @Before
    public void setup() throws IOException {
        db = GuancialeDB.getInstance();

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
}
