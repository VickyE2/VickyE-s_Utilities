/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.guiparent;

import static org.vicky.guiparent.GuiCreator.parseSlots;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.ecosystem.utils.ItemSerializer;
import org.vicky.guiparent.subGui.PagedGui;
import org.vicky.listeners.BaseGuiListener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * ButtonAction represents an action that can be triggered by a GUI button.
 * <p>
 * It supports various action types such as opening a GUI, closing a GUI, giving
 * an item, running a command, running custom code (via a lambda), or executing
 * a class. Additionally, it provides a chainable API that allows you to chain
 * multiple actions together. Each chained action receives both the
 * {@code Player} and the result of the previous action.
 * </p>
 *
 * <p>
 * Example chain usage:
 * </p>
 * 
 * <pre>
 * {@code
 * ButtonAction.chain(player -> {
 * 	// Initial action: return some result.
 * 	String result = "Initial Result";
 * 	return result;
 * }, plugin).thenBi((player, res) -> {
 * 	player.sendMessage("Received: " + res);
 * 	return res.length();
 * }).thenRun((player, len) -> {
 * 	player.sendMessage("Length: " + len);
 * }).thenWait(2000) // Wait for 2 seconds asynchronously
 * 		.thenRunAsync((player, len) -> {
 * 			player.sendMessage("Running heavy async task with result: " + len);
 *        }).finallyRun(player, (p, finalResult) -> {
 * 			p.sendMessage("Chain complete, final result: " + finalResult);
 *        });
 * }
 * </pre>
 *
 * @param <T>
 *            The type produced by the chainable action.
 */
@SuppressWarnings({"unchecked", "rawtypes", "unused"})
public class ButtonAction<T> {

    private final ActionType actionType;
    private final Runnable action;
    private final Class<? extends BaseGui> guiClass;
    private final Object actionData; // Data associated with the action.
    private final Consumer<Player> customAction; // Custom action for RUN_CODE.
    private final boolean closeGui;
    private final JavaPlugin main_plugin;
    // For chainable actions.
    private final Function<Player, T> chainAction;

    /**
     * Private constructor for chainable actions.
     *
     * @param action A function that, given a Player, produces a result of type T.
     * @param plugin The JavaPlugin instance.
     */
    private ButtonAction(Function<Player, T> action, JavaPlugin plugin) {
        this.chainAction = action;
        this.actionType = ActionType.CHAIN;
        this.action = null;
        this.guiClass = null;
        this.actionData = null;
        this.customAction = null;
        this.closeGui = true;
        this.main_plugin = plugin;
    }

	/**
	 * Constructs a ButtonAction with an action type, action data, and closeGui
	 * flag.
	 *
     * @param actionType
     *            The type of action.
     * @param actionData
     *            The data associated with the action.
     * @param closeGui
     *            Whether to close the GUI after executing the action.
     * @deprecated Use {@link #ofGeneric(ActionType, Object, boolean)} instead.
     */
    @Deprecated
    public ButtonAction(ActionType actionType, Object actionData, boolean closeGui) {
        this.actionType = actionType;
        this.actionData = actionData;
        this.chainAction = null;
        this.customAction = null;
        this.action = null;
        this.guiClass = null;
        this.closeGui = closeGui;
        this.main_plugin = null;
    }

    /**
     * Constructs a ButtonAction for opening a GUI.
     *
     * @param actionType
     *            The type of action.
     * @param guiClass
     *            The class of the GUI to open.
     * @param plugin
     *            The JavaPlugin instance.
     * @param closeGui
     *            Whether to close the current GUI before opening the new one.
     * @deprecated Use {@link #ofOpenGui(Class, JavaPlugin, boolean)} instead.
     */
    @Deprecated
    public ButtonAction(ActionType actionType, Class<? extends BaseGui> guiClass, JavaPlugin plugin, boolean closeGui) {
        this.actionType = actionType;
        this.guiClass = guiClass;
        this.customAction = null;
        this.chainAction = null;
        this.action = null;
        this.actionData = null;
        this.closeGui = closeGui;
        this.main_plugin = plugin;
    }

