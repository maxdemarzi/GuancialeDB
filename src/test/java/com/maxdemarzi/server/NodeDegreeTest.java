package com.maxdemarzi.server;

import com.maxdemarzi.GuancialeDB;
import org.jooby.test.JoobyRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class NodeDegreeTest {
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
    public void integrationTestGetNodeDegreeNotThere() {
        when().
                get("/db/node/node0/degree").
                then().
                assertThat().
                statusCode(404);
    }

    @Test
    public void integrationTestGetNodeDegree() {
        when().
                get("/db/node/node1/degree").
        then().
                assertThat().
                body(equalTo("2")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetNodeIncomingDegree() {
        when().
                get("/db/node/node2/degree/in").
                then().
                assertThat().
                body(equalTo("1")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetNodeOutgoingDegree() {
        when().
                get("/db/node/node1/degree/out").
                then().
                assertThat().
                body(equalTo("2")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetNodeIncomingTypedDegree() {
        when().
                get("/db/node/node2/degree/in?type=FOLLOWS").
                then().
                assertThat().
                body(equalTo("1")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }

    @Test
    public void integrationTestGetNodeOutgoingTypedDegree() {
        when().
                get("/db/node/node1/degree/out?type=FOLLOWS").
                then().
                assertThat().
                body(equalTo("2")).
                statusCode(200).
                contentType("application/json;charset=UTF-8");
    }
}
