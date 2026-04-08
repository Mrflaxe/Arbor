package by.mrflaxe.arbor.api;

import by.mrflaxe.arbor.core.Platform;

import java.util.ServiceLoader;

public final class Arbor {

    private Arbor() {}

    public static ArborAPI create(Object platformContext) {
        Platform platform = ServiceLoader.load(Platform.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .filter(p -> p.supports(platformContext.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No platform found for " + platformContext.getClass().getName()
                        + ". Make sure the appropriate Arbor platform module is on the classpath."
                ));

        return new ArborApiImpl(platform, platformContext);
    }
}