    /**
     * Constructs a ButtonAction with a runnable action.
     *
     * @param action
     *            The runnable action to execute.
     * @param closeGui
     *            Whether to close the GUI after executing the action.
     * @deprecated Use {@link #ofRunAction(Runnable, boolean)} instead.
     */
    @Deprecated
    public ButtonAction(ActionType actionType, Runnable action, boolean closeGui) {
        this.actionType = actionType;
        this.actionData = null;
        this.customAction = null;
        this.chainAction = null;
        this.guiClass = null;
        this.action = action;
        this.closeGui = closeGui;
        this.main_plugin = null;
    }

    /**
     * Constructs a ButtonAction with a custom Consumer action.
     *
     * @param customAction
     *            A Consumer&lt;Player&gt; representing the custom code to run.
     * @param closeGui
     *            Whether to close the GUI after executing the action.
     * @deprecated Use {@link #ofRunCode(Consumer, boolean)} instead.
     */
    @Deprecated
    public ButtonAction(Consumer<Player> customAction, boolean closeGui) {
        this.actionType = ActionType.RUN_CODE;
        this.customAction = customAction;
        this.closeGui = closeGui;
        this.actionData = null;
        this.chainAction = null;
        this.action = null;
        this.guiClass = null;
        this.main_plugin = null;
    }

    /**
     * Starts a chainable ButtonAction with the given starting action.
     *
     * @param action
     *            A function that takes a Player and returns a value of type T.
     * @param plugin
     *            The JavaPlugin instance.
     * @param <T>
     *            The type returned by the action.
     * @return A new chainable ButtonAction.
     */
    public static <T> ButtonAction<T> chain(Function<Player, T> action, JavaPlugin plugin) {
        return new ButtonAction<>(action, plugin);
    }

    /**
     * Creates a ButtonAction for opening a GUI.
     *
     * @param guiClass
     *            The class of the GUI to open.
     * @param plugin
     *            The JavaPlugin instance.
     * @param closeGui
     *            Whether to close the current GUI before opening the new one.
     * @return A new {@link ButtonAction} instance configured to open a GUI.
     */
    public static ButtonAction ofOpenGui(Class<? extends BaseGui> guiClass, JavaPlugin plugin, boolean closeGui) {
        return new ButtonAction(ActionType.OPEN_GUI, guiClass, plugin, closeGui);
    }

    /**
     * Creates a ButtonAction for giving an item to the player.
     *
     * @param item
     *            The ItemStack to give.
     * @param closeGui
     *            Whether to close the GUI after giving the item.
     * @return A new {@link ButtonAction} instance configured to give an item.
     */
    public static ButtonAction ofGiveItem(ItemStack item, boolean closeGui) {
        return new ButtonAction(ActionType.GIVE_ITEM, item, closeGui);
    }

    /**
     * Creates a ButtonAction for running a command.
     *
     * @param command
     *            The command string to run.
     * @param closeGui
     *            Whether to close the GUI after executing the command.
     * @return A new {@link ButtonAction} instance configured to run a command.
     */
    public static ButtonAction ofRunCommand(String command, boolean closeGui) {
        return new ButtonAction(ActionType.RUN_COMMAND, command, closeGui);
    }

    /**
     * Creates a ButtonAction for running custom code using a lambda.
     *
     * @param customAction
     *            A Consumer&lt;Player&gt; representing the custom code to execute.
     *            The lambda receives the player as its argument.
     * @param closeGui
     *            Whether to close the GUI after executing the custom code.
     * @return A new {@link ButtonAction} instance configured to run custom code.
     */
    public static ButtonAction ofRunCode(Consumer<Player> customAction, boolean closeGui) {
        return new ButtonAction(customAction, closeGui);
    }

    // --- Chainable Helper Methods --- //

    /**
     * Creates a ButtonAction for running a runnable action.
     *
     * @param action
     *            A Runnable representing the action to execute.
     * @param closeGui
     *            Whether to close the GUI after executing the action.
     * @return A new {@link ButtonAction} instance configured to run the given
     *         action.
     */
    public static ButtonAction ofRunAction(Runnable action, boolean closeGui) {
        return new ButtonAction(ActionType.RUN_CLASS, action, closeGui);
    }

