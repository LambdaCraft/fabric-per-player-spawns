package dev.lambdacraft.perplayerspawns.access;

import net.minecraft.entity.mob.MobEntity;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public interface SpawnHelperAccess {
	AtomicInteger SPAWNS = new AtomicInteger();
	ThreadLocal<Integer> maxSpawns = new ThreadLocal<>();
	ThreadLocal<Consumer<MobEntity>> trackEntity = new ThreadLocal<>();
}
