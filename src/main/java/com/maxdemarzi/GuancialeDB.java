package com.maxdemarzi;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.openhft.chronicle.map.ChronicleMap;

import java.io.IOException;
import java.util.*;

public class GuancialeDB {

    private static ChronicleMap<String, HashMap> nodes;
    private static ChronicleMap<String, HashMap> relationships;
    private static HashMap<String, ReversibleMultiMap<String, String>> related;
    private static final ObjectMapper mapper = new ObjectMapper();

    private static GuancialeDB instance;
    public static GuancialeDB init(Integer maxNodes, Integer maxRelationships) {
        if (instance == null) {
            synchronized (GuancialeDB.class) {
                if (instance == null){
                    instance = new GuancialeDB(maxNodes, maxRelationships);
                }
            }
        }
        return instance;
    }

    public static GuancialeDB getInstance() {
        return instance;
    }

    public boolean isAvailable() {
        return true;
    }

    public void clear() {
        nodes.clear();
        relationships.clear();
        related = new HashMap<>();
    }

    private GuancialeDB(Integer maxNodes, Integer maxRelationships) {
        HashMap<String, Object> relProperties = new HashMap<>();
        relProperties.put("one", 10000);

        relationships = ChronicleMap
                .of(String.class, HashMap.class)
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
                .of(String.class, HashMap.class)
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

    public List<String> getRelationshipTypes() {
        return new ArrayList<>(related.keySet());
    }

    public boolean addNode (String key) {
        if (nodes.containsKey(key)) {
         return false;
        } else {
            nodes.put(key, new HashMap<>());
        }
        return true;
    }

    public boolean addNode (String key, String properties)  {
        if (nodes.containsKey(key)) {
            return false;
        } else {
            try {
                nodes.put(key, mapper.readValue(properties, HashMap.class));
            } catch (IOException e) {
                HashMap<String, String> value = new HashMap<>();
                value.put("value", properties);
                nodes.put(key, value);
            }
            return true;
        }
    }

    public boolean addNode (String key, HashMap properties)  {
        if (nodes.containsKey(key)) {
            return false;
        } else {
            nodes.put(key, properties);
            return true;
        }
    }

    public boolean addNode (String key, Object properties)  {
        if (nodes.containsKey(key)) {
            return false;
        } else {
            HashMap<String, Object> value = new HashMap<>();
            value.put("value", properties);
            nodes.put(key, value);
            return true;
        }
    }

    public boolean updateNode (String key, String properties)  {
        if (nodes.containsKey(key)) {
            try {
                nodes.put(key, mapper.readValue(properties, HashMap.class));
            } catch (IOException e) {
                HashMap<String, String> value = new HashMap<>();
                value.put("value", properties);
                nodes.put(key, value);
            }
            return true;

        } else {
            return false;
        }
    }

    public boolean updateNode (String key, HashMap properties)  {
        if (nodes.containsKey(key)) {
            nodes.put(key, properties);
            return true;
        } else {
            return false;
        }
    }

    public boolean updateNode (String key, Object properties)  {
        if (nodes.containsKey(key)) {
            HashMap<String, Object> value = new HashMap<>();
            value.put("value", properties);
            nodes.put(key, value);
            return true;
        } else {
            return false;
        }
    }

    public HashMap<String, Object> getNode(String id) {
        return nodes.get(id);
    }

    public boolean removeNode(String id) {
        if(nodes.containsKey(id)) {
            nodes.remove(id);
            for (String type : related.keySet()) {
                ReversibleMultiMap<String, String> rels = related.get(type);
                for (String value : rels.get(id)) {
                    relationships.remove(id + "-" + value + type);
                }
                for (String key : rels.getKeysByValue(id)) {
                    relationships.remove(key + "-" + id + type);
                }
                rels.removeAll(id);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean addRelationship (String type, String from, String to) {
        related.putIfAbsent(type, new ReversibleMultiMap<>());
        return related.get(type).put(from, to);
    }

    public boolean addRelationship (String type, String from, String to, String properties)  {
        try {
            relationships.put(from + "-" + to + type, mapper.readValue(properties, HashMap.class));
        } catch (IOException e) {
            HashMap<String, String> value = new HashMap<>();
            value.put("value", properties);
            relationships.put(from + "-" + to + type, value);
        }
        return true;
    }

    public boolean addRelationship (String type, String from, String to, HashMap properties) {
        relationships.put(from + "-" + to + type, properties);
        related.putIfAbsent(type, new ReversibleMultiMap<>());
        return related.get(type).put(from, to);
    }

    public boolean addRelationship (String type, String from, String to, Object properties) {
        HashMap<String, Object> value = new HashMap<>();
        value.put("value", properties);
        relationships.put(from + "-" + to + type, value);
        related.putIfAbsent(type, new ReversibleMultiMap<>());
        return related.get(type).put(from, to); 
    }

    public Object getRelationship(String type, String from, String to) {
        Object rel = relationships.get(from + "-" + to + type);
        if (rel == null) {
            if (related.get(type).get(from).contains(to)) {
                return new HashMap<>();
            } else {
                return null;
            }
        }
        return rel;
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


    public Integer getNodeDegree(String id) {
        return getNodeDegree(id, "all", new ArrayList<>());
    }

    public Integer getNodeDegree(String id, String direction) {
        return getNodeDegree(id, direction, new ArrayList<>());
    }

    public Integer getNodeDegree(String id, String direction, List<String> types) {
        if (nodes.containsKey(id)) {
            Integer count = 0;
            List<String> relTypes;
            if (types.size() == 0) {
                relTypes = new ArrayList<>(related.keySet());
            } else {
                relTypes = types;
            }

            for (String type : relTypes) {
                ReversibleMultiMap<String, String> rels = related.get(type);
                if (direction.equals("all") || direction.equals("out")) {
                    count += rels.get(id).size();
                }
                if (direction.equals("all") || direction.equals("in")) {
                    count += rels.getKeysByValue(id).size();
                }
            }
            return count;
        }

        return null;
    }
}
