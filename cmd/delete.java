import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.server.Server;
import org.crsh.cli.Argument;
import org.crsh.cli.Command;
import org.crsh.cli.Named;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.jooby.Err;
import org.jooby.Status;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.HashMap;

@Usage("Delete")
public class delete extends BaseCommand {

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
            Server.db.removeNode(id);
            context.append(mapper.writeValueAsString(node));
        }
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
            Server.db.removeRelationship(type, from, to);
            context.append(mapper.writeValueAsString(rel));
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
            node.remove(key);
            Server.db.updateNode(id, node);
            node = Server.db.getNode(id);
            context.append(mapper.writeValueAsString(node));
        }
    }

    @Usage("relationship-property <type> <from> <to> <key>")
    @Named("relationship-property")
    @Command
    public void relationshipProperty(InvocationContext<ObjectName> context,
                                     @Usage("the relationship type") @Argument final String type,
                                     @Usage("the starting node") @Argument final String from,
                                     @Usage("the ending node") @Argument final String to,
                                     @Usage("the property key") @Argument final String key) throws Exception {
        HashMap<String, Object>  rel = Server.db.getRelationship(type, from, to);
        if (rel == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            rel.remove(key);
            Server.db.updateRelationship(type, from, to, rel);
            rel = Server.db.getRelationship(type, from, to);
            context.append(mapper.writeValueAsString(rel));
        }
    }

    @Usage("node-properties <id>")
    @Named("node-properties")
    @Command
    public void nodeProperties(InvocationContext<ObjectName> context,
                     @Usage("the node id") @Argument final String id) throws Exception {
        Object node = Server.db.getNode(id);
        if (node == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            Server.db.updateNode(id, new HashMap());
            node = Server.db.getNode(id);
            context.append(mapper.writeValueAsString(node));
        }
    }

    @Usage("relationship-properties <type> <from> <to>")
    @Named("relationship-properties")
    @Command
    public void relationshipProperties(InvocationContext<ObjectName> context,
                                       @Usage("the relationship type") @Argument final String type,
                                       @Usage("the starting node") @Argument final String from,
                                       @Usage("the ending node") @Argument final String to) throws Exception {
        Object rel = Server.db.getRelationship(type, from, to);
        if (rel == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            Server.db.deleteRelationshipProperties(type, from, to);
            rel = Server.db.getRelationship(type, from, to);
            context.append(mapper.writeValueAsString(rel));
        }
    }
}