    /**
     * Creates a generic ButtonAction with custom action data.
     * <p>
     * This method is intended for advanced usage where action data is used to
     * determine behavior.
     * </p>
     *
     * @param actionType
     *            The type of action.
     * @param actionData
     *            Arbitrary data associated with the action.
     * @param closeGui
     *            Whether to close the GUI after executing the action.
     * @return A new {@link ButtonAction} instance configured with the provided
     *         parameters.
     */
    public static ButtonAction ofGeneric(ActionType actionType, Object actionData, boolean closeGui) {
        return new ButtonAction(actionType, actionData, closeGui);
    }

    public static ButtonAction<?> fromJson(JsonObject json, JavaPlugin plugin) {
        String typeStr = json.get("type").getAsString();
        boolean closeGui = json.has("closeGui") && json.get("closeGui").getAsBoolean();

        ActionType type = ActionType.valueOf(typeStr.toUpperCase());

        switch (type) {
            case OPEN_GUI -> {
                String className = json.get("data").getAsString();
                try {
                    Class<?> clazz = Class.forName(className);
                    if (!BaseGui.class.isAssignableFrom(clazz)) {
                        throw new JsonParseException("Class is not a subtype of BaseGui: " + className);
                    }
                    return ButtonAction.ofOpenGui((Class<? extends BaseGui>) clazz, plugin, closeGui);
                } catch (ClassNotFoundException e) {
                    throw new JsonParseException("GUI class not found: " + className, e);
                }
            }
            case RUN_COMMAND -> {
                String command = json.get("data").getAsString();
                return ButtonAction.ofRunCommand(command, closeGui);
            }
            case GIVE_ITEM -> {
                JsonElement itemData = json.get("data");
                ItemStack item = ItemSerializer.deserializeItem(itemData);
                return ButtonAction.ofGiveItem(item, closeGui);
            }
            case RUN_CODE -> {
                throw new JsonParseException("RUN_CODE cannot be deserialized from JSON.");
            }
            case RUN_CLASS -> {
                String className = json.get("data").getAsString();
                try {
                    Class<?> clazz = Class.forName(className);
                    Runnable instance = (Runnable) clazz.getDeclaredConstructor().newInstance();
                    return ButtonAction.ofRunAction(instance, closeGui);
                } catch (Exception e) {
                    throw new JsonParseException("Failed to instantiate class: " + className, e);
                }
            }
            default -> throw new JsonParseException("Unsupported action type: " + type);
        }
    }

    static void refreshInventoryContents(Player player, BaseGui newGui) {
        Inventory current = player.getOpenInventory().getTopInventory();
        newGui.buildInventory(player).thenAccept(updated -> {
            BaseGuiListener listener = BaseGuiListener.getListenerInstance(newGui.listener.getClass());
            if (newGui instanceof PagedGui paged) {
                Set<Integer> slots = Optional.of(parseSlots(paged.getPageSlots())).orElse(Set.of());
                listener.removeButtons(current, slots.stream().mapToInt(Integer::intValue).toArray());
                int size = Math.min(current.getSize(), updated.getSize());
                for (int i = 0; i < size; i++) {
                    ItemStack currentItem = current.getItem(i);
                    ItemStack newItem = updated.getItem(i);
                    if (!Objects.equals(currentItem, newItem) && slots.contains(i)) {
                        current.setItem(i, newItem);
                    }
                    GuiCreator.ItemConfig config = findItemConfigAtSlot(paged.getItems(), i);
                    if (config != null && config.getButtonAction() != null) {
                        config.setSlotRange(String.valueOf(i + 1));
                        listener.registerButton(current, config);
                    }
                }
            } else {
                int size = Math.min(current.getSize(), updated.getSize());
                for (int i = 0; i < size; i++) {
                    ItemStack currentItem = current.getItem(i);
                    ItemStack newItem = updated.getItem(i);
                    if (!Objects.equals(currentItem, newItem)) {
                        current.setItem(i, newItem);
                    }
                    GuiCreator.ItemConfig config = findItemConfigAtSlot(newGui.getRegistered(), i);
                    if (config != null && config.getButtonAction() != null) {
                        listener.registerButton(current, config);
                    }
                }
            }
        });
    }

    // --- Deprecated constructors and static factory methods below --- //

    private static GuiCreator.ItemConfig findItemConfigAtSlot(List<GuiCreator.ItemConfig> configs, int slot) {
        for (GuiCreator.ItemConfig config : configs) {
            if (config.getSlotRange().isEmpty())
                continue;
            if (GuiCreator.parseSlots(config.getSlotRange()).contains(slot)) {
                return config;
            }
        }
        return null;
    }

