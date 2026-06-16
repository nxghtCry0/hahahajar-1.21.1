package hahaha.jar.client;

import hahaha.jar.ThreadBleederEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ThreadBleederRenderer extends EntityRenderer<ThreadBleederEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("hahahajar:textures/entity/thread_bleeder_glitch.png");

    public ThreadBleederRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ThreadBleederEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(ThreadBleederEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        int mode = (entity.tickCount / 5) % 3;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        Matrix4f pose = poseStack.last().pose();
        Matrix3f normal = poseStack.last().normal();

        if (mode == 0) {
            drawQuad(pose, normal, consumer, -0.5f, 0.0f, 0.0f, 0.5f, 1.8f, 0.0f, packedLight);
        } else if (mode == 1) {
            drawBox(pose, normal, consumer, -0.4f, 0.0f, -0.4f, 0.4f, 0.8f, 0.4f, packedLight);
        } else {
            drawBox(pose, normal, consumer, -0.25f, 1.4f, -0.25f, 0.25f, 1.9f, 0.25f, packedLight);
            drawBox(pose, normal, consumer, -0.25f, 0.6f, -0.15f, 0.25f, 1.4f, 0.15f, packedLight);
            drawBox(pose, normal, consumer, -0.4f, 0.6f, -0.1f, -0.25f, 1.4f, 0.1f, packedLight);
            drawBox(pose, normal, consumer, 0.25f, 0.6f, -0.1f, 0.4f, 1.4f, 0.1f, packedLight);
            drawBox(pose, normal, consumer, -0.2f, 0.0f, -0.1f, -0.05f, 0.6f, 0.1f, packedLight);
            drawBox(pose, normal, consumer, 0.05f, 0.0f, -0.1f, 0.2f, 0.6f, 0.1f, packedLight);
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void drawQuad(Matrix4f pose, Matrix3f normal, VertexConsumer consumer, float minX, float minY, float z, float maxX, float maxY, float uMax, int light) {
        addVertex(pose, normal, consumer, minX, minY, z, 0.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, minY, z, 1.0f, 1.0f, light);
        addVertex(pose, normal, consumer, maxX, maxY, z, 1.0f, 0.0f, light);
        addVertex(pose, normal, consumer, minX, maxY, z, 0.0f, 0.0f, light);
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
