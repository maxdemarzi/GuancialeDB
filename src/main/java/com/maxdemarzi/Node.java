package com.maxdemarzi;

import org.jooby.mvc.Consumes;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;
import org.jooby.mvc.Produces;

import javax.inject.Inject;
import java.util.HashMap;

@Path("/db/node")
@Consumes("json")
@Produces("json")
public class Node {
    private GuancialeDB db = GuancialeDB.getInstance();

    @Inject
    public Node() {
        db.addNode("max", "props");
        db.addNode("max2");
        HashMap<String, Object> props =  new HashMap<>();
        props.put("city", "Chicago");
        db.addNode("max3", props);
    }

    /**
     *
     * Find node by ID.
     *
     * @param id Node ID.
     * @return Returns a single Node
     */
    @Path("/:id")
    @GET
    public Object get(final String id) {
        return db.getNode(id);
    }

}
