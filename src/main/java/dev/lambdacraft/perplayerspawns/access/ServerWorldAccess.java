package dev.lambdacraft.perplayerspawns.access;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

public interface ServerWorldAccess {
	// why not Int2IntMap? Because this is an easy hack:
	Object2IntMap<Integer> countMobs();
}
