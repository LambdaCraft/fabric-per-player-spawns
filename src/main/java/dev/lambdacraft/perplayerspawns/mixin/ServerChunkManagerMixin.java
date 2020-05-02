package dev.lambdacraft.perplayerspawns.mixin;

import dev.lambdacraft.perplayerspawns.access.PlayerEntityAccess;
import dev.lambdacraft.perplayerspawns.access.ServerWorldAccess;
import dev.lambdacraft.perplayerspawns.access.SpawnHelperAccess;
import dev.lambdacraft.perplayerspawns.access.TACSAccess;
import dev.lambdacraft.perplayerspawns.util.ArrayInt2IntMap;
import dev.lambdacraft.perplayerspawns.util.PlayerMobDistanceMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Arrays;

// crust name: ChunkProviderServer
@Mixin (ServerChunkManager.class)
public class ServerChunkManagerMixin {
	@Shadow @Final private ServerWorld world;

	@Shadow @Final public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

	@Shadow @Final private static int CHUNKS_ELIGIBLE_FOR_SPAWNING;

	@Redirect (method = "tickChunks",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/server/world/ServerWorld;getMobCountsByCategory()Lit/unimi/dsi/fastutil/objects/Object2IntMap;"))
	private Object2IntMap getMobCounts(ServerWorld world) {
		// update distance map
		((TACSAccess) this.threadedAnvilChunkStorage).playerMobDistanceMap().update(
			this.world.getPlayers(),
			((TACSAccess) this.threadedAnvilChunkStorage).renderDistance()
		);
		// re-set mob counts
		for (PlayerEntity player : this.world.getPlayers()) {
			Arrays.fill(((PlayerEntityAccess) player).getMobCounts(), 0);
		}
		return ((ServerWorldAccess) this.world).countMobs();
	}

	//(JZ[Lnet/minecraft/entity/EntityCategory;ZILit/unimi/dsi/fastutil/objects/Object2IntMap;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/server/world/ChunkHolder;)V
	@Redirect (method = "method_20801",
	           at = @At (value = "INVOKE",
	                     target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;getInt(Ljava/lang/Object;)I"))
	private int doThing(
			Object2IntMap<Integer> map, Object obj, long time, boolean b, EntityCategory[] categories, boolean b2, int i,
			Object2IntMap map2, BlockPos pos, int j, ChunkHolder holder
	) {
		EntityCategory category = (EntityCategory) obj;
		int difference;
		int minDiff = Integer.MAX_VALUE;
		TACSAccess tacs = (TACSAccess)this.threadedAnvilChunkStorage;
		PlayerMobDistanceMap mobDistanceMap = tacs.playerMobDistanceMap();
		for (PlayerEntity entityPlayer : mobDistanceMap.getPlayersInRange(holder.getPos())) {
			minDiff = Math.min(category.getSpawnCap() - tacs.getMobCountNear(entityPlayer, category), minDiff);
		}
		difference = (minDiff == Integer.MAX_VALUE) ? 0 : minDiff;

		if (difference > 0) {
			SpawnHelperAccess.maxSpawns.set(difference); // to pass diff to spawnEntitiesInChunk
			SpawnHelperAccess.trackEntity.set(tacs::updatePlayerMobTypeMap);
			SpawnHelper.spawnEntitiesInChunk(
				category,
				this.world,
				holder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).left().get(),
				pos
			);
			int spawnCount = SpawnHelperAccess.SPAWNS.get();
			((ArrayInt2IntMap)map).set(category.ordinal(), map.getInt(category.ordinal()) + spawnCount);
		}
		return Integer.MAX_VALUE;
	}
}


