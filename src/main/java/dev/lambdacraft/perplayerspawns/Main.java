package dev.lambdacraft.perplayerspawns;

import net.minecraft.entity.EntityCategory;
import net.minecraft.server.MinecraftServer;

public class Main {
	public static final int ENTITIES_CATEGORY_LENGTH = EntityCategory.values().length;
	// non final cus clients, even though I'm pretty sure none of this works with clients anyways
	public static MinecraftServer current;
}
