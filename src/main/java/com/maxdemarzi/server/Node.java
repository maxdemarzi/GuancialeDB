package com.maxdemarzi.server;

import org.jooby.Err;
import org.jooby.Jooby;
import org.jooby.Results;
import org.jooby.Status;

public class Node extends Jooby {

    public Node() {
        super("node");
    }
    {
        use("/db")
            /*
             * Find node by ID.
             * @param id Node ID.
             * @return Returns <code>200</code> with a single Node or <code>404</code>
             */
            .get ("/node/:id", req -> {
                Object node = Server.db.getNode(req.param("id").value());
                if (node == null) {
                    throw new Err(Status.NOT_FOUND);
                } else {
                    return node;
                }
            })
            /*
             * Create a node with Properties
             * @param id Node ID.
             * @param body Node Properties. Default is "{}".
             * @return Returns <code>201</code>
             */
            .post("/node/:id", (req, rsp) -> {
                String id = req.param("id").value();
                Server.db.addNode(id, req.body().toOptional().orElse("{}"));
                rsp.status(201);
                rsp.send(Server.db.getNode(id));
            })
            /*
             * Update a node with Properties
             * @param id Node ID.
             * @param body Node Properties. Default is "{}".
             * @return Returns <code>201</code>
             */
            .put("/node/:id", (req, rsp) -> {
                String id = req.param("id").value();
                if (Server.db.updateNode(id, req.body().toOptional().orElse("{}"))) {
                    rsp.status(201);
                    rsp.send(Server.db.getNode(id));
                } else {
                    throw new Err(Status.NOT_MODIFIED);
                }
            })
            /*
             * Delete node by ID.
             * @param id Node ID.
             * @return Returns <code>204</code> or <code>404</code>
             */
            .delete ("/node/:id", req -> {
                if (Server.db.removeNode(req.param("id").value())) {
                    return Results.with(204);
                } else {
                    throw new Err(Status.NOT_FOUND);
                }
            }).produces("json");
    }
}
