package skid.krypton.mixin;

import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({MobSpawnerLogic.class})
public interface MobSpawnerLogicAccessor {
    @Accessor("spawnEntry")
    MobSpawnerEntry getSpawnEntry();
}