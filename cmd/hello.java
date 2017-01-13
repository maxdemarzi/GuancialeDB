import org.crsh.cli.Command;
import org.crsh.cli.Usage;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;

public class hello extends BaseCommand {

    @Usage("Say Hello")
    @Command
    public Object main(InvocationContext<Object> context) {
        return "Hello";
    }
}