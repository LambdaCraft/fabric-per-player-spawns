package dev.lambdacraft.perplayerspawns.access;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface MinecraftServerAccess {
	Thread getServerThread();
}
