package com.maxdemarzi;

import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.jooby.*;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.swagger.SwaggerUI;
import org.jooby.whoops.Whoops;

import java.util.HashMap;

public class Server extends Jooby {
    private static final HashMap<String, String> VERSION = new HashMap<String, String>(){{
        put("version","0.0.1");
    }};

    private static GuancialeDB  db;

    {
        onStart(() -> {
            Config conf = require(Config.class);
            GuancialeDB.init(conf.getInt("guanciale.max_nodes"),conf.getInt("guanciale.max_rels"));
            db = GuancialeDB.getInstance();
        });

        // JSON via Jackson
        ObjectMapper mapper = new ObjectMapper();
        use(new Jackson(mapper));

        // Error messages via Whoops
        use(new Whoops());

        get("/", () -> "Hello World!").produces(MediaType.text);
        get("/break", req -> { throw new IllegalStateException("Something broke!"); });

        use("/db")
                /*
                 * Server Root
                 * @return Returns server version
                 */
                .get("/", () -> VERSION)
                /*
                 * Find node by ID.
                 * @param id Node ID.
                 * @return Returns <code>200</code> with a single Node or <code>404</code>
                 */
                .get ("/node/:id", req -> {
                    Object node = db.getNode(req.param("id").value());
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
                    db.addNode(id, req.body().toOptional().orElse("{}"));
                    rsp.status(201);
                    rsp.send(db.getNode(id));
                })
                /*
                 * Delete node by ID.
                 * @param id Node ID.
                 * @return Returns <code>204</code>
                 */
                .delete ("/node/:id", req -> {
                    if (db.removeNode(req.param("id").value())) {
                        return Results.noContent();
                    } else {
                        throw new Err(Status.NOT_FOUND);
                    }
                })
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
                 * Delete relationship by Type, From, To.
                 * @param type Relationship Type.
                 * @param from Starting Node ID.
                 * @param to Ending Node ID.
                 * @return Returns <code>204</code>
                 */
                .delete ("/relationship/:type/:from/:to", req -> {
                    if (db.removeRelationship(
                            req.param("type").value(),
                            req.param("from").value(),
                            req.param("to").value())) {
                        return Results.noContent();
                    } else {
                        throw new Err(Status.NOT_FOUND);
                    }
                })
                .produces("json");

        // API Documentation by Swagger
        new SwaggerUI()
                .filter(route -> route.pattern().startsWith("/db"))
                .install(this);

        // Metrics
        use(new Metrics()
                .request()
                .threadDump()
                .ping()
                .healthCheck("db", new DatabaseHealthCheck())
                .metric("memory", new MemoryUsageGaugeSet())
                .metric("threads", new ThreadStatesGaugeSet())
                .metric("gc", new GarbageCollectorMetricSet())
                .metric("fs", new FileDescriptorRatioGauge())
        );
  }

  public static void main(final String[] args) {
    run(Server::new, args);
  }

}
