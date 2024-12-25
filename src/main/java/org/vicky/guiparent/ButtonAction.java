/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

import java.lang.reflect.Constructor;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class ButtonAction {

  public enum ActionType {
    OPEN_GUI, // Open a new GUI
    CLOSE_GUI, // Close the current GUI
    GIVE_ITEM, // Give the player an item
    RUN_COMMAND, // Run a command
    RUN_CODE, // Run custom code (a lambda function)
    RUN_CLASS
  }

  private final ActionType actionType;
  private final Runnable action;
  private final Class<? extends BaseGui> guiClass;
  private final Object actionData; // Data associated with the action
  private final Consumer<Player> customAction; // Custom action for RUN_CODE
  private final boolean closeGui;
  private final JavaPlugin main_plugin;

  // Constructors
  public ButtonAction(ActionType actionType, Object actionData, boolean closeGui) {
    this.actionType = actionType;
    this.actionData = actionData;
    this.customAction = null;
    this.action = null;
    this.guiClass = null;
    this.closeGui = closeGui;
    this.main_plugin = null;
  }

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

  public ButtonAction(ActionType actionType, Runnable action, boolean closeGui) {
    this.actionType = actionType;
    this.actionData = null;
    this.customAction = null;
    this.guiClass = null;
    this.action = action;
    this.closeGui = closeGui;
    this.main_plugin = null;
  }

  public ButtonAction(Consumer<Player> customAction, boolean closeGui) {
    this.actionType = ActionType.RUN_CODE;
    this.customAction = customAction;
    this.closeGui = closeGui;
    this.actionData = null;
    this.action = null;
    this.guiClass = null;
    this.main_plugin = null;
  }

  // Execute the action based on the action type
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
          // Assuming your constructor requires a plugin and a listener
          Constructor<? extends BaseGui> constructor =
              guiClass.getDeclaredConstructor(JavaPlugin.class);

          // Instantiate the class with plugin and player arguments
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

  // Getters
  public ActionType getActionType() {
    return actionType;
  }

  public Consumer<Player> getCustomAction() {
    return customAction;
  }

  public Runnable getAction() {
    return action;
  }

  public Object getActionData() {
    return actionData;
  }
}
