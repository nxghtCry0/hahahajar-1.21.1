package hahaha.jar;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HahahaJar implements ModInitializer {
	public static final String MOD_ID = "hahahajar";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final net.minecraft.world.entity.EntityType<LaughEchoEntity> LAUGH_ECHO = net.minecraft.core.Registry.register(
		net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE,
		net.minecraft.resources.ResourceLocation.parse("hahahajar:laugh_echo"),
		net.minecraft.world.entity.EntityType.Builder.of(LaughEchoEntity::new, net.minecraft.world.entity.MobCategory.MONSTER)
			.sized(0.6f, 1.8f)
			.build("laugh_echo")
	);

	public static final net.minecraft.world.entity.EntityType<ThreadBleederEntity> THREAD_BLEEDER = net.minecraft.core.Registry.register(
		net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE,
		net.minecraft.resources.ResourceLocation.parse("hahahajar:thread_bleeder"),
		net.minecraft.world.entity.EntityType.Builder.of(ThreadBleederEntity::new, net.minecraft.world.entity.MobCategory.MONSTER)
			.sized(0.6f, 1.8f)
			.build("thread_bleeder")
	);

	public static final net.minecraft.world.entity.EntityType<FrameLockPortraitEntity> FRAME_LOCK_PORTRAIT = net.minecraft.core.Registry.register(
		net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE,
		net.minecraft.resources.ResourceLocation.parse("hahahajar:frame_lock_portrait"),
		net.minecraft.world.entity.EntityType.Builder.of(FrameLockPortraitEntity::new, net.minecraft.world.entity.MobCategory.MONSTER)
			.sized(0.8f, 2.5f)
			.build("frame_lock_portrait")
	);

	public static final net.minecraft.world.entity.EntityType<L4ughEntity> L4UGH = net.minecraft.core.Registry.register(
		net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE,
		net.minecraft.resources.ResourceLocation.parse("hahahajar:l4ugh"),
		net.minecraft.world.entity.EntityType.Builder.of(L4ughEntity::new, net.minecraft.world.entity.MobCategory.MONSTER)
			.sized(1.2f, 5.0f)
			.build("l4ugh")
	);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		try {
			for (java.lang.reflect.Method m : net.minecraft.world.entity.Entity.class.getDeclaredMethods()) {
				if (m.getName().toLowerCase().contains("step")) {
					LOGGER.info("FOUND METHOD: " + m.getName() + " with parameters " + java.util.Arrays.toString(m.getParameterTypes()));
				}
			}
			for (java.lang.reflect.Field f : net.minecraft.world.entity.Entity.class.getDeclaredFields()) {
				if (f.getName().toLowerCase().contains("step")) {
					LOGGER.info("FOUND FIELD: " + f.getName());
				}
			}
		} catch (Exception e) {}
		try {
			java.io.File rootDir = new java.io.File(".").getAbsoluteFile();
			if (rootDir.getName().equals(".") || rootDir.getName().equals("")) {
				rootDir = rootDir.getParentFile();
			}
			if (rootDir.getName().equals("run")) {
				rootDir = rootDir.getParentFile();
			}
			java.io.File assetsSrc = new java.io.File(rootDir, "src/main/resources/assets/hahahajar/textures/entity");
			assetsSrc.mkdirs();
			java.io.File file = new java.io.File(assetsSrc, "laugh_echo.png");
			if (!file.exists()) {
				java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				java.awt.Graphics2D g = img.createGraphics();
				g.setColor(java.awt.Color.BLACK);
				g.fillRect(0, 0, 64, 64);
				g.setColor(java.awt.Color.WHITE);
				g.fillRect(10, 12, 2, 1);
				g.fillRect(13, 12, 2, 1);
				g.dispose();
				javax.imageio.ImageIO.write(img, "png", file);
			}
			java.io.File buildDir = new java.io.File(rootDir, "build/resources/main/assets/hahahajar/textures/entity");
			buildDir.mkdirs();
			javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(file), "png", new java.io.File(buildDir, "laugh_echo.png"));
			java.io.File runBuildDir = new java.io.File(rootDir, "run/build/resources/main/assets/hahahajar/textures/entity");
			if (runBuildDir.exists() || runBuildDir.mkdirs()) {
				javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(file), "png", new java.io.File(runBuildDir, "laugh_echo.png"));
			}

			java.io.File file2 = new java.io.File(assetsSrc, "white.png");
			if (!file2.exists()) {
				java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				java.awt.Graphics2D g = img.createGraphics();
				g.setColor(java.awt.Color.WHITE);
				g.fillRect(0, 0, 64, 64);
				g.dispose();
				javax.imageio.ImageIO.write(img, "png", file2);
			}
			javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(file2), "png", new java.io.File(buildDir, "white.png"));
			if (runBuildDir.exists() || runBuildDir.mkdirs()) {
				javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(file2), "png", new java.io.File(runBuildDir, "white.png"));
			}

			java.io.File file3 = new java.io.File(assetsSrc, "saccade.png");
			if (!file3.exists()) {
				java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(256, 256, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				java.awt.Graphics2D g = img.createGraphics();
				g.setColor(new java.awt.Color(0, 0, 0, 180));
				g.fillRect(0, 0, 256, 256);
				g.setColor(java.awt.Color.WHITE);
				g.fillOval(60, 80, 40, 50);
				g.fillOval(156, 80, 40, 50);
				g.setColor(java.awt.Color.BLACK);
				g.fillOval(75, 95, 10, 10);
				g.fillOval(171, 95, 10, 10);
				g.setColor(java.awt.Color.WHITE);
				g.setStroke(new java.awt.BasicStroke(4.0f));
				g.drawArc(60, 130, 136, 60, 0, -180);
				java.util.Random rand = new java.util.Random(42L);
				for (int i = 0; i < 20; i++) {
					int x1 = rand.nextInt(256);
					int y1 = rand.nextInt(256);
					int x2 = x1 + rand.nextInt(50) - 25;
					int y2 = y1 + rand.nextInt(10) - 5;
					g.drawLine(x1, y1, x2, y2);
				}
				g.dispose();
				javax.imageio.ImageIO.write(img, "png", file3);
			}
			javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(file3), "png", new java.io.File(buildDir, "saccade.png"));
			if (runBuildDir.exists() || runBuildDir.mkdirs()) {
				javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(file3), "png", new java.io.File(runBuildDir, "saccade.png"));
			}

			java.io.File file4 = new java.io.File(assetsSrc, "bleeding_heart.png");
			if (!file4.exists()) {
				java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(9, 9, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				java.awt.Graphics2D g = img.createGraphics();
				g.setColor(new java.awt.Color(0, 0, 0, 0));
				g.fillRect(0, 0, 9, 9);
				g.setColor(java.awt.Color.BLACK);
				g.fillRect(1, 1, 3, 3);
				g.fillRect(5, 1, 3, 3);
				g.fillRect(2, 4, 5, 2);
				g.fillRect(3, 6, 3, 2);
				g.fillRect(4, 8, 1, 1);
				g.setColor(new java.awt.Color(180, 0, 0));
				g.fillRect(2, 5, 1, 1);
				g.fillRect(6, 5, 1, 1);
				g.fillRect(3, 7, 1, 1);
				g.fillRect(4, 9, 1, 1);
				g.dispose();
				javax.imageio.ImageIO.write(img, "png", file4);
			}
			javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(file4), "png", new java.io.File(buildDir, "bleeding_heart.png"));
			if (runBuildDir.exists() || runBuildDir.mkdirs()) {
				javax.imageio.ImageIO.write(javax.imageio.ImageIO.read(file4), "png", new java.io.File(runBuildDir, "bleeding_heart.png"));
			}

			java.io.File file5 = new java.io.File(assetsSrc, "l4ugh.png");
			if (!file5.exists()) {
				java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(64, 64, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				java.awt.Graphics2D graphics = img.createGraphics();
				graphics.setBackground(new java.awt.Color(0, 0, 0, 0));
				graphics.clearRect(0, 0, 64, 64);
				img.setRGB(9, 10, 0xFFFF0000);
				img.setRGB(10, 10, 0xFFFF0000);
				img.setRGB(13, 10, 0xFFFF0000);
				img.setRGB(14, 10, 0xFFFF0000);
				img.setRGB(8, 13, 0xFFFF0000);
				img.setRGB(9, 14, 0xFFFF0000);
				img.setRGB(10, 14, 0xFFFF0000);
				img.setRGB(11, 14, 0xFFFF0000);
				img.setRGB(12, 14, 0xFFFF0000);
				img.setRGB(13, 14, 0xFFFF0000);
				img.setRGB(14, 14, 0xFFFF0000);
				img.setRGB(15, 13, 0xFFFF0000);
				for (int y = 20; y < 32; y++) {
					img.setRGB(23, y, 0xFFFF0000);
					img.setRGB(24, y, 0xFFFF0000);
				}
				for (int y = 22; y <= 30; y += 3) {
					for (int x = 21; x <= 26; x++) {
						img.setRGB(x, y, 0xFFFF0000);
					}
				}
				graphics.dispose();
				javax.imageio.ImageIO.write(img, "png", file5);
			}
			java.nio.file.Files.copy(file5.toPath(), new java.io.File(buildDir, "l4ugh.png").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			if (runBuildDir.exists() || runBuildDir.mkdirs()) {
				java.nio.file.Files.copy(file5.toPath(), new java.io.File(runBuildDir, "l4ugh.png").toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Exception e) {
		}

		net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(LAUGH_ECHO, LaughEchoEntity.createMobAttributes());
		net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(THREAD_BLEEDER, ThreadBleederEntity.createMobAttributes());
		net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(FRAME_LOCK_PORTRAIT, FrameLockPortraitEntity.createMobAttributes());
		net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(L4UGH, L4ughEntity.createMobAttributes());

		PayloadTypeRegistry.playS2C().register(SfxPayload.TYPE, SfxPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ChasePayload.TYPE, ChasePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CameraPullPayload.TYPE, CameraPullPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(GuiErrorPayload.TYPE, GuiErrorPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(StopMusicPayload.TYPE, StopMusicPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(GaslightBlockPayload.TYPE, GaslightBlockPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ThreadBleederLagPayload.TYPE, ThreadBleederLagPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RegistryCorruptorWakeupPayload.TYPE, RegistryCorruptorWakeupPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CorruptorSoundPayload.TYPE, CorruptorSoundPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ThreatSyncPayload.TYPE, ThreatSyncPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(L4ughFlashPayload.TYPE, L4ughFlashPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(TooltipEventPayload.TYPE, TooltipEventPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(FogEventPayload.TYPE, FogEventPayload.CODEC);
		HahahaJarEventHandler.register();
	}
}