package com.maxdemarzi;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class GuancialeDBTest {
    private GuancialeDB db;

    @Before
    public void setup() throws IOException {
        GuancialeDB.init(10000, 100000);
        db = GuancialeDB.getInstance();
        db.clear();
    }

    @Test
    public void shouldAddRelationship() {
        boolean created = db.addRelationship("FRIENDS", "one", "two");
        Assert.assertTrue(created);
    }

    @Test
    public void shouldAddRelationshipWithProperty() {
        db.addRelationship("FRIENDS", "one", "two", 3);
        Assert.assertEquals(1, db.getRelationshipTypeAttributes("FRIENDS").get("FRIENDS"));
    }

    @Test
    public void shouldAddRelationshipWithProperties() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("stars", 5);
        db.addRelationship("RATED", "one", "two", properties);
        Object actual = db.getRelationship("RATED", "one", "two");
        Assert.assertEquals(properties, actual);
        Assert.assertEquals(1, db.getRelationshipTypeAttributes("RATED").get("RATED"));
    }

    @Test
    public void shouldRemoveRelationship() {
        db.addRelationship("HATES", "one", "two");
        Assert.assertEquals(1, db.getRelationshipTypeAttributes("HATES").get("HATES"));
        db.removeRelationship("HATES", "one", "two");
        Assert.assertEquals(0, db.getRelationshipTypeAttributes("HATES").get("HATES"));
    }

    @Test
    public void shouldGetNodeNotThere() {
        Assert.assertEquals(null, db.getNode("NotThere"));
    }


    @Test
    public void shouldAddNode() {
        boolean created = db.addNode("key");
        Assert.assertTrue(created);
        Assert.assertEquals(new HashMap<>(), db.getNode("key"));
    }

    @Test
    public void shouldNotAddNodeAlreadyThere() {
        boolean created = db.addNode("key");
        Assert.assertTrue(created);
        Assert.assertEquals(new HashMap<>(), db.getNode("key"));
        created = db.addNode("key");
        Assert.assertFalse(created);
    }

    @Test
    public void shouldAddNodeWithProperties() {
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "max");
        properties.put("email", "maxdemarzi@hotmail.com");
        boolean created = db.addNode("max", properties);
        Assert.assertTrue(created);
        Assert.assertEquals(properties, db.getNode("max"));
    }

    @Test
    public void shouldAddNodeWithSimpleProperty() {
        boolean created = db.addNode("simple", 5);
        Assert.assertTrue(created);
        Assert.assertEquals(new HashMap<String, Object>(){{put("value", 5);}}, db.getNode("simple"));
    }

    @Test
    public void shouldNotRemoveNodeNotThere() {
        boolean result = db.removeNode("not_there");
        Assert.assertFalse(result);
    }


    @Test
    public void shouldRemoveNode() {
        boolean result = db.addNode("simple", 5);
        Assert.assertTrue(result);
        result = db.removeNode("simple");
        Assert.assertTrue(result);
    }

    @Test
    public void shouldRemoveNodeRelationships() {
        db.addNode("one");
        db.addNode("two");
        db.addNode("three");
        db.addRelationship("FRIENDS", "one", "two", 9);
        db.addRelationship("FRIENDS", "three", "one", 10);

        boolean result = db.removeNode("one");
        Assert.assertTrue(result);
        Assert.assertEquals(0, db.getRelationshipTypeAttributes("FRIENDS").get("FRIENDS"));

        Assert.assertEquals(null, db.getRelationship("FRIENDS", "one", "two"));
        Assert.assertEquals(null, db.getRelationship("FRIENDS", "three", "one"));
    }

    @Test
    public void shouldAddNodeWithObjectProperties() {
        HashMap<String, Object> address = new HashMap<>();
        address.put("Country", "USA");
        address.put("Zip", "60601");
        address.put("State", "TX");
        address.put("City", "Chicago");
        address.put("Line1 ", "175 N. Harbor Dr.");
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "max");
        properties.put("email", "maxdemarzi@hotmail.com");
        properties.put("address", address);
        boolean created = db.addNode("complex", properties);
        Assert.assertTrue(created);
        Assert.assertEquals(properties, db.getNode("complex"));
    }

    @Test
    public void shouldGetRelationshipTypes() {
        db.addRelationship("FOLLOWS", "one", "two");
        List<String> types = db.getRelationshipTypes();
        Assert.assertEquals(new ArrayList<String>(){{add("FOLLOWS");}}, types);
    }

    @Test
    public void shouldGetNodeOutgoingRelationshipNodeIds() {
        db.addNode("one");
        db.addNode("two");
        db.addNode("three");
        db.addRelationship("FRIENDS", "one", "two");
        db.addRelationship("FRIENDS", "one", "three");
        HashSet<String> actual = db.getOutgoingRelationshipNodeIds("FRIENDS", "one");
        Assert.assertEquals(new HashSet<String>() {{ add("two"); add("three");}}, actual);
    }

    @Test
    public void shouldGetNodeIncomingRelationshipNodeIds() {
        db.addNode("one");
        db.addNode("two");
        db.addNode("three");
        db.addRelationship("FRIENDS", "one", "two");
        db.addRelationship("FRIENDS", "one", "three");
        HashSet<String> actual = db.getIncomingRelationshipNodeIds("FRIENDS", "two");
        Assert.assertEquals(new HashSet<String>() {{ add("one"); }}, actual);
    }

    @Test
    public void shouldGetNodeOutgoingRelationshipNodes() {
        db.addNode("one", 1);
        db.addNode("two", "node two");

        HashMap<String, Object> node3props = new HashMap<> ();
        node3props.put("property1", 3);
        db.addNode("three", node3props);

        db.addRelationship("FRIENDS", "one", "two");
        db.addRelationship("FRIENDS", "one", "three");
        Set<Object> actual = db.getOutgoingRelationshipNodes("FRIENDS", "one");

        Set<Object> expected = new HashSet<Object>() {{
            add( new HashMap<String, Object>() {{
                put("_id", "two");
                put("properties", new HashMap<String, Object>(){{put("value", "node two");}});
            }});
            add( new HashMap<String, Object>() {{
                put("_id", "three");
                put("properties", node3props);
            }});
        }};
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetNodeIncomingRelationshipNodes() {
        db.addNode("one", 1);
        db.addNode("two", "node two");

        HashMap<String, Object> node3props = new HashMap<> ();
        node3props.put("property1", 3);
        db.addNode("three", node3props);

        db.addRelationship("FRIENDS", "two", "one");
        db.addRelationship("FRIENDS", "three", "one");
        Set<Object> actual = db.getIncomingRelationshipNodes("FRIENDS", "one");

        Set<Object> expected = new HashSet<Object>() {{
            add( new HashMap<String, Object>() {{
                put("_id", "two");
                put("properties", new HashMap<String, Object>(){{put("value", "node two");}});
            }});
            add( new HashMap<String, Object>() {{
                put("_id", "three");
                put("properties", node3props);
            }});
        }};
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void shouldGetNodeDegree() {
        db.addNode("four");
        db.addNode("five");
        db.addNode("six");
        db.addRelationship("FRIENDS", "four", "five");
        db.addRelationship("ENEMIES", "four", "six");
        Integer actual = db.getNodeDegree("four");
        Assert.assertEquals(Integer.valueOf(2), actual);
    }

    @Test
    public void shouldGetNodeIncomingDegree() {
        db.addNode("four");
        db.addNode("five");
        db.addNode("six");
        db.addRelationship("FRIENDS", "four", "five");
        db.addRelationship("ENEMIES", "six", "four");
        Integer actual = db.getNodeDegree("four", "out");
        Assert.assertEquals(Integer.valueOf(1), actual);
    }

    @Test
    public void shouldGetNodeOutgoingDegree() {
        db.addNode("four");
        db.addNode("five");
        db.addNode("six");
        db.addRelationship("FRIENDS", "four", "five");
        db.addRelationship("ENEMIES", "six", "four");
        Integer actual = db.getNodeDegree("four", "out");
        Assert.assertEquals(Integer.valueOf(1), actual);
    }

    @Test
    public void shouldGetNodeIncomingTypedDegree() {
        db.addNode("four");
        db.addNode("five");
        db.addNode("six");
        db.addRelationship("FRIENDS", "five", "four");
        db.addRelationship("ENEMIES", "six", "four");
        Integer actual = db.getNodeDegree("four", "in", new ArrayList<String>(){{add("ENEMIES");}});
        Assert.assertEquals(Integer.valueOf(1), actual);
    }

    @Test
    public void shouldGetNodeOutgoingTypedDegree() {
        db.addNode("four");
        db.addNode("five");
        db.addNode("six");
        db.addRelationship("FRIENDS", "four", "five");
        db.addRelationship("ENEMIES", "four", "six");
        Integer actual = db.getNodeDegree("four", "out", new ArrayList<String>(){{add("ENEMIES");}});
        Assert.assertEquals(Integer.valueOf(1), actual);
    }
}
