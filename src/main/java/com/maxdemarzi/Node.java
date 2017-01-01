package com.maxdemarzi;

import org.jooby.Result;
import org.jooby.Results;
import org.jooby.mvc.*;

import java.util.Optional;

@Path("/db/node/")
@Consumes("json")
@Produces("json")
public class Node {
    private GuancialeDB db = GuancialeDB.getInstance();

    /**
     *
     * Find node by ID.
     *
     * @param id Node ID.
     * @return Returns a single Node
     */
    @Path(":id")
    @GET
    public Object get(final String id) {
        return db.getNode(id);
    }

    /**
     *
     * Create node with Properties
     *
     * @param id Node ID.
     * @param body Node Properties. Default is "".
     * @return Returns <code>201</code>
     */
    @Path(":id")
    @POST
    public Result create(final String id, @Body final Optional<Object> body) {
        db.addNode(id, body.orElse(""));
        return Results.with(201);
    }

}
