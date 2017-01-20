import com.fasterxml.jackson.databind.ObjectMapper;
import com.maxdemarzi.server.Server;
import org.crsh.cli.*;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.jooby.Err;
import org.jooby.Status;

import javax.management.ObjectName;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Usage("Put")
public class put extends BaseCommand {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Usage("node <id> <properties> in JSON Format '{\"key\":value}'")
    @Named("node")
    @Command
    public void node(InvocationContext<ObjectName> context,
                     @Usage("the node id") @Required @Argument final String id,
                     @Usage("the properties") @Required @Argument final String properties) throws Exception {
        HashMap<String, Object> node = Server.db.getNode(id);
        if (node == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            Server.db.updateNode(id, properties);
            node = Server.db.getNode(id);
            context.append(mapper.writeValueAsString(node));
        }
    }

    @Usage("relationship <type> <from> <to> <properties> in JSON Format '{\"key\":value}'")
    @Named("relationship")
    @Command
    public void relationship(InvocationContext<ObjectName> context,
                             @Usage("the relationship type") @Required @Argument final String type,
                             @Usage("the starting node") @Required @Argument final String from,
                             @Usage("the ending node") @Required @Argument final String to,
                             @Usage("the properties") @Argument final String properties) throws Exception {
        HashMap<String, Object> rel = Server.db.getRelationship(type, from, to);
        if (rel == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            Server.db.updateRelationship(type, from, to, properties);
            rel = Server.db.getRelationship(type, from, to);
            context.append(mapper.writeValueAsString(rel));
        }
    }

    @Usage("node-properties <id> <properties> in JSON Format '{\"key\":value}'")
    @Named("node-properties")
    @Command
    public void nodeProperties(InvocationContext<ObjectName> context,
                               @Usage("the node id") @Required @Argument final String id,
                               @Usage("the properties") @Required @Argument final String properties) throws Exception {
        HashMap<String, Object> node = Server.db.getNode(id);
        if (node == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            HashMap<String, Object> propertiesMap;
            try {
                propertiesMap = mapper.readValue(properties, HashMap.class);
            } catch (IOException e) {
                throw new Err(Status.BAD_REQUEST);
            }
            node.putAll(propertiesMap);
            Server.db.updateNode(id, node);
            node = Server.db.getNode(id);
            context.append(mapper.writeValueAsString(node));
        }
    }

    @Usage("relationship-properties <type> <from> <to> <properties> in JSON Format '{\"key\":value}'")
    @Named("relationship-properties")
    @Command
    public void relationshipProperties(InvocationContext<ObjectName> context,
                                       @Usage("the relationship type") @Required @Argument final String type,
                                       @Usage("the starting node") @Required @Argument final String from,
                                       @Usage("the ending node") @Required @Argument final String to,
                                       @Usage("the properties") @Required @Argument final String properties) throws Exception {
        HashMap<String, Object> rel = Server.db.getRelationship(type, from, to);
        if (rel == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            HashMap<String, Object> propertiesMap;
            try {
                propertiesMap = mapper.readValue(properties, HashMap.class);
            } catch (IOException e) {
                throw new Err(Status.BAD_REQUEST);
            }
            rel.putAll(propertiesMap);
            Server.db.updateRelationship(type, from, to, rel);
            rel = Server.db.getRelationship(type, from, to);
            context.append(mapper.writeValueAsString(rel));
        }
    }
}