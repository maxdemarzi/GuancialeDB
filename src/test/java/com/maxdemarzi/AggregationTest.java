package com.maxdemarzi;

import net.openhft.chronicle.hash.serialization.MapMarshaller;
import net.openhft.chronicle.hash.serialization.impl.*;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class AggregationTest {
    private static GuancialeDB db;
    private static GuancialeDB2 db2;
    private static Random rand = new Random();

    @BeforeClass
    public static void setup() throws IOException {
        GuancialeDB.init(10000000, 100000);
        db = GuancialeDB.getInstance();
        db.clear();
        GuancialeDB2.init(10000000, 100000);
        db2 = GuancialeDB2.getInstance();
        db2.clear();

        Integer personCount = 1632803;

        for (int person = 0; person < personCount; person++) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("id" + person, "id" + person);
            properties.put("weight", rand.nextInt(120));
            db.addNode("id" + person, properties);
            db2.addNode("id" + person, properties);
        }
    }

    @Test
    public void shouldAggregate() {
        Iterator<Map.Entry<String, HashMap>> iter = db.getAllNodes();
        HashMap<Integer, Integer> weights = new HashMap<>();
        Integer weight;
        Long start = System.currentTimeMillis();
        while (iter.hasNext()) {
            Map.Entry<String, HashMap> nodeEntry = iter.next();
            weight = (Integer) nodeEntry.getValue().get("weight");
            weights.merge(weight, 1, Integer::sum);
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    @Test
    public void shouldAggregate2() {
        Iterator<PropertyContainer> iter = db2.getAllNodes();
        HashMap<Integer, Integer> weights = new HashMap<>();
        Integer weight;
        Long start = System.currentTimeMillis();
        while (iter.hasNext()) {
            PropertyContainer nodeEntry = iter.next();
            weight = (Integer) nodeEntry.properties.get("weight");
            weights.merge(weight, 1, Integer::sum);
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    @Test
    public void shouldAggregate3() {
        int[] ages = new int[120];
        Long start = System.currentTimeMillis();
        db.getNodes().forEachEntry((node) -> {
            ages[(Integer) node.value().get().get("weight")]++;
        });
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    @Test
    public void shouldAggregate4() {
        int[] ages = new int[120];
        Long start = System.currentTimeMillis();
        Iterator<PropertyContainer> iter = db2.getAllNodes();
        while (iter.hasNext()) {
            ages[(Integer) iter.next().properties.get("weight")]++;
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    @Test
    public void shouldAggregate5() {
        Integer personCount = 1632803;
        ChronicleMap<String, Integer> properties = ChronicleMap
                .of(String.class, Integer.class)
                .name("nodes")
                .entries(1632803 * 10)
                .averageKey("uno-dos-tres-cuatro")
                .create();


        for (int person = 0; person < personCount; person++) {
            properties.put(String.valueOf(person), rand.nextInt(120));
        }

        int[] ages = new int[120];
        Long start = System.currentTimeMillis();
        for (int person = 0; person < personCount; person++) {
            ages[properties.get(String.valueOf(person))]++;
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    @Test
    public void shouldAggregate6() {
        Integer personCount = 1632803;

        HashMap<String, String> sampleProperties = new HashMap<>();
        sampleProperties.put("id", "user1");
        sampleProperties.put("weight", "200");


        ChronicleMap<String, HashMap<String, String>> properties = ChronicleMap
                .of(String.class, (Class<HashMap<String, String>>) (Class) HashMap.class)
                .name("props")
                .entries(1632803 * 10)
                .averageKey("uno-dos-tres-cuatro")
                .averageValue(sampleProperties)
                .create();

        for (int person = 0; person < personCount; person++) {
            HashMap<String, String> nodeProperties = new HashMap<>();
            nodeProperties.put("id" + person, "id" + person);
            nodeProperties.put("weight", String.valueOf(rand.nextInt(120)));
            properties.put("id" + person, nodeProperties);
        }

        int[] ages = new int[120];
        Long start = System.currentTimeMillis();
        for (int person = 0; person < personCount; person++) {
            ages[Integer.valueOf(properties.get("id" + person).get("weight"))]++;
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

    @Test
    public void shouldAggregate7() {
        Integer personCount = 1632803;

        HashMap<String, String> sampleProperties = new HashMap<>();
        sampleProperties.put("id", "user1");
        sampleProperties.put("weight", "200");

        MapMarshaller<String, String> valueMarshaller = new MapMarshaller<>(new StringBytesReader(), CharSequenceBytesWriter.INSTANCE,
                new StringBytesReader(), CharSequenceBytesWriter.INSTANCE);

        ChronicleMap<String, Map<String, String>> properties = ChronicleMapBuilder
                .of(String.class, (Class<Map<String, String>>) (Class) Map.class)
                .name("props")
                .entries(1632803 * 10)
                .valueMarshaller(valueMarshaller)
                .averageKey("uno-dos-tres-cuatro")
                .averageValue(sampleProperties).create();

        for (int person = 0; person < personCount; person++) {
            HashMap<String, String> nodeProperties = new HashMap<>();
            nodeProperties.put("id" + person, "id" + person);
            nodeProperties.put("weight", String.valueOf(rand.nextInt(120)));
            properties.put("id" + person, nodeProperties);
        }

        int[] ages = new int[120];
        Long start = System.currentTimeMillis();
        for (int person = 0; person < personCount; person++) {
            ages[Integer.valueOf(properties.get("id" + person).get("weight"))]++;
        }
        Long end = System.currentTimeMillis();
        System.out.println(end-start);
    }

}
