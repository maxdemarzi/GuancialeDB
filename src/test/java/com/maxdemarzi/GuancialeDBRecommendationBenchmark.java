package com.maxdemarzi;

import org.openjdk.jmh.annotations.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

@State(Scope.Benchmark)
public class GuancialeDBRecommendationBenchmark {
    private GuancialeDB db;
    private Random rand = new Random();

    @Param({"1000000"})
    private int maxNodes;

    @Param({"10000000"})
    private int maxRels;

    @Param({"1000", "10000"})
    private int personCount;

    @Param({"200", "2000"})
    private int itemCount;

    @Param({"10", "100"})
    private int likesCount;

    @Setup
    public void prepare() throws IOException {
        db = new GuancialeDB(maxNodes, maxRels);

        for (int item = 0; item < itemCount; item++) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("id", item);
            properties.put("itemname", "itemname" + item );
            db.addNode("item" + item, properties);
        }

        for (int person = 0; person < personCount; person++) {
            for (int like = 0; like < likesCount; like++) {
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
    public List measureRecommendationTraversal() throws IOException {
        Set<String> itemsYouLike = db.getOutgoingRelationshipNodeIds("LIKES", "person" + rand.nextInt(personCount));
        Map<String, LongAdder> occurrences = new HashMap<>();
        for (String item : itemsYouLike) {
            for (String person : db.getIncomingRelationshipNodeIds("LIKES", item)) {
                Set<String> itemsYouMightLike = db.getOutgoingRelationshipNodeIds("LIKES", person);
                itemsYouMightLike.removeAll(itemsYouLike);
                for (String unlikeditem : itemsYouMightLike) {
                    occurrences.computeIfAbsent(unlikeditem, (t) -> new LongAdder()).increment();
                }
            }
        }
        List<Map.Entry<String, LongAdder>> itemList = new ArrayList<>(occurrences.entrySet());
        Collections.sort(itemList, (a, b) -> ( b.getValue().intValue() - a.getValue().intValue() ));
        return itemList.subList(0, Math.min(itemList.size(), 10));
    }
}
