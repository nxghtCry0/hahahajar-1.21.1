package hahaha.jar.client;

import hahaha.jar.L4ughEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.EntityType;

public class L4ughRenderer extends HumanoidMobRenderer<L4ughEntity, HumanoidModel<L4ughEntity>> {
    private static final ResourceLocation WHITE_TEXTURE = ResourceLocation.parse("hahahajar:textures/entity/white.png");
    private static final ResourceLocation L4UGH_TEXTURE = ResourceLocation.parse("hahahajar:textures/entity/l4ugh.png");

    private final CowRenderer cowRenderer;
    private final SheepRenderer sheepRenderer;
    private final PigRenderer pigRenderer;
    private final ChickenRenderer chickenRenderer;
    private final FoxRenderer foxRenderer;
    private final WolfRenderer wolfRenderer;
    private final CatRenderer catRenderer;
    private final RabbitRenderer rabbitRenderer;
    private final VillagerRenderer villagerRenderer;

    private Cow dummyCow;
    private Sheep dummySheep;
    private Pig dummyPig;
    private Chicken dummyChicken;
    private Fox dummyFox;
    private Wolf dummyWolf;
    private Cat dummyCat;
    private Rabbit dummyRabbit;
    private Villager dummyVillager;

    public L4ughRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.cowRenderer = new CowRenderer(context);
        this.sheepRenderer = new SheepRenderer(context);
        this.pigRenderer = new PigRenderer(context);
        this.chickenRenderer = new ChickenRenderer(context);
        this.foxRenderer = new FoxRenderer(context);
        this.wolfRenderer = new WolfRenderer(context);
        this.catRenderer = new CatRenderer(context);
        this.rabbitRenderer = new RabbitRenderer(context);
        this.villagerRenderer = new VillagerRenderer(context);
    }

    @Override
    public ResourceLocation getTextureLocation(L4ughEntity entity) {
        return L4UGH_TEXTURE;
    }

    @Override
    protected net.minecraft.client.renderer.RenderType getRenderType(L4ughEntity entity, boolean invisible, boolean translucent, boolean glowing) {
        if (!entity.isDisguised()) {
            return net.minecraft.client.renderer.RenderType.entityCutoutNoCull(getTextureLocation(entity));
        }
        return super.getRenderType(entity, invisible, translucent, glowing);
    }

    @Override
    protected void scale(L4ughEntity entity, PoseStack poseStack, float partialTickTime) {
        if (!entity.isDisguised()) {
            poseStack.scale(1.5f, 2.7f, 1.5f);
        }
    }

    @Override
    public void render(L4ughEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isDisguised()) {
            net.minecraft.world.level.Level level = entity.level();
            int type = entity.getDisguiseType();
            if (type == 0) {
                if (dummyCow == null) {
                    dummyCow = new Cow(EntityType.COW, level);
                }
                updateDummy(dummyCow, entity);
                cowRenderer.render(dummyCow, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 1) {
                if (dummySheep == null) {
                    dummySheep = new Sheep(EntityType.SHEEP, level);
                }
                updateDummy(dummySheep, entity);
                sheepRenderer.render(dummySheep, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 2) {
                if (dummyPig == null) {
                    dummyPig = new Pig(EntityType.PIG, level);
                }
                updateDummy(dummyPig, entity);
                pigRenderer.render(dummyPig, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 3) {
                if (dummyChicken == null) {
                    dummyChicken = new Chicken(EntityType.CHICKEN, level);
                }
                updateDummy(dummyChicken, entity);
                chickenRenderer.render(dummyChicken, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 4) {
                if (dummyFox == null) {
                    dummyFox = new Fox(EntityType.FOX, level);
                }
                updateDummy(dummyFox, entity);
                foxRenderer.render(dummyFox, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 5) {
                if (dummyWolf == null) {
                    dummyWolf = new Wolf(EntityType.WOLF, level);
                }
                updateDummy(dummyWolf, entity);
                wolfRenderer.render(dummyWolf, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 6) {
                if (dummyCat == null) {
                    dummyCat = new Cat(EntityType.CAT, level);
                }
                updateDummy(dummyCat, entity);
                catRenderer.render(dummyCat, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else if (type == 7) {
                if (dummyRabbit == null) {
                    dummyRabbit = new Rabbit(EntityType.RABBIT, level);
                }
                updateDummy(dummyRabbit, entity);
                rabbitRenderer.render(dummyRabbit, entityYaw, partialTicks, poseStack, buffer, packedLight);
            } else {
                if (dummyVillager == null) {
                    dummyVillager = new Villager(EntityType.VILLAGER, level);
                }
                updateDummy(dummyVillager, entity);
                villagerRenderer.render(dummyVillager, entityYaw, partialTicks, poseStack, buffer, packedLight);
            }
        } else {
            poseStack.pushPose();
            float cameraYaw = this.entityRenderDispatcher.camera.getYRot();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-cameraYaw + 180.0f));
            VertexConsumer consumer = buffer.getBuffer(net.minecraft.client.renderer.RenderType.entityCutoutNoCull(L4UGH_TEXTURE));
            Matrix4f pose = poseStack.last().pose();
            float width = 3.17f;
            float height = 5.0f;
            float minX = -width / 2.0f;
            float maxX = width / 2.0f;
            float minY = 0.0f;
            float maxY = height;
            addVertex(pose, consumer, minX, minY, 0.0f, 0.0f, 1.0f, packedLight);
            addVertex(pose, consumer, maxX, minY, 0.0f, 1.0f, 1.0f, packedLight);
            addVertex(pose, consumer, maxX, maxY, 0.0f, 1.0f, 0.0f, packedLight);
            addVertex(pose, consumer, minX, maxY, 0.0f, 0.0f, 0.0f, packedLight);
            poseStack.popPose();
        }
    }

    private void addVertex(Matrix4f pose, VertexConsumer consumer, float x, float y, float z, float u, float v, int light) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0.0f, 1.0f, 0.0f);
    }

    private void updateDummy(net.minecraft.world.entity.Mob dummy, L4ughEntity parent) {
        dummy.setPos(parent.getX(), parent.getY(), parent.getZ());
        dummy.xo = parent.xo;
        dummy.yo = parent.yo;
        dummy.zo = parent.zo;
        dummy.setYRot(parent.getYRot());
        dummy.setXRot(parent.getXRot());
        dummy.yRotO = parent.yRotO;
        dummy.xRotO = parent.xRotO;
        dummy.yBodyRot = parent.yBodyRot;
        dummy.yBodyRotO = parent.yBodyRotO;
        dummy.yHeadRot = parent.yHeadRot;
        dummy.yHeadRotO = parent.yHeadRotO;
        dummy.walkAnimation.setSpeed(parent.walkAnimation.speed());
        dummy.walkAnimation.position(parent.walkAnimation.position());
        dummy.setOnGround(parent.onGround());
    }
}
