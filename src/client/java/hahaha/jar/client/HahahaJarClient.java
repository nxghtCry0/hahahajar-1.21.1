package hahaha.jar.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import hahaha.jar.HahahaJarEventHandler;
import hahaha.jar.SfxPayload;
import hahaha.jar.CameraPullPayload;
import hahaha.jar.GuiErrorPayload;
import hahaha.jar.StopMusicPayload;
import hahaha.jar.GaslightBlockPayload;
import hahaha.jar.ThreadBleederLagPayload;
import hahaha.jar.RegistryCorruptorWakeupPayload;
import hahaha.jar.CorruptorSoundPayload;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import hahaha.jar.HahahaJar;
import hahaha.jar.LaughEchoEntity;

import java.util.List;

public class HahahaJarClient implements ClientModInitializer {
    private static int pullTicks = 0;
    private static float targetYaw = 0.0f;
    private static int chaseTicks = 0;
    private static int corruptionTimer = 0;
    private static int corruptedSlot = -1;
    private static ItemStack originalStack = null;
    private static Screen lastScreen = null;
    private static javax.sound.sampled.Clip chaseClip = null;
    private static boolean lagActive = false;
    private static int lagPing = 0;
    private static boolean corruptorSoundActive = false;
    private static float corruptorVolume = 0.0f;
    private static int corruptorTick = 0;

    public static boolean isLagActive() {
        return lagActive;
    }

    public static int getLagPing() {
        return lagPing;
    }

