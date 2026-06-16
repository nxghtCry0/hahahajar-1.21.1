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
		} catch (Exception e) {
		}

		net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(LAUGH_ECHO, LaughEchoEntity.createMobAttributes());
		net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(THREAD_BLEEDER, ThreadBleederEntity.createMobAttributes());
		net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(FRAME_LOCK_PORTRAIT, FrameLockPortraitEntity.createMobAttributes());

		PayloadTypeRegistry.playS2C().register(SfxPayload.TYPE, SfxPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ChasePayload.TYPE, ChasePayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CameraPullPayload.TYPE, CameraPullPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(GuiErrorPayload.TYPE, GuiErrorPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(StopMusicPayload.TYPE, StopMusicPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(GaslightBlockPayload.TYPE, GaslightBlockPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ThreadBleederLagPayload.TYPE, ThreadBleederLagPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(RegistryCorruptorWakeupPayload.TYPE, RegistryCorruptorWakeupPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CorruptorSoundPayload.TYPE, CorruptorSoundPayload.CODEC);
		HahahaJarEventHandler.register();
	}
}