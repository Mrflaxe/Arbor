# Arbor

Arbor is a declarative command framework built on top of [Brigadier](https://github.com/Mojang/brigadier) — the command library used in Minecraft. It solves the biggest pain points of working with Brigadier directly:

- **Brigadier is verbose.** Registering a command with two argument overloads requires building and wiring a tree of `LiteralArgumentBuilder` and `RequiredArgumentBuilder` nodes by hand, and getting the execution order wrong causes silent bugs.
- **Type safety is lost at execution time.** Brigadier stores parsed values by name as `Object`, so every executor has to cast.
- **Argument parsing and suggestion logic is scattered.** Each `ArgumentType` duplicates parse + suggest in one anonymous class per registration site.
- **Error messages are ugly.** Brigadier throws a raw `CommandSyntaxException` with a cursor position and a machine-readable message. Players see something like `Incorrect argument for command at position 8: greet <--[HERE]`.

Arbor replaces the Brigadier boilerplate with a small, strongly-typed API. You declare what a command looks like, plug in argument definitions and executors, and let the assembler build the Brigadier tree for you.

---

## Concepts

### Definitions

Everything in Arbor is a **definition** — a plain Java object that describes a piece of the command tree. There are two kinds:

| Interface | Purpose |
|---|---|
| `CommandDefinition<S>` | A leaf command that can be executed (e.g. `/kick`, `/greet`) |
| `NamespaceDefinition` | A grouping node that holds subcommands (e.g. `/admin`) |

Both implement the marker interface `Definition` and are registered in a `DefinitionRegistry`.

### Signatures

A `CommandSignature<S>` is one **overload** of a command — a specific combination of arguments and an executor. 
A single `CommandDefinition` can expose multiple signatures; the assembler translates each one into a separate branch in the Brigadier tree.

### Arguments

An `ArgumentDefinition<S, T>` owns everything related to one argument type:
- its name (used as the Brigadier node name and for typed retrieval)
- its Java type (`Class<T>`)
- how to parse a raw string into `T` (throws `ArgumentParseException` on bad input)
- how to generate tab-completion suggestions

### Assembly

`BrigadierDispatcherAssembler<S>` reads a `DefinitionRegistry`, validates all signatures, builds the Brigadier dispatcher, 
and returns a `CommandProcessor<S>` — a simple facade for executing commands and fetching suggestions.

---

## Quick Start

### 1. Define a command

```java
public class GreetCommand implements CommandDefinition<CommandSender> {

    @Override
    public String getName() {
        return "greet";
    }

    @Override
    public List<CommandSignature<CommandSender>> getCommandSignatures() {
        return List.of(
            Signature.of(this::greetSelf),
            Signature.of(PlayerArgument.INSTANCE, this::greetTarget)
        );
    }

    private ExecutionResult greetSelf(CommandSender sender) {
        sender.sendMessage("Hello, " + sender.getName() + "!");
        return ExecutionResult.success();
    }

    private ExecutionResult greetTarget(CommandSender sender, Player target) {
        sender.sendMessage("Hello, " + target.getName() + "!");
        target.sendMessage(sender.getName() + " says hello!");
        return ExecutionResult.success();
    }
}
```

`/greet` calls `greetSelf`. `/greet <player>` calls `greetTarget`. Both overloads live in one class with zero Brigadier boilerplate.

### 2. Register and start (Bukkit)

In your plugin's `onEnable()`:

```java
@Override
public void onEnable() {
    Arbor.create(this)
            .register(new GreetCommand())
            .start();
}
```

`Arbor.create(this)` detects the active platform automatically via `ServiceLoader` — no manual wiring needed. The framework sets up Bukkit's executor and tab-completer for every registered command.

> **Note:** Each root command name must still be declared in `plugin.yml`. Bukkit requires this regardless of how the executor is registered.

```yaml
# plugin.yml
commands:
  greet:
    description: Greet a player
```

---

## API Reference

### `Arbor` — entry point

`Arbor.create(platformContext)` is the single entry point for library consumers. It uses `ServiceLoader` to find the platform implementation that accepts the given context object, then returns a fluent `ArborAPI` for registering definitions.

```java
// Bukkit
Arbor.create(this)           // "this" = JavaPlugin
        .register(new GreetCommand())
        .register(new BanCommand())
        .start();
```

If no platform module is on the classpath that understands the given context type, `create()` throws `IllegalStateException` with a descriptive message.

`register()` accepts any `Definition` — `CommandDefinition`, `NamespaceDefinition`, or any custom implementation. `start()` triggers assembly and hands the result to the detected platform.

---

### `CommandDefinition<S>`

```java
public interface CommandDefinition<S> extends Definition {
    String getName();
    default @Nullable String getParent() { return null; }
    default List<RequirementDefinition> getRequirements() { return List.of(); }
    default List<CommandSignature<S>> getCommandSignatures() { return List.of(); }
}
```

Return a non-null parent name to nest this command under a `NamespaceDefinition`. The assembler will attach it as a subcommand.

---

### `NamespaceDefinition`

```java
public interface NamespaceDefinition extends Definition {
    String getNamespace();
    default @Nullable String getParent() { return null; }
}
```

Used to create grouping nodes. A namespace has no executor of its own — it is a container for subcommands.

```java
// /admin kick, /admin ban — both under the "admin" namespace
Arbor.create(this)
        .register((NamespaceDefinition) () -> "admin")
        .register(new KickCommand())  // KickCommand.getParent() returns "admin"
        .register(new BanCommand())   // BanCommand.getParent() returns "admin"
        .start();
```

---

### `Signature` — factory class

`Signature` provides static factories for the most common cases so you can use method references directly.

| Factory | Use when |
|---|---|
| `Signature.of(SenderExecutor<S>)` | command with no arguments |
| `Signature.of(ArgumentDefinition<S,T>, SingleArgExecutor<S,T>)` | command with one typed argument |
| `Signature.builder()` | command with two or more arguments |

**No arguments:**
```java
Signature.of(this::execute)

private ExecutionResult execute(CommandSender sender) { ... }
```

**One argument:**
```java
Signature.of(PlayerArgument.INSTANCE, this::execute)

private ExecutionResult execute(CommandSender sender, Player target) { ... }
```

**Multiple arguments (builder):**
```java
Signature.<CommandSender>builder()
    .arg(PlayerArgument.INSTANCE)
    .arg(DurationArgument.INSTANCE)
    .executes(ctx -> execute(
            ctx.getSource(),
            ctx.get(PlayerArgument.INSTANCE),
            ctx.get(DurationArgument.INSTANCE)))
    .build()
```

---

### `ArgumentDefinition<S, T>`

Implement this interface to add a new argument type.

```java
public interface ArgumentDefinition<S, T> {
    String getName();
    Class<T> getValueType();
    T parse(String input) throws ArgumentParseException;
    List<String> suggest(S source, String input);
}
```

Example — an argument that resolves online players on a Bukkit server:

```java
public class PlayerArgument implements ArgumentDefinition<CommandSender, Player> {

    public static final PlayerArgument INSTANCE = new PlayerArgument();

    @Override public String getName() { return "player"; }
    @Override public Class<Player> getValueType() { return Player.class; }

    @Override
    public Player parse(String input) throws ArgumentParseException {
        Player player = Bukkit.getPlayer(input);
        if (player == null) {
            throw new ArgumentParseException("Player '" + input + "' not found or not online");
        }
        return player;
    }

    @Override
    public List<String> suggest(CommandSender source, String input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}
```

**Rules for argument names:**
- The name becomes the Brigadier node name. It must be unique within a single signature path.
- Arbor prevents you from registering a `String` argument alongside another type at the same branching point — `String` matches anything, so the other branch would be unreachable.

---

### `ExecutionResult`

The return value of every executor. Use the static factories:

```java
ExecutionResult.success()
ExecutionResult.failure("your.message-key")
ExecutionResult.failure("your.message-key").with("player", target.getName())
```

The `messageKey` is an opaque string — what you do with it is up to your `ErrorHandler`. The `placeholders` map carries any values the error message template needs.

---

### `CommandContext<S>`

Passed to `CommandExecutor` and `builder`-style executors. Provides access to the source and typed argument values.

```java
S source = ctx.getSource();
Player target = ctx.get(PlayerArgument.INSTANCE);
```

`get()` is typed by the `ArgumentDefinition` you pass in — no casting, no string keys.

---

### `BrigadierDispatcherAssembler<S>`

```java
// Without error handling
CommandProcessor<S> processor = assembler.assemble(registry);

// With error handling
CommandProcessor<S> processor = assembler.assemble(registry, errorHandler);
```

The assembler validates all signatures before building. It throws:
- `IllegalArgumentException` — duplicate signatures on the same command, or a `String` argument coexisting with another type at the same branching point
- `IllegalStateException` — a command declares a parent that was not registered

---


## Error Handling (still in work)

Without an `ErrorHandler`, parse errors silently return `ExecutionResult.failure("command-error")` and execution failures are ignored. Add an `ErrorHandler` to display messages to the sender.

### Built-in components

| Class | Role |
|---|---|
| `MessageKey` | Bridges a code constant to a YAML path string |
| `CoreMessageKey` | Built-in keys: `COMMAND_ERROR` → `core.command-error` |
| `MessageProvider` | Returns raw template strings by key |
| `DefaultMessageProvider` | Hardcoded English fallback |
| `MessageSender<S>` | Delivers a rendered string to the sender |
| `ErrorHandler<S>` | Receives parse errors and execution failures |
| `DefaultErrorHandler<S>` | Renders templates, substitutes placeholders, delivers via `MessageSender` |

### Placeholder substitution

`DefaultErrorHandler` substitutes the following placeholders in the `core.command-error` template:

| Placeholder | Value |
|---|---|
| `{detail}` | The raw error message from Brigadier |
| `{usage}` | Auto-generated usage string (all signatures of the command) |
| `{input}` | The full input string the player typed |
| `{cursor}` | A `^^^` marker positioned under the bad token |

### Minimal setup

```java
MessageProvider messages = new DefaultMessageProvider();
MessageSender<CommandSender> sender = (source, msg) -> source.sendMessage(msg);
ErrorHandler<CommandSender> errors = new DefaultErrorHandler<>(messages, sender);

CommandProcessor<CommandSender> processor = assembler.assemble(registry, errors);
```

### YAML messages (Bukkit)

`YamlMessageProvider` (in `platform-bukkit`) reads a flat YAML file and serves templates by dotted key path.

```yaml
# messages.yml
core:
  command-error: "&cError: &f{detail}\n&7Usage: &f{usage}"
```

```java
MessageProvider messages = YamlMessageProvider.fromFile(new File(dataFolder, "messages.yml"));
```

The default template is bundled in `platform-bukkit/src/main/resources/default-messages.yml` as a reference.

### Execution failure with consumer-defined placeholders

```java
private ExecutionResult run(CommandSender sender) {
    if (!sender.hasPermission("my.plugin.use")) {
        return ExecutionResult.failure("errors.no-permission")
                .with("permission", "my.plugin.use");
    }
    return ExecutionResult.success();
}
```

The `messageKey` and `placeholders` are passed to `ErrorHandler.onExecutionFailure()`. How you resolve and render them is up to your implementation — `DefaultErrorHandler` sends the raw key as-is, which is a reasonable starting point.

---

## Signature Validation

The assembler rejects ambiguous command trees at startup rather than silently misbehaving at runtime.

**Duplicate signatures** — two signatures with identical argument name sequences on the same command:
```
// throws IllegalArgumentException
List.of(
    Signature.of(PlayerArgument.INSTANCE, this::run),
    Signature.of(PlayerArgument.INSTANCE, this::run)  // duplicate
)
```

**String shadowing** — a `String` argument at the same branching point as a typed argument. Because `String` matches anything, the other branch can never be reached:
```
// throws IllegalArgumentException
List.of(
    Signature.of(NameArgument.INSTANCE, this::runWithName),  // String
    Signature.of(PlayerArgument.INSTANCE, this::runWithPlayer)  // also first arg
)
```

**Allowed** — same named argument at the first position, different lengths. This is fine because Brigadier merges nodes with the same name and distinguishes branches by depth:
```
List.of(
    Signature.of(PlayerArgument.INSTANCE, this::kick),
    Signature.<CommandSender>builder()
        .arg(PlayerArgument.INSTANCE)
        .arg(ReasonArgument.INSTANCE)
        .executes(ctx -> kick(ctx.getSource(), ctx.get(PlayerArgument.INSTANCE), ctx.get(ReasonArgument.INSTANCE)))
        .build()
)
```