    /**
     * Chains a subsequent action that transforms the result of the previous action.
     *
     * @param nextAction
     *            A BiFunction that receives the Player and the result of the
     *            current chain, and returns a new result.
     * @param <R>
     *            The type of the result of the next action.
     * @return A new ButtonAction representing the extended chain.
     */
    public <R> ButtonAction<R> thenBi(BiFunction<Player, T, R> nextAction) {
        return new ButtonAction<>(player -> nextAction.apply(player, chainAction.apply(player)), main_plugin);
    }

    /**
     * Chains a subsequent Runnable action that does not change the previous chain's
     * result.
     *
     * @param nextAction
     *            A BiConsumer that accepts the Player and the current chain result.
     * @return A new ButtonAction with the same result as the previous chain.
     */
    public ButtonAction<T> thenRun(BiConsumer<Player, T> nextAction) {
        return new ButtonAction<>(player -> {
            T previous = chainAction.apply(player);
            nextAction.accept(player, previous);
            return previous;
        }, main_plugin);
    }

    /**
     * Chains a wait period for the specified number of milliseconds without
     * blocking the main thread.
     * <p>
     * This method delays the continuation of the chain asynchronously.
     * </p>
     *
     * @param milliseconds
     *            The time to wait in milliseconds.
     * @return A new ButtonAction with the same result as the previous chain, after
     *         waiting.
     */
    public ButtonAction<T> thenWait(long milliseconds) {
        return new ButtonAction<>(player -> {
            T previous = chainAction.apply(player);
            CompletableFuture<Void> delay = CompletableFuture.runAsync(() -> {
            }, CompletableFuture.delayedExecutor(milliseconds, TimeUnit.MILLISECONDS));
            delay.join();
            return previous;
        }, main_plugin);
    }

    // --- New Static Factory Methods --- //

    /**
     * Chains an asynchronous action that transforms the result of the previous
     * chain.
     * <p>
     * The provided BiFunction is executed asynchronously.
     * </p>
     *
     * @param nextAction
     *            A BiFunction that receives the Player and the previous result, and
     *            returns a new result.
     * @param <R>
     *            The type of the result of the next action.
     * @return A new ButtonAction representing the extended chain.
     */
    public <R> ButtonAction<R> thenBiAsync(BiFunction<Player, T, R> nextAction) {
        return new ButtonAction<>(player -> CompletableFuture
                .supplyAsync(() -> nextAction.apply(player, chainAction.apply(player))).join(), main_plugin);
    }

    /**
     * Chains an asynchronous Runnable action that does not change the previous
     * chain's result.
     * <p>
     * The provided BiConsumer is executed asynchronously.
     * </p>
     *
     * @param nextAction
     *            A BiConsumer that receives the Player and the current chain
     *            result.
     * @return A new ButtonAction with the same result as the previous chain.
     */
    public ButtonAction<T> thenRunAsync(BiConsumer<Player, T> nextAction) {
        return new ButtonAction<>(player -> {
            T previous = CompletableFuture.supplyAsync(() -> chainAction.apply(player)).join();
            CompletableFuture.runAsync(() -> nextAction.accept(player, previous)).join();
            return previous;
        }, main_plugin);
    }

    /**
     * Terminates the chain by executing it asynchronously and then passing the
     * final result to a BiConsumer.
     *
     * @param player
     *            The player context for the chain execution.
     * @param finalAction
     *            A BiConsumer that accepts the Player and the result of the last
     *            action in the chain.
     */
    public void finallyRun(Player player, BiConsumer<Player, T> finalAction) {
        CompletableFuture.supplyAsync(() -> chainAction.apply(player))
                .thenAccept(result -> finalAction.accept(player, result));
    }

    /**
     * Immediately executes the chain with the provided player.
     *
     * @param player
     *            The player on which the chain action is executed.
     * @return The result of the chain action.
     */
    public T chainExecute(Player player) {
        return chainAction.apply(player);
    }

