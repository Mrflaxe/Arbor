package by.mrflaxe.arbor.core.message;

@FunctionalInterface
public interface MessageSender<S> {

    void send(S source, String message);
}
