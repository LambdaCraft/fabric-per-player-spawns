package dev.lambdacraft.perplayerspawns.mixin;

import dev.lambdacraft.perplayerspawns.Main;
import dev.lambdacraft.perplayerspawns.access.ServerWorldAccess;
import dev.lambdacraft.perplayerspawns.access.TACSAccess;
import dev.lambdacraft.perplayerspawns.util.ArrayInt2IntMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin (ServerWorld.class)
public abstract class ServerWorldMixin implements ServerWorldAccess {
	@Shadow @Final private Int2ObjectMap<Entity> entitiesById;

	@Shadow
	public abstract ServerChunkManager getChunkManager();

	@Override
	public Object2IntMap<Integer> countMobs(boolean update) {
		ArrayInt2IntMap mobs = new ArrayInt2IntMap(new int[Main.ENTITIES]);
		ObjectIterator<Entity> var2 = this.entitiesById.values().iterator();

		while (true) {
			Entity entity;
			MobEntity mobEntity;
			do {
				if (!var2.hasNext()) {
					return mobs;
				}

				entity = var2.next();
				if (!(entity instanceof MobEntity)) {
					break;
				}

				mobEntity = (MobEntity) entity;
			} while (mobEntity.isPersistent() || mobEntity.cannotDespawn());

			EntityCategory category = entity.getType().getCategory();
			if (category != EntityCategory.MISC && this.getChunkManager().method_20727(entity)) {
				if (update) {
					((TACSAccess)this.getChunkManager().threadedAnvilChunkStorage).updatePlayerMobTypeMap(entity);
				}
				mobs.set(category.ordinal(), mobs.getInt(category.ordinal()) + 1);
			}
		}

	}
}
