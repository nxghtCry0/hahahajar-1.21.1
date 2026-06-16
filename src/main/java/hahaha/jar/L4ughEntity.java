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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class L4ughEntity extends Monster {
    private static final EntityDataAccessor<Boolean> DISGUISED = SynchedEntityData.defineId(L4ughEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DISGUISE_TYPE = SynchedEntityData.defineId(L4ughEntity.class, EntityDataSerializers.INT);

    private BlockPos lastLightPos = null;
    private int attackTimer = 0;
    private int breathTimer = 0;
    private int flashTimer = 0;

    public L4ughEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 500.0)
            .add(Attributes.FOLLOW_RANGE, 200.0)
            .add(Attributes.MOVEMENT_SPEED, 0.12);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DISGUISED, true);
        builder.define(DISGUISE_TYPE, 0);
    }

    public boolean isDisguised() {
        return this.entityData.get(DISGUISED);
    }

    public void setDisguised(boolean value) {
        this.entityData.set(DISGUISED, value);
        if (!value) {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.35);
        } else {
            this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.12);
        }
    }

    public int getDisguiseType() {
        return this.entityData.get(DISGUISE_TYPE);
    }

    public void setDisguiseType(int value) {
        this.entityData.set(DISGUISE_TYPE, value);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Disguised", isDisguised());
        tag.putInt("DisguiseType", getDisguiseType());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setDisguised(tag.getBoolean("Disguised"));
        setDisguiseType(tag.getInt("DisguiseType"));
    }

    @Override
    public float maxUpStep() {
        return 3.0f;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, net.minecraft.world.level.LevelReader world) {
        net.minecraft.world.level.block.state.BlockState state = world.getBlockState(pos.below());
        if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(net.minecraft.tags.BlockTags.SAND)) {
            return -5.0f;
        }
        return super.getWalkTargetValue(pos, world);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            return;
        }

        if (this.tickCount == 1 && isDisguised()) {
            boolean nearVillage = !this.level().getEntitiesOfClass(net.minecraft.world.entity.npc.Villager.class, this.getBoundingBox().inflate(50.0)).isEmpty();
            if (nearVillage) {
                setDisguiseType(4);
            } else {
                setDisguiseType(this.random.nextInt(4));
            }
        }

        if (this.onGround() && this.tickCount % 20 == 0) {
            BlockPos pos = this.blockPosition().below();
            net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);
            if (state.is(Blocks.SAND) || state.is(Blocks.RED_SAND) || state.is(net.minecraft.tags.BlockTags.SAND)) {
                this.hurt(this.damageSources().magic(), 10.0f);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_HURT, SoundSource.HOSTILE, 1.0f, 0.5f);
            }
        }

        if (!isDisguised()) {
            BlockPos currentPos = this.blockPosition().above(2);
            if (lastLightPos == null || !lastLightPos.equals(currentPos)) {
                if (lastLightPos != null) {
                    if (this.level().getBlockState(lastLightPos).is(Blocks.LIGHT)) {
                        this.level().setBlock(lastLightPos, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                if (this.level().getBlockState(currentPos).isAir()) {
                    this.level().setBlock(currentPos, Blocks.LIGHT.defaultBlockState().setValue(net.minecraft.world.level.block.LightBlock.LEVEL, 15), 3);
                    lastLightPos = currentPos;
                } else {
                    lastLightPos = null;
                }
            }
        }

        Player player = this.level().getNearestPlayer(this, 150.0);
        if (player != null) {
            if (player.isDeadOrDying() && player.getTags().contains("hahahajar_l4ugh_damaged")) {
                this.discard();
                return;
            }
            double distSqr = this.distanceToSqr(player);
            double dist = Math.sqrt(distSqr);

            if (isDisguised()) {
                if (this.tickCount % 10 == 0) {
                    this.getNavigation().moveTo(player, 1.0);
                }

                breathTimer++;
                if (breathTimer >= 80) {
                    breathTimer = 0;
                }
                if (breathTimer < 15 && breathTimer % 3 == 0) {
                    HahahaJarEventHandler.triggerBreath((ServerPlayer) player);
                }

                if (dist <= 5.0) {
                    setDisguised(false);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0f, 0.5f);
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.0f, 0.5f);
                    
                    ServerPlayer sp = (ServerPlayer) player;
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("L4UGH").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("RUN RUN RUN").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)));
                    ServerPlayNetworking.send(sp, new L4ughFlashPayload());
                }
            } else {
                if (this.tickCount % 5 == 0) {
                    this.getNavigation().moveTo(player, 1.5);
                }

                attackTimer++;
                if (attackTimer >= 20) {
                    attackTimer = 0;
                    if (dist <= 3.0) {
                        player.hurt(this.damageSources().magic(), 6.0f);
                        player.addTag("hahahajar_l4ugh_damaged");
                        this.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, 0.5f);
                    }
                }

                flashTimer++;
                if (flashTimer >= 70) {
                    flashTimer = 0;
                    ServerPlayer sp = (ServerPlayer) player;
                    String[] creepyTitles = {"L4UGH", "LAUGH", "I SEE YOU", "NO ESCAPE"};
                    String titleText = creepyTitles[this.random.nextInt(creepyTitles.length)];
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal(titleText).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
                    sp.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("hahaha").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)));
                    ServerPlayNetworking.send(sp, new L4ughFlashPayload());
                }
            }
        }
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
