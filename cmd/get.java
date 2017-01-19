import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.server.Server;
import org.crsh.cli.*;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.jooby.Err;
import org.jooby.Status;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Usage("node-property <id> <key>")
    @Named("node-property")
    @Command
    public void nodeProperty(InvocationContext<ObjectName> context,
                             @Usage("the node id") @Argument final String id,
                             @Usage("the property key") @Argument final String key) throws Exception {
        HashMap<String, Object> node = Server.db.getNode(id);
        if (node == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            if (node.containsKey(key)) {
                context.append(mapper.writeValueAsString(node.get(key)));
            } else {
                throw new Err(Status.NOT_FOUND);
            }
        }
    }

    @Usage("node-degree <id>")
    @Named("node-degree")
    @Command
    public void nodeDegree(InvocationContext<ObjectName> context,
                           @Usage("the node id") @Argument final String id,
                           @Usage("only incoming relationships") @Option(names = {"in","incoming"}) final Boolean incoming,
                           @Usage("only outgoing relationships") @Option(names = {"out","outgoing"}) final Boolean outgoing,
                           @Usage("the relationship types") @Option(names="types") List<String> types) throws Exception {
        Object node = Server.db.getNode(id);
        if (node == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            if(types == null) {
                types = new ArrayList<String>();
            }
            if(Boolean.TRUE.equals(incoming) ) {
                context.append(Server.db.getNodeDegree(id, "in", types ).toString());
            } else if (Boolean.TRUE.equals(outgoing)) {
                context.append(Server.db.getNodeDegree(id, "out", types).toString());
            } else {
                context.append(Server.db.getNodeDegree(id, "all", types).toString());
            }
        }
    }

    @Usage("relationship-types")
    @Named("relationship-types")
    @Command
    public void relationshipTypes(InvocationContext<ObjectName> context) throws Exception {
        List<String> types = Server.db.getRelationshipTypes();
        context.append(mapper.writeValueAsString(types));
    }


    @Usage("relationship <type> <from> <to>")
    @Named("relationship")
    @Command
    public void relationship(InvocationContext<ObjectName> context,
                             @Usage("the relationship type") @Argument final String type,
                             @Usage("the starting node") @Argument final String from,
                             @Usage("the ending node") @Argument final String to) throws Exception {
        Object rel = Server.db.getRelationship(type, from, to);
        if (rel == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            context.append(mapper.writeValueAsString(rel));
        }
    }
}
