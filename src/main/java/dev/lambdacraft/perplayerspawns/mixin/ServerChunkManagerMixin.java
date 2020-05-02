package dev.lambdacraft.perplayerspawns.mixin;

import dev.lambdacraft.perplayerspawns.Main;
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
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

// crust name: ChunkProviderServer
@Mixin (ServerChunkManager.class)
public class ServerChunkManagerMixin {
	@Shadow @Final private ServerWorld world;

	@Shadow @Final public ThreadedAnvilChunkStorage threadedAnvilChunkStorage;

	@Shadow @Final private static int CHUNKS_ELIGIBLE_FOR_SPAWNING;

	@Inject(
			method = "tickChunks",
			at = @At (value = "INVOKE", target = "Lnet/minecraft/entity/EntityCategory;values()[Lnet/minecraft/entity/EntityCategory;"))
	/*
		Every all-chunks tick:
		1. Update distance map by adding all players
		2. Reset player's nearby mob counts
		3. Loop through all world's entities and add them to player's counts
	 */
	private void updateDistanceMap(CallbackInfo info) {
		TACSAccess tacs = ((TACSAccess) this.threadedAnvilChunkStorage);
		// update distance map
		tacs.playerMobDistanceMap().update(this.world.getPlayers(), tacs.renderDistance());
		// re-set mob counts
		for (PlayerEntity player : this.world.getPlayers()) {
			Arrays.fill(((PlayerEntityAccess) player).getMobCounts(), 0);
		}
		((ServerWorldAccess)this.world).updatePlayerMobTypeMapFromWorld();
		for (PlayerEntity player : this.world.getPlayers()) {
			System.out.println(player.getName().asString() + ": " + Arrays.toString(((PlayerEntityAccess) player).getMobCounts()));
		}
	}

	@Redirect (method = "method_20801",
	           at = @At (value = "INVOKE",
	                     target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;getInt(Ljava/lang/Object;)I"))
	private int spawnCalculatedMobsInChunk(
			Object2IntMap<Integer> map, Object obj, long time, boolean b, EntityCategory[] categories, boolean b2, int i,
			Object2IntMap map2, BlockPos pos, int j, ChunkHolder holder
	) {
		EntityCategory category = (EntityCategory) obj;
		TACSAccess tacs = (TACSAccess)this.threadedAnvilChunkStorage;
		PlayerMobDistanceMap mobDistanceMap = tacs.playerMobDistanceMap();
		WorldChunk chunk = holder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).left().get();

		int minDiff = Integer.MAX_VALUE;

		// Compute minimum mobs that should be spawned between all players in range of chunk
		for (PlayerEntity entityPlayer : mobDistanceMap.getPlayersInRange(chunk.getPos())) {
			int mobCountNearPlayer = ((PlayerEntityAccess)entityPlayer).getMobCountForEntityCategory(category);
			minDiff = Math.min(category.getSpawnCap() -  mobCountNearPlayer, minDiff);
//			System.out.println(category.getName() + " near player " + entityPlayer.getName().asString() + " = " + mobCountNearPlayer);
		}

		int difference = (minDiff == Integer.MAX_VALUE) ? 0 : minDiff;
		if (difference > 0) {
			SpawnHelperAccess.maxSpawns.set(difference); // to pass diff to spawnEntitiesInChunk
			SpawnHelperAccess.trackEntity.set(tacs::updatePlayerMobTypeMap);
			SpawnHelper.spawnEntitiesInChunk(category, this.world, chunk, pos);
		}

		// Return max value to stop vanilla spawning if statement condition
		return Integer.MAX_VALUE;
	}
}


