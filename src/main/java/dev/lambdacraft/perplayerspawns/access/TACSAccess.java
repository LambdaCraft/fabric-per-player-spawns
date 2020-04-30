package dev.lambdacraft.perplayerspawns.access;

import dev.lambdacraft.perplayerspawns.util.PlayerMobDistanceMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.player.PlayerEntity;

public interface TACSAccess {
	void updatePlayerMobTypeMap(Entity entity);
	PlayerMobDistanceMap map();
	int renderDistance();
	int getMobCountNear(PlayerEntity entity, EntityCategory category);
}
