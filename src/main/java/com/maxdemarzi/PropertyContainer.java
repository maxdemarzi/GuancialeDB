package com.maxdemarzi;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.HashMap;

public class PropertyContainer {

    public final String id;
    public final HashMap<String, Object> properties;

    public PropertyContainer(String id, HashMap<String, Object> properties) {
        this.id = id;
        this.properties = properties;
    }

    public static final Attribute<PropertyContainer, String> ID = new SimpleAttribute<PropertyContainer, String>("id") {
        public String getValue(PropertyContainer propertyContainer, QueryOptions queryOptions) { return propertyContainer.id; }
    };

}
