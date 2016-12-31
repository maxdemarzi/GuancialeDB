package com.maxdemarzi;

import net.openhft.chronicle.map.ChronicleMap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GuancialeDB {

    private static ChronicleMap<String, Object> nodes;
    private static ChronicleMap<String, Object> relationships;
    private static HashMap<String, ReversibleMultiMap<String, String>> related;

    public GuancialeDB(Integer maxNodes, Integer maxRelationships) {
        HashMap<String, Object> relProperties = new HashMap<>();
        relProperties.put("one", 10000);

        relationships = ChronicleMap
                .of(String.class, Object.class)
                .name("relationships")
                .entries(maxRelationships)
                .averageValue(relProperties)
                .averageKey("100000-100000-TYPE")
                .create();

        HashMap<String, Object> nodeProperties = new HashMap<>();
        nodeProperties.put("one", 10000);
        nodeProperties.put("two", "username10000");
        nodeProperties.put("three", "email@yahoo.com");
        nodeProperties.put("four", 50.55D);

        nodes = ChronicleMap
                .of(String.class, Object.class)
                .name("nodes")
                .entries(maxNodes)
                .averageKey("uno-dos-tres-cuatro")
                .averageValue(nodeProperties)
                .create();
        related = new HashMap<>();
    }

    public HashMap<String, Object> getRelationshipTypeAttributes(String type) {
        HashMap<String, Object> attributes = new HashMap<>();
        if (related.containsKey(type)) {
            attributes.put(type, related.get(type).size());
        }
        return attributes;
    }
    public boolean addNode (String key) {
        return addNode(key,"");
    }

    public boolean addNode (String key, Object properties) {
        nodes.put(key, properties);
        return true;
    }

    public Object getNode(String id) {
        if (nodes.containsKey(id)) {
            return nodes.get(id);
        } else {
            return new HashMap<>();
        }
    }

    public boolean removeNode(String id) {
        nodes.remove(id);
        for (String type : related.keySet()) {
            ReversibleMultiMap<String, String> rels = related.get(type);
            for (String value : rels.get(id) ){
                relationships.remove(id + "-" + value + type);
            }
            for (String key : rels.getKeysByValue(id) ){
                relationships.remove(key + "-" + id + type);
            }
            rels.removeAll(id);
        }

        return true;
    }

    public boolean addRelationship (String type, String from, String to) {
        related.putIfAbsent(type, new ReversibleMultiMap<>());
        return related.get(type).put(from, to);
    }

    public boolean addRelationship (String type, String from, String to, Object properties) {
        relationships.put(from + "-" + to + type, properties);
        related.putIfAbsent(type, new ReversibleMultiMap<>());
        return related.get(type).put(from, to); 
    }

    public Object getRelationship(String type, String from, String to) {
        return relationships.get(from + "-" + to + type);
    }

    public boolean removeRelationship (String type, String from, String to) {
        if(!related.containsKey(type)) {
            return false;
        }
        related.get(type).remove(from, to);
        relationships.remove(from + "-" + to + type);
        return true;
    }

    public HashSet<String> getOutgoingRelationshipNodeIds(String type, String from) {
        return new HashSet<>(related.get(type).get(from));
    }

    public HashSet<String> getIncomingRelationshipNodeIds(String type, String to) {
        return new HashSet<>(related.get(type).getKeysByValue(to));
    }

    public Set<Object> getOutgoingRelationshipNodes(String type, String from) {
        Set<Object> results = new HashSet<>();
        for (String key : related.get(type).get(from) ) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("_id", key);
            properties.put("properties", nodes.get(key));
            results.add(properties);
        }
        return results;
    }

    public Set<Object> getIncomingRelationshipNodes(String type, String from) {
        Set<Object> results = new HashSet<>();
        for (String key : related.get(type).getKeysByValue(from) ) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("_id", key);
            properties.put("properties", nodes.get(key));
            results.add(properties);
        }
        return results;
    }
}
