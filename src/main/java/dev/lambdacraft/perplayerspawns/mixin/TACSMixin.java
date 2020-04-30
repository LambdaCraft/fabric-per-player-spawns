package dev.lambdacraft.perplayerspawns.mixin;

import dev.lambdacraft.perplayerspawns.access.PlayerEntityAccess;
import dev.lambdacraft.perplayerspawns.access.TACSAccess;
import dev.lambdacraft.perplayerspawns.util.PlayerMobDistanceMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// crust name: PlayerChunkMap
@Mixin (ThreadedAnvilChunkStorage.class)
public abstract class TACSMixin implements TACSAccess {
	@Shadow private int watchDistance;
	private final PlayerMobDistanceMap map = new PlayerMobDistanceMap();

	@Override
	public void updatePlayerMobTypeMap(Entity entity) {
		int chunkX = (int) Math.floor(entity.getX()) >> 4;
		int chunkZ = (int) Math.floor(entity.getY()) >> 4;
		int index = entity.getType().getCategory().ordinal();
		for (PlayerEntity player : this.map.getPlayersInRange(chunkX, chunkZ)) {
			++((PlayerEntityAccess)player).getMobCounts()[index];
		}
	}

	@Override
	public int getMobCountNear(PlayerEntity entity, EntityCategory category) {
		return ((PlayerEntityAccess)entity).getMobCounts()[category.ordinal()];
	}

	@Override
	public PlayerMobDistanceMap map() {
		return this.map;
	}

	@Override
	public int renderDistance() {
		return this.watchDistance;
	}
}
