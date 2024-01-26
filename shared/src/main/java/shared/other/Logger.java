package shared.other;

import java.time.Instant;

public class Logger
{
    public void log(String message)
    {
        System.out.println(Instant.now() + "\n" + message + "\n\n");
    }

    public void say(Object message)
    {
        System.out.println(message + "\n\n");
    }
}
