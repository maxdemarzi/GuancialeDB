package com.maxdemarzi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Striped;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import net.openhft.chronicle.map.ChronicleMap;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;

import static com.googlecode.cqengine.query.QueryFactory.equal;

public class GuancialeDB2 {

    private static IndexedCollection<PropertyContainer> nodes = new ConcurrentIndexedCollection<>();
    //private static ChronicleMap<String, HashMap> nodes;
    private static ChronicleMap<String, HashMap<String, Object>> relationships;
    private static HashMap<String, ReversibleMultiMap<String, String>> related;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static Striped<Lock> nodeLocks;
    private static GuancialeDB2 instance;
    public static GuancialeDB2 init(Integer maxNodes, Integer maxRelationships) {
        if (instance == null) {
            synchronized (GuancialeDB2.class) {
                if (instance == null){
                    instance = new GuancialeDB2(maxNodes, maxRelationships);
                    nodeLocks = Striped.lazyWeakLock(maxNodes);
                }
            }
        }
        return instance;
    }

    public static GuancialeDB2 getInstance() {
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

    private GuancialeDB2(Integer maxNodes, Integer maxRelationships) {
        HashMap<String, Object> relProperties = new HashMap<>();
        relProperties.put("one", 10000);

        relationships = ChronicleMap
                .of(String.class, (Class<HashMap<String, Object>>) (Class) HashMap.class)
                .name("relationships")
                .entries(maxRelationships)
                .averageValue(relProperties)
                .averageKey("100000-100000-TYPE")
                .create();

        nodes.addIndex(UniqueIndex.onAttribute(PropertyContainer.ID));

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

    public boolean addNode (String id) {
        final Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        Lock l = nodeLocks.get(id);
        l.lock();
        try {
            if (nodes.retrieve(query).isNotEmpty()) {
                return false;
            } else {
                nodes.add(new PropertyContainer(id, new HashMap<>()));
            }
            return true;
        } finally {
            l.unlock();
        }
    }

    public boolean addNode (String id, String properties)  {
        final Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        Lock l = nodeLocks.get(id);
        l.lock();
        try {
             if (nodes.retrieve(query).isNotEmpty()) {
                return false;
            } else {
                try {
                    nodes.add(new PropertyContainer(id, mapper.readValue(properties, HashMap.class)));
                } catch (IOException e) {
                    HashMap<String, Object> value = new HashMap<>();
                    value.put("value", properties);
                    nodes.add(new PropertyContainer(id, value));
                }
                return true;
            }
        } finally {
            l.unlock();
        }
    }

    public boolean addNode (String id, HashMap properties)  {
        final Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        Lock l = nodeLocks.get(id);
        l.lock();
        try {
            if (nodes.retrieve(query).isNotEmpty()) {
            return false;
            } else {
                 nodes.add(new PropertyContainer(id, properties));
                return true;
            }
        } finally {
            l.unlock();
        }
    }

    public boolean addNode (String id, Object value)  {
        final Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        Lock l = nodeLocks.get(id);
        l.lock();
        try {
            if (nodes.retrieve(query).isNotEmpty()) {
                return false;
            } else {
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("value", value);
                nodes.add(new PropertyContainer(id, properties));
                return true;
            }
        } finally {
            l.unlock();
        }
    }

    public boolean updateNode (String id, String value)  {
         final Query<PropertyContainer> query = equal(PropertyContainer.ID, id); 
         if (nodes.retrieve(query).isNotEmpty()) {
            try {
                nodes.add(new PropertyContainer(id, mapper.readValue(value, HashMap.class)));
            } catch (IOException e) {
                HashMap<String, Object> properties = new HashMap<>();
                properties.put("value", value);
                nodes.add(new PropertyContainer(id, properties));
            }
            return true;

        } else {
            return false;
        }
    }

    public boolean updateNode (String id, HashMap<String, Object> properties)  {
         final Query<PropertyContainer> query = equal(PropertyContainer.ID, id); 
         if (nodes.retrieve(query).isNotEmpty()) {
             nodes.add(new PropertyContainer(id, properties));
            return true;
        } else {
            return false;
        }
    }

    public Iterator<PropertyContainer> getAllNodes(){
        return nodes.iterator();
    }

    public HashMap<String, Object> getNode(String id) {
        final Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        ResultSet<PropertyContainer> results = nodes.retrieve(query);
        if (results.isNotEmpty()) {
            return results.iterator().next().properties;
        } else {
            return null;
        }
    }

    public boolean removeNode(String id) {
        final Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        ResultSet<PropertyContainer> results = nodes.retrieve(query);
        if (results.isNotEmpty()) {
            nodes.remove(results.iterator().next());
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
            HashMap<String, Object> value = new HashMap<>();
            value.put("value", properties);
            relationships.put(from + "-" + to + type, value);
        }
        related.putIfAbsent(type, new ReversibleMultiMap<>());
        return related.get(type).put(from, to);
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

    public HashMap<String, Object> getRelationship(String type, String from, String to) {
        HashMap<String, Object> rel = relationships.get(from + "-" + to + type);
        if (rel == null) {
            if (related.get(type).get(from).contains(to)) {
                return new HashMap<>();
            } else {
                return null;
            }
        }
        return rel;
    }

    public boolean updateRelationship(String type, String from, String to, String properties) {
        HashMap<String, Object> rel = relationships.get(from + "-" + to + type);
        if (rel == null) {
            if (!related.get(type).get(from).contains(to)) {
                return false;
            }
        }
        try {
            relationships.put(from + "-" + to + type, mapper.readValue(properties, HashMap.class));
        } catch (IOException e) {
            HashMap<String, Object> value = new HashMap<>();
            value.put("value", properties);
            relationships.put(from + "-" + to + type, value);
        }
        return true;
    }

    public boolean updateRelationship(String type, String from, String to, HashMap properties) {
        HashMap<String, Object> rel = relationships.get(from + "-" + to + type);
        if (rel == null) {
            if (!related.get(type).get(from).contains(to)) {
                return false;
            }
        }
        relationships.put(from + "-" + to + type, properties);
        return true;
    }

    public boolean deleteRelationshipProperties(String type, String from, String to) {
        HashMap<String, Object> rel = relationships.get(from + "-" + to + type);
        if (rel == null) {
            if (!related.get(type).get(from).contains(to)) {
                return false;
            }
        } else {
            relationships.remove(from + "-" + to + type);
        }
        return true;
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
        for (String id : related.get(type).get(from) ) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("_id", id);
            properties.put("properties", getNode(id));
            results.add(properties);
        }
        return results;
    }

    public Set<Object> getIncomingRelationshipNodes(String type, String from) {
        Set<Object> results = new HashSet<>();
        for (String id : related.get(type).getKeysByValue(from) ) {
            HashMap<String, Object> properties = new HashMap<>();
            properties.put("_id", id);
            properties.put("properties", getNode(id));
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
        final Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        if (nodes.retrieve(query).isNotEmpty()) {
            Integer count = 0;
            List<String> relTypes;
            if (types.size() == 0) {
                relTypes = new ArrayList<>(related.keySet());
            } else {
                types.retainAll(related.keySet());
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
