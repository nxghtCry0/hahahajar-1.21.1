package hahaha.jar;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class ThreadBleederEntity extends Monster {
    private int lagTicks = 0;

    public ThreadBleederEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
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
            if (player != null && (player.isCreative() || player.isSpectator())) {
                player = null;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                double dist = this.distanceTo(serverPlayer);
                if (dist < 2.0) {
                    Vec3 look = serverPlayer.getViewVector(1.0f);
                    Vec3 front = serverPlayer.getEyePosition().add(look.scale(1.2));
                    this.teleportTo(front.x, front.y - 0.9, front.z);
                    serverPlayer.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(net.minecraft.network.chat.Component.literal("SYSTEM ERROR").withStyle(net.minecraft.ChatFormatting.DARK_RED, net.minecraft.ChatFormatting.BOLD)));
                    this.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 2.0f, 0.5f);
                    ServerPlayNetworking.send(serverPlayer, new ThreadBleederLagPayload(false, 0));
                    this.discard();
                    return;
                }

                lagTicks++;
                if (lagTicks >= 200) {
                    ServerPlayNetworking.send(serverPlayer, new ThreadBleederLagPayload(false, 0));
                    this.discard();
                    return;
                }
                if (lagTicks % 5 == 0) {
                    int ping = 1000 + lagTicks * 30;
                    ServerPlayNetworking.send(serverPlayer, new ThreadBleederLagPayload(true, ping));
                }
            }
        }
    }

    public void onPlayerAction(ServerPlayer player) {
        if (!this.level().isClientSide()) {
            double dx = player.getX() - this.getX();
            double dy = player.getY() - this.getY();
            double dz = player.getZ() - this.getZ();
            this.teleportTo(this.getX() + dx * 0.25, this.getY() + dy * 0.25, this.getZ() + dz * 0.25);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 5, 0.1, 0.1, 0.1, 0.0);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide() && this.level().getServer() != null) {
            for (ServerPlayer player : this.level().getServer().getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, new ThreadBleederLagPayload(false, 0));
            }
        }
        super.remove(reason);
    }
}
