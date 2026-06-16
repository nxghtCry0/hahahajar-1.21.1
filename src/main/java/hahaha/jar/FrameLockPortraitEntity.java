package hahaha.jar;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;

public class FrameLockPortraitEntity extends Monster {
    private int soundTicks = 0;

    public FrameLockPortraitEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 100.0)
            .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.setDeltaMovement(0, 0, 0);

        if (!this.level().isClientSide()) {
            Player player = this.level().getNearestPlayer(this, 100.0);
            if (player instanceof ServerPlayer serverPlayer) {
                Vec3 look = serverPlayer.getViewVector(1.0f);
                Vec3 toEntity = this.position().subtract(serverPlayer.position()).normalize();
                double dot = look.dot(toEntity);

                if (dot > 0.2) {
                    teleportBehind(serverPlayer);
                }

                soundTicks++;
                if (soundTicks % 30 == 0) {
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.HOSTILE, 1.0f, 0.5f);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 0.5f, 0.6f);
                }
            }
        }
    }

    public void teleportBehind(ServerPlayer player) {
        Vec3 look = player.getViewVector(1.0f);
        Vec3 behind = player.position().subtract(look.x * 4.0, 0.0, look.z * 4.0);
        BlockPos spawnPos = LaughEchoEntity.findSpawnPos(player.serverLevel(), behind.x, player.getY(), behind.z);
        this.teleportTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        this.setYRot(player.getYRot());
        this.setYHeadRot(player.getYRot());
    }
}
