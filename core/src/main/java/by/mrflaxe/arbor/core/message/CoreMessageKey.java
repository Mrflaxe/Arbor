package by.mrflaxe.arbor.core.message;

public enum CoreMessageKey implements MessageKey {

    COMMAND_ERROR("core.command-error");

    private final String path;

    CoreMessageKey(String path) {
        this.path = path;
    }

    @Override
    public String path() {
        return path;
    }
}