    /**
     * Chains an action to open a GUI while preserving the previous chain's result.
     * <p>
     * When executed, this action will first run all previous chain actions
     * (retaining their result), then optionally close the player's inventory and
     * open the specified GUI.
     * </p>
     *
     * @param guiClass
     *            The class of the GUI to open.
     * @param closeGui
     *            Whether to close the current GUI before opening the new one.
     * @return A new ButtonAction whose chain result is the same as the previous
     *         chain.
     */
    public ButtonAction<T> thenOpenGui(Class<? extends BaseGui> guiClass, boolean closeGui) {
        return new ButtonAction<>(player -> {
            T result = chainAction.apply(player);
            if (closeGui) {
                player.closeInventory();
            }
            try {
                if (PagedGui.class.isAssignableFrom(guiClass)) {
                    Constructor<? extends PagedGui> constructor = (Constructor<? extends PagedGui>) guiClass
                            .getDeclaredConstructor(JavaPlugin.class, int.class);
                    BaseGui gui = constructor.newInstance(main_plugin, 1);
                    gui.showGui(player);
                } else {
                    Constructor<? extends BaseGui> constructor = guiClass.getDeclaredConstructor(JavaPlugin.class);
                    BaseGui gui = constructor.newInstance(main_plugin);
                    gui.showGui(player);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }, main_plugin);
    }

    /**
     * Chains an action to give an item to the player while preserving the previous
     * chain's result.
     * <p>
     * When executed, this action will first run all previous chain actions
     * (retaining their result), then add the specified item to the player's
     * inventory.
     * </p>
     *
     * @param item
     *            The ItemStack to give.
     * @param closeGui
     *            Whether to close the GUI after giving the item.
     * @return A new ButtonAction whose chain result is the same as the previous
     *         chain.
     */
    public ButtonAction<T> thenGiveItem(ItemStack item, boolean closeGui) {
        return new ButtonAction<>(player -> {
            T result = chainAction.apply(player);
            player.getInventory().addItem(item);
            if (closeGui) {
                player.closeInventory();
            }
            return result;
        }, main_plugin);
    }

    /**
     * Chains an action to close the player's GUI while preserving the previous
     * chain's result.
     * <p>
     * When executed, this action will first run all previous chain actions
     * (retaining their result), then close the player's current inventory.
     * </p>
     *
     * @return A new ButtonAction whose chain result is the same as the previous
     *         chain.
     */
    public ButtonAction<T> thenCloseGui() {
        return new ButtonAction<>(player -> {
            T result = chainAction.apply(player);
            Bukkit.getScheduler().scheduleSyncDelayedTask(main_plugin, player::closeInventory);
            return result;
        }, main_plugin);
    }

    /**
     * Chains an action to run a command as the console while preserving the
     * previous chain's result.
     * <p>
     * When executed, this action will first run all previous chain actions
     * (retaining their result), then dispatch the specified command (with
     * "%player%" replaced by the player's name).
     * </p>
     *
     * @param command
     *            The command string to run.
     * @param closeGui
     *            Whether to close the GUI after executing the command.
     * @return A new ButtonAction whose chain result is the same as the previous
     *         chain.
     */
    public ButtonAction<T> thenRunCommand(String command, boolean closeGui) {
        return new ButtonAction<>(player -> {
            T result = chainAction.apply(player);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
            if (closeGui) {
                player.closeInventory();
            }
            return result;
        }, main_plugin);
    }

    public ButtonAction<T> thenUpdateGui(Class<? extends BaseGui> guiClass, int page) {
        return new ButtonAction<>(player -> {
            T result = chainAction.apply(player);
            try {
                if (PagedGui.class.isAssignableFrom(guiClass)) {
                    Constructor<? extends PagedGui> constructor = (Constructor<? extends PagedGui>) guiClass
                            .getDeclaredConstructor(JavaPlugin.class, int.class);
                    BaseGui newGui = constructor.newInstance(main_plugin, page);
                    refreshInventoryContents(player, newGui);
                } else {
                    Constructor<? extends BaseGui> constructor = guiClass.getDeclaredConstructor(JavaPlugin.class);
                    BaseGui newGui = constructor.newInstance(main_plugin);
                    refreshInventoryContents(player, newGui);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }, main_plugin);
    }

    public ButtonAction<T> thenUpdateGui(Class<? extends BaseGui> guiClass) {
        return thenUpdateGui(guiClass, 1);
    }

    /**
     * Executes the action associated with this ButtonAction.
     * <p>
     * Depending on the action type, this method performs the following:
     * <ul>
     * <li>OPEN_GUI: Instantiates and displays the GUI using the provided GUI
     * class.</li>
     * <li>CLOSE_GUI: Closes the player's current inventory.</li>
     * <li>GIVE_ITEM: Gives the player an item if actionData is an ItemStack.</li>
     * <li>RUN_COMMAND: Executes a command as the console, replacing "%player%" with
     * the player's name.</li>
     * <li>RUN_CODE: Executes the custom Consumer action with the player as an
     * argument.</li>
     * <li>RUN_CLASS: Executes the runnable action if actionData is a class.</li>
     * </ul>
     *
     * @param player
     *            The player triggering the action.
     * @param plugin
     *            The Plugin instance used for logging errors and warnings.
     */
    public void execute(Player player, Plugin plugin) {
        switch (actionType) {
            case OPEN_GUI:
                if (guiClass == null) {
                    plugin.getLogger().severe("GUI class is null for action type: OPEN_GUI");
                    return;
                }
                if (closeGui) {
                    player.closeInventory();
                }
                try {
                    if (PagedGui.class.isAssignableFrom(guiClass)) {
                        Constructor<? extends PagedGui> constructor = (Constructor<? extends PagedGui>) guiClass
                                .getDeclaredConstructor(JavaPlugin.class, int.class);
                        BaseGui gui = constructor.newInstance(main_plugin, 1);
                        gui.showGui(player);
                    } else {
                        Constructor<? extends BaseGui> constructor = guiClass.getDeclaredConstructor(JavaPlugin.class);
                        BaseGui gui = constructor.newInstance(main_plugin);
                        gui.showGui(player);
                    }
                } catch (NoSuchMethodException e) {
                    plugin.getLogger().severe("No matching constructor found: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to open GUI: " + e.getMessage());
                    e.printStackTrace();
                }
                break;
            case CLOSE_GUI:
                player.closeInventory();
                break;
            case GIVE_ITEM:
                if (actionData instanceof ItemStack) {
                    player.getInventory().addItem((ItemStack) actionData);
                    if (closeGui) {
                        player.closeInventory();
                    }
                }
                break;
            case RUN_COMMAND:
                if (actionData instanceof String command) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                    if (closeGui) {
                        player.closeInventory();
                    }
                }
                break;
            case RUN_CODE:
                if (customAction != null) {
                    if (closeGui) {
                        player.closeInventory();
                    }
                    customAction.accept(player);
                }
                break;
            case RUN_CLASS:
                if (actionData instanceof Class<?>) {
                    if (closeGui) {
                        player.closeInventory();
                    }
                    if (action != null) {
                        action.run();
                    }
                }
                break;
            default:
                plugin.getLogger().warning("Unhandled action type: " + actionType);
                break;
        }
    }

    /**
     * Gets the action type of this ButtonAction.
     *
     * @return the {@link ActionType} representing this action's category.
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Gets the custom action (lambda) associated with this ButtonAction, if any.
     *
     * @return a {@link Consumer<Player>} representing the custom action, or null if
     *         not defined.
     */
    public Consumer<Player> getCustomAction() {
        return customAction;
    }

    /**
     * Gets the runnable action associated with this ButtonAction, if any.
     *
     * @return a {@link Runnable} representing the action logic, or null if not
     *         defined.
     */
    public Runnable getAction() {
        return action;
    }

    /**
     * Gets the action data associated with this ButtonAction.
     *
     * @return an {@link Object} representing additional data for the action.
     */
    public Object getActionData() {
        return actionData;
    }

    /**
     * Enumeration representing the type of action.
     */
    public enum ActionType {
        /**
         * Open a new GUI.
         */
        OPEN_GUI,
        /**
         * Makes a button action chain.
         */
        CHAIN,
        /**
         * Close the current GUI.
         */
        CLOSE_GUI,
        /**
         * Give the player an item.
         */
        GIVE_ITEM,
        /**
         * Run a command.
         */
        RUN_COMMAND,
        /**
         * Run custom code (a lambda function).
         */
        RUN_CODE,
        /**
         * Run a class-based action.
         */
        RUN_CLASS
    }
}
