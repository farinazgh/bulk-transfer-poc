package luna;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

/**
 * Azure Function triggered by Event Grid.
 */
public class Function {
    @FunctionName("EventGridListener")
    public void run(
            @EventGridTrigger(name = "event") EventSchema event,
            final ExecutionContext context) {
        context.getLogger().info("Event content: ");
        context.getLogger().info("Subject: " + event.subject);
        context.getLogger().info("Time: " + event.eventTime); // automatically converted to Date by the runtime
        context.getLogger().info("Id: " + event.id);
        context.getLogger().info("Data: " + event.data);
    }
}
