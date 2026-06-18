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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import java.util.UUID;
import java.util.List;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;

public class TeddyEntity extends Monster {
    private UUID targetPlayerUuid = null;
    private int teleportCooldown = 0;
    private int stuckSeconds = 0;
    private Vec3 lastStuckPos = null;

    public void setTargetPlayerUuid(UUID uuid) {
        this.targetPlayerUuid = uuid;
    }

    public TeddyEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 150.0)
            .add(Attributes.FOLLOW_RANGE, 64.0)
            .add(Attributes.MOVEMENT_SPEED, 0.4);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetPlayerUuid != null) {
            tag.putUUID("TargetPlayerUuid", this.targetPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("TargetPlayerUuid")) {
            this.targetPlayerUuid = tag.getUUID("TargetPlayerUuid");
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 3.0f, 0.5f);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_STEP, SoundSource.HOSTILE, 3.0f, 0.4f);
    }

    private void teleportToDarkSpot() {
        BlockPos current = this.blockPosition();
        for (int i = 0; i < 50; i++) {
            int dx = this.random.nextInt(31) - 15;
            int dy = this.random.nextInt(7) - 3;
            int dz = this.random.nextInt(31) - 15;
            BlockPos target = current.offset(dx, dy, dz);
            if (this.level().getBlockState(target).isAir() && this.level().getBlockState(target.above()).isAir() && this.level().getBlockState(target.below()).isSolid()) {
                int blockLight = this.level().getBrightness(LightLayer.BLOCK, target);
                int skyLight = this.level().getBrightness(LightLayer.SKY, target);
                int actualSkyLight = this.level().isDay() ? skyLight : 0;
                if (Math.max(blockLight, actualSkyLight) < 8) {
                    this.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.5f, 0.8f);
                    break;
                }
            }
        }
    }

    private void teleportToRemoteSpot() {
        BlockPos current = this.blockPosition();
        for (int i = 0; i < 50; i++) {
            int dx = this.random.nextInt(61) - 30;
            int dy = this.random.nextInt(11) - 5;
            int dz = this.random.nextInt(61) - 30;
            BlockPos target = current.offset(dx, dy, dz);
            if (this.level().getBlockState(target).isAir() && this.level().getBlockState(target.above()).isAir() && this.level().getBlockState(target.below()).isSolid()) {
                List<net.minecraft.world.entity.LivingEntity> entities = this.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, new AABB(target).inflate(24.0));
                if (entities.isEmpty()) {
                    this.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                    break;
                }
            }
        }
    }

    private void teleportToPlayer(Player player) {
        BlockPos playerPos = player.blockPosition();
        for (int i = 0; i < 30; i++) {
            int dx = this.random.nextInt(7) - 3;
            int dy = this.random.nextInt(7) - 3;
            int dz = this.random.nextInt(7) - 3;
            BlockPos target = playerPos.offset(dx, dy, dz);
            if (this.level().getBlockState(target).isAir() && this.level().getBlockState(target.above()).isAir() && this.level().getBlockState(target.below()).isSolid()) {
                this.teleportTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STONE_BREAK, SoundSource.HOSTILE, 4.5f, 0.4f);
                break;
            }
        }
    }

    private boolean isPlayerLooking(Player player) {
        Vec3 toBear = this.position().subtract(player.getEyePosition(1.0f)).normalize();
        Vec3 look = player.getViewVector(1.0f);
        double dot = look.dot(toBear);
        return dot > 0.3 && player.hasLineOfSight(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (this.tickCount == 1) {
            List<net.minecraft.world.entity.LivingEntity> entities = this.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, this.getBoundingBox().inflate(24.0));
            entities.remove(this);
            if (!entities.isEmpty()) {
                teleportToRemoteSpot();
            }
        }

        int blockLight = this.level().getBrightness(LightLayer.BLOCK, this.blockPosition());
        int skyLight = this.level().getBrightness(LightLayer.SKY, this.blockPosition());
        int actualSkyLight = this.level().isDay() ? skyLight : 0;
        if (Math.max(blockLight, actualSkyLight) >= 8) {
            this.hurt(this.damageSources().magic(), 1.0f);
            if (this.tickCount % 10 == 0) {
                teleportToDarkSpot();
            }
        }

        Player player = null;
        if (this.targetPlayerUuid != null) {
            player = this.level().getPlayerByUUID(this.targetPlayerUuid);
        }
        if (player == null || player.isCreative() || player.isSpectator()) {
            player = this.level().getNearestPlayer(this, 100.0);
            if (player != null && !player.isCreative() && !player.isSpectator()) {
                this.targetPlayerUuid = player.getUUID();
            } else {
                player = null;
            }
        }

        if (player != null) {
            double dx = player.getX() - this.getX();
            double dz = player.getZ() - this.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            double dy = player.getEyeY() - this.getEyeY();
            double dist = Math.sqrt(dx * dx + dz * dz);
            float pitch = (float) -(Math.atan2(dy, dist) * 180.0 / Math.PI);
            this.setYRot(yaw);
            this.setXRot(pitch);
            this.yHeadRot = yaw;
            this.yBodyRot = yaw;
            this.yRotO = yaw;
            this.xRotO = pitch;
            this.yHeadRotO = yaw;
            this.yBodyRotO = yaw;
        }

        boolean lookedAt = false;
        List<Player> players = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(64.0));
        for (Player p : players) {
            if (!p.isCreative() && !p.isSpectator() && isPlayerLooking(p)) {
                lookedAt = true;
                break;
            }
        }

        if (lookedAt) {
            this.getNavigation().stop();
            this.setDeltaMovement(0, 0, 0);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
            this.xxa = 0.0f;
            this.yya = 0.0f;
            this.zza = 0.0f;
            stuckSeconds = 0;
            lastStuckPos = null;
        } else {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.4);
            if (player != null) {
                double dist = this.distanceTo(player);
                if (dist > 1.8) {
                    if (this.tickCount % 5 == 0) {
                        this.getNavigation().moveTo(player, 1.0);
                    }
                    if (lastStuckPos == null) {
                        lastStuckPos = this.position();
                    }
                    if (this.tickCount % 20 == 0) {
                        if (this.position().distanceTo(lastStuckPos) < 0.2) {
                            stuckSeconds++;
                        } else {
                            stuckSeconds = 0;
                        }
                        lastStuckPos = this.position();
                    }
                    int secondsLeft = 30 - stuckSeconds;
                    if (stuckSeconds > 0 && secondsLeft >= 0) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("Teddy relocation in: " + secondsLeft).withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD), true);
                    }
                    if (stuckSeconds >= 30) {
                        stuckSeconds = 0;
                        lastStuckPos = null;
                        teleportToPlayer(player);
                    }
                } else {
                    stuckSeconds = 0;
                    lastStuckPos = null;
                    ServerPlayer sp = (ServerPlayer) player;
                    ServerPlayNetworking.send(sp, new TeddySnapPayload());
                    sp.hurt(this.damageSources().magic(), 1000.0f);
                    this.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.ANVIL_BREAK, SoundSource.HOSTILE, 2.0f, 0.5f);
                    this.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.WITHER_BREAK_BLOCK, SoundSource.HOSTILE, 1.5f, 0.5f);
                    this.discard();
                }
            } else {
                stuckSeconds = 0;
                lastStuckPos = null;
            }
        }
    }
}
