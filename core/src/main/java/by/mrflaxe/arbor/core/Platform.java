package by.mrflaxe.arbor.core;

public interface Platform {

    boolean supports(Class<?> contextType);

    void initialize(Object context, DefinitionRegistry registry);
}
