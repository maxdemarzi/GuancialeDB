import com.maxdemarzi.server.Server;
import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;

public class version extends BaseCommand {

    @Usage("Say Guanciale DB version")
    @Command
    public Object main(InvocationContext<Object> context) {
        return Server.getVersion();
    }
}