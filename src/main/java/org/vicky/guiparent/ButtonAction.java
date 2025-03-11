/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ButtonAction represents an action that can be triggered by a GUI button.
 * <p>
 * It supports various action types such as opening a GUI, closing a GUI,
 * giving an item, running a command, running custom code (via a lambda), or executing a class.
 * </p>
 *
 * <p>
 * This class now provides modern, lambda-friendly static factory methods for creating instances,
 * and the older constructors are marked as deprecated tho still used.....I suggest using the static factories...
 * </p>
 */
public class ButtonAction {

  /**
   * Enumeration representing the type of action.
   */
  public enum ActionType {
    /** Open a new GUI. */
    OPEN_GUI,
    /** Close the current GUI. */
    CLOSE_GUI,
    /** Give the player an item. */
    GIVE_ITEM,
    /** Run a command. */
    RUN_COMMAND,
    /** Run custom code (a lambda function). */
    RUN_CODE,
    /** Run a class-based action. */
    RUN_CLASS
  }

  private final ActionType actionType;
  private final Runnable action;
  private final Class<? extends BaseGui> guiClass;
  private final Object actionData; // Data associated with the action
  private final Consumer<Player> customAction; // Custom action for RUN_CODE
  private final boolean closeGui;
  private final JavaPlugin main_plugin;

  // Deprecated Constructors

  /**
   * Constructs a ButtonAction with an action type, action data, and closeGui flag.
   *
   * @param actionType The type of action.
   * @param actionData The data associated with the action.
   * @param closeGui   Whether to close the GUI after executing the action.
   * @deprecated Use {@link #ofGeneric(ActionType, Object, boolean)} instead.
   */
  @Deprecated
  public ButtonAction(ActionType actionType, Object actionData, boolean closeGui) {
    this.actionType = actionType;
    this.actionData = actionData;
    this.customAction = null;
    this.action = null;
    this.guiClass = null;
    this.closeGui = closeGui;
    this.main_plugin = null;
  }

  /**
   * Constructs a ButtonAction for opening a GUI.
   *
   * @param actionType The type of action.
   * @param guiClass   The class of the GUI to open.
   * @param plugin     The JavaPlugin instance.
   * @param closeGui   Whether to close the current GUI before opening the new one.
   * @deprecated Use {@link #ofOpenGui(Class, JavaPlugin, boolean)} instead.
   */
  @Deprecated
  public ButtonAction(
      ActionType actionType,
      Class<? extends BaseGui> guiClass,
      JavaPlugin plugin,
      boolean closeGui) {
    this.actionType = actionType;
    this.guiClass = guiClass;
    this.customAction = null;
    this.action = null;
    this.actionData = null;
    this.closeGui = closeGui;
    this.main_plugin = plugin;
  }

  /**
   * Constructs a ButtonAction with a runnable action.
   *
   * @param action   The runnable action to execute.
   * @param closeGui Whether to close the GUI after executing the action.
   * @deprecated Use {@link #ofRunAction(Runnable, boolean)} instead.
   */
  @Deprecated
  public ButtonAction(ActionType actionType, Runnable action, boolean closeGui) {
    this.actionType = actionType;
    this.actionData = null;
    this.customAction = null;
    this.guiClass = null;
    this.action = action;
    this.closeGui = closeGui;
    this.main_plugin = null;
  }

  /**
   * Constructs a ButtonAction with a custom Consumer action.
   *
   * @param customAction A Consumer&lt;Player&gt; representing the custom code to run.
   * @param closeGui     Whether to close the GUI after executing the action.
   * @deprecated Use {@link #ofRunCode(Consumer, boolean)} instead.
   */
  @Deprecated
  public ButtonAction(Consumer<Player> customAction, boolean closeGui) {
    this.actionType = ActionType.RUN_CODE;
    this.customAction = customAction;
    this.closeGui = closeGui;
    this.actionData = null;
    this.action = null;
    this.guiClass = null;
    this.main_plugin = null;
  }

  // New Static Factory Methods

  /**
   * Creates a ButtonAction for opening a GUI.
   *
   * @param guiClass The class of the GUI to open.
   * @param plugin   The JavaPlugin instance.
   * @param closeGui Whether to close the current GUI before opening the new one.
   * @return A new {@link ButtonAction} instance configured to open a GUI.
   */
  public static ButtonAction ofOpenGui(
      Class<? extends BaseGui> guiClass, JavaPlugin plugin, boolean closeGui) {
    return new ButtonAction(ActionType.OPEN_GUI, guiClass, plugin, closeGui);
  }

