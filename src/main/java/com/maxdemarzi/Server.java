package com.maxdemarzi;

import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooby.Jooby;
import org.jooby.MediaType;
import org.jooby.json.Jackson;
import org.jooby.metrics.Metrics;
import org.jooby.swagger.SwaggerUI;
import org.jooby.whoops.Whoops;

import java.util.HashMap;

public class Server extends Jooby {
    private static final HashMap<String, String> VERSION = new HashMap<String, String>(){{
        put("version","0.0.1");
    }};

    {
        // JSON via Jackson
        ObjectMapper mapper = new ObjectMapper();
        use(new Jackson(mapper));

        // Error messages via Whoops
        use(new Whoops());

        // GuancialeDB
        GuancialeDB.init(1000000,10000000);
        lifeCycle(GuancialeDB.class);

        get("/", () -> "Hello World!").produces(MediaType.text);
        get("/break", req -> { throw new IllegalStateException("Something broke!"); });

        use("/db")
        .get("/",()-> VERSION);

        use(Node.class);

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
