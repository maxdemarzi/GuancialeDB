package com.maxdemarzi.server;

import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.util.Json;
import org.jooby.Err;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;

import java.util.HashMap;
import java.util.Map;

public class RelationshipProperties extends Jooby {
    public RelationshipProperties() {
        super("relationship_properties");
    }
    private static TypeReference<HashMap<String,Object>> typeRef = new TypeReference<HashMap<String,Object>>() {};

    {
        use("/db")
                /*
                 * Get relationship property by key.
                 * @param type Relationship Type.                  
                 * @param from Starting Node ID.                  
                 * @param to Ending Node ID.
                 * @param key Node property
                 * @return Returns <code>200</code> with a single value or <code>404</code>
                 */
                .get("/relationship/:type/:from/:to/property/:key", req -> {
                    HashMap<String, Object> rel = Server.db.getRelationship(
                            req.param("type").value(),
                            req.param("from").value(),
                            req.param("to").value());
                    if (rel == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        if (rel.containsKey(req.param("key").value())) {
                            return rel.getOrDefault(req.param("key").value(),null);
                        } else {
                            throw new Err(Status.NOT_FOUND);
                        }

                    }
                })
                /*
                 * Set relationship property
                 * @param type Relationship Type.                  
                 * @param from Starting Node ID.                  
                 * @param to Ending Node ID.
                 * @param key Property name.
                 * @param body Property Value
                 * @return Returns <code>204</code> with a single rel or <code>404</code>
                 */
                .put("/relationship/:type/:from/:to/property/:key", (req,rsp) -> {
                    HashMap<String, Object> rel = Server.db.getRelationship(
                            req.param("type").value(),                             
                            req.param("from").value(),                             
                            req.param("to").value());
                    if (rel == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        Object value = Json.mapper().readValue(req.body().value(), Object.class);
                        if (value == null) {
                            throw new Err(Status.NOT_MODIFIED);
                        } else {
                            rel.put(req.param("key").value(), value);
                            Server.db.updateRelationship(
                                    req.param("type").value(),
                                    req.param("from").value(),
                                    req.param("to").value(),
                                    rel);
                        }
                    }

                    // Instead of 204 and sending nothing back,
                    rsp.status(201);
                    rsp.send(Server.db.getRelationship(                             
                            req.param("type").value(),                             
                            req.param("from").value(),                             
                            req.param("to").value()));
                })
                /*
                 * Set multiple relationship properties
                 * @param type Relationship Type.                  
                 * @param from Starting Node ID.                  
                 * @param to Ending Node ID.
                 * @param body Properties as Key and Value JSON Hash
                 * @return Returns <code>201</code> with a single rel or <code>404</code>
                 */
                .put("/relationship/:type/:from/:to/properties", (req,rsp) -> {
                    HashMap<String, Object> rel = Server.db.getRelationship(                             
                            req.param("type").value(),                             
                            req.param("from").value(),                             
                            req.param("to").value());
                    if (rel == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        HashMap<String, Object> value = Json.mapper().readValue(req.body().value(), typeRef);
                        if (value == null) {
                            throw new Err(Status.NOT_MODIFIED);
                        } else {
                            for (Map.Entry property : value.entrySet()) {
                                rel.put((String)property.getKey(), property.getValue());
                            }
                            Server.db.updateRelationship(
                                    req.param("type").value(),
                                    req.param("from").value(),
                                    req.param("to").value(),
                                    rel);
                        }
                    }

                    // Instead of 204 and sending nothing back,
                    rsp.status(201);
                    rsp.send(Server.db.getRelationship(                             
                            req.param("type").value(),                             
                            req.param("from").value(),                             
                            req.param("to").value()));

                })
                /*
                 * Delete a single relationship property by ID.
                 * @param type Relationship Type.                  
                 * @param from Starting Node ID.                  
                 * @param to Ending Node ID.
                 * @param key Property name.
                 * @return Returns <code>204</code> or <code>404</code>
                 */
                .delete ("/relationship/:type/:from/:to/property/:key", req -> {
                    String key = req.param("key").value();
                    HashMap<String, Object> rel = Server.db.getRelationship(                             
                            req.param("type").value(),                             
                            req.param("from").value(),                             
                            req.param("to").value());
                    if (rel == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        if (rel.containsKey(key)) {
                            rel.remove(key);
                            Server.db.updateRelationship(
                                    req.param("type").value(),
                                    req.param("from").value(),
                                    req.param("to").value(),
                                    rel);
                            return Results.with(204);
                        } else {
                            throw new Err(Status.NOT_MODIFIED);
                        }
                    }
                })
                /*
                 * Delete relationship properties by ID.
                 * @param type Relationship Type.                  
                 * @param from Starting Node ID.                  
                 * @param to Ending Node ID.
                 * @return Returns <code>204</code> or <code>404</code>
                 */
                .delete ("/relationship/:type/:from/:to/properties", req -> {
                    boolean deleted = Server.db.deleteRelationshipProperties(
                            req.param("type").value(),
                            req.param("from").value(),
                            req.param("to").value());
                    if (deleted) {
                        return Results.with(204);

                    }
                    throw new Err(Status.NOT_FOUND);
                })
                .produces("json");
    }
}
