package com.maxdemarzi.server;

import org.jooby.Err;
import org.jooby.Jooby;
import org.jooby.Status;

public class NodeDegree extends Jooby {
    public NodeDegree() {
        super("node_degree");
    }
    {
        use("/db")
                /*
                 * Get node degree by ID.
                 * @param id Node ID.
                 * @return Returns <code>200</code> with a single value or <code>404</code>
                 */
                .get ("/node/:id/degree", req -> {
                    Integer count = Server.db.getNodeDegree(req.param("id").value());
                    if (count == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        return count;
                    }
                })
                /*
                 * Get node degree by ID, direction and types.
                 * @param id Node ID.
                 * @param direction all, out, in
                 * @param types relationship types list. Default is <code>null</code>.
                 * @return Returns <code>200</code> with a single value or <code>404</code>
                 */
                .get ("/node/:id/degree/:direction", req -> {
                    Integer count = Server.db.getNodeDegree(
                            req.param("id").value(),
                            req.param("direction").value(),
                            req.param("types").toList(String.class)
                    );
                    if (count == null) {
                        throw new Err(Status.NOT_FOUND);
                    } else {
                        return count;
                    }
                })
                .produces("json");
    }
}