  /**
   * Creates a ButtonAction for giving an item to the player.
   *
   * @param item     The ItemStack to give.
   * @param closeGui Whether to close the GUI after giving the item.
   * @return A new {@link ButtonAction} instance configured to give an item.
   */
  public static ButtonAction ofGiveItem(ItemStack item, boolean closeGui) {
    return new ButtonAction(ActionType.GIVE_ITEM, item, closeGui);
  }

  /**
   * Creates a ButtonAction for running a command.
   *
   * @param command  The command string to run.
   * @param closeGui Whether to close the GUI after executing the command.
   * @return A new {@link ButtonAction} instance configured to run a command.
   */
  public static ButtonAction ofRunCommand(String command, boolean closeGui) {
    return new ButtonAction(ActionType.RUN_COMMAND, command, closeGui);
  }

  /**
   * Creates a ButtonAction for running custom code using a lambda.
   *
   * @param customAction A Consumer&lt;Player&gt; representing the custom code to execute.
   * The lambda receives the player as its argument.
   * @param closeGui Whether to close the GUI after executing the custom code.
   * @return A new {@link ButtonAction} instance configured to run custom code.
   */
  public static ButtonAction ofRunCode(Consumer<Player> customAction, boolean closeGui) {
    return new ButtonAction(customAction, closeGui);
  }

  /**
   * Creates a ButtonAction for running a runnable action.
   *
   * @param action   A Runnable representing the action to execute.
   * @param closeGui Whether to close the GUI after executing the action.
   * @return A new {@link ButtonAction} instance configured to run the given action.
   */
  public static ButtonAction ofRunAction(Runnable action, boolean closeGui) {
    return new ButtonAction(ActionType.RUN_CLASS, action, closeGui);
  }

  /**
   * Creates a generic ButtonAction with custom action data.
   * <p>
   * This method is intended for advanced usage where action data is used to determine behavior.
   * @param actionType The type of action.
   * @param actionData Arbitrary data associated with the action.
   * @param closeGui   Whether to close the GUI after executing the action.
   * @return A new {@link ButtonAction} instance configured with the provided parameters.
   */
  public static ButtonAction ofGeneric(ActionType actionType, Object actionData, boolean closeGui) {
    return new ButtonAction(actionType, actionData, closeGui);
  }

  /**
   * Executes the action associated with this ButtonAction.
   * <p>\n   * Depending on the action type, this method performs the following:\n   * - OPEN_GUI: Attempts to instantiate and display the GUI using the provided GUI class.\n   * - CLOSE_GUI: Closes the player's current inventory.\n   * - GIVE_ITEM: Gives the player an item if actionData is an ItemStack.\n   * - RUN_COMMAND: Executes a command as the console, replacing \"%player%\" with the player's name.\n   * - RUN_CODE: Executes the custom Consumer action with the player as an argument.\n   * - RUN_CLASS: Executes the runnable action if actionData is a class.\n   *</p>\n   *\n   * @param player The player triggering the action.\n   * @param plugin The Plugin instance used for logging errors and warnings.\n   */
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
          // Assuming the GUI class has a constructor that accepts a JavaPlugin argument.
          Constructor<? extends BaseGui> constructor =
              guiClass.getDeclaredConstructor(JavaPlugin.class);
          // Instantiate the GUI with the main_plugin and display it to the player.
          BaseGui gui = constructor.newInstance(main_plugin);
          gui.showGui(player);
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
        if (actionData instanceof String) {
          String command = (String) actionData;
          Bukkit.dispatchCommand(
              Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
          if (closeGui) {
            player.closeInventory();
          }
        }
        break;
      case RUN_CODE:
        if (customAction != null) {
          customAction.accept(player);
          if (closeGui) {
            player.closeInventory();
          }
        }
        break;
      case RUN_CLASS:
        if (actionData instanceof Class<?>) {
          if (closeGui) {
            player.closeInventory();
          }
          if (action != null) {
            action.run(); // Execute the passed logic
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
   * @return a {@link Consumer<Player>} of type player representing the custom action, or null if not defined.
   */
  public Consumer<Player> getCustomAction() {
    return customAction;
  }

  /**
   * Gets the runnable action associated with this ButtonAction, if any.
   *
   * @return a {@link Runnable} representing the action logic, or null if not defined.
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
}
