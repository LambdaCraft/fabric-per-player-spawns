package dev.lambdacraft.perplayerspawns.access;

import dev.lambdacraft.perplayerspawns.util.PooledHashSets;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.player.PlayerEntity;

public interface PlayerEntityAccess {
	int[] getMobCounts();
	PooledHashSets.PooledObjectLinkedOpenHashSet<PlayerEntity> getDistanceMap();
	int getMobCountForEntityCategory(EntityCategory category);
}
