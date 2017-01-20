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
import java.util.Map;
import java.util.Properties;

@Usage("Post")
public class post extends BaseCommand {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Usage("node <id> <properties> in JSON Format '{\"key\":value}'")
    @Named("node")
    @Command
    public void node(InvocationContext<ObjectName> context,
                      @Usage("the node id") @Argument final String id,
                      @Usage("the properties") @Argument final String properties) throws Exception {
        if (properties == null) {
            Server.db.addNode(id);
        } else {
            Server.db.addNode(id, properties);
        }
        Object node = Server.db.getNode(id);
        if (node == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            context.append(mapper.writeValueAsString(node));
        }
    }

    @Usage("relationship <type> <from> <to> <properties> in JSON Format '{\"key\":value}'")
    @Named("relationship")
    @Command
    public void relationship(InvocationContext<ObjectName> context,
                             @Usage("the relationship type") @Argument final String type,
                             @Usage("the starting node") @Argument final String from,
                             @Usage("the ending node") @Argument final String to,
                             @Usage("the properties") @Argument final String properties) throws Exception {
        if (properties == null) {
            Server.db.addRelationship(type, from, to);
        } else {
            Server.db.addRelationship(type, from, to, properties);
        }
        Object rel = Server.db.getRelationship(type, from, to);
        if (rel == null) {
            throw new Err(Status.NOT_FOUND);
        } else {
            context.append(mapper.writeValueAsString(rel));
        }
    }
}