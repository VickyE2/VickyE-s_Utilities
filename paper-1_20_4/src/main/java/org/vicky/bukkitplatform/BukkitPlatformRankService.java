/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.vicky.platform.PlatformRankService;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.NodeType;

public class BukkitPlatformRankService implements PlatformRankService {

	private final LuckPerms lp;

	public BukkitPlatformRankService(LuckPerms lp) {
		this.lp = lp;
	}

	@Override
	public CompletableFuture<List<String>> getPlayerGroups(UUID uuid) {
		return lp.getUserManager().loadUser(uuid).thenApply(user -> user.getNodes(NodeType.INHERITANCE).stream()
				.map(n -> n.getGroupName()).collect(Collectors.toList()));
	}

	@Override
	public List<String> getAllGroups() {
		return lp.getGroupManager().getLoadedGroups().stream().map(Group::getName).collect(Collectors.toList());
	}

	@Override
	public Map<String, Integer> getGroupWeights() {
		Map<String, Integer> map = new HashMap<>();
		for (Group group : lp.getGroupManager().getLoadedGroups()) {
			group.getWeight().ifPresent(w -> map.put(group.getName(), w));
		}
		return map;
	}

	@Override
	public CompletableFuture<OptionalInt> getHighestGroupWeight(UUID uuid) {
		return getPlayerGroups(uuid).thenApply(groups -> {
			int max = groups.stream().mapToInt(g -> getGroupWeights().getOrDefault(g, 0)).max().orElse(0);
			return OptionalInt.of(max);
		});
	}

	@Override
	public CompletableFuture<String> getUserPrefix(UUID uuid) {
		return lp.getUserManager().loadUser(uuid).thenApply(user -> user.getCachedData().getMetaData().getPrefix());
	}

	@Override
	public CompletableFuture<String> getUserSuffix(UUID uuid) {
		return lp.getUserManager().loadUser(uuid).thenApply(user -> user.getCachedData().getMetaData().getSuffix());
	}
}
