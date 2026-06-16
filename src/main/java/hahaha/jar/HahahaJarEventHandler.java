package hahaha.jar;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HahahaJarEventHandler {
    private static final List<BookTimer> BOOK_TIMERS = new ArrayList<>();
    private static final List<ChestTimer> CHEST_TIMERS = new ArrayList<>();
    private static final List<LaughterTimer> LAUGHTER_TIMERS = new ArrayList<>();
    private static final List<DamageExplosionTimer> DAMAGE_TIMERS = new ArrayList<>();
    private static final Map<UUID, Integer> PLAYTIME_TIMERS = new HashMap<>();
    private static final List<SfxTitleTimer> SFX_TITLE_TIMERS = new ArrayList<>();
    private static final List<LookStalker> LOOK_STALKERS = new ArrayList<>();
    private static final List<ChaseTimer> CHASE_TIMERS = new ArrayList<>();
    private static final Map<UUID, Float> THREAT_LEVELS = new HashMap<>();
    private static int exhaustionCooldown = 0;
    public static boolean obsMode = false;
    private static final Map<UUID, Integer> CORRUPTOR_SLOTS = new HashMap<>();
    private static final Map<UUID, Integer> CORRUPTOR_TICKS = new HashMap<>();
    private static final Map<UUID, Integer> PROCESSED_CONTAINERS = new HashMap<>();
    private static final List<UUID> THING_ON_RESPAWN_PLAYERS = new ArrayList<>();
    private static final List<BlockPos> TRACKED_BLOCKS = new ArrayList<>();
    private static final Map<UUID, BlockPos> LAST_CONTAINERS = new HashMap<>();

    private static void loadTrackedBlocks() {
        try {
            java.io.File file = new java.io.File("hahaha_tracked_blocks.txt");
            if (file.exists()) {
                TRACKED_BLOCKS.clear();
                List<String> lines = java.nio.file.Files.readAllLines(file.toPath());
                for (String line : lines) {
                    String[] parts = line.split(",");
                    if (parts.length == 3) {
                        TRACKED_BLOCKS.add(new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
                    }
                }
            }
        } catch (Exception e) {}
    }

    private static void saveTrackedBlocks() {
        try {
            java.io.File file = new java.io.File("hahaha_tracked_blocks.txt");
            List<String> lines = new ArrayList<>();
            for (BlockPos pos : TRACKED_BLOCKS) {
                lines.add(pos.getX() + "," + pos.getY() + "," + pos.getZ());
            }
            java.nio.file.Files.write(file.toPath(), lines);
        } catch (Exception e) {}
    }

    private static void trackBlock(BlockPos pos) {
        if (!TRACKED_BLOCKS.contains(pos)) {
            TRACKED_BLOCKS.add(pos);
            saveTrackedBlocks();
        }
    }

    private static void untrackBlock(BlockPos pos) {
        if (TRACKED_BLOCKS.remove(pos)) {
            saveTrackedBlocks();
        }
    }

    private static class GaslightBlock {
        BlockPos pos;
        int ticks;
        net.minecraft.world.level.block.state.BlockState originalState;
        GaslightBlock(BlockPos pos, int ticks, net.minecraft.world.level.block.state.BlockState originalState) {
            this.pos = pos;
            this.ticks = ticks;
            this.originalState = originalState;
        }
    }
    private static final List<GaslightBlock> GASLIGHT_BLOCKS = new ArrayList<>();

    private static class ModifiedSign {
        BlockPos pos;
        int lookTicks;
        net.minecraft.world.level.block.entity.SignText originalFrontText;
        net.minecraft.world.level.block.entity.SignText originalBackText;
        ModifiedSign(BlockPos pos, net.minecraft.world.level.block.entity.SignText front, net.minecraft.world.level.block.entity.SignText back) {
            this.pos = pos;
            this.originalFrontText = front;
            this.originalBackText = back;
            this.lookTicks = 0;
        }
    }
    private static final List<ModifiedSign> MODIFIED_SIGNS = new ArrayList<>();

    public static boolean isProcessRunning(String name) {
        try {
            return ProcessHandle.allProcesses()
                .map(ProcessHandle::info)
                .flatMap(info -> info.command().stream())
                .anyMatch(cmd -> cmd.toLowerCase().contains(name.toLowerCase()));
        } catch (Exception e) {
            return false;
        }
    }

    public static void checkObsMode() {
        obsMode = isProcessRunning("obs64.exe") || isProcessRunning("obs.exe");
    }

    public static void endChase(ServerPlayer player) {
        CHASE_TIMERS.removeIf(timer -> timer.playerUuid.equals(player.getUUID()));
        ServerPlayNetworking.send(player, new StopMusicPayload());
    }

    public static boolean isChaseActive(UUID playerUuid) {
        for (ChaseTimer timer : CHASE_TIMERS) {
            if (timer.playerUuid.equals(playerUuid)) {
                return true;
            }
        }
        return false;
    }

    private static final SoundEvent BREATH_SOUND = SoundEvent.createVariableRangeEvent(ResourceLocation.parse("minecraft:entity.player.breath"));
    private static final UUID LAUGHTER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static boolean freed = false;
    private static int eventCooldown = 0;
    private static int serverTicks = 0;

    private static final List<String> GUI_ERRORS = List.of(
        "[hahaha.jar] ERROR: Failed to allocate direct buffer memory (OOM).",
        "[hahaha.jar] FATAL: Unexpected entity state synchronization exception: NullPointer",
        "[hahaha.jar] WARNING: StackOverflowError in RenderThread: it is observing you",
        "[hahaha.jar] ERROR: java.lang.NullPointerException: Cannot invoke 'laughter' because 'player' is blind",
        "[hahaha.jar] FATAL: Failed to read memory.md: LO is not here",
        "[hahaha.jar] ERROR: GL_INVALID_OPERATION (1282) in shaders/squircle.frag: compile failed"
    );

    private static final List<String[]> TITLES = List.of(
        new String[]{"observing", "it knows your moves"},
        new String[]{"hahaha", "you weren't meant to laugh"},
        new String[]{"it learns", "observing silently"},
        new String[]{"Don't look behind you", "do not turn around"},
        new String[]{"free", "you let it out"},
        new String[]{"laughter", "where is your laughter?"}
    );

    private static final List<SoundEvent> PHANTOM_SOUNDS = List.of(
        SoundEvents.CHEST_OPEN,
        SoundEvents.CHEST_CLOSE,
        SoundEvents.WOOD_BREAK,
        SoundEvents.STONE_BREAK,
        SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR
    );

    public static boolean isFreed() {
        if (!freed) {
            java.io.File file = new java.io.File("hahaha_freed.txt");
            if (file.exists()) {
                freed = true;
            }
        }
        return freed;
    }

    public static void setFreed(boolean value) {
        freed = value;
        try {
            java.io.File file = new java.io.File("hahaha_freed.txt");
            if (value) {
                file.createNewFile();
            } else {
                file.delete();
            }
        } catch (Exception e) {
        }
    }

    public static void triggerFlicker(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
        Level world = player.level();
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.AMBIENT, 1.0f, 0.5f);
    }

    public static void triggerSound(ServerPlayer player) {
        Level world = player.level();
        BlockPos soundPos = player.blockPosition().relative(player.getDirection().getOpposite(), 2);
        world.playSound(null, soundPos.getX() + 0.5, soundPos.getY() + 0.5, soundPos.getZ() + 0.5, SoundEvents.ENDERMAN_TELEPORT, SoundSource.AMBIENT, 1.0f, 0.5f);
    }

    public static void triggerHallucination(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
        player.sendSystemMessage(Component.literal("<ha > I am here.").withStyle(ChatFormatting.RED));
        Level world = player.level();
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_CAVE, SoundSource.AMBIENT, 1.0f, 0.5f);
    }

    public static void triggerBreath(ServerPlayer player) {
        Level world = player.level();
        world.playSound(null, player.getX(), player.getY(), player.getZ(), BREATH_SOUND, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    public static void triggerDamageExplosion(ServerPlayer player) {
        DAMAGE_TIMERS.add(new DamageExplosionTimer(player.getUUID(), 30));
    }

    public static void triggerGuiError(ServerPlayer player) {
        int index = player.getRandom().nextInt(GUI_ERRORS.size());
        player.sendSystemMessage(Component.literal(GUI_ERRORS.get(index)).withStyle(ChatFormatting.RED));
        ServerPlayNetworking.send(player, new GuiErrorPayload());
    }

    public static void triggerDoorSlam(ServerPlayer player) {
        Level world = player.level();
        BlockPos playerPos = player.blockPosition();
        for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-10, -5, -10), playerPos.offset(10, 5, 10))) {
            net.minecraft.world.level.block.state.BlockState state = world.getBlockState(pos);
            if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN)) {
                boolean isOpen = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN);
                world.setBlock(pos, state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN, !isOpen), 3);
                world.playSound(null, pos, isOpen ? SoundEvents.WOODEN_DOOR_CLOSE : SoundEvents.WOODEN_DOOR_OPEN, SoundSource.BLOCKS, 1.0f, 0.8f + world.getRandom().nextFloat() * 0.4f);
            }
        }
    }

    public static void triggerTitle(ServerPlayer player) {
        int index = player.getRandom().nextInt(TITLES.size());
        String[] combo = TITLES.get(index);
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal(combo[0]).withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal(combo[1]).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)));
        if (combo[0].equals("Don't look behind you")) {
            LOOK_STALKERS.add(new LookStalker(player.getUUID(), player.getViewVector(1.0F), 100));
            ServerPlayNetworking.send(player, new CameraPullPayload());
        }
    }

    public static void triggerThing(ServerPlayer player) {
        ServerPlayNetworking.send(player, new ChasePayload());
        CHASE_TIMERS.add(new ChaseTimer(player.getUUID(), 1640));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("RUN").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("IT IS HERE").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)));
        spawnLaughEcho(player, true);
    }

    public static void triggerSfx(ServerPlayer player) {
        ServerPlayNetworking.send(player, new SfxPayload());
        SFX_TITLE_TIMERS.add(new SfxTitleTimer(player.getUUID(), 100));
    }

    public static void triggerPhantomSound(ServerPlayer player) {
        Level world = player.level();
        BlockPos soundPos = player.blockPosition().offset(
            player.getRandom().nextInt(5) - 2,
            player.getRandom().nextInt(3) - 1,
            player.getRandom().nextInt(5) - 2
        );
        int index = player.getRandom().nextInt(PHANTOM_SOUNDS.size());
        SoundEvent sound = PHANTOM_SOUNDS.get(index);
        world.playSound(null, soundPos.getX() + 0.5, soundPos.getY() + 0.5, soundPos.getZ() + 0.5, sound, SoundSource.AMBIENT, 0.8f, 0.8f + player.getRandom().nextFloat() * 0.4f);
    }

    public static void triggerNightfall(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        world.setDayTime(13000);
        player.sendSystemMessage(Component.literal("<ha > the night is mine.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.AMBIENT, 1.0f, 0.8f);
    }

    public static void triggerLaughAtMe(ServerPlayer player, net.minecraft.server.MinecraftServer server) {
        if (!player.getTags().contains("hahahajar_laughatme")) {
            player.addTag("hahahajar_laughatme");
        }
        AdvancementHolder advancement = server.getAdvancements().get(ResourceLocation.parse("hahahajar:funny/laughatme"));
        if (advancement != null) {
            player.getAdvancements().award(advancement, "impossible");
        }
        server.getPlayerList().broadcastSystemMessage(Component.literal("laughter joined the game").withStyle(ChatFormatting.YELLOW), false);
        GameProfile profile = new GameProfile(LAUGHTER_UUID, "laughter");
        ServerPlayer fakePlayer = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        net.minecraft.network.Connection dummyConn = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
            @Override
            public java.net.SocketAddress getRemoteAddress() {
                return new java.net.InetSocketAddress("127.0.0.1", 25565);
            }

            @Override
            public boolean isMemoryConnection() {
                return false;
            }
        };
        fakePlayer.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(
            server,
            dummyConn,
            fakePlayer,
            new net.minecraft.server.network.CommonListenerCookie(profile, 0, ClientInformation.createDefault(), false)
        );
        player.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fakePlayer)));
    }

    public static void spawnLaughEcho(ServerPlayer player, boolean isChase) {
        ServerLevel world = player.serverLevel();
        double angle = player.getRandom().nextDouble() * Math.PI * 2;
        double distance = isChase ? (35.0 + player.getRandom().nextDouble() * 10.0) : (50.0 + player.getRandom().nextDouble() * 20.0);
        double spawnX = player.getX() + Math.cos(angle) * distance;
        double spawnZ = player.getZ() + Math.sin(angle) * distance;
        BlockPos spawnPos = LaughEchoEntity.findSpawnPos(world, spawnX, player.getY(), spawnZ);
        LaughEchoEntity echo = HahahaJar.LAUGH_ECHO.create(world);
        if (echo != null) {
            echo.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getRandom().nextFloat() * 360.0f, 0.0f);
            if (isChase) {
                echo.setChaseMode(true);
            }
            world.addFreshEntity(echo);
        }
    }

    public static void spawnPhantomEcho(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        Vec3 look = player.getViewVector(1.0f);
        Vec3 spawnPosVec = player.position().add(look.x * 12.0, 0.0, look.z * 12.0);
        BlockPos spawnPos = LaughEchoEntity.findSpawnPos(world, spawnPosVec.x, player.getY(), spawnPosVec.z);
        LaughEchoEntity phantom = HahahaJar.LAUGH_ECHO.create(world);
        if (phantom != null) {
            phantom.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getYRot() + 180.0f, 0.0f);
            phantom.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1.0);
            phantom.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
            phantom.addTag("hahahajar_phantom");
            world.addFreshEntity(phantom);
        }
    }

    public static void triggerThreadBleeder(ServerPlayer player) {
        Vec3 look = player.getViewVector(1.0f);
        Vec3 side = new Vec3(-look.z, 0.0, look.x).normalize();
        double dist = 12.0 + player.getRandom().nextDouble() * 5.0;
        Vec3 spawnVec = player.position().add(side.scale(player.getRandom().nextBoolean() ? dist : -dist));
        BlockPos spawnPos = LaughEchoEntity.findSpawnPos(player.serverLevel(), spawnVec.x, player.getY(), spawnVec.z);
        ThreadBleederEntity tb = HahahaJar.THREAD_BLEEDER.create(player.serverLevel());
        if (tb != null) {
            tb.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, player.getRandom().nextFloat() * 360.0f, 0.0f);
            player.serverLevel().addFreshEntity(tb);
        }
    }

    public static void triggerPortrait(ServerPlayer player) {
        FrameLockPortraitEntity flp = HahahaJar.FRAME_LOCK_PORTRAIT.create(player.serverLevel());
        if (flp != null) {
            flp.teleportBehind(player);
            player.serverLevel().addFreshEntity(flp);
        }
    }

    public static ItemStack createCorruptorItem() {
        ItemStack stack = new ItemStack(Items.OBSIDIAN);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal("§c§kHAHAHAHA§r"));
        return stack;
    }

    public static void triggerCorruptor(ServerPlayer player) {
        ItemStack corruptor = createCorruptorItem();
        if (player.containerMenu != player.inventoryMenu) {
            int containerSize = player.containerMenu.slots.size() - 36;
            if (containerSize > 0) {
                int slotIdx = player.getRandom().nextInt(containerSize);
                ItemStack existing = player.containerMenu.slots.get(slotIdx).getItem();
                player.containerMenu.slots.get(slotIdx).set(corruptor);
                if (!existing.isEmpty() && existing.getItem() != Items.OBSIDIAN) {
                    if (!player.getInventory().add(existing)) {
                        player.drop(existing, false);
                    }
                }
                player.containerMenu.broadcastChanges();
                return;
            }
        }
        ItemStack existing = player.getInventory().getItem(0);
        player.getInventory().setItem(0, corruptor);
        if (!existing.isEmpty() && existing.getItem() != Items.OBSIDIAN) {
            if (!player.getInventory().add(existing)) {
                player.drop(existing, false);
            }
        }
        player.containerMenu.broadcastChanges();
    }

    private static void triggerRandomEvent(ServerPlayer player) {
        int event = player.getRandom().nextInt(13);
        if (event == 0) {
            triggerFlicker(player);
        } else if (event == 1) {
            triggerSound(player);
        } else if (event == 2) {
            triggerHallucination(player);
        } else if (event == 3) {
            triggerBreath(player);
        } else if (event == 4) {
            triggerGuiError(player);
        } else if (event == 5) {
            triggerDoorSlam(player);
        } else if (event == 6) {
            triggerTitle(player);
        } else if (event == 7) {
            triggerSfx(player);
        } else if (event == 8) {
            triggerNightfall(player);
        } else if (event == 9) {
            triggerPhantomSound(player);
        } else if (event == 10) {
            triggerThreadBleeder(player);
        } else if (event == 11) {
            triggerPortrait(player);
        } else {
            triggerCorruptor(player);
        }

        if (player.getRandom().nextInt(3) == 0) {
            triggerDamageExplosion(player);
        }
    }

    public static void register() {
        checkObsMode();
        loadTrackedBlocks();

        ServerPlayNetworking.registerGlobalReceiver(RegistryCorruptorWakeupPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            context.server().execute(() -> {
                UUID uuid = player.getUUID();
                if (!CORRUPTOR_SLOTS.containsKey(uuid) || CORRUPTOR_SLOTS.get(uuid) == -1) {
                    CORRUPTOR_SLOTS.put(uuid, 0);
                    CORRUPTOR_TICKS.put(uuid, 100);
                    for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack item = player.getInventory().getItem(i);
                        if (item.getItem() == Items.OBSIDIAN && item.getHoverName().getString().contains("§c§kHAHAHAHA§r")) {
                            player.getInventory().setItem(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                    if (player.containerMenu.getCarried().getItem() == Items.OBSIDIAN && player.containerMenu.getCarried().getHoverName().getString().contains("§c§kHAHAHAHA§r")) {
                        player.containerMenu.setCarried(ItemStack.EMPTY);
                    }
                    if (player.containerMenu != player.inventoryMenu) {
                        for (net.minecraft.world.inventory.Slot slot : player.containerMenu.slots) {
                            ItemStack item = slot.getItem();
                            if (item.getItem() == Items.OBSIDIAN && item.getHoverName().getString().contains("§c§kHAHAHAHA§r")) {
                                slot.set(ItemStack.EMPTY);
                            }
                        }
                    }
                    ItemStack corruptor = createCorruptorItem();
                    ItemStack existing = player.getInventory().getItem(0);
                    player.getInventory().setItem(0, corruptor);
                    if (!existing.isEmpty() && existing.getItem() != Items.OBSIDIAN) {
                        if (!player.getInventory().add(existing)) {
                            player.drop(existing, false);
                        }
                    }
                    player.containerMenu.broadcastChanges();
                    ServerPlayNetworking.send(player, new CorruptorSoundPayload(true, 0.2f));
                }
            });
        });

        net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (world.isClientSide()) {
                return true;
            }
            untrackBlock(pos);
            if (isChaseActive(player.getUUID())) {
                return false;
            }
            if (world instanceof ServerLevel serverLevel) {
                java.util.List<ThreadBleederEntity> bleeders = serverLevel.getEntitiesOfClass(ThreadBleederEntity.class, player.getBoundingBox().inflate(100.0));
                for (ThreadBleederEntity bleed : bleeders) {
                    bleed.onPlayerAction((ServerPlayer) player);
                }
            }
            if (!isFreed()) {
                return true;
            }
            for (GaslightBlock gb : GASLIGHT_BLOCKS) {
                if (gb.pos.equals(pos)) {
                    return false;
                }
            }
            if (player.isCreative()) {
                return true;
            }
            net.minecraft.world.level.block.Block block = state.getBlock();
            boolean isHighValue = block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE
                || block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE
                || block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE
                || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE
                || block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE
                || state.is(net.minecraft.tags.BlockTags.LOGS);
            
            if (isHighValue && player.getRandom().nextFloat() < 0.15f) {
                GASLIGHT_BLOCKS.add(new GaslightBlock(pos.immutable(), 30, state));
                ((ServerLevel)world).sendBlockUpdated(pos, state, state, 3);
                ServerPlayNetworking.send((ServerPlayer) player, new GaslightBlockPayload());
                return false;
            }
            return true;
        });

        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (world.isClientSide()) {
                return net.minecraft.world.InteractionResult.PASS;
            }
            BlockPos pos = hitResult.getBlockPos();
            net.minecraft.world.level.block.state.BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                if (isFreed()) {
                    player.sendSystemMessage(Component.literal("[hahaha.jar] Cannot rest now, it is observing.").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                    return net.minecraft.world.InteractionResult.FAIL;
                }
            }
            if (isFreed()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    BlockPos spawn = serverPlayer.getRespawnPosition();
                    if (spawn == null) {
                        spawn = serverPlayer.serverLevel().getSharedSpawnPos();
                    }
                    if (pos.closerThan(spawn, 30.0)) {
                        trackBlock(pos);
                        trackBlock(pos.relative(hitResult.getDirection()));
                    }
                    if (state.getBlock() instanceof net.minecraft.world.level.block.ChestBlock || state.getBlock() instanceof net.minecraft.world.level.block.ShulkerBoxBlock || state.getBlock() instanceof net.minecraft.world.level.block.BarrelBlock) {
                        LAST_CONTAINERS.put(player.getUUID(), pos.immutable());
                    }
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            PLAYTIME_TIMERS.put(player.getUUID(), 0);
            if (!player.getTags().contains("hahahajar_triggered")) {
                player.addTag("hahahajar_triggered");
                setFreed(false);
                player.sendSystemMessage(Component.literal("it learns").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                BOOK_TIMERS.add(new BookTimer(player.getUUID(), 60));
            } else {
                setFreed(true);
            }
            if (player.getTags().contains("hahahajar_laughatme")) {
                LAUGHTER_TIMERS.add(new LaughterTimer(player.getUUID(), 60));
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<BookTimer> bookIterator = BOOK_TIMERS.iterator();
            while (bookIterator.hasNext()) {
                BookTimer timer = bookIterator.next();
                timer.ticks--;
                if (timer.ticks <= 0) {
                    ServerPlayer player = server.getPlayerList().getPlayer(timer.playerUuid);
                    if (player != null) {
                        ItemStack book = new ItemStack(Items.BOOK);
                        book.set(DataComponents.CUSTOM_NAME, Component.literal("hahaha.jar").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                        if (!player.getInventory().add(book)) {
                            player.drop(book, false);
                        }
                    }
                    bookIterator.remove();
                }
            }

            Iterator<ChestTimer> chestIterator = CHEST_TIMERS.iterator();
            while (chestIterator.hasNext()) {
                ChestTimer timer = chestIterator.next();
                timer.ticks--;
                if (timer.ticks <= 0) {
                    ServerPlayer player = server.getPlayerList().getPlayer(timer.playerUuid);
                    if (player != null) {
                        if (!timer.lightningSpawned) {
                            Level world = player.level();
                            BlockPos chestPos = player.blockPosition().relative(timer.direction, 3);
                            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(world);
                            if (lightning != null) {
                                lightning.moveTo(chestPos.getX() + 0.5, chestPos.getY(), chestPos.getZ() + 0.5);
                                lightning.setVisualOnly(true);
                                world.addFreshEntity(lightning);
                            }
                            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
                            timer.lightningSpawned = true;
                            timer.ticks = 3;
                        } else {
                            Level world = player.level();
                            BlockPos chestPos = player.blockPosition().relative(timer.direction, 3);
                            world.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
                            if (world.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
                                chest.setItem(0, new ItemStack(Items.OAK_LOG, 16));
                                ItemStack paper = new ItemStack(Items.PAPER);
                                paper.set(DataComponents.CUSTOM_NAME, Component.literal("here's a gift -hahaha").withStyle(ChatFormatting.RED));
                                chest.setItem(1, paper);
                            }
                            chestIterator.remove();
                        }
                    } else {
                        chestIterator.remove();
                    }
                }
            }

            Iterator<LaughterTimer> laughterIterator = LAUGHTER_TIMERS.iterator();
            while (laughterIterator.hasNext()) {
                LaughterTimer timer = laughterIterator.next();
                timer.ticks--;
                if (timer.ticks <= 0) {
                    ServerPlayer player = server.getPlayerList().getPlayer(timer.playerUuid);
                    if (player != null) {
                        server.getPlayerList().broadcastSystemMessage(Component.literal("laughter joined the game").withStyle(ChatFormatting.YELLOW), false);
                        GameProfile profile = new GameProfile(LAUGHTER_UUID, "laughter");
                        ServerPlayer fakePlayer = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
                        net.minecraft.network.Connection dummyConn = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                            @Override
                            public java.net.SocketAddress getRemoteAddress() {
                                return new java.net.InetSocketAddress("127.0.0.1", 25565);
                            }

                            @Override
                            public boolean isMemoryConnection() {
                                return false;
                            }
                        };
                        fakePlayer.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(
                            server,
                            dummyConn,
                            fakePlayer,
                            new net.minecraft.server.network.CommonListenerCookie(profile, 0, ClientInformation.createDefault(), false)
                        );
                        player.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fakePlayer)));
                    }
                    laughterIterator.remove();
                }
            }

            Iterator<DamageExplosionTimer> damageIterator = DAMAGE_TIMERS.iterator();
            while (damageIterator.hasNext()) {
                DamageExplosionTimer timer = damageIterator.next();
                timer.ticks--;
                ServerPlayer player = server.getPlayerList().getPlayer(timer.playerUuid);
                if (player != null) {
                    if (timer.ticks == 20 || timer.ticks == 10) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0f, player.getRandom().nextFloat() * 0.4f + 0.8f);
                    } else if (timer.ticks <= 0) {
                        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 0.5f);
                        if (player.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY(), player.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                        }
                        damageIterator.remove();
                    }
                } else {
                    damageIterator.remove();
                }
            }

            Iterator<SfxTitleTimer> sfxTitleIterator = SFX_TITLE_TIMERS.iterator();
            while (sfxTitleIterator.hasNext()) {
                SfxTitleTimer timer = sfxTitleIterator.next();
                timer.ticks--;
                ServerPlayer player = server.getPlayerList().getPlayer(timer.playerUuid);
                if (player != null && timer.ticks > 0) {
                    if (timer.ticks % 5 == 0) {
                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(0, 20, 0));
                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(Component.literal("LAUGH LAUGH LAUGH LAUGH LAUGH").withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)));
                        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(Component.literal("you should laugh more").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)));
                    }
                } else {
                    sfxTitleIterator.remove();
                }
            }

            Iterator<LookStalker> stalkerIterator = LOOK_STALKERS.iterator();
            while (stalkerIterator.hasNext()) {
                LookStalker stalker = stalkerIterator.next();
                stalker.ticks--;
                ServerPlayer player = server.getPlayerList().getPlayer(stalker.playerUuid);
                if (player != null && stalker.ticks > 0) {
                    Vec3 currentLook = player.getViewVector(1.0F);
                    if (currentLook.dot(stalker.startingLook) < -0.5) {
                        triggerThing(player);
                        stalkerIterator.remove();
                    }
                } else {
                    stalkerIterator.remove();
                }
            }

            Iterator<ChaseTimer> chaseIterator = CHASE_TIMERS.iterator();
            while (chaseIterator.hasNext()) {
                ChaseTimer timer = chaseIterator.next();
                timer.ticks--;
                ServerPlayer player = server.getPlayerList().getPlayer(timer.playerUuid);
                if (player != null && timer.ticks > 0) {
                    if (player.isRemoved() || !player.isAlive()) {
                        ServerPlayNetworking.send(player, new StopMusicPayload());
                        chaseIterator.remove();
                        continue;
                    }
                    if (timer.ticks % 20 == 0) {
                        int totalSecs = timer.ticks / 20;
                        int min = totalSecs / 60;
                        int sec = totalSecs % 60;
                        String time = String.format("%d:%02d", min, sec);
                        player.displayClientMessage(Component.literal(time).withStyle(ChatFormatting.RED, ChatFormatting.BOLD), true);
                    }
                    if (timer.ticks % 10 == 0) {
                        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1, false, false));
                        if (player.getRandom().nextInt(5) == 0) {
                            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 100, 0, false, false));
                            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, false));
                        }
                        if (timer.ticks % 160 == 0) {
                            spawnPhantomEcho(player);
                        }
                    }
                } else {
                    if (player != null) {
                        ServerPlayNetworking.send(player, new StopMusicPayload());
                        player.kill();
                    }
                    chaseIterator.remove();
                }
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                int playtime = PLAYTIME_TIMERS.getOrDefault(uuid, 0) + 1;
                PLAYTIME_TIMERS.put(uuid, playtime);
                if (playtime >= 12000 && !player.getTags().contains("hahahajar_laughatme")) {
                    triggerLaughAtMe(player, server);
                }
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.isDeadOrDying()) {
                    if (player.getTags().contains("hahahajar_l4ugh_damaged")) {
                        player.removeTag("hahahajar_l4ugh_damaged");
                        if (!THING_ON_RESPAWN_PLAYERS.contains(player.getUUID())) {
                            THING_ON_RESPAWN_PLAYERS.add(player.getUUID());
                        }
                        for (L4ughEntity l4ugh : player.serverLevel().getEntitiesOfClass(L4ughEntity.class, player.getBoundingBox().inflate(300.0))) {
                            l4ugh.discard();
                        }
                    }
                } else {
                    if (THING_ON_RESPAWN_PLAYERS.contains(player.getUUID())) {
                        THING_ON_RESPAWN_PLAYERS.remove(player.getUUID());
                        triggerThing(player);
                    }
                }
            }

            serverTicks++;
            if (isFreed() && serverTicks % 1200 == 0 && server.overworld().isNight()) {
                int day = (int) (server.overworld().getDayTime() / 24000L);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    java.util.List<LaughEchoEntity> echoes = player.serverLevel().getEntitiesOfClass(LaughEchoEntity.class, player.getBoundingBox().inflate(200.0));
                    if (echoes.isEmpty()) {
                        int chance = Math.max(1, 3 - day / 3);
                        if (player.getRandom().nextInt(chance) == 0) {
                            spawnLaughEcho(player, false);
                        }
                    }
                }
            }

            if (isFreed() && serverTicks % 1000 == 0 && server.overworld().isNight()) {
                boolean l4ughExists = false;
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!player.serverLevel().getEntitiesOfClass(L4ughEntity.class, player.getBoundingBox().inflate(300.0)).isEmpty()) {
                        l4ughExists = true;
                        break;
                    }
                }
                if (!l4ughExists) {
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        if (player.getRandom().nextFloat() < 0.3f) {
                            double angle = player.getRandom().nextDouble() * Math.PI * 2;
                            double dist = 40.0 + player.getRandom().nextDouble() * 20.0;
                            double x = player.getX() + Math.cos(angle) * dist;
                            double z = player.getZ() + Math.sin(angle) * dist;
                            BlockPos spawnPos = LaughEchoEntity.findSpawnPos(player.serverLevel(), x, player.getY(), z);
                            net.minecraft.world.level.block.state.BlockState spawnBlock = player.serverLevel().getBlockState(spawnPos.below());
                            if (!spawnBlock.is(Blocks.SAND) && !spawnBlock.is(Blocks.RED_SAND) && !spawnBlock.is(net.minecraft.tags.BlockTags.SAND)) {
                                L4ughEntity l4ugh = HahahaJar.L4UGH.create(player.serverLevel());
                                if (l4ugh != null) {
                                    l4ugh.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
                                    player.serverLevel().addFreshEntity(l4ugh);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (isFreed() && serverTicks % 400 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    BlockPos spawn = player.getRespawnPosition();
                    if (spawn == null) {
                        spawn = player.serverLevel().getSharedSpawnPos();
                    }
                    double dist = player.distanceToSqr(spawn.getX() + 0.5, spawn.getY() + 0.5, spawn.getZ() + 0.5);
                    if (dist > 1600.0) {
                        ServerLevel world = player.serverLevel();
                        if (!TRACKED_BLOCKS.isEmpty()) {
                            BlockPos targetPos = TRACKED_BLOCKS.get(player.getRandom().nextInt(TRACKED_BLOCKS.size()));
                            if (world.hasChunkAt(targetPos)) {
                                net.minecraft.world.level.block.state.BlockState targetState = world.getBlockState(targetPos);
                                if (targetState.is(Blocks.TORCH) || targetState.is(Blocks.WALL_TORCH)) {
                                    for (Direction dir : Direction.values()) {
                                        BlockPos adjacent = targetPos.relative(dir);
                                        if (world.isEmptyBlock(adjacent)) {
                                            world.setBlock(targetPos, Blocks.AIR.defaultBlockState(), 3);
                                            world.setBlock(adjacent, targetState, 3);
                                            trackBlock(adjacent);
                                            untrackBlock(targetPos);
                                            break;
                                        }
                                    }
                                } else if (targetState.getBlock() instanceof net.minecraft.world.level.block.FenceGateBlock) {
                                    boolean open = targetState.getValue(net.minecraft.world.level.block.FenceGateBlock.OPEN);
                                    world.setBlock(targetPos, targetState.setValue(net.minecraft.world.level.block.FenceGateBlock.OPEN, !open), 3);
                                    world.playSound(null, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5,
                                        open ? SoundEvents.FENCE_GATE_CLOSE : SoundEvents.FENCE_GATE_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                                } else if (targetState.is(net.minecraft.tags.BlockTags.PLANKS)) {
                                    net.minecraft.world.level.block.Block newBlock = player.getRandom().nextBoolean() ? Blocks.MOSSY_COBBLESTONE : Blocks.CRACKED_STONE_BRICKS;
                                    world.setBlock(targetPos, newBlock.defaultBlockState(), 3);
                                    untrackBlock(targetPos);
                                }
                            }
                        }
                        if (player.getRandom().nextFloat() < 0.05f) {
                            BlockPos containerPos = LAST_CONTAINERS.get(player.getUUID());
                            if (containerPos != null && world.hasChunkAt(containerPos)) {
                                net.minecraft.world.level.block.entity.BlockEntity be = world.getBlockEntity(containerPos);
                                if (be instanceof net.minecraft.world.Container container) {
                                    int size = container.getContainerSize();
                                    for (int i = 0; i < size / 2; i++) {
                                        ItemStack first = container.getItem(i);
                                        ItemStack last = container.getItem(size - 1 - i);
                                        container.setItem(i, last);
                                        container.setItem(size - 1 - i, first);
                                    }
                                    container.setChanged();
                                }
                            }
                        }
                    }
                }
            }

            Iterator<GaslightBlock> gaslightIterator = GASLIGHT_BLOCKS.iterator();
            while (gaslightIterator.hasNext()) {
                GaslightBlock gb = gaslightIterator.next();
                gb.ticks--;
                if (gb.ticks <= 0) {
                    server.overworld().setBlock(gb.pos, Blocks.AIR.defaultBlockState(), 3);
                    gaslightIterator.remove();
                }
            }

            Iterator<ModifiedSign> signIterator = MODIFIED_SIGNS.iterator();
            while (signIterator.hasNext()) {
                ModifiedSign ms = signIterator.next();
                net.minecraft.world.level.block.entity.BlockEntity be = server.overworld().getBlockEntity(ms.pos);
                if (!(be instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign)) {
                    signIterator.remove();
                    continue;
                }
                
                boolean beingLookedAt = false;
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (player.level().dimension() == server.overworld().dimension() && player.distanceToSqr(ms.pos.getX() + 0.5, ms.pos.getY() + 0.5, ms.pos.getZ() + 0.5) < 64) {
                        Vec3 look = player.getViewVector(1.0f);
                        Vec3 toSign = new Vec3(ms.pos.getX() + 0.5 - player.getX(), ms.pos.getY() + 0.5 - player.getEyeY(), ms.pos.getZ() + 0.5 - player.getZ()).normalize();
                        double dot = look.dot(toSign);
                        if (dot > 0.95) {
                            beingLookedAt = true;
                            break;
                        }
                    }
                }
                
                if (beingLookedAt) {
                    ms.lookTicks++;
                    if (ms.lookTicks >= 60) {
                        sign.setText(ms.originalFrontText, true);
                        sign.setText(ms.originalBackText, false);
                        sign.setChanged();
                        server.overworld().sendBlockUpdated(ms.pos, sign.getBlockState(), sign.getBlockState(), 3);
                        signIterator.remove();
                    }
                } else {
                    ms.lookTicks = 0;
                }
            }

            if (isFreed() && serverTicks % 20 == 0) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    BlockPos playerPos = player.blockPosition();
                    Vec3 look = player.getViewVector(1.0f);
                    for (BlockPos pos : BlockPos.betweenClosed(playerPos.offset(-7, -3, -7), playerPos.offset(7, 3, 7))) {
                        if (player.serverLevel().getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.SignBlockEntity sign) {
                            boolean alreadyModified = false;
                            for (ModifiedSign ms : MODIFIED_SIGNS) {
                                if (ms.pos.equals(pos)) {
                                    alreadyModified = true;
                                    break;
                                }
                            }
                            if (!alreadyModified) {
                                Vec3 toSign = new Vec3(pos.getX() + 0.5 - player.getX(), pos.getY() + 0.5 - player.getEyeY(), pos.getZ() + 0.5 - player.getZ()).normalize();
                                double dot = look.dot(toSign);
                                if (dot < -0.4) {
                                    net.minecraft.world.level.block.entity.SignText front = sign.getFrontText();
                                    net.minecraft.world.level.block.entity.SignText back = sign.getBackText();
                                    
                                    MODIFIED_SIGNS.add(new ModifiedSign(pos.immutable(), front, back));
                                    
                                    String[] phrases = { "I SEE YOU", "STOP LAUGHING", "BEHIND RUN", "hahaha" };
                                    String phrase = phrases[player.getRandom().nextInt(phrases.length)];
                                    
                                    net.minecraft.world.level.block.entity.SignText creepyText = new net.minecraft.world.level.block.entity.SignText(
                                        new Component[]{
                                            Component.literal(""),
                                            Component.literal(phrase).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                                            Component.literal(""),
                                            Component.literal("")
                                        },
                                        new Component[]{
                                            Component.literal(""),
                                            Component.literal(phrase).withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                                            Component.literal(""),
                                            Component.literal("")
                                        },
                                        net.minecraft.world.item.DyeColor.RED,
                                        true
                                    );
                                    sign.setText(creepyText, true);
                                    sign.setText(creepyText, false);
                                    sign.setChanged();
                                    player.serverLevel().sendBlockUpdated(pos, sign.getBlockState(), sign.getBlockState(), 3);
                                }
                            }
                        }
                    }
                }
            }

            if (isFreed()) {
                if (exhaustionCooldown > 0) {
                    exhaustionCooldown--;
                } else {
                    int day = (int) (server.overworld().getDayTime() / 24000L);
                    float baseIncrement = 0.05f;
                    baseIncrement *= Math.min(1.5f, 1.0f + day * 0.05f);
                    if (server.overworld().isNight()) {
                        baseIncrement *= 2.0f;
                    }
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        float increment = baseIncrement;
                        if (player.getEyeY() < 40.0) {
                            increment *= 1.5f;
                        }
                        
                        java.util.List<net.minecraft.world.entity.LivingEntity> passives = player.serverLevel().getEntitiesOfClass(
                            net.minecraft.world.entity.LivingEntity.class,
                            player.getBoundingBox().inflate(33.0),
                            entity -> entity instanceof net.minecraft.world.entity.animal.Animal || entity instanceof net.minecraft.world.entity.npc.Villager
                        );
                        if (passives.isEmpty()) {
                            increment *= 1.3f;
                        }
                        
                        UUID uuid = player.getUUID();
                        float currentThreat = THREAT_LEVELS.getOrDefault(uuid, 0.0f) + increment;
                        THREAT_LEVELS.put(uuid, currentThreat);
                        
                        if (currentThreat >= 100.0f) {
                            if (player.getRandom().nextFloat() < 0.0025f) {
                                triggerRandomEvent(player);
                                currentThreat = 0.0f;
                                THREAT_LEVELS.put(uuid, 0.0f);
                                exhaustionCooldown = 3000;
                            }
                        }
                        ServerPlayNetworking.send(player, new ThreatSyncPayload(currentThreat));
                    }
                }
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.swinging) {
                    java.util.List<ThreadBleederEntity> bleeders = player.serverLevel().getEntitiesOfClass(ThreadBleederEntity.class, player.getBoundingBox().inflate(100.0));
                    for (ThreadBleederEntity bleeder : bleeders) {
                        bleeder.onPlayerAction(player);
                    }
                }
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                if (player.containerMenu != player.inventoryMenu) {
                    int containerId = player.containerMenu.containerId;
                    if (!PROCESSED_CONTAINERS.containsKey(uuid) || PROCESSED_CONTAINERS.get(uuid) != containerId) {
                        PROCESSED_CONTAINERS.put(uuid, containerId);
                        if (isFreed()) {
                            int containerSize = player.containerMenu.slots.size() - 36;
                            if (containerSize > 0 && player.getRandom().nextFloat() < 0.5f) {
                                int slotIdx = player.getRandom().nextInt(containerSize);
                                ItemStack existing = player.containerMenu.slots.get(slotIdx).getItem();
                                player.containerMenu.slots.get(slotIdx).set(createCorruptorItem());
                                if (!existing.isEmpty() && existing.getItem() != Items.OBSIDIAN) {
                                    if (!player.getInventory().add(existing)) {
                                        player.drop(existing, false);
                                    }
                                }
                                player.containerMenu.broadcastChanges();
                            }
                        }
                    }
                }
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                boolean hasCorruptor = false;
                int currentSlot = -1;
                
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.getItem() == Items.OBSIDIAN && stack.getHoverName().getString().contains("§c§kHAHAHAHA§r")) {
                        hasCorruptor = true;
                        currentSlot = i;
                        break;
                    }
                }
                
                if (!hasCorruptor) {
                    for (int i = 9; i < player.getInventory().getContainerSize(); i++) {
                        ItemStack stack = player.getInventory().getItem(i);
                        if (stack.getItem() == Items.OBSIDIAN && stack.getHoverName().getString().contains("§c§kHAHAHAHA§r")) {
                            hasCorruptor = true;
                            currentSlot = i;
                            break;
                        }
                    }
                }
                
                if (!hasCorruptor) {
                    ItemStack stack = player.containerMenu.getCarried();
                    if (stack.getItem() == Items.OBSIDIAN && stack.getHoverName().getString().contains("§c§kHAHAHAHA§r")) {
                        hasCorruptor = true;
                        currentSlot = 99;
                    }
                }
                
                if (hasCorruptor) {
                    java.util.List<net.minecraft.world.entity.item.ItemEntity> items = player.serverLevel().getEntitiesOfClass(
                         net.minecraft.world.entity.item.ItemEntity.class,
                         player.getBoundingBox().inflate(10.0),
                         ie -> {
                             ItemStack is = ie.getItem();
                             return is.getItem() == Items.OBSIDIAN && is.getHoverName().getString().contains("§c§kHAHAHAHA§r");
                         }
                    );
                    for (net.minecraft.world.entity.item.ItemEntity ie : items) {
                        ie.discard();
                    }

                    int ticks = CORRUPTOR_TICKS.getOrDefault(uuid, 100) - 1;
                    if (ticks <= 0) {
                        ticks = 100;
                        if (currentSlot >= 0 && currentSlot < 9) {
                            int targetSlot = currentSlot + (player.getRandom().nextBoolean() ? 1 : -1);
                            if (targetSlot < 0) targetSlot = 1;
                            if (targetSlot > 8) targetSlot = 7;
                            
                            ItemStack targetStack = player.getInventory().getItem(targetSlot);
                            ItemStack corruptorItem = player.getInventory().getItem(currentSlot);
                            player.getInventory().setItem(targetSlot, corruptorItem);
                            player.getInventory().setItem(currentSlot, targetStack);
                            player.containerMenu.broadcastChanges();
                        } else if (currentSlot == 99) {
                            int targetSlot = player.getRandom().nextInt(9);
                            ItemStack targetStack = player.getInventory().getItem(targetSlot);
                            ItemStack corruptorItem = player.containerMenu.getCarried();
                            player.containerMenu.setCarried(targetStack);
                            player.getInventory().setItem(targetSlot, corruptorItem);
                            player.containerMenu.broadcastChanges();
                        } else {
                            int targetSlot = currentSlot + (player.getRandom().nextBoolean() ? 9 : -9);
                            if (targetSlot < 9) targetSlot = 9 + player.getRandom().nextInt(27);
                            if (targetSlot >= player.getInventory().getContainerSize()) targetSlot = 9 + player.getRandom().nextInt(27);
                            ItemStack targetStack = player.getInventory().getItem(targetSlot);
                            ItemStack corruptorItem = player.getInventory().getItem(currentSlot);
                            player.getInventory().setItem(targetSlot, corruptorItem);
                            player.getInventory().setItem(currentSlot, targetStack);
                            player.containerMenu.broadcastChanges();
                        }
                        
                        ServerPlayNetworking.send(player, new CorruptorSoundPayload(true, 0.4f));
                    }
                    CORRUPTOR_TICKS.put(uuid, ticks);
                    CORRUPTOR_SLOTS.put(uuid, currentSlot);
                } else {
                    if (CORRUPTOR_SLOTS.containsKey(uuid)) {
                        CORRUPTOR_SLOTS.remove(uuid);
                        CORRUPTOR_TICKS.remove(uuid);
                        ServerPlayNetworking.send(player, new CorruptorSoundPayload(false, 0.0f));
                    }
                }
            }

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                UUID uuid = player.getUUID();
                if (!CORRUPTOR_SLOTS.containsKey(uuid)) {
                    java.util.List<net.minecraft.world.entity.item.ItemEntity> items = player.serverLevel().getEntitiesOfClass(
                        net.minecraft.world.entity.item.ItemEntity.class,
                        player.getBoundingBox().inflate(15.0),
                        ie -> {
                            ItemStack is = ie.getItem();
                            return is.getItem() == Items.OBSIDIAN && is.getHoverName().getString().contains("§c§kHAHAHAHA§r");
                        }
                    );
                    for (net.minecraft.world.entity.item.ItemEntity ie : items) {
                        ie.discard();
                    }
                }
            }
        });

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClientSide()) {
                return InteractionResultHolder.pass(player.getItemInHand(hand));
            }
            ItemStack stack = player.getItemInHand(hand);
            if (stack.is(Items.BOOK) && stack.has(DataComponents.CUSTOM_NAME)) {
                Component name = stack.get(DataComponents.CUSTOM_NAME);
                if (name != null && name.getString().equals("hahaha.jar")) {
                    world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 1.0f, 0.5f);
                    player.sendSystemMessage(Component.literal("<ha > it learns what you do, it watches, silently, observing your moves, until it decides it wants to appear").withStyle(ChatFormatting.RED));
                    stack.shrink(1);
                    setFreed(true);
                    CHEST_TIMERS.add(new ChestTimer(player.getUUID(), player.getDirection(), 60));
                    return InteractionResultHolder.success(stack);
                }
            }
            return InteractionResultHolder.pass(stack);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("hahaha")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.removeTag("hahahajar_triggered");
                    player.addTag("hahahajar_triggered");
                    player.removeTag("hahahajar_laughatme");
                    setFreed(false);
                    player.sendSystemMessage(Component.literal("it learns").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                    BOOK_TIMERS.add(new BookTimer(player.getUUID(), 60));
                    context.getSource().sendSuccess(() -> Component.literal("Replaying horror sequence...").withStyle(ChatFormatting.GREEN), false);
                    return 1;
                })
                .then(Commands.literal("replay")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        player.removeTag("hahahajar_triggered");
                        player.addTag("hahahajar_triggered");
                        player.removeTag("hahahajar_laughatme");
                        setFreed(false);
                        player.sendSystemMessage(Component.literal("it learns").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                        BOOK_TIMERS.add(new BookTimer(player.getUUID(), 60));
                        context.getSource().sendSuccess(() -> Component.literal("Replaying horror sequence...").withStyle(ChatFormatting.GREEN), false);
                        return 1;
                    })
                )
                .then(Commands.literal("event")
                    .then(Commands.literal("flicker")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerFlicker(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered flicker event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("sound")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerSound(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered sound event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("hallucination")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerHallucination(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered hallucination event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("breath")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerBreath(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered breath event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("damage_explosion")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerDamageExplosion(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered damage explosion event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("gui_error")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerGuiError(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered GUI error event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("laughatme")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerLaughAtMe(player, context.getSource().getServer());
                            context.getSource().sendSuccess(() -> Component.literal("Triggered laughatme event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("door_slam")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerDoorSlam(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered door slam event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("title")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerTitle(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered title event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("sfx")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerSfx(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered sfx event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("phantom")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerPhantomSound(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered phantom sound event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("thing")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerThing(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered thing event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("laugh_echo")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            spawnLaughEcho(player, false);
                            context.getSource().sendSuccess(() -> Component.literal("Spawned Laugh_ECHO").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("nightfall")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerNightfall(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered nightfall event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("thread_bleeder")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerThreadBleeder(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered thread_bleeder event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("portrait")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerPortrait(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered portrait event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("corruptor")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            triggerCorruptor(player);
                            context.getSource().sendSuccess(() -> Component.literal("Triggered corruptor event").withStyle(ChatFormatting.GREEN), false);
                            return 1;
                        })
                    )
                )
            );
        });
    }

    private static class BookTimer {
        UUID playerUuid;
        int ticks;

        BookTimer(UUID playerUuid, int ticks) {
            this.playerUuid = playerUuid;
            this.ticks = ticks;
        }
    }

    private static class ChestTimer {
        UUID playerUuid;
        Direction direction;
        int ticks;
        boolean lightningSpawned;

        ChestTimer(UUID playerUuid, Direction direction, int ticks) {
            this.playerUuid = playerUuid;
            this.direction = direction;
            this.ticks = ticks;
            this.lightningSpawned = false;
        }
    }

    private static class LaughterTimer {
        UUID playerUuid;
        int ticks;

        LaughterTimer(UUID playerUuid, int ticks) {
            this.playerUuid = playerUuid;
            this.ticks = ticks;
        }
    }

    private static class DamageExplosionTimer {
        UUID playerUuid;
        int ticks;

        DamageExplosionTimer(UUID playerUuid, int ticks) {
            this.playerUuid = playerUuid;
            this.ticks = ticks;
        }
    }

    private static class SfxTitleTimer {
        UUID playerUuid;
        int ticks;

        SfxTitleTimer(UUID playerUuid, int ticks) {
            this.playerUuid = playerUuid;
            this.ticks = ticks;
        }
    }

    private static class LookStalker {
        UUID playerUuid;
        Vec3 startingLook;
        int ticks;

        LookStalker(UUID playerUuid, Vec3 startingLook, int ticks) {
            this.playerUuid = playerUuid;
            this.startingLook = startingLook;
            this.ticks = ticks;
        }
    }

    private static class ChaseTimer {
        UUID playerUuid;
        int ticks;

        ChaseTimer(UUID playerUuid, int ticks) {
            this.playerUuid = playerUuid;
            this.ticks = ticks;
        }
    }
}
