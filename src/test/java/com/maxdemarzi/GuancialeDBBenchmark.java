package com.maxdemarzi;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class GuancialeDBBenchmark {

    private GuancialeDB db;
    private Random rand = new Random();

    @Param({"1000000"})
    private int maxNodes;

    @Param({"10000000"})
    private int maxRels;

    @Param({"1000"})
    private int userCount;

    @Param({"1"})
    private int personCount;

    @Param({"200"})
    private int itemCount;

    @Param({"100"})
    private int friendsCount;

    @Param({"1000"})
    private int likesCount;

    @Setup(Level.Invocation )
    public void prepare() throws IOException {
        GuancialeDB.init(maxNodes, maxRels);
        db = GuancialeDB.getInstance();

        for (int item = 0; item < itemCount; item++) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("id", item);
            properties.put("itemname", "itemname" + item );
            db.addNode("item" + item, properties);
        }

        for (int person = 0; person < personCount; person++) {
            for (int like = 0; like < itemCount; like++) {
                db.addRelationship("LIKES", "person" + person, "item" + rand.nextInt(itemCount));
            }
        }

    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public int measureCreateEmptyNodes() throws IOException {
        int user;
        for (user = 0; user < userCount; user++) {
            db.addNode("user" + user);
        }
        return user;
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public int measureCreateNodesWithProperties() throws IOException {
        int user;
        for (user = 0; user < userCount; user++) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("id", user);
            properties.put("username", "username" + user );
            db.addNode("user" +user, properties);
        }
        return user;
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @Threads(1)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public int measureCreateEmptyNodesAndRelationships() throws IOException {
        int user;
        for (user = 0; user < userCount; user++) {
            db.addNode("user" + user);
        }
        for (user = 0; user < userCount; user++) {
            for (int like = 0; like < friendsCount; like++) {
                db.addRelationship("FRIENDS", "user" + user, "user" + rand.nextInt(userCount));
            }
        }
        return user;
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public int measureTraverse() throws IOException {
        int person;
        for (person = 0; person < personCount; person++) {
            db.getOutgoingRelationshipNodeIds("LIKES", "person" + person);
        }
        return person;
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public int measureTraverseAndGetNodes() throws IOException {
        int person;
        for (person = 0; person < personCount; person++) {
            db.getOutgoingRelationshipNodes("LIKES", "person" + person);
        }
        return person;
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureSingleTraversal() throws IOException {
        db.getOutgoingRelationshipNodeIds("LIKES", "person" + rand.nextInt(personCount));
    }

    @Benchmark
    @Warmup(iterations = 10)
    @Measurement(iterations = 10)
    @Fork(1)
    @Threads(4)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.SECONDS)
    public void measureSingleTraversalAndGetNodes() throws IOException {
        db.getOutgoingRelationshipNodes("LIKES", "person" + rand.nextInt(personCount));
    }

}
