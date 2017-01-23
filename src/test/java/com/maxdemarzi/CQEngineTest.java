package com.maxdemarzi;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.index.radixinverted.InvertedRadixTreeIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.resultset.ResultSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static com.googlecode.cqengine.query.QueryFactory.equal;

public class CQEngineTest {

    @Test
    public void shouldAddandFindEntries() {
        IndexedCollection<PropertyContainer> nodes = new ConcurrentIndexedCollection<>();
        nodes.addIndex(UniqueIndex.onAttribute(PropertyContainer.ID));
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Max");
        String id = "max";
        PropertyContainer node =new PropertyContainer(id , properties);
        nodes.add(node);
        Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        ResultSet<PropertyContainer> results = nodes.retrieve(query);
        Assert.assertEquals(node, results.uniqueResult());
    }

    @Test
    public void shouldLetMeAddIndexesToAlreadyPopulatedCollection() {
        IndexedCollection<PropertyContainer> nodes = new ConcurrentIndexedCollection<>();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Max");
        String id = "max";
        PropertyContainer node =new PropertyContainer(id , properties);
        nodes.add(node);
        nodes.addIndex(UniqueIndex.onAttribute(PropertyContainer.ID));
        Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        ResultSet<PropertyContainer> results = nodes.retrieve(query);
        Assert.assertEquals(node, results.uniqueResult());
    }

    @Test
    public void shouldLetMeAddNewIndexes() {
        IndexedCollection<PropertyContainer> nodes = new ConcurrentIndexedCollection<>();
        nodes.addIndex(UniqueIndex.onAttribute(PropertyContainer.ID));

        HashMap<String, Object> properties = new HashMap<>();
        properties.put("name", "Max");
        String id = "max";
        ExtendedPropertyContainer node = new ExtendedPropertyContainer(id , properties);
        nodes.add(node);
        nodes.addIndex(InvertedRadixTreeIndex.onAttribute(ExtendedPropertyContainer.NAME));

        Query<PropertyContainer> query = equal(PropertyContainer.ID, id);
        ResultSet<PropertyContainer> results = nodes.retrieve(query);
        Assert.assertEquals(node, results.uniqueResult());

        query = equal(ExtendedPropertyContainer.NAME, (String)properties.get("name"));
        results = nodes.retrieve(query);
        Assert.assertEquals(node, results.uniqueResult());
    }

}
