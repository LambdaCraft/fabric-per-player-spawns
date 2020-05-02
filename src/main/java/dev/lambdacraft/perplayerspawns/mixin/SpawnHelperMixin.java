package dev.lambdacraft.perplayerspawns.mixin;

import dev.lambdacraft.perplayerspawns.access.SpawnHelperAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(SpawnHelper.class)
public class SpawnHelperMixin {

	@Inject(method = "spawnEntitiesInChunk", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void record(
			EntityCategory category, ServerWorld serverWorld, WorldChunk chunk, BlockPos spawnPos,
			CallbackInfo ci, ChunkGenerator<?> chunkGenerator, int i
	) {
		SpawnHelperAccess.SPAWNS.set(i);
	}

	@Redirect(
			method = "spawnEntitiesInChunk",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/mob/MobEntity;getLimitPerChunk()I"))
	private static int getMaxSpawns(MobEntity entity) {
		return SpawnHelperAccess.maxSpawns.get();
	}

	@Redirect(
			method = "spawnEntitiesInChunk",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
//					shift = At.Shift.AFTER
			))
	private static boolean updatePlayerMobTypeMapAfterAddEntity(ServerWorld serverWorld, Entity entity) {
		boolean didSpawn = serverWorld.spawnEntity(entity);
		SpawnHelperAccess.trackEntity.get().accept((MobEntity) entity);
		return didSpawn;
	}
}