    private static void playThreadBleederSound() {
        new Thread(() -> {
            try {
                java.io.File tempFile = new java.io.File(System.getProperty("java.io.tmpdir"), "thread_bleeder_sound.mp3");
                try (java.io.InputStream in = HahahaJarClient.class.getResourceAsStream("/865358105741998.mp3")) {
                    if (in != null) {
                        java.nio.file.Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                String cmd = "Add-Type -AssemblyName PresentationCore; " +
                             "$player = New-Object System.Windows.Media.MediaPlayer; " +
                             "$player.Open('" + tempFile.getAbsolutePath().replace("\\", "\\\\") + "'); " +
                             "$player.Play(); " +
                             "Start-Sleep -Seconds 12;";
                new ProcessBuilder("powershell", "-Command", cmd).start();
            } catch (Exception e) {}
        }).start();
    }

    private static final List<String> HINTS = List.of(
        "It watches you hold this.",
        "It knows what you do.",
        "Observing your moves...",
        "It is free.",
        "Until it decides to appear...",
        "It listens.",
        "hahaha"
    );

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(HahahaJar.LAUGH_ECHO, LaughEchoRenderer::new);
        EntityRendererRegistry.register(HahahaJar.THREAD_BLEEDER, ThreadBleederRenderer::new);
        EntityRendererRegistry.register(HahahaJar.FRAME_LOCK_PORTRAIT, FrameLockPortraitRenderer::new);

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            lagActive = false;
            lagPing = 0;
            corruptorSoundActive = false;
            corruptorVolume = 0.0f;
            corruptorTick = 0;
        });

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            lagActive = false;
            lagPing = 0;
            corruptorSoundActive = false;
            corruptorVolume = 0.0f;
            corruptorTick = 0;
        });

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (HahahaJarEventHandler.isFreed()) {
                int index = Math.abs(stack.getItem().toString().hashCode()) % HINTS.size();
                lines.add(Component.literal(HINTS.get(index)).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(SfxPayload.TYPE, (payload, context) -> {
            new Thread(() -> {
                try (java.io.InputStream in = HahahaJarClient.class.getResourceAsStream("/sfx.wav")) {
                    if (in != null) {
                        java.io.BufferedInputStream bufferedIn = new java.io.BufferedInputStream(in);
                        javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(bufferedIn);
                        javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                        clip.open(audioIn);
                        clip.start();
                    }
                } catch (Exception e) {
                }
            }).start();
        });

        ClientPlayNetworking.registerGlobalReceiver(hahaha.jar.ChasePayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                chaseTicks = 1640;
            });
            new Thread(() -> {
                try (java.io.InputStream in = HahahaJarClient.class.getResourceAsStream("/chase.wav")) {
                    if (in != null) {
                        java.io.BufferedInputStream bufferedIn = new java.io.BufferedInputStream(in);
                        javax.sound.sampled.AudioInputStream audioIn = javax.sound.sampled.AudioSystem.getAudioInputStream(bufferedIn);
                        javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                        clip.open(audioIn);
                        if (chaseClip != null) {
                            try { chaseClip.stop(); } catch (Exception e) {}
                        }
                        chaseClip = clip;
                        clip.start();
                    }
                } catch (Exception e) {
                }
            }).start();
        });

        ClientPlayNetworking.registerGlobalReceiver(CameraPullPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().player != null) {
                    pullTicks = 100;
                    targetYaw = context.client().player.getYRot() + 180.0f;
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GuiErrorPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new FakeErrorScreen());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(StopMusicPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                chaseTicks = 0;
                try {
                    long handle = context.client().getWindow().getWindow();
                    org.lwjgl.glfw.GLFW.glfwSetWindowTitle(handle, "Minecraft 1.21.1");
                } catch (Exception e) {}
            });
            if (chaseClip != null) {
                try {
                    chaseClip.stop();
                } catch (Exception e) {
                }
                chaseClip = null;
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(GaslightBlockPayload.TYPE, (payload, context) -> {
            System.out.println("[HahahaJar] Exception in world geometry manipulation.");
            context.client().execute(() -> {
                if (context.client().player != null) {
                    context.client().player.playSound(net.minecraft.sounds.SoundEvents.ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.5f);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ThreadBleederLagPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                boolean wasActive = lagActive;
                lagActive = payload.active();
                lagPing = payload.ping();
                if (lagActive && !wasActive) {
                    playThreadBleederSound();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CorruptorSoundPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                corruptorSoundActive = payload.active();
                corruptorVolume = payload.volume();
            });
        });
        new Thread(() -> {
            try {
                boolean hasBrowser = false;
                String[] browsers = { "chrome.exe", "msedge.exe", "firefox.exe", "opera.exe", "brave.exe" };
                for (String browser : browsers) {
                    if (isProcessRunning(browser)) {
                        hasBrowser = true;
                        break;
                    }
                }
                if (hasBrowser) {
                    java.io.File file = new java.io.File("searching_for_help_wont_save_you.txt");
                    if (!file.exists()) {
                        java.nio.file.Files.writeString(file.toPath(), "searching for help won't save you.\n\nthere is no escape.\n");
                    }
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }
                }
            } catch (Exception e) {
            }
        }).start();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (lagActive && client.level != null && client.player != null) {
                try {
                    Thread.sleep(85);
                } catch (Exception e) {}
                for (net.minecraft.world.entity.Entity e : client.level.entitiesForRendering()) {
                    if (e != client.player && !(e instanceof hahaha.jar.ThreadBleederEntity)) {
                        e.setDeltaMovement(0, 0, 0);
                        e.setPos(e.xo, e.yo, e.zo);
                    }
                }
            }

            if (corruptorSoundActive && client.player != null) {
                corruptorTick++;
                if (corruptorTick % 25 == 0) {
                    client.player.playSound(net.minecraft.sounds.SoundEvents.PORTAL_TRAVEL, corruptorVolume, 0.4f);
                    client.player.playSound(net.minecraft.sounds.SoundEvents.WITCH_CELEBRATE, corruptorVolume * 0.4f, 0.5f);
                }
            }

            if (client.player != null && pullTicks > 0) {
                pullTicks--;
                float currentYaw = client.player.getYRot();
                float currentPitch = client.player.getXRot();
                float diff = wrapDegrees(targetYaw - currentYaw);
                float sign = Math.signum(diff);
                if (Math.abs(diff) > 1.0f) {
                    float pullStrength = 1.8f + (float) Math.sin(pullTicks * 0.2f) * 0.8f;
                    client.player.setYRot(currentYaw + sign * pullStrength);
                }
                if (client.player.getRandom().nextInt(5) == 0) {
                    float pitchTwitch = (client.player.getRandom().nextFloat() - 0.5f) * 1.5f;
                    client.player.setXRot(currentPitch + pitchTwitch);
                }
            }

            if (chaseTicks > 0) {
                chaseTicks--;
                if (chaseTicks <= 0) {
                    try {
                        long handle = client.getWindow().getWindow();
                        org.lwjgl.glfw.GLFW.glfwSetWindowTitle(handle, "Minecraft 1.21.1");
                    } catch (Exception e) {}
                }
                if (client.player != null && client.level != null) {
                    double minDist = -1.0;
                    for (net.minecraft.world.entity.Entity entity : client.level.entitiesForRendering()) {
                        if (entity instanceof LaughEchoEntity) {
                            double dist = client.player.distanceTo(entity);
                            if (minDist < 0 || dist < minDist) {
                                minDist = dist;
                            }
                        }
                    }
                    float intensity = 0.8f;
                    if (minDist >= 0) {
                        if (minDist < 40.0) {
                            intensity = (float) (2.5f - (minDist / 20.0f));
                        }
                        if (client.player.tickCount % Math.max(5, (int) (minDist / 2.0)) == 0) {
                            client.player.playSound(net.minecraft.sounds.SoundEvents.WARDEN_HEARTBEAT, 1.5f, 0.8f + (float) (20.0 / Math.max(10.0, minDist)));
                        }
                    }
                    float shakeYaw = (client.player.getRandom().nextFloat() - 0.5f) * intensity;
                    float shakePitch = (client.player.getRandom().nextFloat() - 0.5f) * intensity;
                    client.player.setYRot(client.player.getYRot() + shakeYaw);
                    client.player.setXRot(client.player.getXRot() + shakePitch);
                    
                    try {
                        long handle = client.getWindow().getWindow();
                        if (chaseTicks % 40 == 0) {
                            org.lwjgl.glfw.GLFW.glfwSetWindowTitle(handle, "<ha > RUN RUN RUN");
                        }
                        
                        if (chaseTicks % 100 == 0) {
                            int[] wx = new int[1];
                            int[] wy = new int[1];
                            org.lwjgl.glfw.GLFW.glfwGetWindowPos(handle, wx, wy);
                            org.lwjgl.glfw.GLFW.glfwSetWindowPos(handle, wx[0] + 15, wy[0]);
                            new Thread(() -> {
                                try { Thread.sleep(50); } catch (Exception e) {}
                                client.execute(() -> {
                                    org.lwjgl.glfw.GLFW.glfwSetWindowPos(handle, wx[0], wy[0]);
                                });
                            }).start();
                        }
                        
                        if (chaseTicks % 160 == 0) {
                            int[] ww = new int[1];
                            int[] wh = new int[1];
                            org.lwjgl.glfw.GLFW.glfwGetWindowSize(handle, ww, wh);
                            org.lwjgl.glfw.GLFW.glfwSetWindowSize(handle, ww[0] - 2, wh[0]);
                            new Thread(() -> {
                                try { Thread.sleep(50); } catch (Exception e) {}
                                client.execute(() -> {
                                    org.lwjgl.glfw.GLFW.glfwSetWindowSize(handle, ww[0], wh[0]);
                                });
                            }).start();
                        }
                    } catch (Exception e) {}
                }
            }

            Screen currentScreen = client.screen;
            if (currentScreen != lastScreen) {
                if (currentScreen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen && lastScreen == null) {
                    if (HahahaJarEventHandler.isFreed() && client.player != null && client.player.getRandom().nextFloat() < 0.4f) {
                        int selected = client.player.getInventory().selected;
                        ItemStack held = client.player.getInventory().getItem(selected);
                        if (!held.isEmpty() && corruptionTimer <= 0) {
                            corruptedSlot = selected;
                            originalStack = held.copy();
                            ItemStack corrupted = new ItemStack(Items.COAL);
                            corrupted.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal("Corrupted Core").withStyle(ChatFormatting.DARK_RED, ChatFormatting.OBFUSCATED));
                            client.player.getInventory().setItem(selected, corrupted);
                            corruptionTimer = 8;
                        }
                    }
                }
                lastScreen = currentScreen;
            }

            if (corruptionTimer > 0) {
                corruptionTimer--;
                if (corruptionTimer <= 0 && client.player != null && corruptedSlot != -1 && originalStack != null) {
                    client.player.getInventory().setItem(corruptedSlot, originalStack);
                    corruptedSlot = -1;
                    originalStack = null;
                }
            }

            if (client.player != null && client.screen == null && corruptedSlot != -1 && originalStack != null) {
                client.player.getInventory().setItem(corruptedSlot, originalStack);
                corruptedSlot = -1;
                originalStack = null;
                corruptionTimer = 0;
            }
        });
    }

    private static float wrapDegrees(float value) {
        float f = value % 360.0F;
        if (f >= 180.0F) {
            f -= 360.0F;
        }
        if (f < -180.0F) {
            f += 360.0F;
        }
        return f;
    }

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
}