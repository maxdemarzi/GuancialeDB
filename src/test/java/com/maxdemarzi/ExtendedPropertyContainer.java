package com.maxdemarzi;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.HashMap;

public class ExtendedPropertyContainer extends PropertyContainer {
    public ExtendedPropertyContainer(String id, HashMap<String, Object> properties) {
        super(id, properties);
    }

    public static final Attribute<PropertyContainer, String> NAME = new SimpleAttribute<PropertyContainer, String>("name") {
        public String getValue(PropertyContainer car, QueryOptions queryOptions) { return (String)car.properties.get("name"); }
    };

}
