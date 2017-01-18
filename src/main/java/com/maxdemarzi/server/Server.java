package com.maxdemarzi.server;

import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.DatabaseHealthCheck;
import com.maxdemarzi.GuancialeDB;
import com.typesafe.config.Config;
import org.jooby.Jooby;
import org.jooby.MediaType;
import org.jooby.Results;
import org.jooby.banner.Banner;
import org.jooby.crash.Crash;
import org.jooby.ftl.Ftl;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.swagger.SwaggerUI;
import org.jooby.whoops.Whoops;

import java.util.HashMap;

public class Server extends Jooby {
    private static final HashMap<String, String> VERSION = new HashMap<String, String>(){{
        put("version","0.0.1");
    }};

    public static GuancialeDB db;

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

        use(new Node());
        use(new NodeDegree());
        use(new NodeProperties());
        use(new Relationship());
        use(new RelationshipProperties());

        use("/db")
                /*
                 * Server Root
                 * @return Returns server version
                 */
                .get("/", () -> VERSION)

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

        get("/dashboard", req -> Results.html("dashboard"));

        // Banner
        use(new Banner("Guanciale DB"));

        // Shell
        use(new Crash());

        // Template
        use(new Ftl());

        // Assets
        assets("/assets/**");


  }

  public static void main(final String[] args) {
    run(Server::new, args);
  }

  public static HashMap<String, String> getVersion() {
        return VERSION;
  }

}
