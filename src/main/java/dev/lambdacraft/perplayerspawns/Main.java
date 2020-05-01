package dev.lambdacraft.perplayerspawns;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.EntityCategory;
import net.minecraft.server.MinecraftServer;

public class Main implements ModInitializer {
	public static final int ENTITIES_CATEGORY_LENGTH = EntityCategory.values().length;
	// non final cus clients, even though I'm pretty sure none of this works with clients anyways
	public static MinecraftServer current;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");
	}
}
