package com.maxdemarzi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooby.Jooby;
import org.jooby.json.Jackson;
import org.jooby.whoops.Whoops;

import java.util.HashMap;

public class Server extends Jooby {
    private static final HashMap<String, String> VERSION = new HashMap<String, String>(){{
        put("version","0.0.1");
    }};

  {
      // GuancialeDB
      GuancialeDB.init(1000000,10000000);
      lifeCycle(GuancialeDB.class);
      //use(new GuancialeDB(1000000,10000000));

    // JSON via Jackson
    ObjectMapper mapper = new ObjectMapper();
    use(new Jackson(mapper));

    // Error messages via Whoops
    use(new Whoops());


      get("/", () -> "Hello World!");
      get("/break", req -> { throw new IllegalStateException("Something broke!"); });

      use("/db")
      .get("/",()-> VERSION);
      use(Node.class);

  }

  public static void main(final String[] args) {
    run(Server::new, args);
  }

}
