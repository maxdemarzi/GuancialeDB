package com.maxdemarzi.server;

import io.swagger.util.Json;
import org.jooby.Err;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;

import java.util.HashMap;
import java.util.Map;

public class NodeProperties extends Jooby {
    public NodeProperties() {
        super("node_properties");
    }

    {
        use("/db")
                /*
                 * Get node property by key.
                 * @param id Node ID.
                 * @param key Node property
                 * @return Returns <code>200</code> with a single value or <code>404</code>
                 */
                .get("/node/:id/property/:key", req -> {
                    HashMap<String, Object> node = Server.db.getNode(req.param("id").value());
                    if (node == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        if (node.containsKey(req.param("key").value())) {
                            return node.get(req.param("key").value());
                        }
                        throw new Err(Status.NOT_FOUND);
                    }
                })
                /*
                 * Set node property
                 * @param id Node ID.
                 * @param key Property name.
                 * @param body Property Value
                 * @return Returns <code>204</code> with a single node or <code>404</code>
                 */
                .put("/node/:id/property/:key", (req,rsp) -> {
                    String id = req.param("id").value();
                    HashMap<String, Object> node = Server.db.getNode(id);
                    if (node == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        Object value = Json.mapper().readValue(req.body().value(), Object.class);
                        if (value == null) {
                            throw new Err(Status.NOT_MODIFIED);
                        } else {
                            node.put(req.param("key").value(), value);
                            Server.db.updateNode(id, node);
                        }
                    }

                    // Instead of 204 and sending nothing back,
                    rsp.status(201);
                    rsp.send(Server.db.getNode(id));

                })
                /*
                 * Set multiple node properties
                 * @param id Node ID.
                 * @param body Properties as Key and Value JSON Hash
                 * @return Returns <code>201</code> with a single node or <code>404</code>
                 */
                .put("/node/:id/properties", (req,rsp) -> {
                    String id = req.param("id").value();
                    HashMap<String, Object> node = Server.db.getNode(id);
                    if (node == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        HashMap<String, Object> value = Json.mapper().readValue(req.body().value(), HashMap.class);
                        if (value == null) {
                            throw new Err(Status.NOT_MODIFIED);
                        } else {
                            for (Map.Entry<String, Object> property : value.entrySet()) {
                                node.put(property.getKey(), property.getValue());
                            }
                            Server.db.updateNode(id, node);
                        }
                    }

                    // Instead of 204 and sending nothing back,
                    rsp.status(201);
                    rsp.send(Server.db.getNode(id));

                })
                /*
                 * Delete a single node property by ID.
                 * @param id Node ID.
                 * @param key Property name.
                 * @return Returns <code>204</code> or <code>404</code>
                 */
                .delete ("/node/:id/property/:key", req -> {
                    String id = req.param("id").value();
                    String key = req.param("key").value();
                    HashMap<String, Object> node = Server.db.getNode(id);
                    if (node == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        if (node.containsKey(key)) {
                            node.remove(key);
                            Server.db.updateNode(id, node);
                            return Results.with(204);
                        } else {
                            throw new Err(Status.NOT_MODIFIED);
                        }
                    }
                })
                /*
                 * Delete node properties by ID.
                 * @param id Node ID.
                 * @return Returns <code>204</code> or <code>404</code>
                 */
                .delete ("/node/:id/properties", req -> {
                    String id = req.param("id").value();
                    HashMap<String, Object> node = Server.db.getNode(id);
                    if (node == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        Server.db.updateNode(id, new HashMap());
                        return Results.with(204);
                    }
                })
                .produces("json");
    }
}
