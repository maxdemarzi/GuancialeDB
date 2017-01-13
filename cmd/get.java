import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.server.Server;
import org.crsh.cli.*;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.jooby.Err;
import org.jooby.Status;

import javax.management.ObjectName;
import java.util.List;

@Usage("Get")
public class get extends BaseCommand {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Usage("node <id>")
    @Named("node")
    @Command
    public void node(InvocationContext<ObjectName> context,
                     @Usage("the node id") @Argument final String id) throws Exception {
        Object node = Server.db.getNode(id);
        if (node == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            context.append(mapper.writeValueAsString(node));
        }
    }

    @Usage("node-degree <id>")
    @Named("node-degree")
    @Command
    public void node(InvocationContext<ObjectName> context,
                     @Usage("the node id") @Argument final String id,
                     @Usage("only incoming relationships") @Option(names = {"in","incoming"}) final Boolean incoming,
                     @Usage("only outgoing relationships") @Option(names = {"out","outgoing"}) final Boolean outgoing,
                     @Usage("the relationship types") @Option(names="types") List<String> types) throws Exception {
        Object node = Server.db.getNode(id);
        if (node == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            if(Boolean.TRUE.equals(incoming) ) {
                context.append(Server.db.getNodeDegree(id, "in", types ).toString());
            } else if (Boolean.TRUE.equals(outgoing)) {
                context.append(Server.db.getNodeDegree(id, "out", types).toString());
            } else {
                context.append(Server.db.getNodeDegree(id, "all", types).toString());
            }
        }
    }
}
