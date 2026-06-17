package hahaha.jar;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;

public class LaughEchoEntity extends Monster {
    private BlockPos lastLightPos = null;
    private boolean chaseMode = false;
    private int stuckTicks = 0;
    private int echoAge = 0;
    private java.util.UUID targetPlayerUuid = null;

    public void setTargetPlayerUuid(java.util.UUID uuid) {
        this.targetPlayerUuid = uuid;
    }

    public LaughEchoEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 20.0)
            .add(Attributes.FOLLOW_RANGE, 500.0)
            .add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    public void setChaseMode(boolean value) {
        this.chaseMode = value;
        if (value) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.45);
        }
    }

    public boolean isChaseMode() {
        return this.chaseMode;
    }

    @Override
    public float maxUpStep() {
        return this.chaseMode ? 3.0f : 1.5f;
    }

    public static BlockPos findSpawnPos(ServerLevel world, double x, double y, double z) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos((int) x, (int) y, (int) z);
        for (int dy = 0; dy <= 30; dy++) {
            pos.setY((int) y + dy);
            if (world.getBlockState(pos).isAir() && world.getBlockState(pos.above()).isAir() && !world.getBlockState(pos.below()).isAir()) {
                return pos.immutable();
            }
            if (dy > 0) {
                pos.setY((int) y - dy);
                if (world.getBlockState(pos).isAir() && world.getBlockState(pos.above()).isAir() && !world.getBlockState(pos.below()).isAir()) {
                    return pos.immutable();
                }
            }
        }
        int spawnY = world.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, (int) x, (int) z);
        return new BlockPos((int) x, spawnY, (int) z);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("ChaseMode", this.chaseMode);
        tag.putInt("EchoAge", this.echoAge);
        if (this.targetPlayerUuid != null) {
            tag.putUUID("TargetPlayerUuid", this.targetPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.chaseMode = tag.getBoolean("ChaseMode");
        this.echoAge = tag.getInt("EchoAge");
        if (this.chaseMode) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100.0);
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.45);
        }
        if (tag.hasUUID("TargetPlayerUuid")) {
            this.targetPlayerUuid = tag.getUUID("TargetPlayerUuid");
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide()) {
            this.echoAge++;
            if (!this.chaseMode && this.echoAge >= 6000) {
                this.discard();
                return;
            }
        }
        
        if (this.getTags().contains("hahahajar_phantom")) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
            Player nearest = this.level().getNearestPlayer(this, 10.0);
            if (nearest != null && (this.tickCount > 60 || this.distanceToSqr(nearest) < 20.0)) {
                if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 10, 0.2, 0.5, 0.2, 0.0);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.5f);
                }
                this.discard();
            }
            return;
        }

        if (!this.level().isClientSide()) {
            BlockPos currentPos = this.blockPosition().above();
            if (lastLightPos == null || !lastLightPos.equals(currentPos)) {
                if (lastLightPos != null) {
                    if (this.level().getBlockState(lastLightPos).is(Blocks.LIGHT)) {
                        this.level().setBlock(lastLightPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                if (this.level().getBlockState(currentPos).isAir()) {
                    this.level().setBlock(currentPos, Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 12), 3);
                    lastLightPos = currentPos;
                } else {
                    lastLightPos = null;
                }
            }
        }

        Player player = null;
        if (this.targetPlayerUuid != null) {
            player = this.level().getPlayerByUUID(this.targetPlayerUuid);
        }
        if (player == null || player.isCreative() || player.isSpectator()) {
            player = this.level().getNearestPlayer(this, 600.0);
            if (player != null && !player.isCreative() && !player.isSpectator()) {
                this.targetPlayerUuid = player.getUUID();
            } else {
                player = null;
            }
        }
        if (player != null) {
            double distSqr = this.distanceToSqr(player);
            
            if (this.chaseMode) {
                if (distSqr < 2.5) {
                    ServerPlayer sp = (ServerPlayer) player;
                    HahahaJarEventHandler.endChase(sp);
                    sp.sendSystemMessage(net.minecraft.network.chat.Component.literal("you lost").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD));
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(net.minecraft.network.chat.Component.literal("YOU LOST").withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD)));
                    BlockPos spawnPos = findSpawnPos((ServerLevel) this.level(), sp.getX() + 10.0, sp.getY(), sp.getZ() + 10.0);
                    L4ughEntity l4ugh = HahahaJar.L4UGH.create(this.level());
                    if (l4ugh != null) {
                        l4ugh.setTargetPlayerUuid(sp.getUUID());
                        l4ugh.setDisguised(true);
                        l4ugh.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                        this.level().addFreshEntity(l4ugh);
                    }
                    player.kill();
                    this.discard();
                    return;
                }

                double dx = player.getX() - this.getX();
                double dz = player.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);

                double horizontalMovement = Math.sqrt((getX() - xo) * (getX() - xo) + (getZ() - zo) * (getZ() - zo));
                if (horizontalMovement < 0.05 && dist > 5.0) {
                    this.stuckTicks++;
                } else {
                    this.stuckTicks = 0;
                }

                if (dist > 80.0 || this.stuckTicks > 20) {
                    this.stuckTicks = 0;
                    double spawnAngle = player.getRandom().nextDouble() * Math.PI * 2;
                    double spawnDist = 20.0 + player.getRandom().nextDouble() * 15.0;
                    double tx = player.getX() + Math.cos(spawnAngle) * spawnDist;
                    double tz = player.getZ() + Math.sin(spawnAngle) * spawnDist;
                    BlockPos tpPos = findSpawnPos((ServerLevel) this.level(), tx, player.getY(), tz);
                    this.teleportTo(tpPos.getX() + 0.5, tpPos.getY(), tpPos.getZ() + 0.5);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 1.0, this.getZ(), 15, 0.2, 0.5, 0.2, 0.0);
                        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0f, 0.5f);
                    }
                    return;
                }

                double speed = 0.15;
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.15);

                if (dist > 10.0) {
                    double vx = (dx / dist) * speed;
                    double vz = (dz / dist) * speed;
                    this.setDeltaMovement(vx, this.getDeltaMovement().y, vz);
                    float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                    this.setYRot(yaw);
                    this.setYHeadRot(yaw);
                } else {
                    if (!this.level().isClientSide() && this.tickCount % 5 == 0) {
                        this.getNavigation().moveTo(player, 1.5);
                    }
                }
            } else {
                Vec3 lookVec = player.getViewVector(1.0f);
                Vec3 toEntity = this.position().subtract(player.position()).normalize();
                double dot = lookVec.dot(toEntity);
                boolean lookingAt = dot > 0.7;

                if (lookingAt) {
                    this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.08);
                    if (distSqr < 100.0) {
                        if (this.tickCount % 20 == 0) {
                            player.hurt(this.damageSources().magic(), 2.0f);
                            this.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 0.5f, 1.5f);
                        }
                    }
                } else {
                    this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.32);
                }

                if (this.horizontalCollision && this.onGround()) {
                    this.jumpFromGround();
                }

                double dx = player.getX() - this.getX();
                double dz = player.getZ() - this.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 15.0) {
                    double speed = 0.22;
                    double vx = (dx / dist) * speed;
                    double vz = (dz / dist) * speed;
                    this.setDeltaMovement(vx, this.getDeltaMovement().y, vz);
                    float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
                    this.setYRot(yaw);
                    this.setYHeadRot(yaw);
                } else {
                    if (!this.level().isClientSide() && this.tickCount % 10 == 0) {
                        this.getNavigation().moveTo(player, 1.0);
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (this.chaseMode) {
            if (!this.level().isClientSide()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.0f, 0.5f);
            }
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (!this.level().isClientSide() && lastLightPos != null) {
            if (this.level().getBlockState(lastLightPos).is(Blocks.LIGHT)) {
                this.level().setBlock(lastLightPos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }
}
