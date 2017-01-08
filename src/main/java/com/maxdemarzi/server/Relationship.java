package com.maxdemarzi.server;

import org.jooby.Err;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;

import static com.maxdemarzi.server.Server.db;

public class Relationship extends Jooby {

    public Relationship() {
        super("relationship");
    }

    {
        use("/db")
                /*
                 * Find relationship by Type, From, To.
                 * @param type Relationship Type.
                 * @param from Starting Node ID.
                 * @param to Ending Node ID.
                 * @return Returns <code>200</code> with a single Relationship or <code>404</code>
                 */
                .get("/relationship/:type/:from/:to", req -> {
                    Object rel = db.getRelationship(
                            req.param("type").value(),
                            req.param("from").value(),
                            req.param("to").value());
                    if (rel == null) {
                        throw new Err(Status.NOT_FOUND);
                    }
                    return rel;
                })
                /*
                 * Create a relationship with Properties
                 * @param type Relationship Type.
                 * @param from Starting Node ID.
                 * @param to Ending Node ID.
                 * @param body Node Properties. Default is "{}".
                 * @return Returns <code>201</code>
                 */
                .post("/relationship/:type/:from/:to", (req, rsp) -> {
                    String type = req.param("type").value();
                    String from =req.param("from").value();
                    String to = req.param("to").value();
                    db.addRelationship(type, from, to, req.body().toOptional().orElse("{}"));
                    rsp.status(201);
                    rsp.send(db.getRelationship(type, from, to));
                })
                /*
                 * Delete relationship by Type, From, To.
                 * @param type Relationship Type.
                 * @param from Starting Node ID.
                 * @param to Ending Node ID.
                 * @return Returns <code>204</code> or <code>404</code>
                 */
                .delete ("/relationship/:type/:from/:to", req -> {
                    if (db.removeRelationship(
                            req.param("type").value(),
                            req.param("from").value(),
                            req.param("to").value())) {
                        return Results.with(204);
                    } else {
                        throw new Err(Status.NOT_FOUND);
                    }
                })
                /*
                 * Get relationship Types.
                 * @return Returns <code>200</code> with a list of Relationship types
                 */
                .get("/relationship/types", req -> db.getRelationshipTypes())
                .produces("json");
    }
}
