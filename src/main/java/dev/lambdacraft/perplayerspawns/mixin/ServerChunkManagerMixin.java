package dev.lambdacraft.perplayerspawns.mixin;

import dev.lambdacraft.perplayerspawns.access.PlayerEntityAccess;
import dev.lambdacraft.perplayerspawns.access.ServerWorldAccess;
import dev.lambdacraft.perplayerspawns.access.TACSAccess;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
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

	@Redirect (method = "tickChunks",
	           at = @At (value = "INVOKE",
	                     target = "Lnet/minecraft/server/world/ServerWorld;getMobCountsByCategory()Lit/unimi/dsi/fastutil/objects/Object2IntMap;"))
	private Object2IntMap getMobCounts(ServerWorld world) {
		Object2IntMap counts;
		if (((TACSAccess)this.threadedAnvilChunkStorage).map() != null) {
			// update distance map
			((TACSAccess)this.threadedAnvilChunkStorage).map().update(this.world.getPlayers(), ((TACSAccess)this.threadedAnvilChunkStorage).renderDistance());
			// re-set mob counts
			for (PlayerEntity player : this.world.getPlayers()) {
				Arrays.fill(((PlayerEntityAccess)player).getMobCounts(), 0);
			}
			counts = ((ServerWorldAccess)this.world).countMobs(true);
		} else {
			counts = ((ServerWorldAccess)this.world).countMobs(false);
		}
		return counts;
	}
}
