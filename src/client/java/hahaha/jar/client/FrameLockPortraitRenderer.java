package hahaha.jar.client;

import hahaha.jar.FrameLockPortraitEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class FrameLockPortraitRenderer extends EntityRenderer<FrameLockPortraitEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("hahahajar:textures/entity/white.png");

    public FrameLockPortraitRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(FrameLockPortraitEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(FrameLockPortraitEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            Vec3 look = client.player.getViewVector(1.0f);
            Vec3 toEntity = entity.position().subtract(client.player.position()).normalize();
            double dot = look.dot(toEntity);
            if (dot > 0.2) {
                return;
            }
        }

        poseStack.pushPose();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entitySolid(TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        drawBox(pose, normal, consumer, -0.3f, 2.0f, -0.3f, 0.3f, 2.6f, 0.3f, packedLight);
        drawBox(pose, normal, consumer, -0.35f, 0.8f, -0.2f, 0.35f, 2.0f, 0.2f, packedLight);
        drawBox(pose, normal, consumer, -0.55f, 0.8f, -0.15f, -0.35f, 2.0f, 0.15f, packedLight);
        drawBox(pose, normal, consumer, 0.35f, 0.8f, -0.15f, 0.55f, 2.0f, 0.15f, packedLight);
        drawBox(pose, normal, consumer, -0.3f, 0.0f, -0.15f, -0.05f, 0.8f, 0.15f, packedLight);
        drawBox(pose, normal, consumer, 0.05f, 0.0f, -0.15f, 0.3f, 0.8f, 0.15f, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void drawBox(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int light) {
        addVertex(pose, normal, consumer, minX, minY, maxZ, 0.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, minY, maxZ, 1.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, maxY, maxZ, 1.0f, 0.0f, light);
        addVertex(pose, normal, consumer, minX, maxY, maxZ, 0.0f, 0.0f, light);

        addVertex(pose, normal, consumer, maxX, minY, minZ, 0.0f, 1.0f, light);
        addVertex(pose, normal, consumer, minX, minY, minZ, 1.0f, 1.0f, light);
        addVertex(pose, normal, consumer, minX, maxY, minZ, 1.0f, 0.0f, light);
        addVertex(pose, normal, consumer, maxX, maxY, minZ, 0.0f, 0.0f, light);

        addVertex(pose, normal, consumer, minX, maxY, maxZ, 0.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, maxY, maxZ, 1.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, maxY, minZ, 1.0f, 0.0f, light);
        addVertex(pose, normal, consumer, minX, maxY, minZ, 0.0f, 0.0f, light);

        addVertex(pose, normal, consumer, minX, minY, minZ, 0.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, minY, minZ, 1.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, minY, maxZ, 1.0f, 0.0f, light);
        addVertex(pose, normal, consumer, minX, minY, maxZ, 0.0f, 0.0f, light);

        addVertex(pose, normal, consumer, maxX, minY, maxZ, 0.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, minY, minZ, 1.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, maxY, minZ, 1.0f, 0.0f, light);
        addVertex(pose, normal, consumer, maxX, maxY, maxZ, 0.0f, 0.0f, light);

        addVertex(pose, normal, consumer, minX, minY, minZ, 0.0f, 1.0f, light);
        addVertex(pose, normal, consumer, minX, minY, maxZ, 1.0f, 1.0f, light);
        addVertex(pose, normal, consumer, minX, maxY, maxZ, 1.0f, 0.0f, light);
        addVertex(pose, normal, consumer, minX, maxY, minZ, 0.0f, 0.0f, light);
    }

    private void addVertex(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float x, float y, float z, float u, float v, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
