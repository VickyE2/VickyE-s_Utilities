package org.vicky.platform;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.OptionalInt;

public interface RankService {
    CompletableFuture<List<String>> getPlayerGroups(UUID uuid);
    List<String> getAllGroups();
    Map<String, Integer> getGroupWeights();
    CompletableFuture<OptionalInt> getHighestGroupWeight(UUID uuid);
    CompletableFuture<String> getUserPrefix(UUID uuid);
    CompletableFuture<String> getUserSuffix(UUID uuid);
}
