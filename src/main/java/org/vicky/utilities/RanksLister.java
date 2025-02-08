/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

public class RanksLister {
  private final LuckPerms luckPerms;

  public RanksLister() {
    this.luckPerms = LuckPermsProvider.get();
  }

  public CompletableFuture<String[]> getPlayerRanks(UUID who) {
    // Load the user asynchronously
    CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(who);

    // Process the user once it's available
    return userFuture.thenApply(
        user -> {
          // Get the inherited groups with the user's query options
          Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());

          // Map the groups to their names
          List<String> groupNames = inheritedGroups.stream().map(Group::getName).toList();

          // Return the group names as an array
          return groupNames.toArray(new String[0]);
        });
  }

  public String[] getAllRanks() {
    List<String> rankNames = new ArrayList<>();

    // Fetch all loaded groups
    for (Group group : luckPerms.getGroupManager().getLoadedGroups()) {
      rankNames.add(group.getName());
    }

    return rankNames.toArray(new String[0]);
  }

  // Method to get ranks with weights
  public String[][] getAllRanksWithWeights(double multiplier) {
    // Retrieve the list of groups and their weights, apply multiplier
    List<String[]> rankNamesWithWeights =
        luckPerms.getGroupManager().getLoadedGroups().stream()
            .map(
                group ->
                    new String[] {
                      group.getName(), // Rank name
                      String.valueOf(
                          group.getWeight().orElse(0) * multiplier) // Apply multiplier to weight
                    })
            .toList();

    // Convert the list to a 2D array
    return rankNamesWithWeights.toArray(new String[0][0]);
  }

  public CompletableFuture<String> getHighestWeighingGroup(UUID who) {
    CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(who);
    return userFuture.thenApply(
        user ->
            user.getInheritedGroups(user.getQueryOptions()).stream() // Get inherited groups
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                .map(Group::getName)
                .orElse(user.getPrimaryGroup()));
  }

  public CompletableFuture<OptionalInt> getHighestWeighingGroupWeight(UUID who) {
    CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(who);
    return userFuture.thenApply(
        user ->
            user.getInheritedGroups(user.getQueryOptions()).stream() // Get inherited groups
                .max(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
                .map(Group::getWeight)
                .orElse(OptionalInt.of(0)));
  }

  public CompletableFuture<String> getUserPrefix(UUID who) {
    return luckPerms
        .getUserManager()
        .loadUser(who)
        .thenApply(user -> user.getCachedData().getMetaData().getPrefix());
  }

  public CompletableFuture<String> getUserSuffix(UUID who) {
    return luckPerms
        .getUserManager()
        .loadUser(who)
        .thenApply(
            user -> {
              return user.getCachedData().getMetaData().getSuffix();
            });
  }
}
