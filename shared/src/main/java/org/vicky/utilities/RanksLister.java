package org.vicky.utilities;

import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.PlatformRankService;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class RanksLister {
  private final PlatformRankService service;

  public RanksLister() {
    this.service = PlatformPlugin.rankService();
  }

  public CompletableFuture<String[]> getPlayerRanks(UUID who) {
    return service.getPlayerGroups(who)
            .thenApply(groups -> groups.toArray(new String[0]));
  }

  public String[] getAllRanks() {
    return service.getAllGroups().toArray(new String[0]);
  }

  public String[][] getAllRanksWithWeights(double multiplier) {
    Map<String, Integer> weights = service.getGroupWeights();
    return weights.entrySet().stream()
            .map(entry -> new String[]{
                    entry.getKey(),
                    String.valueOf(entry.getValue() * multiplier)
            })
            .toArray(String[][]::new);
  }

  public CompletableFuture<String> getHighestWeighingGroup(UUID who) {
    return service.getPlayerGroups(who).thenApply(groups -> {
      Map<String, Integer> weights = service.getGroupWeights();
      return groups.stream()
              .max(Comparator.comparingInt(g -> weights.getOrDefault(g, 0)))
              .orElse("default");
    });
  }

  public CompletableFuture<OptionalInt> getHighestWeighingGroupWeight(UUID who) {
    return getHighestWeighingGroup(who).thenApply(group -> {
      int weight = service.getGroupWeights().getOrDefault(group, 0);
      return OptionalInt.of(weight);
    });
  }

  public CompletableFuture<String> getUserPrefix(UUID who) {
    return service.getUserPrefix(who);
  }

  public CompletableFuture<String> getUserSuffix(UUID who) {
    return service.getUserSuffix(who);
  }
}
